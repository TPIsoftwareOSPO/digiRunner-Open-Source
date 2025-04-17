package tpi.dgrv4.dpaa.component.apptJob;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.dpaa.constant.TsmpDpCertType;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.constant.TsmpNoticeSrc;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.*;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWE/TLS加密憑證到期提醒<br>
 * * HouseKeeping: {@link HousekeepingJob#deleteNoticeLog()}
 */
@SuppressWarnings("serial")
@Transactional
public class NoticeExpCertJob extends ApptJob {

	private final String default_notice_mthd = "EMAIL";

	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao;
	private TsmpNoticeLogDao tsmpNoticeLogDao;
	private TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	private PrepareMailService prepareMailService;
	private MailHelper mailHelper;

	private ZonedDateTime jobStartTime;

	@Autowired
	public NoticeExpCertJob(TsmpDpApptJob tsmpDpApptJob, TsmpClientDao tsmpClientDao,
			TsmpDpClientextDao tsmpDpClientextDao, TsmpClientCertDao tsmpClientCertDao,
			TsmpClientCert2Dao tsmpClientCert2Dao, TsmpNoticeLogDao tsmpNoticeLogDao,
			TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy, PrepareMailService prepareMailService,
			MailHelper mailHelper, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.jobStartTime = ZonedDateTime.now();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.tsmpNoticeLogDao = tsmpNoticeLogDao;
		this.tsmpDpMailTpltCacheProxy = tsmpDpMailTpltCacheProxy;
		this.prepareMailService = prepareMailService;
		this.mailHelper = mailHelper;
	}

	@Override
	public String runApptJob() throws Exception {
		String noticeSrc = getTsmpDpApptJob().getRefSubitemNo();
		
		// 找出合法的 client 清單
		List<TsmpClient> validClientList = getValidClientList();
		if (CollectionUtils.isEmpty(validClientList)) {
			this.logger.debug(String.format("沒有合法的用戶端需要通知(%s)", noticeSrc));
			step("0/0");
			return "SUCCESS";
		}
		
		// 找出每個 client 即將到期的憑證(JWE/TLS)
		List<TsmpClientCertBasic> readyToExpireCertList = getReadyToExpireCertList(validClientList);
		if (CollectionUtils.isEmpty(readyToExpireCertList)) {
			this.logger.debug(String.format("沒有即將到期的%s憑證需要通知", noticeSrc));
			step("0/0");
			return "SUCCESS";
		}
		
		// 篩選出已經可以再次寄信通知的憑證
		List<TsmpClientCertBasic> availableToNotice = filterByNoticeLog(readyToExpireCertList);
		if (CollectionUtils.isEmpty(availableToNotice)) {
			this.logger.debug(String.format("所有即將到期的%s憑證，在近期皆已發送過通知", noticeSrc));
			step("0/0");
			return "SUCCESS";
		}

		// 發送到期提醒通知給 client 所設定的 email
		sendEmailAndSaveNoticeLog(availableToNotice);

		return "SUCCESS";
	}

	// 找出會員資格已放行、用戶端狀態啟用且有設定 email 的用戶
	private List<TsmpClient> getValidClientList() {
		List<TsmpClient> clientList = getTsmpClientDao().queryByRegStatusAndLike( //
			null, null, TsmpDpRegStatus.PASS.value(), Integer.MAX_VALUE);
		return clientList.stream().filter((client) -> {
			boolean isActive = "1".equals(client.getClientStatus());
			boolean hasEmails = !StringUtils.isEmpty(client.getEmails());
			return isActive && hasEmails;
		}).collect(Collectors.toList());
	}

	// 以憑證到期日判斷是否即將到期 (n-30)
	// JWE: TSMP_CLIENT_CERT.expired_at
	// TLS: TSMP_CLIENT_CERT2.expired_at
	private List<TsmpClientCertBasic> getReadyToExpireCertList(List<TsmpClient> clientList) {
		List<TsmpClientCertBasic> readyToExpireCertList = new ArrayList<>();
		
		Sort sort = Sort.by(Sort.Direction.ASC, "expiredAt");	// 依憑證到期日, 由舊到新排序
		String jobType = getTsmpDpApptJob().getRefSubitemNo();
		if(TsmpDpCertType.JWE.value().equals(jobType)) {
			List<TsmpClientCert> clientCertList = null;
			for (TsmpClient client : clientList) {
				clientCertList = getTsmpClientCertDao().findByClientId(client.getClientId(), sort);
				if (CollectionUtils.isEmpty(clientCertList)) {
					continue;
				}
				for (TsmpClientCert clientCert : clientCertList) {
					if ( isReadyToExpire(clientCert.getExpiredAt()) ) {
						readyToExpireCertList.add(clientCert);
					}
				}
			}
		} else if(TsmpDpCertType.TLS.value().equals(jobType)) {
			List<TsmpClientCert2> clientCert2List = null;
			for (TsmpClient client : clientList) {
				clientCert2List = getTsmpClientCert2Dao().findByClientId(client.getClientId(), sort);
				if (CollectionUtils.isEmpty(clientCert2List)) {
					continue;
				}
				for (TsmpClientCert2 clientCert2 : clientCert2List) {
					if ( isReadyToExpire(clientCert2.getExpiredAt()) ) {
						readyToExpireCertList.add(clientCert2);
					}
				}
			}
		}
		
		return readyToExpireCertList;
	}

	// 判斷 jobStartTime 是否為到期前30天(只比到日期，不含已過期，但包含當日到期)
	// EX: 01/31到期，則01/01、01/31要發通知
	private boolean isReadyToExpire(Long expiredAt) {
		Date expiredAtDt = new Date(expiredAt);
		ZonedDateTime expiredAtZdt = ZonedDateTime.ofInstant(expiredAtDt.toInstant(), ZoneId.systemDefault());
		expiredAtZdt = expiredAtZdt.truncatedTo(ChronoUnit.DAYS);

		ZonedDateTime currentZdt = getJobStartTime().truncatedTo(ChronoUnit.DAYS);
		if (currentZdt.compareTo(expiredAtZdt) == 0) {
			return true;
		} else if (currentZdt.compareTo(expiredAtZdt) > 0) {
			return false;
		}
		
		ZonedDateTime after30Days = currentZdt.plusDays(30L);
		return after30Days.compareTo(expiredAtZdt) >= 0;
	}

	// 若此憑證在近7天內還沒通知過才可以發送通知，但如果排程執行當天就是憑證到期日，則允許再發一次通知
	private List<TsmpClientCertBasic> filterByNoticeLog(List<TsmpClientCertBasic> certBasicList) {
		return certBasicList.stream().filter((certBasic) -> {
			String clientCertId = null;
			String noticeSrc = null;
			if(certBasic instanceof TsmpClientCert) {
				clientCertId = String.valueOf(((TsmpClientCert) certBasic).getClientCertId());
				noticeSrc = TsmpNoticeSrc.JWE加密憑證到期.value();
			} else if(certBasic instanceof TsmpClientCert2) {
				clientCertId = String.valueOf(((TsmpClientCert2) certBasic).getClientCert2Id());
				noticeSrc = TsmpNoticeSrc.TLS通訊憑證到期.value();
			}
			Date before7Days = getBefore7Days(getJobStartTime());
			List<TsmpNoticeLog> logs = getTsmpNoticeLogDao().query_noticeExpCertJob_01(//
				noticeSrc, default_notice_mthd, clientCertId, before7Days);
			if (CollectionUtils.isEmpty(logs)) {
				return true;
			}
			
			// 若近7天曾經發過通知，但今天是到期日，且今天還沒發過信，就可以再發一次
			if (
				isOnExpiredDate(getJobStartTime(), certBasic.getExpiredAt()) &&
				!hasNoticedOnJobStartDate(logs)
			) {
				return true;
			}
			
			return false;
		}).collect(Collectors.toList());
	}

	// 判斷 zdt 是否為憑證到期日當天
	private boolean isOnExpiredDate(ZonedDateTime zdt, Long expiredAt) {
		Date expiredAtDt = new Date(expiredAt);
		ZonedDateTime expiredAtZdt = ZonedDateTime.ofInstant(expiredAtDt.toInstant(), ZoneId.systemDefault());
		expiredAtZdt = expiredAtZdt.truncatedTo(ChronoUnit.DAYS);
		return expiredAtZdt.compareTo(zdt.truncatedTo(ChronoUnit.DAYS)) == 0;
	}

	// 是否在排程執行日當天有發過通知
	private boolean hasNoticedOnJobStartDate(List<TsmpNoticeLog> logs) {
		ZonedDateTime jobStartDate = getJobStartTime().truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime lastNoticeDateTime = null;
		for (TsmpNoticeLog log : logs) {
			lastNoticeDateTime = ZonedDateTime.ofInstant(log.getLastNoticeDateTime().toInstant(), ZoneId.systemDefault());
			lastNoticeDateTime = lastNoticeDateTime.truncatedTo(ChronoUnit.DAYS);
			if (lastNoticeDateTime.compareTo(jobStartDate) == 0) {
				return true;
			}
		}
		return false;
	}

	// 排入 emailJob
	private void sendEmailAndSaveNoticeLog(List<TsmpClientCertBasic> certList) {
		TsmpClientCertBasic clientCertBasic = null;
		List<TsmpMailEvent> mailEvents = null;
		String clientCertId = null;
		TsmpDpCertType certType = null;
		TsmpNoticeSrc noticeSrc = null;
		for (int i = 0; i < certList.size(); i++) {
			clientCertBasic = certList.get(i);
			if(clientCertBasic instanceof TsmpClientCert) {
				clientCertId = String.valueOf(((TsmpClientCert) clientCertBasic).getClientCertId());
				certType = TsmpDpCertType.JWE;
				noticeSrc = TsmpNoticeSrc.JWE加密憑證到期;
			} else if(clientCertBasic instanceof TsmpClientCert2) {
				clientCertId = String.valueOf(((TsmpClientCert2) clientCertBasic).getClientCert2Id());
				certType = TsmpDpCertType.TLS;
				noticeSrc = TsmpNoticeSrc.TLS通訊憑證到期;
			}

			try {
				// 準備 TsmpMailEvents
				mailEvents = getTsmpMailEvents(clientCertBasic, clientCertId, certType);
				if (CollectionUtils.isEmpty(mailEvents)) {
					throw new Exception("取得信件參數失敗, 無法寄出通知信");
				}
				
				//identif 可寫入識別資料，ex: userName=mini 或 userName=mini,　reqOrdermId=17002	若有多個資料則以逗號和全型空白分隔
				String identif = ""//
					+ "mailType=CERT_EXP"
					+ ", certType=" + certType.value()
					+ ", clientId=" + clientCertBasic.getClientId()
					+ ", certFileName=" + clientCertBasic.getCertFileName()
					+ ", expiredAt=" + getDateTime(clientCertBasic.getExpiredAt())
					;
				
				/* 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信 */
				// 準備好資料,以寫入排程
				getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(), "0");
				
				// 寫入通知歷程
				saveNoticeLog(noticeSrc, clientCertId);
				
				step((i + 1) + "/" + certList.size());
			} catch (Exception e) {
				this.logger.error(String.format("寄出%s憑證到期通知信失敗: clientCertId=%s, caused by %s", //
					certType.value(), clientCertId, e.getMessage()));
			}
		}
	}

	public List<TsmpMailEvent> getTsmpMailEvents(TsmpClientCertBasic certBasic, String clientCertId, TsmpDpCertType certType) // 
			throws Exception {
		// 確認憑證是否存在
		if (TsmpDpCertType.JWE.equals(certType)) {
			Optional<TsmpClientCert> opt = getTsmpClientCertDao().findById(Long.valueOf(clientCertId));
			if (opt.isEmpty()) {
				throw new Exception("JWE憑證不存在, 無法寄出到期通知信");
			}
		} else if (TsmpDpCertType.TLS.equals(certType)) {
			Optional<TsmpClientCert2> opt = getTsmpClientCert2Dao().findById(Long.valueOf(clientCertId));
			if (opt.isEmpty()) {
				throw new Exception("TLS憑證不存在, 無法寄出到期通知信");
			}
		}
		// 收件者
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(certBasic.getClientId());
		if (opt_client.isEmpty()) {
			throw new Exception("憑證用戶端(" + certBasic.getClientId() + ")不存在，無法寄出通知信");
		}
		String recipients = Optional.ofNullable(opt_client.get().getEmails()) //
			.map((emails) -> emails)
			.orElseThrow(() -> new Exception("憑證用戶端未設定電子郵件，無法寄出通知信"));
		
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent mailEvent = getTsmpMailEvent(recipients, certBasic, certType);
		if (mailEvent != null) {
			mailEvents.add(mailEvent);
		}
		return mailEvents;
	}
	
	/*
	 * 信件模板:
	 * 
	 * 親愛的 digiRunner 用戶您好：
	 *    提醒您，{{clientId}} 之 {{certType}} 憑證 - {{certFileName}} 將於 {{expiredDate}} 到期。
	*/
	protected TsmpMailEvent getTsmpMailEvent(String recipients, TsmpClientCertBasic certBasic, TsmpDpCertType certType) {
		String subjTpltCode = "subject.cert-exp";
		String bodyTpltCode = "body.cert-exp";
		
		//主旨
		String subject = getTemplate(subjTpltCode);
		String body = getTemplate(bodyTpltCode);
		if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(body)) {
			this.logger.debug(String.format("Cannot find email templates: %s, %s", subjTpltCode, bodyTpltCode));
			return null;
		}
		
		//內文
		Map<String, Object> bodyParams = new HashMap<>();
		bodyParams.put("clientId", certBasic.getClientId());
		bodyParams.put("certType", certType.value());
		bodyParams.put("certFileName", certBasic.getCertFileName());
		bodyParams.put("expiredDate", getDateTime(certBasic.getExpiredAt()));
		String content = getMailHelper().buildNestedContent(bodyTpltCode, bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser("SYS")
		.setRefCode(bodyTpltCode)
		.build();
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> list = getTsmpDpMailTpltCacheProxy().findByCode(code);
		if (list != null && !list.isEmpty()) {
			return list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295));// yyyy/MM/dd
		return dtStr;
	}

	private void saveNoticeLog(TsmpNoticeSrc noticeSrc, String noticeKey) {
		TsmpNoticeLog log = new TsmpNoticeLog();
		log.setNoticeSrc(noticeSrc.value());
		log.setNoticeMthd(default_notice_mthd);
		log.setNoticeKey(noticeKey);
		log.setLastNoticeDateTime(DateTimeUtil.now());
		log = getTsmpNoticeLogDao().save(log);
	}

	// ex: 2021-01-08 09:00:35 -> 2021-01-02 00:00:00
	private Date getBefore7Days(ZonedDateTime jobStartTime) {
		return Date.from(jobStartTime.truncatedTo(ChronoUnit.DAYS).minusDays(6L).toInstant());
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpClientCertDao getTsmpClientCertDao() {
		return this.tsmpClientCertDao;
	}

	protected TsmpClientCert2Dao getTsmpClientCert2Dao() {
		return this.tsmpClientCert2Dao;
	}

	protected TsmpNoticeLogDao getTsmpNoticeLogDao() {
		return this.tsmpNoticeLogDao;
	}

	protected TsmpDpMailTpltCacheProxy getTsmpDpMailTpltCacheProxy() {
		return this.tsmpDpMailTpltCacheProxy;
	}

	protected PrepareMailService getPrepareMailService() {
		return this.prepareMailService;
	}

	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}
	
	protected ZonedDateTime getJobStartTime() {
		return this.jobStartTime;
	}
	
}