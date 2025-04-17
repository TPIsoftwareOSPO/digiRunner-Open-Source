package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.vo.AA0314Req;
import tpi.dgrv4.dpaa.vo.AA0314Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;

@Service
public class AA0314Service {
	
	private TPILogger logger = TPILogger.tl; 
	
	private JobHelper jobHelper;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpApiDao tsmpApiDao;
	private ApplicationContext ctx;
	private DgrAuditLogService dgrAuditLogService;
	private CApiKeyService capiKeyService;

	@Autowired
	public AA0314Service(JobHelper jobHelper, TsmpApiRegDao tsmpApiRegDao, TsmpApiDao tsmpApiDao,
			ApplicationContext ctx, DgrAuditLogService dgrAuditLogService, CApiKeyService capiKeyService) {
		super();
		this.jobHelper = jobHelper;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpApiDao = tsmpApiDao;
		this.ctx = ctx;
		this.dgrAuditLogService = dgrAuditLogService;
		this.capiKeyService = capiKeyService;
	}

	public AA0314Resp confirmAPI(AA0314Req req, ReqHeader reqHeader, InnerInvokeParam iip, HttpHeaders headers) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
		
		// 驗證CApiKey
		getCapiKeyService().verifyCApiKey(headers, true, true);
		
		TsmpApiReg tsmpApiReg = checkParams(req);
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApiReg); //舊資料統一轉成 String
		try {
			String userName = "SYSTEM";
			tsmpApiReg.setRegStatus("1");	// 註冊狀態: 確認
			tsmpApiReg.setUpdateTime(DateTimeUtil.now());
			tsmpApiReg.setUpdateUser(userName);
			tsmpApiReg = getTsmpApiRegDao().save(tsmpApiReg);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();	
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpApiReg.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpApiReg);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return new AA0314Resp();
	}

	private void checkParams(String uuid, String cApikey) {
		if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(cApikey)) {
			this.logger.error("uuid :"+uuid);
			this.logger.error("cApikey :"+cApikey);
			throw TsmpDpAaRtnCode._1522.throwing();
		}
		
	}

	public TsmpApiReg checkParams(AA0314Req req) {	

		
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		String apiUUID = req.getApiUUID();
		if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(moduleName) || StringUtils.isEmpty(apiUUID)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String apiSrc = TsmpApiSrc.COMPOSED.value();
		List<TsmpApi> tsmpApiList = getTsmpApiDao().findByApiKeyAndModuleNameAndApiSrc(apiKey, moduleName, apiSrc);
		if (CollectionUtils.isEmpty(tsmpApiList)) {
			throw TsmpDpAaRtnCode._1456.throwing();
		}
		
		List<TsmpApiReg> tsmpApiRegList = getTsmpApiRegDao().findByApiKeyAndModuleNameAndApiUuid(apiKey, moduleName, apiUUID);
		if (CollectionUtils.isEmpty(tsmpApiRegList)) {
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		
		return tsmpApiRegList.get(0);
	}


	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected NoticeClearCacheEventsJob getNoticeClearCacheEventsJob(Integer action, String cacheName, //
			List<String> tableNameList) {
		return (NoticeClearCacheEventsJob) getCtx().getBean("noticeClearCacheEventsJob", action, cacheName, tableNameList);
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}
	
	

}