package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.vo.DPB0073ApiList;
import tpi.dgrv4.dpaa.vo.DPB0073Req;
import tpi.dgrv4.dpaa.vo.DPB0073Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0073Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private BcryptParamHelper helper;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public DPB0073Service(TsmpApiDao tsmpApiDao, TsmpOrganizationDao tsmpOrganizationDao, BcryptParamHelper helper,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.helper = helper;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public DPB0073Resp setApiPublicFlag(TsmpAuthorization authorization, DPB0073Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		DPB0073Resp resp = new DPB0073Resp();
		
		try {
			String encodePublicFlag = req.getEncodePublicFlag();//參考 TSMP_DP_ITEMS.ITEM_NO = 'API_AUTHORITY' (API露出權限), 需使用 BcryptParam 設計, {0,1,2}
 			List<DPB0073ApiList> apiList = req.getApiPKs();
			String orgId = authorization.getOrgId();
 
			//chk param
			if (StringUtils.isEmpty(encodePublicFlag)){
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if(apiList == null || apiList.isEmpty()) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if(StringUtils.isEmpty(orgId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
 
			String publicFlag = getDecodePublicFlag(encodePublicFlag, reqHeader.getLocale());//解碼
			
			//找出包含此 orgId 及其向下的所有組織
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			
			//檢查所選API組織是否都在本組織以下,否則不能更新
			checkApiOrg(apiList, orgDescList);
 
			//更新API開放權限
			int uuidIndex = 1;
			String uuid = iip != null ? iip.getTxnUid() : null;
			for (DPB0073ApiList dpb0073ApiList : apiList) {
				if(iip != null) {
					iip.setTxnUid(uuid + "_" + uuidIndex);
					uuidIndex++;
				}
				resp = setApiPublicFlag(publicFlag, dpb0073ApiList.getApiKey(), dpb0073ApiList.getModuleName(), resp, iip);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	/**
	 * 檢查若所選的API有部份不屬於組織,則丟出更新失敗
	 * 
	 * @param apiList
	 * @param orgDescList
	 */
	public void checkApiOrg(List<DPB0073ApiList> apiList, List<String> orgDescList) {
		boolean isApiOrgErr = false;
		for (DPB0073ApiList dpb0073ApiList : apiList) {
			TsmpApiId apiId = new TsmpApiId(dpb0073ApiList.getApiKey(), dpb0073ApiList.getModuleName());
			Optional<TsmpApi> opt_api = getTsmpApiDao().findById(apiId);
			TsmpApi api = null;
			if(opt_api.isPresent()) {
				api = opt_api.get();
				String orgId = api.getOrgId();
				if (!StringUtils.isEmpty(orgId)) {
					if ( CollectionUtils.isEmpty(orgDescList) || !orgDescList.contains(orgId)) {
						isApiOrgErr = true;
						break;
					}
				}
			}
		}
		if(isApiOrgErr) {
			throw TsmpDpAaRtnCode._1221.throwing();
		}
	}
	
	public DPB0073Resp setApiPublicFlag(String publicFlag, String apiKey, String moduleName, DPB0073Resp resp, InnerInvokeParam iip) {
		TsmpApiId apiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> opt_api = getTsmpApiDao().findById(apiId);
		TsmpApi api = null;
		if(opt_api.isPresent()) {
			api = opt_api.get();
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			if (TsmpApiSrc.REGISTERED.value().equals(api.getApiSrc())){
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_REGISTER_API.value());
			}else if (TsmpApiSrc.COMPOSED.value().equals(api.getApiSrc())){
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
			}	
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, api); //舊資料統一轉成 String
			
			api.setPublicFlag(publicFlag);
			api.setUpdateUser(getClass().getSimpleName());
			api.setUpdateTime(DateTimeUtil.now());
			api = getTsmpApiDao().saveAndFlush(api);
			
			if (TsmpApiSrc.REGISTERED.value().equals(api.getApiSrc()) || TsmpApiSrc.COMPOSED.value().equals(api.getApiSrc())) {
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();	
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						TsmpApi.class.getSimpleName(), TableAct.U.value(), oldRowStr, api);
			}
		}
		return resp;
	}
	
	public String getDecodePublicFlag(String encodeFlag, String locale) {
		String flag = null;
		try {
			flag = getBcryptParamHelper().decode(encodeFlag, "API_AUTHORITY", locale);//BcryptParam解碼,API露出權限
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return flag;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
