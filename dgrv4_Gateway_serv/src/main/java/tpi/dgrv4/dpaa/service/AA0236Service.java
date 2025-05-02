package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0236API;
import tpi.dgrv4.dpaa.vo.AA0236Req;
import tpi.dgrv4.dpaa.vo.AA0236Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0236Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpApiDao tsmpApiDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0236Service(TsmpGroupApiDao tsmpGroupApiDao, TsmpApiDao tsmpApiDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpApiDao = tsmpApiDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0236Resp queryAPIByTsmpGroupApiIdAndKeyword(TsmpAuthorization authorization, AA0236Req req) {

		AA0236Resp resp = new AA0236Resp();
		try {

			String groupId = req.getGroupId();
			String moduleName = req.getModuleName();
			String apiKey = req.getApiKey();
			String[] keyword = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			TsmpGroupApi lastTsmpGroupApi = null;
			
			//若有傳最後一筆的apiKey資料，則查詢TsmpGroupApi資料。
			if (StringUtils.isEmpty(groupId)==false && StringUtils.isEmpty(moduleName)==false && StringUtils.isEmpty(apiKey)==false) {
				TsmpGroupApiId id = new TsmpGroupApiId(); 
				id.setGroupId(groupId);
				id.setModuleName(moduleName);
				id.setApiKey(apiKey);
				Optional<TsmpGroupApi> opt_tsmpGroupApiId =  getTsmpGroupApiDao().findById(id);
				if (opt_tsmpGroupApiId.isPresent()) {
					lastTsmpGroupApi = opt_tsmpGroupApiId.get();
				}
			}
			
			// 條件 GROUP_ID = AA0236Req.groupId AND MODULE_NAME = AA0236Req.moduleName +分頁 + 關鍵字查詢
			List<TsmpGroupApi> tsmpGroupApiList = getTsmpGroupApiDao().findByTsmpGroupApiIdAndKeyword(lastTsmpGroupApi, groupId, moduleName, keyword,  getPageSize());
			
			//parsing api name
			List<AA0236API> apiList = converAA0236APIList(tsmpGroupApiList);
			resp.setApiList(apiList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<AA0236API> converAA0236APIList(List<TsmpGroupApi> tsmpGroupApiList){
		List<AA0236API> aa0236APIList = new ArrayList<AA0236API>();
		
		for (TsmpGroupApi tsmpGroupApi : tsmpGroupApiList) {
			AA0236API aa0236API = new AA0236API();
			aa0236API.setGroupId(tsmpGroupApi.getGroupId());
			aa0236API.setModuleName(tsmpGroupApi.getModuleName());
			aa0236API.setApiKey(tsmpGroupApi.getApiKey());
			
			TsmpApiId id = new TsmpApiId();
			id.setModuleName(tsmpGroupApi.getModuleName());
			id.setApiKey(tsmpGroupApi.getApiKey());
			Optional<TsmpApi> opt_tsmpApi = getTsmpApiDao().findById(id);	
			if (opt_tsmpApi.isPresent()) {
				aa0236API.setApiName(opt_tsmpApi.get().getApiName());
			}
			
			aa0236APIList.add(aa0236API);
		}
		
		return aa0236APIList;
	}
	

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0232");
		return this.pageSize;
	}
	
}
