package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.LicenseType;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0510Req;
import tpi.dgrv4.dpaa.vo.AA0510Resp;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@Scope("prototype")
public class AA0510Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingService tsmpSettingService;
	private TsmpSettingDao tsmpSettingDao;
	private LicenseUtilBase licenseUtil;

	@Autowired
	public AA0510Service(TsmpSettingService tsmpSettingService, TsmpSettingDao tsmpSettingDao,
			LicenseUtilBase licenseUtil) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpSettingDao = tsmpSettingDao;
		this.licenseUtil = licenseUtil;
	}

	public AA0510Resp getAcConf(TsmpAuthorization authorization, AA0510Req req) {
		AA0510Resp resp = new AA0510Resp();
		try {

			// <=== 考慮到在TSMP_SETTING資料表沒有TSMP_AC_CONF，
			// TsmpSettingService會拋出Exception，預設給false
			try {
				Map<String, Object> setting = getTsmpSettingService().getVal_TSMP_AC_CONF();
				resp.setDp(setting.get("dp") == null ? 0 : Integer.parseInt(setting.get("dp").toString()));
				resp.setNet(setting.get("net") == null ? "false" : setting.get("net").toString());
			} catch (TsmpDpAaException e) {
				if (TsmpDpAaRtnCode.NO_ITEMS_DATA.getCode().equals(e.getError().getCode())) {
					resp.setDp(0);
					resp.setNet("false");
				}
			}
			// ===> 考慮到在TSMP_SETTING資料表沒有TSMP_AC_CONF，
			// TsmpSettingService會拋出Exception，預設給false

			
			// <=== 考慮到在TSMP_SETTING資料表沒有TSMP_APILOG_FORCE_WRITE_RDB，
			// TsmpSettingService會拋出Exception，預設給false
			try {
				resp.setApiLogWriteRDB(getTsmpSettingService().getVal_TSMP_APILOG_FORCE_WRITE_RDB());
			} catch (TsmpDpAaException e) {
				if (TsmpDpAaRtnCode.NO_ITEMS_DATA.getCode().equals(e.getError().getCode())) {
					resp.setApiLogWriteRDB("false");
				}
			}
			// ===> 考慮到在TSMP_SETTING資料表沒有TSMP_APILOG_FORCE_WRITE_RDB，
			// TsmpSettingService會拋出Exception，預設給false

			resp.setCoreVer("");
			//resp.setDcPrefix(getTsmpSettingService().getVal_DC_MAPPING_PREFIX());
			//v4 has no this value
			resp.setDcPrefix("N/A");
			
			String key = getTsmpSettingService().getVal_TSMP_LICENSE_KEY();
			getLicenseUtil().initLicenseUtil(key, null);
			resp.setEdition(getLicenseUtil().getEdition(key));
			
			String logoutUrl="";
			try {
				logoutUrl = getTsmpSettingService().getVal_DGR_LOGOUT_URL();
			}catch (Exception e){
				this.logger.error(StackTraceUtil.logStackTrace(e));
				logoutUrl ="";
			}
			if (!StringUtils.hasText(logoutUrl)){
				logoutUrl ="";
			}
			resp.setLogoutUrl(logoutUrl);
			
			LocalDate expiryDateLd = getLicenseUtil().getExpiryDate(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());
			Optional<String> opt_dateStr = DateTimeUtil.dateTimeToString(expiryDateLd, DateTimeFormatEnum.西元年月日_2);
			if (!opt_dateStr.isPresent()) {
				throw TsmpDpAaRtnCode._1295.throwing();
			}
			resp.setExpiryDate(opt_dateStr.get());
			
			String account = getLicenseUtil().getAccount(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());
			resp.setAccount(account);
			
			String env =  getLicenseUtil().getValue(getTsmpSettingService().getVal_TSMP_LICENSE_KEY(),LicenseType.env);
			resp.setEnv(env);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected LicenseUtilBase getLicenseUtil(){
		return this.licenseUtil;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
