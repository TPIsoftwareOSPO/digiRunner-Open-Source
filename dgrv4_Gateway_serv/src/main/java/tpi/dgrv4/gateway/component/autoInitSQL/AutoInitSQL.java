package tpi.dgrv4.gateway.component.autoInitSQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.ifs.ITsmpFirstInstallHelper;
import tpi.dgrv4.common.utils.LicenseEditionType;
import tpi.dgrv4.common.utils.LicenseEditionTypeVo;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.AuthoritiesTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.DgrRdbConnectionTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.OauthClientDetailsTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpAlertTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpClientGroupTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpClientTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpDpItemsTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpDpMailTpltTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpFuncTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpGroupTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpOrganizationTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpReportUrlTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpRoleAlertTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpRoleRoleMappingTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpRoleTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpRtnCodeTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpSecurityLevelInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpSettingTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpUserTableInitializr;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.UserTableInitializer;
import tpi.dgrv4.common.utils.autoInitSQL.vo.AuthoritiesVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.AutoInitSQLTsmpDpMailTpltVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.AutoInitSQLTsmpRoleRoleMappingVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.DgrRdbConnectionVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.OauthClientDetailsVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpAlertVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpClientGroupVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpClientVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpDpItemsVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpFuncVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpGroupVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpOrganizationVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpReportUrlVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpRoleAlertVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpRoleVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpRtnCodeVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpSecurityLevelVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpSettingVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpUserVo;
import tpi.dgrv4.common.utils.autoInitSQL.vo.UsersVo;
import tpi.dgrv4.dpaa.component.rjob.SystemDefaultRjobInitializer;
import tpi.dgrv4.dpaa.service.DPB0101Service;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.vo.DPB0101Req;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.AuthoritiesId;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientGroupId;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpFuncId;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.entity.TsmpRoleFuncId;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.autoInitSQL.AutoInitSQLTsmpDpMailTplt;
import tpi.dgrv4.entity.entity.autoInitSQL.AutoInitSQLTsmpRoleRoleMapping;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayer;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrlId;
import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlert;
import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlertId;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.entity.repository.TsmpRoleAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.entity.repository.autoInitSQL.AutoInitSQLTsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.autoInitSQL.AutoInitSQLTsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.autoInitSQL.AutoInitSQLTsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class AutoInitSQL {
	
//	@Autowired(required = false)
	private ITsmpFirstInstallHelper tsmpFirstInstallHelper;
	
	private LicenseUtilBase licenseUtil; // non-Singleton

	private final TsmpUserDao tsmpUserDao;
	private final UsersDao usersDao;
	private final TsmpOrganizationDao tsmpOrganizationDao;
	private final TsmpRoleDao tsmpRoleDao;
	private final TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	private final AutoInitSQLTsmpRoleRoleMappingDao autoInitSQLTsmpRoleRoleMappingDao;
	private final AuthoritiesDao authoritiesDao;
	private final TsmpDpItemsDao tsmpDpItemsDao;
	private final TsmpRtnCodeDao tsmpRtnCodeDao;
	private final TsmpSettingDao tsmpSettingDao;
	private final TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private final AutoInitSQLTsmpDpApptRjobDDao autoInitSQLTsmpDpApptRjobDDao;
	private final TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private final TsmpFuncDao tsmpFuncDao;
	private final TsmpRoleFuncDao tsmpRoleFuncDao;
	private final TsmpClientDao tsmpClientDao;
	private final OauthClientDetailsDao oauthClientDetailsDao;
	private final TsmpSecurityLevelDao tsmpSecurityLevelDao;
	private final TsmpReportUrlDao tsmpReportUrlDao;
	private final AutoInitSQLTsmpDpMailTpltDao autoInitSQLTsmpDpMailTpltDao;
	private final TsmpGroupDao tsmpGroupDao;
	private final TsmpClientGroupDao tsmpClientGroupDao;
	private final TsmpDpChkLayerDao tsmpDpChkLayerDao;
	private final TsmpAlertDao tsmpAlertDao;
	private final TsmpRoleAlertDao tsmpRoleAlertDao;
	private final DgrRdbConnectionDao dgrRdbConnectionDao;
	private final SeqStoreService seqStoreService;
	private final DPB0101Service dpb0101Service;
	private final BcryptParamHelper bcryptParamHelper;
	private final TsmpDpClientextDao tsmpDpClientextDao;
	private final DgrAuditLogService dgrAuditLogService;
	protected final TsmpUserTableInitializr tsmpUserTableInitializr;
	private final UserTableInitializer userTableInitializr;
	private final TsmpOrganizationTableInitializer tsmpOrganizationTableInitializr;
	private final TsmpRoleTableInitializer tsmpRoleTableInitializr;
	private final TsmpRoleRoleMappingTableInitializer tsmpRoleRoleMappingTableInitializr;
	private final AuthoritiesTableInitializer authoritiesTableInitializr;
	private final TsmpDpItemsTableInitializer tsmpDpItemsTableInitializr;
	private final TsmpRtnCodeTableInitializer tsmpRtnCodeTableInitializr;
	private final TsmpSettingTableInitializer tsmpSettingTableInitializr;
	private final TsmpClientTableInitializer tsmpClientTableInitializr;
	private final OauthClientDetailsTableInitializer oauthClientDetailsTableInitializr;
	private final TsmpSecurityLevelInitializer tsmpSecurityLevelTableInitializr;
	private final TsmpReportUrlTableInitializer tsmpReportUrlTableInitializr;
	private final TsmpGroupTableInitializer tsmpGroupTableInitializr;
	private final TsmpClientGroupTableInitializer tsmpClientGroupTableInitializr;
	private final TsmpFuncTableInitializer tsmpFuncTableInitializr;
	private final TsmpDpMailTpltTableInitializer tsmpDpMailTpltTableInitializr;
	private final TsmpAlertTableInitializer tsmpAlertTableInitializer;
	private final TsmpRoleAlertTableInitializer tsmpRoleAlertTableInitializer;
	private final DgrRdbConnectionTableInitializer dgrRdbConnectionTableInitializer;
	
	@Autowired
	public void setAutoInitSQL(@Nullable ITsmpFirstInstallHelper tsmpFirstInstallHelper,
			ApplicationContext applicationContext) {
		this.tsmpFirstInstallHelper = tsmpFirstInstallHelper;
		if (applicationContext != null) {
			this.licenseUtil = applicationContext.getBean(LicenseUtilBase.class); // non-Singleton
		}
	}

	@Value("${service.mail.installation}")
	private String mailLocalelInstallation;

	private int featureListMaxLength;

	private static LicenseEditionTypeVo licenseEditionTypeVo;

	public static List<TsmpUser> tsmpUserlist = new LinkedList<>();
	public static List<Users> userlist = new LinkedList<>();
	public static List<TsmpOrganization> tsmpOrganizationList = new LinkedList<>();
	public static List<TsmpRole> tsmpRolelist = new LinkedList<>();
	public static List<AutoInitSQLTsmpRoleRoleMapping> autoInitSQLTsmpRoleRoleMappinglist = new LinkedList<>();
	public static List<Authorities> authoritieslist = new LinkedList<>();
	public static List<TsmpDpItems> tsmpDpItemslist = new LinkedList<>();
	public static List<TsmpRtnCode> tsmpRtnCodelist = new LinkedList<>();
	public static List<TsmpSetting> tsmpSettinglist = new LinkedList<>();
	public static List<TsmpClient> tsmpClientlist = new LinkedList<>();
	public static List<OauthClientDetails> oauthClientDetailslist = new LinkedList<>();
	public static List<TsmpSecurityLevel> tsmpSecurityLevelList = new LinkedList<>();
	public static List<TsmpFunc> tsmpFuncList = new LinkedList<>();
	public static List<TsmpRoleFunc> tsmpRoleFunclist = new LinkedList<>();
	public static List<TsmpReportUrl> tsmpReportUrlList = new LinkedList<>();
	public static List<TsmpGroup> tsmpGroupList = new LinkedList<>();
	public static List<TsmpClientGroup> tsmpClientGroupList = new LinkedList<>();
	public static List<AutoInitSQLTsmpDpMailTplt> autoInitSQLTsmpDpMailTpltList = new LinkedList<>();
	public static List<TsmpAlert> tsmpAlertList = new LinkedList<>();
	public static List<TsmpRoleAlert> tsmpRoleAlertList = new LinkedList<>();
	public static List<DgrRdbConnection> dgrRdbConnectionList = new LinkedList<>();

	@PostConstruct
	@Transactional
	public void init() {

		// 決定要不要執行AutoInitSQL
		Optional<TsmpSetting> autoInitsqlFlag = getTsmpSettingDao().findById(TsmpSettingDao.Key.AUTO_INITSQL_FLAG);
		if (autoInitsqlFlag.isEmpty() == false && "false".equalsIgnoreCase(autoInitsqlFlag.get().getValue())) {
			TPILogger.tl.info("AUTO_INITSQL_FLAG：false");
			return;
		}

		TPILogger.tl.info("AUTO_INITSQL_FLAG：true");

		String logMsgStart = "============Start AutoInitSQL =======================\n"
				+ "        =====================================================";
		TPILogger.tl.info(logMsgStart);
		try {
			TPILogger.tl.info("Insert TsmpUser Finish ");
			insertTsmpUser();
			TPILogger.tl.info("Insert TsmpUser Finish ");
			insertUsers();
			TPILogger.tl.info("Insert User Finish ");
			insertTsmpOrganization();
			TPILogger.tl.info("Insert TsmpOrganization Finish ");
			insertTsmpRole();
			TPILogger.tl.info("Insert TsmpRole Finish ");
			insertTsmpRoleRoleMapping();
			TPILogger.tl.info("Insert TsmpRoleRoleMapping Finish ");
			insertAuthorities();
			TPILogger.tl.info("Insert Authorities Finish ");
			deleteTheOldTsmpDpItem();
			TPILogger.tl.info("delete TsmpDpItem Finish ");
			insertTsmpDpItems();
			TPILogger.tl.info("Insert TsmpDpItems Finish ");
			insertTsmpRtnCode();
			TPILogger.tl.info("Insert TsmpRtnCode Finish ");
			insertTsmpSetting();
			TPILogger.tl.info("Insert TsmpSetting Finish ");
			insertTsmpClient();
			TPILogger.tl.info("Insert TsmpClient Finish ");
			insertOauthClientDetails();
			TPILogger.tl.info("Insert OauthClientDetails Finish ");
			insertTsmpSecurityLevel();
			TPILogger.tl.info("Insert TsmpSecurityLevel Finish ");
			insertTsmpReportUrl();
			TPILogger.tl.info("Insert TsmpReportUrl Finish ");
			insertTsmpGroup();
			TPILogger.tl.info("Insert TsmpGroup Finish ");
			insertTsmpClientGroup();
			TPILogger.tl.info("Insert TsmpClientGroup Finish ");
			insertTsmpAlert();
			TPILogger.tl.info("Insert TsmpAlert Finish ");
			insertTsmpRoleAlert();
			TPILogger.tl.info("Insert TsmpRoleAlert Finish ");
			insertDgrRdbConnection();
			TPILogger.tl.info("Insert DgrRdbConnection Finish ");

//			 TsmpFunc TsmpRoleFunc 初始新增資料
			initializeFunction();

			// 新增TsmpDpMailTplt 設定檔決定語系，若無資料預設英文
			if (!StringUtils.hasLength(mailLocalelInstallation)) {
				mailLocalelInstallation = LocaleType.EN_US;
			}
			TPILogger.tl.info("Insert TsmpDpMailTplt Localel by " + mailLocalelInstallation);

			if (mailLocalelInstallation.equals(LocaleType.ZH_TW)) {

				insertTsmpDpMailTplt4TW();

			} else {

				insertTsmpDpMailTplt4US();

			}

			TPILogger.tl.info("Insert TsmpDpMailTplt Finish ");

			// =========== 這邊開始是資料邏輯的關聯設定 ===========
			// 授權 'manager' 使用者簽核 Open API Key 類型的簽核單
			authorizeManagerToSignOpenApiKey();
			// 寫入系統預設的週期排程
			createDefaultRjobs();
			// 重新授權 ADMIN 角色所擁有的功能
			reauthorizeAdminRoleFunc();
			// 同步 tsmp_dp_clientext 和 tsmp_client
			syncTsmpClientAndTsmpDpClientext();
			// 重設 adminConsole 用戶端的帳密
			resetAdminConsoleData();

			// 清空所有 static List
			clearAllList();

		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			throw e;
		}

		String logMsgEnd = "=====================================================\n"
				+ "        ===============AutoInitSQL is Success================";
		TPILogger.tl.info(logMsgEnd);
	}

	private void clearAllList() {
		tsmpUserlist.clear();
		userlist.clear();
		tsmpOrganizationList.clear();
		tsmpRolelist.clear();
		autoInitSQLTsmpRoleRoleMappinglist.clear();
		authoritieslist.clear();
		tsmpDpItemslist.clear();
		tsmpRtnCodelist.clear();
		tsmpSettinglist.clear();
		tsmpClientlist.clear();
		oauthClientDetailslist.clear();
		tsmpSecurityLevelList.clear();
		tsmpFuncList.clear();
		tsmpRoleFunclist.clear();
		tsmpReportUrlList.clear();
		tsmpGroupList.clear();
		tsmpClientGroupList.clear();
		autoInitSQLTsmpDpMailTpltList.clear();
		tsmpAlertList.clear();
		tsmpRoleAlertList.clear();
	}

	/**
	 * TsmpFunc TsmpRoleFunc 初始新增資料 ，此方法是為了給「恢復原廠設定」排程所呼叫
	 */
	public void initializeFunction() {
		insertTsmpFunc();
		TPILogger.tl.info("Insert TsmpFunc Finish ");
		insertTsmpRoleFunc();
		TPILogger.tl.info("Insert TsmpRoleFunc Finish ");
	}

	private void getFeatureListMaxLength() {
		int max = 0;
		for (String value : TsmpFuncTableInitializer.ALPHA) {
			if (value.length() > max) {
				max = value.length();
			}
		}
		featureListMaxLength = max;
	}

	public LicenseEditionType getLicenseEdition() {
		String tsmpEdition = TsmpSettingDao.Key.TSMP_EDITION;
		Optional<TsmpSetting> optTsmpSetting = getTsmpSettingDao().findById(tsmpEdition);

		// 判斷TSMP_EDITION是否存在TsmpSetting
		if (optTsmpSetting.isEmpty()) {
			TPILogger.tl.error("TSMP_EDITION is not found in TSMP_SETTING");
			return LicenseEditionType.Express;
		}

		TsmpSetting tsmpSetting = optTsmpSetting.get();
		String licenseKey = tsmpSetting.getValue();
		// 判斷TSMP_EDITION是否有值
		if (!StringUtils.hasLength(licenseKey)) {
			TPILogger.tl.error("Value of TSMP_EDITION is empty");
			return LicenseEditionType.Express;
		}

		TPILogger.tl.debug("licenseKey : " + licenseKey);
		getLicenseUtil().initLicenseUtil(licenseKey, null);
		String value = getLicenseUtil().getEdition(licenseKey);

		LicenseEditionType licenseEditionType = LicenseEditionType.resolve(value);

		if (licenseEditionType == null) {
			String types = Arrays.stream(LicenseEditionType.values()).map(type -> type.name())
					.collect(Collectors.joining(", "));
			TPILogger.tl.error("Failed to parse edition from license key. Available editions are: " + types
					+ ", but it parsed as:" + value);
			return LicenseEditionType.Express;
		}

		return licenseEditionType;
	}

	private void insertTsmpDpMailTplt4US() {
		List<AutoInitSQLTsmpDpMailTpltVo> list = getTsmpDpMailTpltTableInitializr().insertTsmpDpMailTplt4US();
		Long mailtpltId;
		for (AutoInitSQLTsmpDpMailTpltVo autoInitSQLTsmpDpMailTpltVo : list) {
			AutoInitSQLTsmpDpMailTplt autoInitSQLTsmpDpMailTplt = new AutoInitSQLTsmpDpMailTplt();
			BeanUtils.copyProperties(autoInitSQLTsmpDpMailTpltVo, autoInitSQLTsmpDpMailTplt);
			mailtpltId = autoInitSQLTsmpDpMailTplt.getMailtpltId();
			// 若DB有資料則list不新增
			if (!getAutoInitSQLTsmpDpMailTpltDao().existsById(mailtpltId)) {
				autoInitSQLTsmpDpMailTpltList.add(autoInitSQLTsmpDpMailTplt);
			}
		}

		if (autoInitSQLTsmpDpMailTpltList.size() > 0) {
			getAutoInitSQLTsmpDpMailTpltDao().saveAllAndFlush(autoInitSQLTsmpDpMailTpltList);
		}
	}

	private void insertTsmpDpMailTplt4TW() {
		List<AutoInitSQLTsmpDpMailTpltVo> list = getTsmpDpMailTpltTableInitializr().insertTsmpDpMailTplt4TW();
		Long mailtpltId;
		for (AutoInitSQLTsmpDpMailTpltVo autoInitSQLTsmpDpMailTpltVo : list) {
			AutoInitSQLTsmpDpMailTplt autoInitSQLTsmpDpMailTplt = new AutoInitSQLTsmpDpMailTplt();
			BeanUtils.copyProperties(autoInitSQLTsmpDpMailTpltVo, autoInitSQLTsmpDpMailTplt);
			mailtpltId = autoInitSQLTsmpDpMailTplt.getMailtpltId();

			// 若DB有資料則list不新增
			if (!getAutoInitSQLTsmpDpMailTpltDao().existsById(mailtpltId)) {
				autoInitSQLTsmpDpMailTpltList.add(autoInitSQLTsmpDpMailTplt);
			}
		}

		if (autoInitSQLTsmpDpMailTpltList.size() > 0) {
			getAutoInitSQLTsmpDpMailTpltDao().saveAllAndFlush(autoInitSQLTsmpDpMailTpltList);
		}
	}

	private void insertTsmpSecurityLevel() {

		List<TsmpSecurityLevelVo> tsmpSecurityLevelVos = getTsmpSecurityLevelTableInitializr()
				.insertTsmpSecurityLevel();
		String securityLevelId;
		for (TsmpSecurityLevelVo tsmpSecurityLevelVo : tsmpSecurityLevelVos) {
			TsmpSecurityLevel tsmpSecurityLevel = new TsmpSecurityLevel();
			BeanUtils.copyProperties(tsmpSecurityLevelVo, tsmpSecurityLevel);

			// 若DB有資料則list不新增
			securityLevelId = tsmpSecurityLevel.getSecurityLevelId();
			if (!getTsmpSecurityLevelDao().existsById(securityLevelId)) {
				tsmpSecurityLevelList.add(tsmpSecurityLevel);
			}

		}

		if (tsmpSecurityLevelList.size() > 0) {
			getTsmpSecurityLevelDao().saveAllAndFlush(tsmpSecurityLevelList);
		}
	}

	private void insertDgrRdbConnection() {
		List<DgrRdbConnectionVo> dgrRdbConnectionVos = getDgrRdbConnectionTableInitializer().insertDgrRdbConnection();
		for (DgrRdbConnectionVo vo : dgrRdbConnectionVos) {
			DgrRdbConnection dbVo = new DgrRdbConnection();
			BeanUtils.copyProperties(vo, dbVo);
			if (!getDgrRdbConnectionDao().existsById(vo.getConnectionName())) {
				dgrRdbConnectionList.add(dbVo);
			}
		}

		if (dgrRdbConnectionList.size() > 0) {
			getDgrRdbConnectionDao().saveAll(dgrRdbConnectionList);
		}
	}
	
	private void insertTsmpRoleAlert() {
		List<TsmpRoleAlertVo> tsmpRoleAlertVos = getTsmpRoleAlertTableInitializer().insertTsmpRoleAlert();
		String roleId;
		Long alertId;
		for (TsmpRoleAlertVo tsmpRoleAlertVo : tsmpRoleAlertVos) {
			TsmpRoleAlert tsmpRoleAlert = new TsmpRoleAlert();
			BeanUtils.copyProperties(tsmpRoleAlertVo, tsmpRoleAlert);
			roleId = tsmpRoleAlert.getRoleId();
			alertId = tsmpRoleAlert.getAlertId();
			TsmpRoleAlertId tsmpRoleAlertId = new TsmpRoleAlertId();
			tsmpRoleAlertId.setRoleId(roleId);
			tsmpRoleAlert.setAlertId(alertId);
			if (!getTsmpRoleAlertDao().existsById(tsmpRoleAlertId)) {
				tsmpRoleAlertList.add(tsmpRoleAlert);
			}
		}

		if (tsmpRoleAlertList.size() > 0) {
			getTsmpRoleAlertDao().saveAll(tsmpRoleAlertList);
		}
	}

	private void insertOauthClientDetails() {
		List<OauthClientDetailsVo> oauthClientDetailsVos = getOauthClientDetailsTableInitializr()
				.insertOauthClientDetails();
		String clientId;
		for (OauthClientDetailsVo oauthClientDetailsVo : oauthClientDetailsVos) {
			OauthClientDetails oauthClientDetails = new OauthClientDetails();
			BeanUtils.copyProperties(oauthClientDetailsVo, oauthClientDetails);

			// 若DB有資料則list不新增
			clientId = oauthClientDetails.getClientId();
			if (!getOauthClientDetailsDao().existsById(clientId)) {
				oauthClientDetailslist.add(oauthClientDetails);
			}
		}

		if (oauthClientDetailslist.size() > 0) {
			getOauthClientDetailsDao().saveAllAndFlush(oauthClientDetailslist);
		}
	}

	private void insertTsmpClient() {
		List<TsmpClientVo> tsmpClientVos = getTsmpClientTableInitializr().insertTsmpClient();
		String tsmpClientId;

		for (TsmpClientVo tsmpClientVo : tsmpClientVos) {
			TsmpClient tsmpClient = new TsmpClient();
			BeanUtils.copyProperties(tsmpClientVo, tsmpClient);
			// 若DB有資料則list不新增
			tsmpClientId = tsmpClient.getClientId();
			if (!getTsmpClientDao().existsById(tsmpClientId)) {
				tsmpClientlist.add(tsmpClient);
			}
		}

		if (tsmpClientlist.size() > 0) {
			getTsmpClientDao().saveAllAndFlush(tsmpClientlist);
		}
	}

	private void insertTsmpRoleFunc() {
		TsmpRoleFuncId tsmpRoleFuncId = new TsmpRoleFuncId();
		String funcCode;
		String roleId = "1000";
		getFeatureListMaxLength();
		for (TsmpFunc tsmpFunc : tsmpFuncList) {
			TsmpRoleFunc tsmpRoleFunc = new TsmpRoleFunc();
			funcCode = tsmpFunc.getFuncCode();
			tsmpRoleFuncId.setFuncCode(funcCode);
			tsmpRoleFuncId.setRoleId(roleId);
			if ((!getTsmpRoleFuncDao().existsById(tsmpRoleFuncId)) && (funcCode.length() == featureListMaxLength)) {
				tsmpRoleFunc.setFuncCode(funcCode);
				tsmpRoleFunc.setRoleId(roleId);
				tsmpRoleFunclist.add(tsmpRoleFunc);
			}
		}

		if (tsmpRoleFunclist.size() > 0) {
			getTsmpRoleFuncDao().saveAllAndFlush(tsmpRoleFunclist);
		}
		// 排程會重新新贈 tsmpFunc tsmpRoleFunc 故需清空
		tsmpRoleFunclist.clear();
		tsmpFuncList.clear();
	}

	private void insertTsmpFunc() {
		boolean isOsType = getLicenseUtil().getClass().equals(LicenseUtilBase.class);
		List<TsmpFuncVo> tsmpFuncVos = getTsmpFuncTableInitializr().insertTsmpFunc(licenseEditionTypeVo, isOsType);
		TsmpFuncId tsmpFuncId = new TsmpFuncId();
		String funcCode;
		String locale;

		for (TsmpFuncVo tsmpFuncVo : tsmpFuncVos) {
			TsmpFunc tsmpFunc = new TsmpFunc();
			BeanUtils.copyProperties(tsmpFuncVo, tsmpFunc);
			// 若DB有資料則list不新增
			funcCode = tsmpFunc.getFuncCode();
			locale = tsmpFunc.getLocale();
			tsmpFuncId.setFuncCode(funcCode);
			tsmpFuncId.setLocale(locale);

			if (!getTsmpFuncDao().existsById(tsmpFuncId)) {
				tsmpFuncList.add(tsmpFunc);
			}

		}

		if (tsmpFuncList.size() > 0) {
			getTsmpFuncDao().saveAllAndFlush(tsmpFuncList);
		}

	}

	private LicenseEditionTypeVo getLicenseEditionTypeVo(LicenseEditionType licenseEdition) {

		LicenseEditionTypeVo licenseEditionTypeVo = LicenseEditionTypeVo.resolve(licenseEdition.name());

		return licenseEditionTypeVo;
	}

	private void insertTsmpSetting() {
		List<TsmpSettingVo> tsmpSettingVos = getTsmpSettingTableInitializr().insertTsmpSetting();
		String id;
		for (TsmpSettingVo tsmpSettingVo : tsmpSettingVos) {
			TsmpSetting tsmpSetting = new TsmpSetting();
			BeanUtils.copyProperties(tsmpSettingVo, tsmpSetting);
			// 若DB有資料則list不新增
			id = tsmpSetting.getId();
			if (!getTsmpSettingDao().existsById(id)) {
				tsmpSettinglist.add(tsmpSetting);
			}

		}

		if (tsmpSettinglist.size() > 0) {
			getTsmpSettingDao().saveAllAndFlush(tsmpSettinglist);
		}

		LicenseEditionType licenseEdition = getLicenseEdition();
		// 把licenseEdition 轉成tpi.dgrv4.common.utils的licenseEditionVo
		// 給Tsmp_Fun使用
		licenseEditionTypeVo = getLicenseEditionTypeVo(licenseEdition);
		tsmpSettinglist.clear();

	}

	public void updateTsmpSetting() {
		LicenseEditionType licenseEdition = getLicenseEdition();
		// 把licenseEdition 轉成tpi.dgrv4.common.utils的licenseEditionVo
		// 給Tsmp_Fun使用
		licenseEditionTypeVo = getLicenseEditionTypeVo(licenseEdition);
		List<TsmpSettingVo> tsmpSettingVos = getTsmpSettingTableInitializr().setSettingsByLicense(licenseEditionTypeVo);
		for (TsmpSettingVo tsmpSettingVo : tsmpSettingVos) {
			TsmpSetting tsmpSetting = new TsmpSetting();
			BeanUtils.copyProperties(tsmpSettingVo, tsmpSetting);

			tsmpSettinglist.add(tsmpSetting);

		}

		if (tsmpSettingVos.size() > 0) {
			getTsmpSettingDao().saveAllAndFlush(tsmpSettinglist);
		}
	}

	private void insertTsmpDpItems() {
		List<TsmpDpItemsVo> tsmpDpItemsVos = getTsmpDpItemsTableInitializr().insertTsmpDpItems();
		TsmpDpItemsId tsmpDpItemsId = new TsmpDpItemsId();
		String itemNo;
		String locale;
		String subitemNo;

		for (TsmpDpItemsVo tsmpDpItemsVo : tsmpDpItemsVos) {
			TsmpDpItems tsmpDpItems = new TsmpDpItems();
			BeanUtils.copyProperties(tsmpDpItemsVo, tsmpDpItems);

			// 若DB有資料則list不新增
			itemNo = tsmpDpItems.getItemNo();
			locale = tsmpDpItems.getLocale();
			subitemNo = tsmpDpItems.getSubitemNo();
			tsmpDpItemsId.setItemNo(itemNo);
			tsmpDpItemsId.setLocale(locale);
			tsmpDpItemsId.setSubitemNo(subitemNo);
			if (!getTsmpDpItemsDao().existsById(tsmpDpItemsId)) {
				tsmpDpItemslist.add(tsmpDpItems);
			} 
		}

		if (tsmpDpItemslist.size() > 0) {
			getTsmpDpItemsDao().saveAllAndFlush(tsmpDpItemslist);
		}
	}

	private void deleteTheOldTsmpDpItem() {

		List<Long> targetList = Arrays.asList(300L, 301L, 302L, 303L, 304L, 305L, 306L, 307L, 308L);
		List<TsmpDpItems> deleteList = new ArrayList<>();
		targetList.forEach(itemId->{
			List<TsmpDpItems> list = getTsmpDpItemsDao().findByItemIdAndLocale(itemId, "zh-TW");
			deleteList.addAll(list);
		});
		getTsmpDpItemsDao().deleteAllInBatch(deleteList);

	}

	private void insertTsmpRtnCode() {
		List<TsmpRtnCodeVo> tsmpRtnCodeVos = getTsmpRtnCodeTableInitializr().insertTsmpRtnCode();
		TsmpRtnCodeId tsmpRtnCodeId = new TsmpRtnCodeId();
		String tsmpRtnCodeString;
		String locale;

		for (TsmpRtnCodeVo tsmpRtnCodeVo : tsmpRtnCodeVos) {
			TsmpRtnCode tsmpRtnCode = new TsmpRtnCode();
			BeanUtils.copyProperties(tsmpRtnCodeVo, tsmpRtnCode);

			// 若DB有資料則list不新增
			tsmpRtnCodeString = tsmpRtnCode.getTsmpRtnCode();
			locale = tsmpRtnCode.getLocale();
			tsmpRtnCodeId.setTsmpRtnCode(tsmpRtnCodeString);
			tsmpRtnCodeId.setLocale(locale);
			if (!getTsmpRtnCodeDao().existsById(tsmpRtnCodeId)) {
				tsmpRtnCodelist.add(tsmpRtnCode);
			}
		}

		if (tsmpRtnCodelist.size() > 0) {
			getTsmpRtnCodeDao().saveAllAndFlush(tsmpRtnCodelist);
		}

	}

	private void insertAuthorities() {
		List<AuthoritiesVo> authoritiesVos = getAuthoritiesTableInitializr().insertAuthorities();
		AuthoritiesId authoritiesId = new AuthoritiesId();
		String userName;
		String authority;

		for (AuthoritiesVo authoritiesVo : authoritiesVos) {
			Authorities authorities = new Authorities();
			BeanUtils.copyProperties(authoritiesVo, authorities);
			// 若DB有資料則list不新增
			userName = authorities.getUsername();
			authority = authorities.getAuthority();
			authoritiesId.setUsername(userName);
			authoritiesId.setAuthority(authority);
			if (!getAuthoritiesDao().existsById(authoritiesId)) {
				authoritieslist.add(authorities);
			}
		}

		if (authoritieslist.size() > 0) {
			getAuthoritiesDao().saveAllAndFlush(authoritieslist);
		}
	}

	private void insertTsmpRoleRoleMapping() {
		List<AutoInitSQLTsmpRoleRoleMappingVo> autoInitSQLTsmpRoleRoleMappingVos = getTsmpRoleRoleMappingTableInitializr()
				.insertTsmpRoleRoleMapping();
		Long roleRoleId;

		for (AutoInitSQLTsmpRoleRoleMappingVo autoInitSQLTsmpRoleRoleMappingVo : autoInitSQLTsmpRoleRoleMappingVos) {
			AutoInitSQLTsmpRoleRoleMapping autoInitSQLTsmpRoleRoleMapping = new AutoInitSQLTsmpRoleRoleMapping();
			BeanUtils.copyProperties(autoInitSQLTsmpRoleRoleMappingVo, autoInitSQLTsmpRoleRoleMapping);

			// 若DB有資料則list不新增
			roleRoleId = autoInitSQLTsmpRoleRoleMapping.getRoleRoleId();
			if (!getAutoInitSQLTsmpRoleRoleMappingDao().existsById(roleRoleId)) {
				autoInitSQLTsmpRoleRoleMappinglist.add(autoInitSQLTsmpRoleRoleMapping);
			}
		}

		if (autoInitSQLTsmpRoleRoleMappinglist.size() > 0) {
			getAutoInitSQLTsmpRoleRoleMappingDao().saveAllAndFlush(autoInitSQLTsmpRoleRoleMappinglist);
		}

	}

	private void insertTsmpRole() {
		List<TsmpRoleVo> tsmpRoleVos = getTsmpRoleTableInitializr().insertTsmpRole();
		String roleId;

		for (TsmpRoleVo tsmpRoleVo : tsmpRoleVos) {
			TsmpRole tsmpRole = new TsmpRole();
			BeanUtils.copyProperties(tsmpRoleVo, tsmpRole);
			// 若DB有資料則list不新增
			roleId = tsmpRole.getRoleId();
			if (!getTsmpRoleDao().existsById(roleId)) {
				tsmpRolelist.add(tsmpRole);
			}
		}

		if (tsmpRolelist.size() > 0) {
			getTsmpRoleDao().saveAllAndFlush(tsmpRolelist);
		}

	}

	private void insertUsers() {
		List<UsersVo> usersVos = getUserTableInitializr().insertUsers();
		String userName;

		for (UsersVo usersVo : usersVos) {
			Users users = new Users();
			BeanUtils.copyProperties(usersVo, users);
			userName = users.getUserName();
			// 若DB有資料則list不新增
			if (!getUsersDao().existsById(userName)) {
				userlist.add(users);
			}
		}

		if (userlist.size() > 0) {
			getUsersDao().saveAllAndFlush(userlist);
		}
	}

	private void insertTsmpOrganization() {

		List<TsmpOrganizationVo> tsmpOrganizationVos = getTsmpOrganizationTableInitializr().insertTsmpOrganization();
		String orgId;

		for (TsmpOrganizationVo tsmpOrganizationVo : tsmpOrganizationVos) {
			TsmpOrganization tsmpOrganization = new TsmpOrganization();
			BeanUtils.copyProperties(tsmpOrganizationVo, tsmpOrganization);
			orgId = tsmpOrganization.getOrgId();
			// 若DB有資料則list不新增
			if (!getTsmpOrganizationDao().existsById(orgId)) {
				tsmpOrganizationList.add(tsmpOrganization);
			}
		}

		if (tsmpOrganizationList.size() > 0) {
			getTsmpOrganizationDao().saveAllAndFlush(tsmpOrganizationList);
		}
	}

	private void insertTsmpUser() {

		List<TsmpUserVo> tsmpUserVoList = getTsmpUserTableInitializr().insertTsmpUser();
		String userName;
		for (TsmpUserVo tsmpUserVo : tsmpUserVoList) {
			TsmpUser tsmpUser = new TsmpUser();
			BeanUtils.copyProperties(tsmpUserVo, tsmpUser);
			userName = tsmpUser.getUserName();
			// 若DB有資料則list不新增
			if (getTsmpUserDao().findFirstByUserName(userName) == null) {
				tsmpUserlist.add(tsmpUser);
			}
		}

		if (tsmpUserlist.size() > 0) {
			getTsmpUserDao().saveAllAndFlush(tsmpUserlist);
		}
	}

	private void insertTsmpGroup() {
		List<TsmpGroupVo> tsmpGroupVos = getTsmpGroupTableInitializr().insertTsmpGroup();
		String groupId;

		for (TsmpGroupVo tsmpGroupVo : tsmpGroupVos) {
			TsmpGroup tsmpGroup = new TsmpGroup();
			BeanUtils.copyProperties(tsmpGroupVo, tsmpGroup);
			// 若DB有資料則list不新增
			groupId = tsmpGroup.getGroupId();
			if (!getTsmpGroupDao().existsById(groupId)) {
				tsmpGroupList.add(tsmpGroup);
			}
		}

		if (tsmpGroupList.size() > 0) {
			getTsmpGroupDao().saveAllAndFlush(tsmpGroupList);
		}
	}

	private void insertTsmpClientGroup() {
		List<TsmpClientGroupVo> tsmpClientGroupVos = getTsmpClientGroupTableInitializr().insertTsmpClientGroup();
		TsmpClientGroupId tsmpClientGroupId = new TsmpClientGroupId();
		String clientId;
		String groupId;

		for (TsmpClientGroupVo tsmpClientGroupVo : tsmpClientGroupVos) {
			TsmpClientGroup tsmpClientGroup = new TsmpClientGroup();
			BeanUtils.copyProperties(tsmpClientGroupVo, tsmpClientGroup);

			clientId = tsmpClientGroup.getClientId();
			groupId = tsmpClientGroup.getGroupId();
			tsmpClientGroupId.setClientId(clientId);
			tsmpClientGroupId.setGroupId(groupId);
			// 若DB有資料則list不新增
			if (!getTsmpClientGroupDao().existsById(tsmpClientGroupId)) {
				tsmpClientGroupList.add(tsmpClientGroup);
			}

		}

		if (tsmpClientGroupList.size() > 0) {
			getTsmpClientGroupDao().saveAllAndFlush(tsmpClientGroupList);
		}

	}

	private void insertTsmpReportUrl() {
		List<TsmpReportUrlVo> tsmpReportUrlVos = getTsmpReportUrlTableInitializr().insertTsmpReportUrl();
		TsmpReportUrlId tsmpReportUrlId = new TsmpReportUrlId();
		String reportId;
		String timeRange;

		for (TsmpReportUrlVo tsmpReportUrlVo : tsmpReportUrlVos) {
			TsmpReportUrl tsmpReportUrl = new TsmpReportUrl();
			BeanUtils.copyProperties(tsmpReportUrlVo, tsmpReportUrl);

			reportId = tsmpReportUrl.getReportId();
			timeRange = tsmpReportUrl.getTimeRange();
			tsmpReportUrlId.setReportId(reportId);
			tsmpReportUrlId.setTimeRange(timeRange);
			// 若DB有資料則list不新增
			if (!getTsmpReportUrlDao().existsById(tsmpReportUrlId)) {
				tsmpReportUrlList.add(tsmpReportUrl);
			}
		}

		if (tsmpReportUrlList.size() > 0) {
			getTsmpReportUrlDao().saveAllAndFlush(tsmpReportUrlList);
		}
	}

	private void insertTsmpAlert() {
		List<TsmpAlertVo> tsmpAlertVos = getTsmpAlertTableInitializer().insertTsmpAlert();
		Long alertId;
		for (TsmpAlertVo tsmpAlertVo : tsmpAlertVos) {
			TsmpAlert tsmpAlert = new TsmpAlert();
			BeanUtils.copyProperties(tsmpAlertVo, tsmpAlert);
			alertId = tsmpAlert.getAlertId();
			// 若DB有資料則list不新增
			if (!getTsmpAlertDao().existsById(alertId)) {
				tsmpAlertList.add(tsmpAlert);
			}
		}

		if (tsmpAlertList.size() > 0) {
			getTsmpAlertDao().saveAllAndFlush(tsmpAlertList);
		}

	}

	// 授權 'manager' 使用者簽核 Open API Key 類型的簽核單
	public void authorizeManagerToSignOpenApiKey() {
		String userName = "manager";
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		if (tsmpUser == null) {
			return;
		}
		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
		if (CollectionUtils.isEmpty(authoritiesList)) {
			return;
		}
		// 賦予 'manager' 所擁有的第一個角色簽核 Open API Key 的權限
		Authorities authorities = authoritiesList.get(0);
		String roleId = authorities.getAuthority();
		boolean isRoleIdExists = getTsmpRoleDao().existsById(roleId);
		if (!isRoleIdExists) {
			return;
		}
		String reviewType = TsmpDpReqReviewType.OPEN_API_KEY.value();
		// 刪除 tsmp_dp_chk_layer 資料表中的使用者(manager)的第一關與第二關資料。
		List<String> roleIdList = Arrays.asList(new String[] { roleId });
		List<TsmpDpChkLayer> tsmpDpChkLayerList = getTsmpDpChkLayerDao().queryByReviewTypeAndRoleIdList(reviewType,
				roleIdList);
		if (!CollectionUtils.isEmpty(tsmpDpChkLayerList)) {
			getTsmpDpChkLayerDao().deleteAll(tsmpDpChkLayerList);
		}
		// 第一關簽核角色資料
		TsmpDpChkLayer tsmpDpChkLayer1 = new TsmpDpChkLayer();
		tsmpDpChkLayer1.setChkLayerId(1L);
		tsmpDpChkLayer1.setReviewType(reviewType);
		tsmpDpChkLayer1.setLayer(1);
		tsmpDpChkLayer1.setRoleId(roleId);
		tsmpDpChkLayer1.setStatus(TsmpDpDataStatus.ON.value());
		getTsmpDpChkLayerDao().saveAndFlush(tsmpDpChkLayer1);

		// 第二關簽核角色資料
		TsmpDpChkLayer tsmpDpChkLayer2 = new TsmpDpChkLayer();
		tsmpDpChkLayer2.setChkLayerId(1L);
		tsmpDpChkLayer2.setReviewType(reviewType);
		tsmpDpChkLayer2.setLayer(2);
		tsmpDpChkLayer2.setRoleId(roleId);
		tsmpDpChkLayer2.setStatus(TsmpDpDataStatus.ON.value());
		getTsmpDpChkLayerDao().saveAndFlush(tsmpDpChkLayer2);

		TPILogger.tl.debug("Authorize 'manager' to sign Open API Key Finish ");
	}
	

	// 寫入系統預設的週期排程
	public void createDefaultRjobs() {
		SystemDefaultRjobInitializer initializer = new SystemDefaultRjobInitializer( //
				getDpb0101Service(), getTsmpDpApptRjobDao(), getBcryptParamHelper());
		List<DPB0101Req> rjobList = initializer.getDefaultRjobList();
		if (CollectionUtils.isEmpty(rjobList)) {
			return;
		}

		DPB0101Req req = null;
		for (int i = 0; i < rjobList.size(); i++) {
			req = rjobList.get(i);
			initializer.createRjob(req);
		}
	}

	// 重新授權 ADMIN 角色所擁有的功能
	public void reauthorizeAdminRoleFunc() {
		String roleId = "1000"; // 系統管理者角色 ID

		// 1.取得系統所有第二層的功能
		// 1-a. 查詢 TSMP_FUNC, 取得不重複的 TSMP_FUNC.func_code 所有資料
		List<String> allFuncCodeList = getTsmpFuncDao().queryAllFuncCode();
		if (CollectionUtils.isEmpty(allFuncCodeList)) {
			TPILogger.tl.debug("No any function can be authorized to ADMIN role.");
			return;
		}

		// 1-b. 判斷長度為6碼的, 即為第二層的功能 func_code, 例如: AC0001, NP0105
		List<String> subFuncCodeList = allFuncCodeList.stream().filter((fc) -> {
			return StringUtils.hasText(fc) && fc.length() == 6;
		}).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(subFuncCodeList)) {
			TPILogger.tl.debug("No any sub-function can be authorized to ADMIN role.");
			return;
		}

		// 2.取得系統管理者角色目前有授權的所有功能
		List<TsmpRoleFunc> roleFuncList = getTsmpRoleFuncDao().findByRoleId(roleId);

		// 3.刪除系統管理者角色多出來的功能: 做比對, 找出 TSMP_ROLE_FUNC 有的, 但 TSMP_FUNC 沒有的, 從
		// TSMP_ROLE_FUNC 刪除
		List<TsmpRoleFunc> delMountFuncCode = roleFuncList.stream().filter((rf) -> {
			return subFuncCodeList.stream().noneMatch((fc) -> fc.equals(rf.getFuncCode()));
		}).collect(Collectors.toList());
		if (delMountFuncCode.size() > 0) {
			getTsmpRoleFuncDao().deleteAll(delMountFuncCode);
		}

		// 4.增加系統管理者角色缺少的功能: 做比對, 找出 TSMP_ROLE_FUNC 沒有的, 但 TSMP_FUNC 有的, 寫入
		// TSMP_ROLE_FUNC
		List<TsmpRoleFunc> unMountRoleFuncList = subFuncCodeList.stream().filter((fc) -> {
			return roleFuncList.stream().noneMatch((rf) -> fc.equals(rf.getFuncCode()));
		}).map((fc) -> {
			TsmpRoleFunc tsmpRoleFunc = new TsmpRoleFunc();
			tsmpRoleFunc.setRoleId(roleId);
			tsmpRoleFunc.setFuncCode(fc);
			return tsmpRoleFunc;
		}).collect(Collectors.toList());
		if (unMountRoleFuncList.size() > 0) {
			getTsmpRoleFuncDao().saveAll(unMountRoleFuncList);
		}
	}

	// 同步 tsmp_dp_clientext 和 tsmp_client
	public void syncTsmpClientAndTsmpDpClientext() {
		InnerInvokeParam iip = null;
		try {
			iip = InnerInvokeParam.getInstance(new HttpHeaders(), null, new TsmpAuthorization());
		} catch (Exception e) {
			TPILogger.tl.debug("Failed to create DGR audit log for creating tsmp_dp_clientext.");
		}
		syncTsmpClientAndTsmpDpClientext(iip);
	}

	// 同步 tsmp_dp_clientext 和 tsmp_client
	public void syncTsmpClientAndTsmpDpClientext(InnerInvokeParam iip) {
		// 1. client 與 clientExt 找出不同步的資料
		List<TsmpClient> list = getTsmpClientDao().queryByClientIdNotExists();
		if (!CollectionUtils.isEmpty(list)) {
			// 寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_CLIENT.value());

			for (int i = 0; i < list.size(); i++) {
				TsmpClient clientVo = list.get(i);

				final Long clientSeqId = getSeqStoreService().nextSequence(//
						TsmpDpSeqStoreKey.TSMP_DP_CLIENTEXT, 2000000000L, 1L);

				TsmpDpClientext clientextVo = new TsmpDpClientext();
				clientextVo.setClientId(clientVo.getClientId());
				clientextVo.setClientSeqId(clientSeqId);
				clientextVo.setContentTxt("Repaired by " + getClass().getSimpleName());
				clientextVo.setContentTxt("Repaired by system");
				clientextVo.setPublicFlag("2");
				clientextVo.setPwdStatus("1");
				clientextVo.setRegStatus("2");
				clientextVo = getTsmpDpClientextDao().save(clientextVo);

				// 寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpDpClientext.class.getSimpleName(),
						TableAct.C.value(), null, clientextVo);
			}
		}
	}

	// 重設 adminConsole 用戶端的帳密
	public void resetAdminConsoleData() {
		try {
			ITsmpFirstInstallHelper it = getTsmpFirstInstallHelper();
			if (it != null) {
				it.init();
			}
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}
	}

	public static void setAuthoritieslist(List<Authorities> authoritieslist) {
		AutoInitSQL.authoritieslist = authoritieslist;
	}
}
