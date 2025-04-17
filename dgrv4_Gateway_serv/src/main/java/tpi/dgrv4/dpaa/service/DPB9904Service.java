package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9904Req;
import tpi.dgrv4.dpaa.vo.DPB9904Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9904Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingDao tsmpSettingDao;
	private DgrAuditLogService dgrAuditLogService;
	private DaoGenericCacheService daoGenericCacheService;

	@Autowired
	public DPB9904Service(TsmpSettingDao tsmpSettingDao, DgrAuditLogService dgrAuditLogService,
			DaoGenericCacheService daoGenericCacheService) {
		super();
		this.tsmpSettingDao = tsmpSettingDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.daoGenericCacheService = daoGenericCacheService;
	}

	public DPB9904Resp deleteTsmpSetting(TsmpAuthorization auth, DPB9904Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_TSMP_SETTING.value());
		
		String id = checkParams(req);
		
		try {
			TsmpSetting oldSetting = getTsmpSettingDao().findById(id) //
				.orElseThrow(TsmpDpAaRtnCode._1298::throwing);
			
			getTsmpSettingDao().delete(oldSetting);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oldSetting); //舊資料統一轉成 String
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpSetting.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			
			getDaoGenericCacheService().clearAndNotify();

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.SETTING.value());
			
			return new DPB9904Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
	}

	protected String checkParams(DPB9904Req req) {
		String id = req.getId();
		if (!StringUtils.hasLength(id)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		return id;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

}
