package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.constant.TsmpDpCertType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0088Req;
import tpi.dgrv4.dpaa.vo.DPB0088Resp;
import tpi.dgrv4.dpaa.vo.DPB0088certItem;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCertBasic;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0088Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao; 
	private BcryptParamHelper helper;
	private TsmpClientDao tsmpClientDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0088Service(TsmpClientCertDao tsmpClientCertDao, TsmpClientCert2Dao tsmpClientCert2Dao,
			BcryptParamHelper helper, TsmpClientDao tsmpClientDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.helper = helper;
		this.tsmpClientDao = tsmpClientDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0088Resp queryCaListByDate(TsmpAuthorization auth, DPB0088Req req, ReqHeader reqHeader) {
		
		try {
			String local = ServiceUtil.getLocale(reqHeader.getLocale());
			String certType = getCertType(req, local);//解碼
			
			// 檢查日期格式
			final String startDateStr = req.getStartDate();
			final String endDateStr = req.getEndDate();
			Optional<Date> opt_s = DateTimeUtil.stringToDateTime(startDateStr, DateTimeFormatEnum.西元年月日_2);
			Optional<Date> opt_e = DateTimeUtil.stringToDateTime(endDateStr, DateTimeFormatEnum.西元年月日_2);
			if ( !(opt_s.isPresent() && opt_e.isPresent()) ) {
				throw TsmpDpAaRtnCode._1295.throwing();
			}
			
			// 檢查日期邏輯
			// 使用DateTimeUtil轉出來的Date, 時間都是00:00:00
			final Date startDate = opt_s.get();
			Date endDate = opt_e.get();
			if (startDate.compareTo(endDate) > 0) {
				throw TsmpDpAaRtnCode._1295.throwing();
			} else {
				/* 假設查詢同一天(1911/01/01~1911/01/01) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/02 00:00:00
				 * 不同天(1911/01/01~1911/01/03) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/04 00:00:00
				 * 因為SQL條件是 createDateTime >= :startDate and createDateTime < :endDate
				 */
				endDate = plusDay(endDate, 1);
			}
			
			this.logger.debug(String.format("DPB0088Service: %s ~ %s", debugDate(startDate), debugDate(endDate)));
			
			
			List<TsmpClientCertBasic> certBasicList = getCertBasicList(req, startDate, endDate, certType);
			
			return createResp(certBasicList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	private List<TsmpClientCertBasic> getCertBasicList(DPB0088Req req, Date startDate, Date endDate, String certType) {
		List<TsmpClientCertBasic> certBasicList = new ArrayList<TsmpClientCertBasic>();
		
		if(TsmpDpCertType.JWE.value().equals(certType)) {
			TsmpClientCertBasic lastRecord = getLastRecordFromPrevPage(req.getClientCertId(), certType);
			List<TsmpClientCert> certList = getTsmpClientCertDao().query_dpb0088Service(startDate, endDate //
					, (TsmpClientCert)lastRecord, getPageSize());
			for (TsmpClientCert c : certList) {
				certBasicList.add(c);
			}
			
		}else if(TsmpDpCertType.TLS.value().equals(certType)) {
			TsmpClientCertBasic lastRecord = getLastRecordFromPrevPage(req.getClientCert2Id(), certType);
			List<TsmpClientCert2> certList = getTsmpClientCert2Dao().query_dpb0088Service(startDate, endDate //
					, (TsmpClientCert2)lastRecord, getPageSize());
			for (TsmpClientCert2 c : certList) {
				certBasicList.add(c);
			}
		}
		
		if (certBasicList == null || certBasicList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return certBasicList;
	}
	
	
	public String getCertType(DPB0088Req req, String locale) {
		String encodeCertType = req.getEncodeCertType();//使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2
		if (StringUtils.isEmpty(encodeCertType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String dpb0088_certType = getDecodeCertType(encodeCertType, locale);//解碼
		
		return dpb0088_certType;
	}
	
	public String getDecodeCertType(String encodeCertType, String locale) {
		String dpb0088_certType = null;
		try {
			dpb0088_certType = getBcryptParamHelper().decode(encodeCertType, "CERT_TYPE", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return dpb0088_certType;
	}
	
	private TsmpClientCertBasic getLastRecordFromPrevPage(Long clientCertId, String certType) {
		if (clientCertId != null) {
			if(TsmpDpCertType.JWE.value().equals(certType)) {
				Optional<TsmpClientCert> opt = getTsmpClientCertDao().findById(clientCertId);
				if (!opt.isPresent()) {
					this.logger.debug("查無TsmpClientCert: " + clientCertId);
					throw TsmpDpAaRtnCode._1297.throwing();
				}
				return opt.get();
				
			}else if(TsmpDpCertType.TLS.value().equals(certType)) {
				Optional<TsmpClientCert2> opt = getTsmpClientCert2Dao().findById(clientCertId);
				if (!opt.isPresent()) {
					this.logger.debug("查無TsmpClientCert: " + clientCertId);
					throw TsmpDpAaRtnCode._1297.throwing();
				}
				return opt.get();
			}
		}
		return null;
	}

	private DPB0088Resp createResp(List<TsmpClientCertBasic> certBasicList) {
		DPB0088Resp resp = new DPB0088Resp();
		List<DPB0088certItem> dpb0088CertList = new ArrayList<>();
		
		DPB0088certItem dpb0088Cert = null;
		Map<String, TsmpClient> tmpClientStore = new HashMap<>();	// 暫存Client資料, 減少查詢資料庫次數
		String createAt = null;
		String expiredAt = null;
		String clientName = null;
		String clientAlias = null;
		String createDateTime = null;
		String updateDateTime = null;
		for(TsmpClientCertBasic cert : certBasicList) {
			createAt = getCertDateTime(cert.getCreateAt());
			expiredAt = getCertDateTime(cert.getExpiredAt());
			clientName = getClientName(cert.getClientId(), tmpClientStore);
			clientAlias = getClientAlias(cert.getClientId(), tmpClientStore);
			createDateTime = getCreateDateTime(cert.getCreateDateTime());
			updateDateTime = getUpdateDateTime(cert.getUpdateDateTime());
			
			dpb0088Cert = new DPB0088certItem();
			if(cert instanceof TsmpClientCert) {
				dpb0088Cert.setClientCertId(((TsmpClientCert)cert).getClientCertId());
			}else if(cert instanceof TsmpClientCert2) {
				dpb0088Cert.setClientCert2Id(((TsmpClientCert2)cert).getClientCert2Id());
			}
			dpb0088Cert.setClientId(cert.getClientId());
			dpb0088Cert.setPubKey(cert.getPubKey());
			dpb0088Cert.setCertVersion(nvl(cert.getCertVersion()));
			dpb0088Cert.setCertSerialNum(cert.getCertSerialNum());
			dpb0088Cert.setsAlgorithmID(nvl(cert.getsAlgorithmId()));
			dpb0088Cert.setAlgorithmID(cert.getAlgorithmId());
			dpb0088Cert.setCertThumbprint(cert.getCertThumbprint());
			dpb0088Cert.setIuId(nvl(cert.getIuid()));
			dpb0088Cert.setIssuerName(cert.getIssuerName());
			dpb0088Cert.setsUid(nvl(cert.getSuid()));
			dpb0088Cert.setCreateAt(createAt);
			dpb0088Cert.setExpiredAt(expiredAt);
			dpb0088Cert.setClientName(clientName);
			dpb0088Cert.setClientAlias(clientAlias);
			dpb0088Cert.setCertFileName(cert.getCertFileName());
			dpb0088Cert.setCreateDateTime(createDateTime);
			dpb0088Cert.setCreateUser(nvl(cert.getCreateUser()));
			dpb0088Cert.setUpdateDateTime(updateDateTime);
			dpb0088CertList.add(dpb0088Cert);
		}
		
		resp.setCertList(dpb0088CertList);
		return resp;
	}

	protected String getClientName(String clientId, Map<String, TsmpClient> tmpClientStore) {
		return getClientData(clientId, tmpClientStore, (c) -> {return c.getClientName();});
	}

	private String getClientAlias(String clientId, Map<String, TsmpClient> tmpClientStore) {
		return getClientData(clientId, tmpClientStore, (c) -> {return c.getClientAlias();});
	}

	private String getClientData(String clientId, Map<String, TsmpClient> tmpClientStore, //
			Function<TsmpClient, String> func) {
		String rtn = new String();

		TsmpClient client = null;
		boolean hasKey = tmpClientStore.containsKey(clientId);
		if (!hasKey) {
			Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
			if (opt.isPresent()) {
				client = opt.get();
			}
			tmpClientStore.put(clientId, client);
		} else {
			client = tmpClientStore.get(clientId);
		}

		if (client != null) {
			rtn = func.apply(client);
		}
		
		return rtn;
	}

	private String getCreateDateTime(Date createDateTime) {
		return DateTimeUtil.dateTimeToString(createDateTime, DateTimeFormatEnum.西元年月日時分_2).orElse(null);
	}

	private String getUpdateDateTime(Date updateDateTime) {
		return DateTimeUtil.dateTimeToString(updateDateTime, DateTimeFormatEnum.西元年月日時分_2).orElse(new String());
	}

	private String getCertDateTime(Long timeInMillis) {
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(null);
		this.logger.debug(String.format("certDt: %d -> %s", timeInMillis, dtStr));
		return dtStr;
	}

	private Date plusDay(Date dt, int days) {
		LocalDateTime ldt = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		ldt = ldt.plusDays(days);
		return Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}

	private String debugDate(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
	}

	private String nvl(Object obj) {
		if (obj == null) {
			return new String();
		}
		return String.valueOf(obj);
	}

	protected TsmpClientCertDao getTsmpClientCertDao() {
		return this.tsmpClientCertDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0088");
		return this.pageSize;
	}
	
	protected TsmpClientCert2Dao getTsmpClientCert2Dao() {
		return this.tsmpClientCert2Dao;
	}	
 
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
}
