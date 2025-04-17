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
import tpi.dgrv4.dpaa.vo.DPB0075Req;
import tpi.dgrv4.dpaa.vo.DPB0075Resp;
import tpi.dgrv4.dpaa.vo.DPB0075RespItem;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0075Service {

	private TPILogger logger = TPILogger.tl;
	
	private ApiItemService apiItemService;
	private TsmpApiDao tsmpApiDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;
	
	@Autowired
	public DPB0075Service(ApiItemService apiItemService, TsmpApiDao tsmpApiDao, TsmpOrganizationDao tsmpOrganizationDao,
			ServiceConfig serviceConfig) {
		super();
		this.apiItemService = apiItemService;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0075Resp queryApiLov(TsmpAuthorization authorization, DPB0075Req req, ReqHeader header) {
		DPB0075Resp resp = new DPB0075Resp();

		try {
 			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String dpStatus = req.getDpStatus();
			TsmpApiId lastId = getLastIdFromPrevPage(req);
			String orgId = authorization.getOrgId();
			
			//check param
			if(StringUtils.isEmpty(dpStatus)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if(StringUtils.isEmpty(orgId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			List<TsmpApi> apiList = getTsmpApiDao().query_dpb0075Service(lastId, orgDescList, dpStatus, words, getPageSize());
			if(apiList == null || apiList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<TsmpApiItem> apiItemList = getApiItemService().getApiItemList(apiList, header.getLocale());
			List<DPB0075RespItem> dataList = getRespItemList(apiItemList, resp);
			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private List<DPB0075RespItem> getRespItemList(List<TsmpApiItem> apiItemList, DPB0075Resp resp) {
		List<DPB0075RespItem> dataList = resp.getDataList();
		if(dataList == null) {
			dataList = new ArrayList<DPB0075RespItem>();
		}
		for (TsmpApiItem apiItem : apiItemList) {
			DPB0075RespItem item = new DPB0075RespItem();
			item.setApiKey(apiItem.getApiKey());
			item.setModuleName(apiItem.getModuleName());
			item.setApiName(apiItem.getApiName());
			item.setThemeDatas(apiItem.getThemeDatas());
			item.setOrgId(apiItem.getOrgId());
			item.setOrgName(apiItem.getOrgName());
			item.setApiDesc(apiItem.getApiDesc());
			item.setDpStatus(apiItem.getDpStatus());
			item.setApiExtId(apiItem.getApiExtId());
			item.setApiUid(apiItem.getApiUid());
			item.setFileName(apiItem.getFileName());
			item.setFilePath(apiItem.getFilePath());
			item.setPublicFlag(nvl(apiItem.getPublicFlag()));
			item.setPublicFlagName(apiItem.getPublicFlagName());
			
			dataList.add(item);
		} 
		return dataList;
	}
	
	private TsmpApiId getLastIdFromPrevPage(DPB0075Req req) {
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		if (apiKey != null && !apiKey.isEmpty() &&
				moduleName != null && !moduleName.isEmpty()) {
			return new TsmpApiId(apiKey, moduleName);
		}
		return null;
	}
 
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected ApiItemService getApiItemService() {
		return this.apiItemService;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0075");
		return this.pageSize;
	}
}
