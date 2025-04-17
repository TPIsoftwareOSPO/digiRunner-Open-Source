package tpi.dgrv4.dpaa.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0118Req;
import tpi.dgrv4.dpaa.vo.DPB0118Resp;
import tpi.dgrv4.entity.vo.VersionInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.VersionService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
@Scope("prototype")
public class DPB0118Service {

	private TsmpSettingService tsmpSettingService;
	private VersionService versionService;
	private LicenseUtilBase util;
	
	@Autowired
	public DPB0118Service(TsmpSettingService tsmpSettingService, VersionService versionService, LicenseUtilBase util) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.versionService = versionService;
		this.util = util;
	}

	public DPB0118Resp queryModuleVersion(TsmpAuthorization auth, DPB0118Req req) {
		return queryModuleVersion(null, null, false);
	}
	
	public DPB0118Resp queryModuleVersion() {
		return queryModuleVersion(null, null, false);
	}
	
	public DPB0118Resp queryModuleVersion(String ipAddress, String url, boolean hasForward) {
		DPB0118Resp resp = new DPB0118Resp();
		try {
			// 環境
			// Cn88-nNO8-xx8u-un88-nVoF-Fr48-80rc-L5rF-xN#8-e1=x-6#xo-=d4#-2!=n-!#2!-=!!!-!!!
			String key = getTsmpSettingService().getVal_TSMP_LICENSE_KEY();
			util.initLicenseUtil(key, null);
			String edition = util.getEdition(key);
			resp.setEdition(edition);
			
			//有效期限
			LocalDate expiryDateLd = util.getExpiryDate(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());	
			String expiryDate = DateTimeUtil.dateTimeToString(expiryDateLd, DateTimeFormatEnum.西元年月日_2).orElse(null);
			resp.setExpiryDate(expiryDate);
			
			//到期資訊
			Long nearWarnDays = util.getNearWarnDays(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());
			resp.setNearWarnDays(nearWarnDays);
			
			Long overBufferDays = util.getOverBufferDays(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());
			resp.setOverBufferDays(overBufferDays);
			
			String account = util.getAccount(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());
			resp.setAccount(account);
			
			String env = util.getEnv(getTsmpSettingService().getVal_TSMP_LICENSE_KEY());
			resp.setEnv(env);
			
			VersionInfo v = versionService.getVersion();
	
			resp.setMajorVersionNo(v.majorVersionNo);
			resp.setVersion(v.strVersion);
			if (StringUtils.hasLength(ipAddress)) {
				resp.setRemoteAddr(ipAddress);
			}
			
			try {
				if (hasForward) {
					HttpRespData respData = HttpUtil.httpReqByGet(url, null, false);
					String respDataStr = respData.getLogStr();
					resp.setForwardResp(respDataStr);
				}
			} catch (Exception e) {
				TPILogger.tl.error("Forward url fail :" + url);
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
		
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}
	
//	private void myWait(Object waitkey) {
//		synchronized (waitkey) {
//			try {
//				waitkey.wait();
//			} catch (InterruptedException e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			    Thread.currentThread().interrupt();
//			}
//		}
//	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	

}
