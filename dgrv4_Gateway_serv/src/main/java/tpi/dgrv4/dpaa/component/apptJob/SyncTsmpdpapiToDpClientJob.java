package tpi.dgrv4.dpaa.component.apptJob;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.RandomUtils;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.autoInitSQL.AutoInitSQL;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@SuppressWarnings("serial")
@Transactional
public class SyncTsmpdpapiToDpClientJob extends ApptJob {

	private enum Command {
		RESET_MIMA("reset.mima"),
		RESET_FUNC("reset.func");
		private String command;
		private Command(String command) {
			this.command = command;
		}
		public static Optional<Command> resolve(String command) {
			for (Command c : values()) {
				if (c.getCommand().equalsIgnoreCase(command)) {
					return Optional.of(c);
				}
			}
			return Optional.empty();
		}
		public String getCommand() {
			return command;
		}
	}

	private TsmpFuncDao tsmpFuncDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;
	private TsmpClientDao tsmpClientDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpSettingDao tsmpSettingDao;
	private AutoInitSQL autoInitSQL;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	
	@Autowired
	public SyncTsmpdpapiToDpClientJob(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpFuncDao tsmpFuncDao, TsmpRoleFuncDao tsmpRoleFuncDao,
			TsmpClientDao tsmpClientDao, OauthClientDetailsDao oauthClientDetailsDao, TsmpSettingDao tsmpSettingDao,
			AutoInitSQL autoInitSQL, DgrAuditLogService dgrAuditLogService, TsmpTAEASKHelper tsmpTAEASKHelper) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
		this.tsmpClientDao = tsmpClientDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.autoInitSQL = autoInitSQL;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
	}

	@Override
	public String runApptJob() throws Exception {
		Command cmd = getCommand();
		String result;
		switch (cmd) {
			case RESET_FUNC:
				result = resetFunc();
				break;
			case RESET_MIMA:
				result = resetMima();
				break;
			default:
				throw new Exception("Unsupport command: " + cmd.getCommand());
		}
		return result;
	}

	protected Command getCommand() throws Exception {
		String params = super.getTsmpDpApptJob().getInParams();
		return getCommand(params);
	}

	protected Command getCommand(String params) throws Exception {
		Optional<Command> opt = Command.resolve(params);
		if (opt.isEmpty()) {
			throw new Exception("Unknown command: " + params);
		}
		return opt.get();
	}

	// 修改License後，刷新功能清單與TSMP_SETTING值
	protected String resetFunc() {
		TPILogger.tl.debug("Start reload TsmpSetting & TsmpFun & TsmpRoleFunc");

		// 刷新 TSMP_SETTING 值
		getAutoInitSQL().updateTsmpSetting();
		step("1/4");

		getTsmpRoleFuncDao().deleteAllInBatch();
		step("2/4");

		getTsmpFuncDao().deleteAllInBatch();
		step("3/4");

		getAutoInitSQL().initializeFunction();
		step("4/4");

		return "SUCCESS";
	}

	protected String resetMima() {
		return resetMima(null);
	}

	protected String resetMima(InnerInvokeParam iip) {
		if (iip == null) {
			try {
				iip = InnerInvokeParam.getInstance(new HttpHeaders(), null, new TsmpAuthorization());
			} catch (Exception e) {
				TPILogger.tl.debug("Unable to write audit log for resetting password of Client: '" + getDefaultClientId() + "'.");
			}
		}
		
		String defaultClientPw = genRandom();	// 明文

		// 1. 確認是否存在 client id = 'YWRtaW5Db25zb2xl', 若沒有則建立, 默認它為AC帳號
		// RESET TsmpClient
		resetTsmpClient(iip, defaultClientPw);
		step("1/3");

		// 2. OAUTH_CLIENT_DETAILS 密碼資料
		// RESET OauthClientDetails
		resetOauthClientDetails(iip, defaultClientPw);
		step("2/3");

		// 3. 檢查是否環境變數有"TAEASK"，若有:則採用為AES的key加密寫入，若無:則明碼寫入。
		// 在tsmp_setting資料表將下列帳號與密碼刪除
		resetTsmpSetting(iip, defaultClientPw);
		step("3/3");

		return "SUCCESS";
	}

	protected String genRandom() {
		int length = 8;
	    boolean useLetters = true;
	    boolean useNumbers = false;
	    String generatedString = RandomUtils.randomString(length, useLetters, useNumbers);
		return generatedString;
	}

	protected TsmpClient resetTsmpClient(InnerInvokeParam iip, String defaultClientPw) {
		String defaultClientId = getDefaultClientId();
		
		Optional<TsmpClient> opt_tsmpClient = getTsmpClientDao().findById(defaultClientId);
		TsmpClient tsmpClient = null;
		String oldRowStr = null;
		AuditLogEvent auditLogEvent = null;
		if (opt_tsmpClient.isEmpty()) {
			tsmpClient = new TsmpClient();
			tsmpClient.setClientId(defaultClientId);
			tsmpClient.setClientName("Admin Console");
			tsmpClient.setClientStatus("1");
			tsmpClient.setTps(100);
			tsmpClient.setCreateTime(DateTimeUtil.now());
			String updateUser = super.getTsmpDpApptJob().getCreateUser();
			tsmpClient.setCreateUser(updateUser);
			tsmpClient.setOwner("TSMP");
			
			auditLogEvent = AuditLogEvent.ADD_CLIENT;
		} else {
			tsmpClient = opt_tsmpClient.get();
			oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClient); // 舊資料統一轉成 String
			// 解除鎖定(若有鎖定)
			tsmpClient.setClientStatus("1");
			tsmpClient.setPwdFailTimes(0);
			
			auditLogEvent = AuditLogEvent.UPDATE_CLIENT;
		}

		String sha512ClientPwd = ServiceUtil.getSHA512ToBase64(defaultClientPw);
		tsmpClient.setClientSecret(sha512ClientPwd);
		tsmpClient = getTsmpClientDao().save(tsmpClient);
		
		// 寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, auditLogEvent.value());

		// 寫入 Audit Log D
		if (AuditLogEvent.ADD_CLIENT.compareTo(auditLogEvent) == 0) {
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpClient.class.getSimpleName(),
					TableAct.C.value(), null, tsmpClient);
		} else {
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpClient.class.getSimpleName(),
					TableAct.U.value(), oldRowStr, tsmpClient);
		}
		
		return tsmpClient;
	}

	protected OauthClientDetails resetOauthClientDetails(InnerInvokeParam iip, String defaultClientPw) {
		String defaultClientId = getDefaultClientId();

		OauthClientDetails oauthClientDetails = getOauthClientDetailsDao().findById(defaultClientId).orElse(new OauthClientDetails());
		String oldRowStr = null;
		if (StringUtils.hasLength(oauthClientDetails.getClientId())) {
			oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthClientDetails); // 舊資料統一轉成 String
		} else {
			oauthClientDetails.setClientId(defaultClientId);
			oauthClientDetails.setResourceIds(Base64Util.base64EncodeWithoutPadding("adminAPI".getBytes()));
			oauthClientDetails.setScope("1000"); // 1000 為 AC 專用
			oauthClientDetails
					.setAuthorizedGrantTypes("client_credentials,refresh_token,password,authorization_code");
			oauthClientDetails.setAuthorities("client");
			oauthClientDetails.setAdditionalInformation("{}");
		}

		String base64ClientPwd = Base64Util.base64Encode(defaultClientPw.getBytes());
		oauthClientDetails.setClientSecret(OAuthUtil.bCryptEncode(base64ClientPwd)); // 重設密碼
		oauthClientDetails = getOauthClientDetailsDao().save(oauthClientDetails);

		// 寫入 Audit Log D
		if (oldRowStr == null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, OauthClientDetails.class.getSimpleName(),
					TableAct.C.value(), null, oauthClientDetails);
		} else {
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, OauthClientDetails.class.getSimpleName(),
					TableAct.U.value(), oldRowStr, oauthClientDetails);
		}

		return oauthClientDetails;
	}

	// TSMP_AC_CLIENT_ID, TSMP_AC_CLIENT_PW
	protected void resetTsmpSetting(InnerInvokeParam iip, String defaultClientPw) {
		boolean isExists = getTsmpSettingDao().existsById(TsmpSettingDao.Key.TSMP_AC_CLIENT_ID);
		if (isExists) {
			getTsmpSettingDao().deleteById(TsmpSettingDao.Key.TSMP_AC_CLIENT_ID);
		}
		
		isExists = getTsmpSettingDao().existsById(TsmpSettingDao.Key.TSMP_AC_CLIENT_PW);
		if (isExists) {
			getTsmpSettingDao().deleteById(TsmpSettingDao.Key.TSMP_AC_CLIENT_PW);
		}

		// 取得帳密
		Pair<String, String> credentials = getDefaultClientCredentials(defaultClientPw);

		// TsmpSetting 新增帳號與密碼
		List<TsmpSetting> newDataList = new ArrayList<TsmpSetting>();
		TsmpSetting tsmpAcClientID = new TsmpSetting();
		tsmpAcClientID.setId(TsmpSettingDao.Key.TSMP_AC_CLIENT_ID);
		tsmpAcClientID.setValue(credentials.getFirst());
		newDataList.add(tsmpAcClientID);

		TsmpSetting tsmpAcClientPW = new TsmpSetting();
		tsmpAcClientPW.setId(TsmpSettingDao.Key.TSMP_AC_CLIENT_PW);
		tsmpAcClientPW.setValue(credentials.getSecond());
		newDataList.add(tsmpAcClientPW);

		getTsmpSettingDao().saveAll(newDataList);
	}
	
	protected String getDefaultClientId() {
		final String defaultClientId = "YWRtaW5Db25zb2xl";
		return defaultClientId;
	}

	protected Pair<String, String> getDefaultClientCredentials(String defaultClientPw) {
		String tsmpAcClientIDPlaintext = getDefaultClientId();
		String tsmpAcClientIDValue = tsmpAcClientIDPlaintext;

		// 自動隨機生成帳號與密碼
		String tsmpAcClientPWPlaintext = defaultClientPw;
		// 密碼要轉 base64後再加密
		String tsmpAcClientPWValue = Base64Util.base64Encode(tsmpAcClientPWPlaintext.getBytes());

		// 檢查是否環境變數有"TAEASK"，若有:則採用為AES的key加密寫入，若無:則明碼寫入。
		if (hasTAEASK()) {
			tsmpAcClientIDValue = getTsmpTAEASKHelper().encrypt(tsmpAcClientIDValue);
			tsmpAcClientPWValue = getTsmpTAEASKHelper().encrypt(tsmpAcClientPWValue);
		}
		
		return Pair.of(tsmpAcClientIDValue, tsmpAcClientPWValue);
	}

	protected boolean hasTAEASK() {
		return StringUtils.hasText(System.getenv("TAEASK"));
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return this.tsmpFuncDao;
	}

	protected TsmpRoleFuncDao getTsmpRoleFuncDao() {
		return this.tsmpRoleFuncDao;
	}

	protected AutoInitSQL getAutoInitSQL() {
		return this.autoInitSQL;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return this.oauthClientDetailsDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return this.dgrAuditLogService;
	}

	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}

}