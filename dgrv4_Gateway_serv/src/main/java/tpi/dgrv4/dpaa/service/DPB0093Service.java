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
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0093ApiItem;
import tpi.dgrv4.dpaa.vo.DPB0093Req;
import tpi.dgrv4.dpaa.vo.DPB0093Resp;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0093Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private ApiItemService apiItemService;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;

	@Autowired
	public DPB0093Service(TsmpApiDao tsmpApiDao, ApiItemService apiItemService, ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.apiItemService = apiItemService;
		this.serviceConfig = serviceConfig;
	}

	public DPB0093Resp queryApiLikeList(TsmpAuthorization tsmpAuthorization, DPB0093Req req, ReqHeader reqHeader) {
		DPB0093Resp resp = new DPB0093Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");

			TsmpApiId lastId = getLastIdFromPrevPage(req);
			List<TsmpApi> apiList = getTsmpApiDao().queryApiLikeList(lastId, words, getPageSize());
			if(apiList == null || apiList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<TsmpApiItem> apiItemList = getApiItemService().getApiItemList(apiList, reqHeader.getLocale());
			List<DPB0093ApiItem> dataList = getApiItemList(apiItemList, resp);
			resp.setDataList(dataList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<DPB0093ApiItem> getApiItemList(List<TsmpApiItem> apiItemList, DPB0093Resp resp) {
		List<DPB0093ApiItem> dataList = resp.getDataList();
		if(dataList == null) {
			dataList = new ArrayList<DPB0093ApiItem>();
		}
		for (TsmpApiItem apiItem : apiItemList) {			
			DPB0093ApiItem item = new DPB0093ApiItem();
			item.setApiKey(apiItem.getApiKey());
			item.setModuleName(apiItem.getModuleName());
			item.setApiName(apiItem.getApiName());
			item.setThemeDatas(apiItem.getThemeDatas());
			item.setOrgId(apiItem.getOrgId());
			item.setOrgName(apiItem.getOrgName());
			item.setApiDesc(apiItem.getApiDesc());
			item.setApiExtId(apiItem.getApiExtId());
            item.setApiUid(apiItem.getApiUid());
            item.setFileName(apiItem.getFileName());
            item.setFilePath(apiItem.getFilePath());
			
			dataList.add(item);
		}
		return dataList;
	}
	
	private TsmpApiId getLastIdFromPrevPage(DPB0093Req req) {
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		if (!StringUtils.isEmpty(apiKey) && !StringUtils.isEmpty(moduleName)) {
			return new TsmpApiId(apiKey, moduleName);
		}
		return null;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected ApiItemService getApiItemService() {
		return this.apiItemService;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0093");
		return this.pageSize;
	}
}
