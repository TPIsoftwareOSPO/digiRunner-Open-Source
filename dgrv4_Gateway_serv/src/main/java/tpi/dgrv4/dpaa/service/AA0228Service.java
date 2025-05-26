package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0228GroupInfo;
import tpi.dgrv4.dpaa.vo.AA0228ModuleAPIKey;
import tpi.dgrv4.dpaa.vo.AA0228Req;
import tpi.dgrv4.dpaa.vo.AA0228Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0228Service {

	private TPILogger logger = TPILogger.tl;
	
	private Integer pageSize;
	
	private TsmpSecurityLevelDao securityLVDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpGroupDao tsmpGroupDao;
	private ServiceConfig serviceConfig;
	
	@Autowired
	public AA0228Service(TsmpSecurityLevelDao securityLVDao, TsmpClientDao tsmpClientDao,
			TsmpApiDao tsmpApiDao, TsmpGroupApiDao tsmpGroupApiDao, TsmpGroupDao tsmpGroupDao,
			ServiceConfig serviceConfig) {
		super();
		this.securityLVDao = securityLVDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.serviceConfig = serviceConfig;
	}


	public AA0228Resp queryGroupList(AA0228Req req) {
		AA0228Resp resp = new AA0228Resp();
		try {
			String groupName = "";
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String groupId = nvl(req.getGroupId());
			Optional<TsmpGroup> optGroup = getTsmpGroupDao().findById(groupId);
			String securityLevelID = nvl(req.getSecurityLevelID());
			String clientID = nvl(req.getClientID());
			if(optGroup.isPresent()) {
				groupName = optGroup.get().getGroupName();
			}
			
			// 1298:查無資料
			List<TsmpGroup> groupList = getTsmpGroupDao().query_aa0228Service(groupName, groupId, 
										securityLevelID, clientID, "0", words, getPageSize());
			
			if(groupList == null || groupList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			//parser
			List<AA0228GroupInfo> list = getAA0228List(groupList);
			resp.setGroupInfoList(list);
			

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	
	private List<AA0228GroupInfo> getAA0228List(List<TsmpGroup> groupList) {
		
		List<AA0228GroupInfo> dataList = new ArrayList<AA0228GroupInfo>();
		for (TsmpGroup group : groupList) {
			AA0228GroupInfo vo = setAA0228ListData(group);
			dataList.add(vo);
		}
		return dataList;
	}
	
	private AA0228GroupInfo setAA0228ListData(TsmpGroup group) {
		AA0228GroupInfo groupInfo = new AA0228GroupInfo();
		String securityLevelID = group.getSecurityLevelId();
		String createDateStr = "";
		
		String securityLevelName = "";
		if(securityLevelID != null) {
			Optional<TsmpSecurityLevel> optSecurityLV = getSecurityLVDao().findById(securityLevelID);
			if(optSecurityLV.isPresent()) {
				securityLevelName = optSecurityLV.get().getSecurityLevelName();	
			}
		}
		
		groupInfo.setSecurityLevelName(securityLevelName);
		groupInfo.setGroupName(group.getGroupName());
		groupInfo.setGroupID(group.getGroupId());
		groupInfo.setGroupDesc(group.getGroupDesc());
		groupInfo.setGroupAlias(group.getGroupAlias());
		
		Date createDate = group.getCreateTime();
		Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(createDate, null);
		if(createDateOpt.isPresent()) {
			createDateStr = createDateOpt.get();
			
		}
		
		groupInfo.setCreateTime(createDateStr);
		
		groupInfo.setModuleAPIKeyList(getModuleAPIKeyList(group.getGroupId()));
		
		return groupInfo;
	}
	
	private List<AA0228ModuleAPIKey> getModuleAPIKeyList(String groupId) {
		List<TsmpApi> list = getTsmpApiDao().queryByTsmpGroupAPiGroupId(groupId);

		List<AA0228ModuleAPIKey> aa0228ModuleAPIKeyList = new ArrayList<>();
		Map<String, List<String>> map = new LinkedHashMap<>();

		if (list != null) {
			list.forEach((tsmpApi) -> {
				String moduleName = tsmpApi.getModuleName();
				List<String> apiList = map.get(moduleName);
				String apiName = ServiceUtil.nvl(tsmpApi.getApiName());
				String apiKey = ServiceUtil.nvl(tsmpApi.getApiKey());
				

				if (apiList == null) {
					List<String> apiKeyList = new ArrayList<>();
					apiKeyList.add(apiName + "(" + apiKey + ")");
					map.put(moduleName, apiKeyList);
				} else {
					apiList.add(apiName + "(" + apiKey + ")");
					map.put(moduleName, apiList);
				}

			});
		}

		map.forEach((moduleName, apiKeyList) -> {
			String keyValue = "";
			boolean isTruncated = false;
			String truncatedKeyValue = "";
			
			for (String string : apiKeyList) {
				keyValue += string + ",";
			}
			if (keyValue.length() > 0) {
				keyValue = keyValue.substring(0, keyValue.length() - 1);
			}

			AA0228ModuleAPIKey aa0228ModuleApiKey = new AA0228ModuleAPIKey();
			truncatedKeyValue = keyValue;
			if (keyValue.length() > 100) {
				truncatedKeyValue = keyValue.substring(0, 100) + "...";
				isTruncated = true;
			}
			aa0228ModuleApiKey.setModuleName(moduleName);
			aa0228ModuleApiKey.setIsTruncated(isTruncated);
			aa0228ModuleApiKey.setApiNameApiKey(truncatedKeyValue);
			aa0228ModuleApiKey.setOrgApiNameApiKey(keyValue);
			
			aa0228ModuleAPIKeyList.add(aa0228ModuleApiKey);
		});

		return aa0228ModuleAPIKeyList;

	}

	protected TsmpSecurityLevelDao getSecurityLVDao() {
		return securityLVDao;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}	

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0228");
		return this.pageSize;
	}
}
