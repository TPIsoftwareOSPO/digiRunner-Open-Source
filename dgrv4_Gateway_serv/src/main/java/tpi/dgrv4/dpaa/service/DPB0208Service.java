package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0208Req;
import tpi.dgrv4.dpaa.vo.DPB0208Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.entity.DgrXApiKeyMap;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.repository.DgrXApiKeyDao;
import tpi.dgrv4.entity.repository.DgrXApiKeyMapDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * @author Mini <br>
 * 建立 X-Api-Key 資料,並發信通知 client
 */
@Service
public class DPB0208Service {
	private TPILogger logger = TPILogger.tl;

	private DgrXApiKeyDao dgrXApiKeyDao;
	private DgrXApiKeyMapDao dgrXApiKeyMapDao;
	private TsmpClientDao tsmpClientDao;
	private PrepareMailService prepareMailService;
	private TsmpSettingService tsmpSettingService;
	private MailHelper mailHelper;
	private TsmpGroupDao tsmpGroupDao;

	@Autowired
	public DPB0208Service(DgrXApiKeyDao dgrXApiKeyDao, DgrXApiKeyMapDao dgrXApiKeyMapDao, TsmpClientDao tsmpClientDao,
			PrepareMailService prepareMailService, TsmpSettingService tsmpSettingService, MailHelper mailHelper,
			TsmpGroupDao tsmpGroupDao) {
		super();
		this.dgrXApiKeyDao = dgrXApiKeyDao;
		this.dgrXApiKeyMapDao = dgrXApiKeyMapDao;
		this.tsmpClientDao = tsmpClientDao;
		this.prepareMailService = prepareMailService;
		this.tsmpSettingService = tsmpSettingService;
		this.mailHelper = mailHelper;
		this.tsmpGroupDao = tsmpGroupDao;
	}

	public DPB0208Resp createXApiKey(TsmpAuthorization authorization, DPB0208Req httpReq) {
		DPB0208Resp resp = new DPB0208Resp();
		try {

			// 檢查傳入的參數
			List<String> groupIdList = httpReq.getGroupIdList();
			if (CollectionUtils.isEmpty(groupIdList)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{groupIdList}}");
			}

			String clientId = httpReq.getClientId();
			if (!StringUtils.hasLength(clientId)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{clientId}}");
			}

			TsmpClient tsmpClient = getTsmpClientDao().findById(clientId).orElse(null);
			if (tsmpClient == null) {
				throw TsmpDpAaRtnCode._1344.throwing();
			}

			// 檢查 client 沒有 email
			String clientEmails = tsmpClient.getEmails();
			if (!StringUtils.hasLength(clientEmails)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{clientEmails}}");
			}
			
			String createUserName = authorization.getUserName();

			// 生效日轉換,yyyy/MM/dd 改為這一天的最開始
			String effectiveAtStr = httpReq.getEffectiveAt();
			Date effectiveDate = getEffectiveDateToStartOfDay(effectiveAtStr);

			// 到期日轉換,yyyy/MM/dd 改為這一天的最後一秒
			String expiredAtStr = httpReq.getExpiredAt();
			Date expiredDate = getExpiredDateToEndOfDay(expiredAtStr);

			// 產生 xApiKey
			String xApiKey = createXApiKey();// 產生的明文 xApiKey
			String xApiKeyMask = getXApiKeyMask(xApiKey);
			String xApiKeyEn = TokenHelper.getXApiKeyEn(xApiKey);
			
			// X-Api-Key記錄明文是否啟用,預設為false (true/false)
			boolean isXApiKeyPlain = getTsmpSettingService().getVal_X_API_KEY_PLAIN_ENABLE();
			
			DgrXApiKey dgrXApiKey = new DgrXApiKey();
			dgrXApiKey.setClientId(httpReq.getClientId());
			dgrXApiKey.setApiKeyAlias(httpReq.getApiKeyAlias());
			dgrXApiKey.setEffectiveAt(effectiveDate.getTime());
			dgrXApiKey.setExpiredAt(expiredDate.getTime());
			if (isXApiKeyPlain) {// 啟用
				//因為不安全,當啟用時,才寫入
				dgrXApiKey.setApiKey(xApiKey);
			}
			dgrXApiKey.setApiKeyMask(xApiKeyMask);
			dgrXApiKey.setApiKeyEn(xApiKeyEn);

			dgrXApiKey.setCreateUser(createUserName);
			dgrXApiKey.setCreateDateTime(DateTimeUtil.now());
			dgrXApiKey = getDgrXApiKeyDao().save(dgrXApiKey);

			Long apiKeyId = dgrXApiKey.getApiKeyId();
			for (String groupId : groupIdList) {
				DgrXApiKeyMap dgrXApiKeyMap = new DgrXApiKeyMap();
				dgrXApiKeyMap.setRefApiKeyId(apiKeyId);
				dgrXApiKeyMap.setGroupId(groupId);
				dgrXApiKeyMap.setCreateUser(createUserName);
				dgrXApiKeyMap.setCreateDateTime(DateTimeUtil.now());
				getDgrXApiKeyMapDao().save(dgrXApiKeyMap);
			}

			resp.setApiKeyMask(xApiKeyMask);

			// 寄信給 client, 通知 X-Api-Key 的結果
			if (isSendMail()) {
				sendMail(xApiKey, dgrXApiKey, tsmpClient, clientId, clientEmails, groupIdList, createUserName);
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	/**
	 *  寄信給 client, 通知 X-Api-Key 的結果
	 */
	private void sendMail(String xApiKey, DgrXApiKey dgrXApiKey, TsmpClient tsmpClient, String clientId,
			String clientEmail, List<String> groupIdList, String createUserName) throws Exception {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent tsmpMailEvent = getMail(xApiKey, dgrXApiKey, tsmpClient, clientEmail, groupIdList,
				createUserName);
		mailEvents.add(tsmpMailEvent);

		String identif = String.format(//
				"clientId=%s" //
				+ ",　createUserName=%s" //
				+ ",　mailType=%s" //
				+ ",　xApiKeyId=%s" //
				+ ",　xApiKey=%s" //
				, //
				clientId, //
				createUserName, //
				"X-Api-Key notice", //
				dgrXApiKey.getApiKeyId(), //
				dgrXApiKey.getApiKeyMask() //
		);

		try {
			getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(), getSendTime());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * 寄信給 client 的內容
	 */
	private TsmpMailEvent getMail(String xApiKey, DgrXApiKey dgrXApiKey, TsmpClient tsmpClient, String clientEmail,
			List<String> groupIdList, String createUserName) throws Exception {
		String subjTpltCode = "subject.xak-pass";
		String bodyTpltCode = "body.xak-pass";

		// 主旨
		Map<String, Object> subjectParams = new HashMap<>();
		subjectParams.put("projectName", "digiRunner");
		String subject = getMailHelper().buildNestedContent(subjTpltCode, subjectParams);
		
		// 內文
		List<Map<String, Object>> groupMappings = getGroupMappings(tsmpClient, groupIdList);

		Date createDateTime = dgrXApiKey.getCreateDateTime();
		Date effectiveDate = new Date(dgrXApiKey.getEffectiveAt());
		Date expiryDate = new Date(dgrXApiKey.getExpiredAt());

		Map<String, Object> bodyParams = new HashMap<>();
		bodyParams.put("projectName", "digiRunner");
		bodyParams.put("xApiKeyId", dgrXApiKey.getApiKeyId());
		bodyParams.put("xApiKeyAlias", dgrXApiKey.getApiKeyAlias());
		bodyParams.put("xApiKey", xApiKey);// 為明文的 xApiKey
		bodyParams.put("effectiveDate", DateTimeUtil.dateTimeToString(effectiveDate, DateTimeFormatEnum.西元年月日).orElse(null));
		bodyParams.put("expiryDate", DateTimeUtil.dateTimeToString(expiryDate, DateTimeFormatEnum.西元年月日).orElse(null));
		bodyParams.put("createDateTime",
				DateTimeUtil.dateTimeToString(createDateTime, DateTimeFormatEnum.西元年月日時分秒).orElse(null));
		bodyParams.put("createUserName", createUserName);
		bodyParams.put("groupMappings", groupMappings);

		String content = getMailHelper().buildNestedContent(bodyTpltCode, bodyParams);

		// 收件人為 client
		String recipients = clientEmail;

		return new TsmpMailEventBuilder() //
				.setSubject(subject).setContent(content).setRecipients(recipients).setCreateUser("SYS")
				.setRefCode(bodyTpltCode).build();
	}

	/**
	 * 取得 email 內容 "groupMappings"
	 */
	private List<Map<String, Object>> getGroupMappings(TsmpClient tsmpClient, List<String> groupIdList) {
		Map<String, Object> groupMapping = new HashMap<>();
		groupMapping.put("clientId", tsmpClient.getClientId());
		groupMapping.put("clientName", tsmpClient.getClientName());
		groupMapping.put("clientAlias", ServiceUtil.nvl(tsmpClient.getClientAlias()));

		List<String> groupNameList = getGroupNames(groupIdList);
		groupMapping.put("groupNames", groupNameList);

		List<Map<String, Object>> groupMappings = new ArrayList<>();
		groupMappings.add(groupMapping);

		return groupMappings;
	}

	/**
	 * 取得 email 內容 "groupNames"
	 */
	private List<String> getGroupNames(List<String> groupIdList) {
		List<String> groupNameList = new ArrayList<>();
		for (String groupId : groupIdList) {
			if (StringUtils.hasLength(groupId)) {
				TsmpGroup tsmpGroup = getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(groupId, "0");
				if (tsmpGroup != null) {
					String groupName = tsmpGroup.getGroupName();
					groupNameList.add(groupId + "-" + groupName);
				}
			}
		}

		return groupNameList;
	}

	/**
	 * 取得多久後寄發Email時間
	 */
	protected String getSendTime() {
		String sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();// 多久後寄發Email(ms)
		return sendTime;
	}

	/**
	 * 產生 X-Api-Key
	 */
	private String createXApiKey() {
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString();
		String xApiKey = Base64Util.base64URLEncode(uuidStr.getBytes());// 做 Base64Url Encode
		return xApiKey;
	}

	/**
	 * 取得 X-Api-Key 經過遮罩的值
	 */
	private String getXApiKeyMask(String xApiKey) {
		String start = xApiKey.substring(0, 3);
		String end = xApiKey.substring(xApiKey.length() - 3);
		String xApiKeyMask = start + "***" + end;

		return xApiKeyMask;
	}

	/**
	 * 取得生效日, 若沒有值, 則預設為今天, <br>
	 * 改為這一天的最開始
	 */
	private Date getEffectiveDateToStartOfDay(String effectiveAtStr) throws Exception {
		Date effectiveDate = null;
		if (!StringUtils.hasLength(effectiveAtStr)) {// 若沒有值
			effectiveDate = new Date(System.currentTimeMillis());// 預設為今天

		} else {
			Long effectiveAtLong = Long.parseLong(effectiveAtStr);
			effectiveDate = new Date(effectiveAtLong);
		}

		Optional<String> opt = DateTimeUtil.dateTimeToString(effectiveDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}	
		String effectiveDateStr = opt.get();
		effectiveDate = getStartOfDay(effectiveDateStr);

		return effectiveDate;
	}

	/**
	 * 取得過期日, <br>
	 * 改為這一天的最後一秒
	 */
	private Date getExpiredDateToEndOfDay(String expiredAtStr) throws Exception {
		Long expiredAtLong = Long.parseLong(expiredAtStr);
		Date expiredDate = new Date(expiredAtLong);

		Optional<String> opt = DateTimeUtil.dateTimeToString(expiredDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}	
		String expiredDateStr = opt.get();
		expiredDate = getEndOfDay(expiredDateStr);

		return expiredDate;
	}

	/**
	 * 將 yyyy/MM/dd 改為這一天的最開始
	 */
	private Date getStartOfDay(String dateStr) throws Exception {
		dateStr += " 00:00:00.000";// 改為這一天的最開始
		Optional<Date> opt_s = DateTimeUtil.stringToDateTime(dateStr, DateTimeFormatEnum.西元年月日時分秒毫秒_2);
		if (!(opt_s.isPresent())) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		Date date = opt_s.get();

		return date;
	}

	/**
	 * 將 yyyy/MM/dd 改為這一天的最後一秒
	 */
	private Date getEndOfDay(String dateStr) {
		dateStr += " 23:59:59.999";// 改為這一天的最後一秒
		Optional<Date> opt_e = DateTimeUtil.stringToDateTime(dateStr, DateTimeFormatEnum.西元年月日時分秒毫秒_2);
		if (!(opt_e.isPresent())) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		Date date = opt_e.get();

		return date;
	}

	//for UnitTest
	protected boolean isSendMail() {
		return true;
	}
	
	protected DgrXApiKeyDao getDgrXApiKeyDao() {
		return dgrXApiKeyDao;
	}

	protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
		return dgrXApiKeyMapDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected MailHelper getMailHelper() {
		return mailHelper;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}
}
