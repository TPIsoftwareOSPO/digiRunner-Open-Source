package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0321Req;
import tpi.dgrv4.dpaa.vo.AA0321Resp;
import tpi.dgrv4.dpaa.vo.AA0321RespItem;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
	
@Service
public class AA0321Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ApiItemService apiItemService;
	private ServiceConfig serviceConfig;

	private Integer pageSize;
	
	@Autowired
	public AA0321Service(TsmpApiDao tsmpApiDao, TsmpOrganizationDao tsmpOrganizationDao, ApiItemService apiItemService,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.apiItemService = apiItemService;
		this.serviceConfig = serviceConfig;
	}

	public AA0321Resp queryAPIListByOrg(TsmpAuthorization authorization, AA0321Req req, ReqHeader reqHeader) {
		AA0321Resp resp = new AA0321Resp();
		try {
 			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			TsmpApiId lastId = getLastIdFromPrevPage(req);
			String orgId = authorization.getOrgId();
			
			//check param
			if(StringUtils.isEmpty(orgId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			List<TsmpApi> apiList = getTsmpApiDao().query_aa0321Service(lastId, orgDescList, words, req.getApiUidList(), getPageSize());
			if(apiList == null || apiList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
				
			List<TsmpApiItem> apiItemList = getApiItemService().getApiItemList(apiList, reqHeader.getLocale());
			List<AA0321RespItem> dataList = getRespItemList(apiItemList, resp);
			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}	
	
	private List<AA0321RespItem> getRespItemList(List<TsmpApiItem> apiItemList, AA0321Resp resp) {
		List<AA0321RespItem> dataList = resp.getDataList();
		if(dataList == null) {
			dataList = new ArrayList<AA0321RespItem>();
		}
		for (TsmpApiItem apiItem : apiItemList) {
			AA0321RespItem aa0321_item = new AA0321RespItem();
			aa0321_item.setApiKey(apiItem.getApiKey());
			aa0321_item.setModuleName(apiItem.getModuleName());
			aa0321_item.setApiName(apiItem.getApiName());
			aa0321_item.setThemeDatas(apiItem.getThemeDatas());
			aa0321_item.setOrgId(apiItem.getOrgId());
			aa0321_item.setOrgName(apiItem.getOrgName());
			aa0321_item.setApiDesc(apiItem.getApiDesc());
			aa0321_item.setDpStatus(apiItem.getDpStatus());
			aa0321_item.setApiExtId(apiItem.getApiExtId());
			aa0321_item.setApiUid(apiItem.getApiUid());
			aa0321_item.setFileName(apiItem.getFileName());
			aa0321_item.setFilePath(apiItem.getFilePath());
			aa0321_item.setPublicFlag(nvl(apiItem.getPublicFlag()));
			aa0321_item.setPublicFlagName(apiItem.getPublicFlagName());
			
			dataList.add(aa0321_item);
		} 
		return dataList;
	}
	
	private TsmpApiId getLastIdFromPrevPage(AA0321Req req) {
		String aa0321_apiKey = req.getApiKey();
		String aa0321_moduleName = req.getModuleName();
		if (aa0321_apiKey != null && !aa0321_apiKey.isEmpty() &&
				aa0321_moduleName != null && !aa0321_moduleName.isEmpty()) {
			return new TsmpApiId(aa0321_apiKey, aa0321_moduleName);
		}
		return null;
	}
 
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected ApiItemService getApiItemService() {
		return this.apiItemService;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0321");
		return this.pageSize;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
}
