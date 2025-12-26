package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0237Req;
import tpi.dgrv4.dpaa.vo.AA0237Resp;
import tpi.dgrv4.dpaa.vo.AA0237RespA;
import tpi.dgrv4.dpaa.vo.AA0237RespB1;
import tpi.dgrv4.dpaa.vo.AA0237RespB1D;
import tpi.dgrv4.dpaa.vo.AA0237RespB2;
import tpi.dgrv4.dpaa.vo.AA0237RespB2D1;
import tpi.dgrv4.dpaa.vo.AA0237RespB2D2;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.vo.AA0237ReqB;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0237Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpApiDao tsmpApiDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0237Service(TsmpGroupApiDao tsmpGroupApiDao, TsmpApiDao tsmpApiDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpApiDao = tsmpApiDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0237Resp queryVgroupApiList(AA0237Req req) {
		AA0237Resp resp = new AA0237Resp();
		try {
			
			AA0237ReqB reqB = req.getReqB();
			String  vgroupId = req.getVgroupId();
			if(StringUtils.isEmpty(vgroupId)) {
				throw TsmpDpAaRtnCode._1296.throwing();		// 1296:缺少必填參數
			}
			
			if(reqB == null) {
				// 執行功能A
				List<TsmpGroupApi> groupApiList = getTsmpGroupApiDao().query_aa0237Service(null, vgroupId, reqB, null, Integer.MAX_VALUE);
				
				//parser
				resp.setRespA(getAA0237RespAData(groupApiList));
				resp.setRespB1(null);
				resp.setRespB2(null);
			}else {
				
				if(reqB.getP()) {	
					// 執行功能B-1
					excuteFuncB1(reqB, resp, vgroupId);
					
				}else {								
					// 執行功能B-2查出虛擬模組項下所有API清單並依模組名稱分類 或 
					// 功能B-1 依照模組名稱查出虛擬模組項下的API Key清單, 並提供關鍵字搜尋
					List<TsmpGroupApi> groupApiList = new ArrayList<>();
					String moduleName = reqB.getModuleName();
					if(!StringUtils.isEmpty(moduleName)) {
						groupApiList = getTsmpGroupApiDao().query_aa0237Service(null, vgroupId, reqB, null, Integer.MAX_VALUE);
						
						//parser
						resp.setRespA(null);
						resp.setRespB1(getAA0237RespB1Data(groupApiList));
						resp.setRespB2(null);
						
					}else {
						// 功能B-2
						groupApiList = getTsmpGroupApiDao().query_aa0237Service(null, vgroupId, reqB, null, Integer.MAX_VALUE);
						
						//parser
						resp.setRespA(null);
						resp.setRespB1(null);
						resp.setRespB2(getAA0237RespB2Data(groupApiList));
					}
					
				}
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	
	private void excuteFuncB1(AA0237ReqB reqB ,AA0237Resp resp, String  vgroupId) {
		String[] words = ServiceUtil.getKeywords(reqB.getKeyword(), " ");
		String apiKey = reqB.getApiKey();
		String groupId = reqB.getGroupId();
		String moduleName = reqB.getModuleName();
		

		TsmpGroupApi  lastGroupApi = null;
		if(!StringUtils.isEmpty(apiKey) && !StringUtils.isEmpty(groupId)) {
			TsmpGroupApiId groupApiId = new TsmpGroupApiId(groupId, apiKey, moduleName);
				Optional<TsmpGroupApi > tsmpGroupApi = getTsmpGroupApiDao().findById(groupApiId);
			if (tsmpGroupApi.isPresent()) {
				lastGroupApi = tsmpGroupApi.get();
			}
		}
		
		List<TsmpGroupApi> groupApiList = getTsmpGroupApiDao().query_aa0237Service(lastGroupApi, vgroupId, reqB, words, getPageSize());
		
		//parser
		resp.setRespA(null);
		resp.setRespB1(getAA0237RespB1Data(groupApiList));
		resp.setRespB2(null);
	}
	
	private AA0237RespA getAA0237RespAData(List<TsmpGroupApi> groupApiList ) {
		AA0237RespA aa237RespA = new AA0237RespA();
		
		if(groupApiList != null && groupApiList.size() > 0) {
		
			aa237RespA.setModuleNameList(groupApiList.stream().map((f)->{
				return f.getModuleName();
			}).distinct().collect(Collectors.toList()));
		
			
		}else {
			aa237RespA.setModuleNameList(new ArrayList<>());
			
		}
		return aa237RespA;
		
	}
	
	private AA0237RespB1 getAA0237RespB1Data(List<TsmpGroupApi> groupApiList) {
		AA0237RespB1 aa0237RespB1 = new AA0237RespB1();
		
		List<AA0237RespB1D> apiKeyList = new ArrayList<>();
		if(groupApiList != null && groupApiList.size() > 0) {
			
			groupApiList.forEach((ga) ->{
				TsmpApi api = getTsmpApiByTsmpGroupApi(ga);
				if(api != null ) {
					AA0237RespB1D aa0237RespB1D = new AA0237RespB1D();
					aa0237RespB1D.setApiKey(ga.getApiKey());
					aa0237RespB1D.setGroupId(ga.getGroupId());
					aa0237RespB1D.setModuleName(ga.getModuleName());
					aa0237RespB1D.setApiName(api.getApiName());
					
					apiKeyList.add(aa0237RespB1D);
				}
			});
			
		}
		aa0237RespB1.setApiKeyList(apiKeyList);
		
		return aa0237RespB1;
	}
	
	private AA0237RespB2 getAA0237RespB2Data(List<TsmpGroupApi> groupApiList) {
		AA0237RespB2 aa0237RespB2 = new AA0237RespB2();
		List<AA0237RespB2D1> dataList = new ArrayList<>();
		
		Map<String,List<TsmpGroupApi>> map = new LinkedHashMap<>();
		if (groupApiList != null ) {
			groupApiList.forEach((groupApi) -> {
				String moduleName = groupApi.getModuleName();
				List<TsmpGroupApi> gaList = map.get(moduleName);
				
				if (gaList == null) {
					List<TsmpGroupApi> apiKeyList = new ArrayList<>();
					apiKeyList.add(groupApi);
					map.put(moduleName, apiKeyList);
					
				} else {
					if(!gaList.contains(groupApi)) {
						gaList.add(groupApi);
						map.put(moduleName, gaList);
						
					}
				}

			});
		}
		// *****秉持著一個虛擬群組的api不會重複的精神，要相信一個GA只會有有一個api*****
		map.forEach((moduleName, gaList) -> {
			AA0237RespB2D1 aa0237RespB2D1 = new AA0237RespB2D1();
			
			List<AA0237RespB2D2> apiKeyList = new ArrayList<>();
			gaList.forEach((ga) ->{
				TsmpApi api = getTsmpApiByTsmpGroupApi(ga);
				
				if(api != null ) {
					apiKeyList.add(getAA0237RespB2D2(api));
					
				}
				
			});
			
			// set data (aa0237RespB2D1以moduleName為一組)
			aa0237RespB2D1.setModuleName(moduleName);
			if(apiKeyList != null && apiKeyList.size() > 5) {
				aa0237RespB2D1.setIsApiKeyTrunc(true);
			}else {
				aa0237RespB2D1.setIsApiKeyTrunc(false);
			}
			aa0237RespB2D1.setApiKeyList(apiKeyList);
			dataList.add(aa0237RespB2D1);
			
		});
		aa0237RespB2.setDataList(dataList);

		
		return aa0237RespB2;
	}
	
	private TsmpApi getTsmpApiByTsmpGroupApi(TsmpGroupApi ga) {
		TsmpApi api = null;
		String moduleName = ga.getModuleName();
		String apiKey = ga.getApiKey();
		if(!StringUtils.isEmpty(moduleName) && !StringUtils.isEmpty(apiKey)) {
			TsmpApiId tsmpApiId = new TsmpApiId(apiKey, moduleName);
			Optional<TsmpApi> optApi = getTsmpApiDao().findById(tsmpApiId);
			if(optApi.isPresent()) {
				api = optApi.get();
			}
		}
		
		return api;
	}

	private AA0237RespB2D2 getAA0237RespB2D2(TsmpApi api) {
		AA0237RespB2D2 aa0237RespB2D2 = new AA0237RespB2D2();
		aa0237RespB2D2.setApiKey(api.getApiKey());
		aa0237RespB2D2.setApiName(api.getApiName());
		return aa0237RespB2D2;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0237");
		return this.pageSize;
	}
	
	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupApiDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	
}
