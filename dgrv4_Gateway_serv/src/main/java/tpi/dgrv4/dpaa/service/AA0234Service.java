package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0234API;
import tpi.dgrv4.dpaa.vo.AA0234Req;
import tpi.dgrv4.dpaa.vo.AA0234Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0234Service {

	private TPILogger logger = TPILogger.tl;

	private Integer pageSize;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private ServiceConfig serviceConfig;

	@Autowired
	public AA0234Service(TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.serviceConfig = serviceConfig;
	}


	public AA0234Resp queryAPIByOrgIdAndModuleName(TsmpAuthorization authorization, AA0234Req req) {
		AA0234Resp resp = new AA0234Resp();

		try {
			String apiKey = req.getApiKey();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String moduleName = req.getModuleName();
			List<String> selectedApiKeyList  = req.getSelectedApiKeyList();
			
			//1.取得User的組織與子組織的orgId，可以使用TsmpOrganizationDao.queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(), Integer.MAX_VALUE)。
			List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(), Integer.MAX_VALUE);
			
			//2.TSMP_API為主要資料表，找出組織特定Module可以選擇的API。
			TsmpApi lastTsmpApi = null;
			if(!StringUtils.isEmpty(apiKey)) {
				TsmpApiId apiId = new TsmpApiId();
				apiId.setApiKey(apiKey);
				apiId.setModuleName(moduleName);
				lastTsmpApi = getTsmpApiDao().findById(apiId).orElse(null);
			}
			
			List<TsmpApi> tsmpApiList = getTsmpApiDao().queryByAA0234Service(lastTsmpApi, words, moduleName, orgList, selectedApiKeyList, getPageSize());
			if(tsmpApiList.size() == 0) {
				//1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//3.將查詢到TSMP_API資料放進AA0234Resp.apiList。
			List<AA0234API> apiList = new ArrayList<>();
			tsmpApiList.forEach(vo ->{
				AA0234API apiVo = new AA0234API();
				apiVo.setApiKey(vo.getApiKey());
				apiVo.setApiName(vo.getApiName());
				apiList.add(apiVo);
			});
			resp.setApiList(apiList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}


	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return tsmpOrganizationDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0234");
		return pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
	
	
}
