package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpGroupAuthoritiesCacheProxy;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpSecurityLevelCacheProxy;
import tpi.dgrv4.dpaa.constant.TsmpDpTimeUnit;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0214Api;
import tpi.dgrv4.dpaa.vo.AA0214Req;
import tpi.dgrv4.dpaa.vo.AA0214Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiId;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0214Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupApiDao tsmpGroupAPiDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpApiDao tsmpApiDao;
	private TsmpSecurityLevelCacheProxy securityLevelCacheProxy;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0214Service(TsmpGroupDao tsmpGroupDao, TsmpGroupApiDao tsmpGroupAPiDao,
			TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao,
			TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			BcryptParamHelper bcryptParamHelper, TsmpApiDao tsmpApiDao,
			TsmpSecurityLevelCacheProxy securityLevelCacheProxy, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupAPiDao = tsmpGroupAPiDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesCacheProxy = tsmpGroupAuthoritiesCacheProxy;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpApiDao = tsmpApiDao;
		this.securityLevelCacheProxy = securityLevelCacheProxy;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0214Resp updateGroup(TsmpAuthorization auth, AA0214Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_GROUP.value());
		
		AA0214Resp resp = new AA0214Resp();
		TsmpGroup group = null;
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String groupId = nvl(req.getGroupID());
			Optional<TsmpGroup> optGroup = getTsmpGroupDao().findById(groupId);
			if (optGroup.isPresent()) {
				group = optGroup.get();
			}else {
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			Integer allowDays = getAllowDaysFormat(req, locale);
			checkAndUpdateTsmpGroup(auth, req, group, allowDays, iip);
			updateGroupAuthoritiesMap(req, group, iip);
			checkApiKeyIsExistedAndUpdateRelatedTable(auth, group, req, allowDays, iip);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing(); // 1286:更新失敗

		}
		return resp;
	}

	private void checkAndUpdateTsmpGroup(TsmpAuthorization auth, AA0214Req req, TsmpGroup group, Integer allowDays, InnerInvokeParam iip) {
		String newGroupName = (req.getGroupName() == null) ? "" : req.getGroupName();
		String newGroupAlias = (req.getGroupAlias() == null) ? "" : req.getGroupAlias();
		String oriGroupName = nvl(group.getGroupName());
		String oriGroupAlias = nvl(group.getGroupAlias());
		Integer oriAllowDays = group.getAllowDays();
		Integer oriAllowTimes = group.getAllowTimes();

		if (!newGroupName.equals(oriGroupName)) {
			// 1399:群組代碼已存在
			List<TsmpGroup> groupList = getTsmpGroupDao().findByGroupNameAndVgroupFlag(newGroupName, "0");
			if (groupList != null && groupList.size() > 0) {
				throw TsmpDpAaRtnCode._1399.throwing();
			}
		}
		if (!StringUtils.isEmpty(newGroupAlias) && !newGroupAlias.equals(oriGroupAlias)) {
			// 1398:群組名稱已存在
			List<TsmpGroup> groupList = getTsmpGroupDao().findByGroupAliasAndVgroupFlag(newGroupAlias, "0");
			if (groupList != null && groupList.size() > 0) {
				throw TsmpDpAaRtnCode._1398.throwing();
			}
		}

		// update tsmp_group
		//傳入的allowDays & allowTimes必填，所以如果舊資料為空表示要更新
		if(oriAllowDays == null || oriAllowTimes == null
				|| allowDays.intValue() != group.getAllowDays().intValue()
				|| req.getAllowTimes().intValue() != group.getAllowTimes().intValue()
				|| !newGroupName.equals(oriGroupName)
				|| !newGroupAlias.equals(oriGroupAlias)
				|| !nvl(req.getGroupDesc()).equals(nvl(group.getGroupDesc()))
				|| !nvl(req.getSecurityLevel()).equals(nvl(group.getSecurityLevelId())) 
				) {
			
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, group); //舊資料統一轉成 String
			
			group.setUpdateTime(DateTimeUtil.now());
			group.setUpdateUser(auth.getUserName());
			group.setGroupName(nvl(req.getGroupName()));
			group.setGroupAlias(nvl(req.getGroupAlias()));
			group.setGroupDesc(nvl(req.getGroupDesc()));
			group.setGroupAccess("[]");
			group.setSecurityLevelId(getSecurityLV(req));
			group.setAllowDays(allowDays);	// 允許使用時間(AA0214Req.allowDays)。需要參考 時間單位(AA0214Req.allowTimes) 換算成 秒 後填入
			group.setAllowTimes(nvl(req.getAllowTimes()));
			
			group = getTsmpGroupDao().saveAndFlush(group);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpGroup.class.getSimpleName(), TableAct.U.value(), oldRowStr, group);
		}
	}
	
	/**
	 * 更新群組核身資料(TSMP_GROUP_AUTHORITIES_MAP)
	 * 
	 * @param req
	 * @throws Exception
	 */
	private void updateGroupAuthoritiesMap(AA0214Req req, TsmpGroup group, InnerInvokeParam iip) {
		// 1397:授權核身種類:[{{0}}]不存在
		List<TsmpGroupAuthoritiesMap> authMapList = getTsmpGroupAuthoritiesMapDao().findByGroupId(group.getGroupId());
		getTsmpGroupAuthoritiesMapDao().deleteAll(authMapList);
		
		authMapList.forEach(vo ->{
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpGroupAuthoritiesMap.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
		});
		
		
		List<String> newAuthoritiesIdList = req.getGroupAuthoritiesId();
		if(newAuthoritiesIdList != null) {
			newAuthoritiesIdList.forEach((authoritiesId)->{
				TsmpGroupAuthorities tsmpGroupAuthorities =	getAuthoritiesyId(authoritiesId);
				if(tsmpGroupAuthorities == null) {
					throw TsmpDpAaRtnCode._1397.throwing(authoritiesId);
				}
				updateGroupAuthoritiesMap(authoritiesId, group.getGroupId(), iip);
				
			});
		}
	}
	
	private void updateGroupAuthoritiesMap(String groupAuthoritieId, String groupId, InnerInvokeParam iip) {
		TsmpGroupAuthoritiesMap gaMap = new TsmpGroupAuthoritiesMap();
		gaMap.setGroupAuthoritieId(groupAuthoritieId);
		gaMap.setGroupId(groupId);
		getTsmpGroupAuthoritiesMapDao().saveAndFlush(gaMap);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpGroupAuthoritiesMap.class.getSimpleName(), TableAct.C.value(), null, gaMap);
	}
	

	/**
	 * 由 authoritiesId ,使用快取取得TsmpGroupAuthorities
	 * 
	 * @param authoritiesId
	 * @return
	 */
	private TsmpGroupAuthorities getAuthoritiesyId(String authoritiesId) {
		TsmpGroupAuthorities i = getTsmpGroupAuthoritiesCacheProxy().findById(authoritiesId).orElse(null);
		return i;
	}
	
	private String getSecurityLV(AA0214Req req) {

		String securityLevelId = nvl(req.getSecurityLevel());
		if (!StringUtils.isEmpty(securityLevelId)) {
			TsmpSecurityLevel securityLV = getSecurityLVById(securityLevelId);

			if (securityLV == null) {
				throw TsmpDpAaRtnCode._1364.throwing(); // 1364:安全等級不存在
			}
		}
		return securityLevelId;
	}
	
	private Integer getAllowDaysFormat(AA0214Req req, String locale) {
		Integer days = req.getAllowDays();
		if(days == null) {
			return 0;
		}
		String encodeTimeUnit =  req.getAllowDaysUnit();
		
		String aa0214_deodeTimeUnit = nvl(getTimeUnitByBcryptParamHelper(encodeTimeUnit, locale));
	
		if(TsmpDpTimeUnit.MINUTE.value().equalsIgnoreCase(aa0214_deodeTimeUnit)) {
			days = days * 60;
		}else if(TsmpDpTimeUnit.HOUR.value().equalsIgnoreCase(aa0214_deodeTimeUnit)) {
			days = days * 60 * 60;
		}else if(TsmpDpTimeUnit.DAY.value().equalsIgnoreCase(aa0214_deodeTimeUnit)) {
			days = days * 60 * 60 * 24;
		}
		
		return days;
	}
	
	private String getTimeUnitByBcryptParamHelper(String encodeTimeUnit, String locale) {
		String aa0214_decodeTimeUnit = null;
		try {
			aa0214_decodeTimeUnit = getBcryptParamHelper().decode(encodeTimeUnit, "TIME_UNIT", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return aa0214_decodeTimeUnit;
	}

	private void checkApiKeyIsExistedAndUpdateRelatedTable(TsmpAuthorization auth, TsmpGroup group, AA0214Req req, Integer allowDays, InnerInvokeParam iip) throws Exception {
		List<AA0214Api> oriApiList = req.getOriApiList();	//AA0214Api<moduleName.apiKeyList>
		List<AA0214Api> newApiList =req.getNewApiList();
		
		// 整理新增/刪除/無異動的api
		Map<String , List<AA0214Api>> map = getRemoveOrNoChangeList(oriApiList, newApiList);		// 被刪除的
		List<AA0214Api> needRemoveList = map.get("needRemoveList");
		List<AA0214Api> noChangeList = map.get("noChangeList");		// 沒有異動的
		List<AA0214Api> needAddList = getNeedAddList(oriApiList, newApiList);		// 新加入的
		
		//先檢查原本的api是否符合組織原則
		List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(auth.getOrgId(), Integer.MAX_VALUE);		//組織與子組織的orgId
		checkApiKeyIsExistedAndUpdateRelatedTable(needRemoveList, orgList, group, auth, req, allowDays, 1, iip);
		checkApiKeyIsExistedAndUpdateRelatedTable(needAddList, orgList, group, auth, req, allowDays, 2, iip);
		checkApiKeyIsExistedAndUpdateRelatedTable(noChangeList, orgList, group, auth, req, allowDays, 3, iip);
	}
	
	
	private void checkApiKeyIsExistedAndUpdateRelatedTable(List<AA0214Api> aa0214ApiList, List<String> orgList, TsmpGroup group,TsmpAuthorization auth, 
			AA0214Req req, Integer allowDays, int flag, InnerInvokeParam iip) throws Exception {
		
		if(aa0214ApiList != null) {
			for (AA0214Api item : aa0214ApiList) {
				List<String> apikeyList =  item.getApiKeyList();
				String moduleName = nvl(item.getModuleName());
				if(StringUtils.isEmpty(moduleName)) {
					logger.debug("moduleName is null");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				if(apikeyList != null) {
					for (String aa0214_apikey : apikeyList) {
						TsmpApiId aa0214_id = new TsmpApiId(aa0214_apikey,moduleName);
						Optional<TsmpApi> optApi = getTsmpApiDao().findById(aa0214_id);
						if(!optApi.isPresent()) {
							throw TsmpDpAaRtnCode._1400.throwing(aa0214_apikey);	// 1400:API: [{{0}}]不存在
						}
						TsmpApi aa0214_api = optApi.get();
						// 檢查此orgId 是否存在在修改者的向下組織當中
						String apiOrgId = aa0214_api.getOrgId();
						if (!StringUtils.isEmpty(apiOrgId)) {
							if ( CollectionUtils.isEmpty(orgList) || !orgList.contains(apiOrgId)) {
								throw TsmpDpAaRtnCode._1219.throwing();
							}
						}
						if(flag == 1) {
							removeApiHandle(moduleName, aa0214_apikey, group, auth, iip);// 被刪除的API
						}else if(flag == 2) {
							addApiHandle(moduleName, aa0214_apikey, group, auth, req, allowDays, iip);// 新加入的API
						}
					}
				}
			}
		}
	}
	
	/**
	 * 刪除TSMP_GROUP_API
	 * 
	 * @param moduleName
	 * @param apikey
	 * @param group
	 * @param auth
	 */
	private void removeApiHandle(String moduleName, String apikey, TsmpGroup group, TsmpAuthorization auth, InnerInvokeParam iip) {
		/*	WHERE TSMP_GROUP_API.GROUP_ID = AA0214Req.groupID
			AND TSMP_GROUP_API.API_KEY = 被取消選取的API的API_KEY 
			AND TSMP_GROUP_API.MODULE_NAME = 被取消選取的API的MODULE_NAME*/
		TsmpGroupApiId id = new TsmpGroupApiId(group.getGroupId(), apikey, moduleName);
		TsmpGroupApi vo = getTsmpGroupApiDao().findById(id).orElse(null);
		if(vo != null) {
			getTsmpGroupApiDao().delete(vo);
		}

		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpGroupApi.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
		
	}
	
	/**
	 * 新增TSMP_GROUP_API
	 * 
	 * @param moduleName
	 * @param apikey
	 * @param group
	 * @param auth
	 * @param req
	 * @param allowDays
	 */
	private void addApiHandle(String moduleName, String apikey, TsmpGroup group, TsmpAuthorization auth, 
			 AA0214Req req, Integer allowDays, InnerInvokeParam iip) {
		/*	b1.新增群組(TSMP_GROUP)與API(TSMP_API)的關聯資料表(TSMP_GROUP_API)。
	    	b2.欄位
	       		TSMP_GROUP_API.GROUP_ID = AA0214Req.groupID
	       		TSMP_GROUP_API.API_KEY = 新選取的API 的API_KEY 
	       		TSMP_GROUP_API.MODULE_NAME = 新選取的API 的MODULE_NAME 
	       		TSMP_GROUP_API.MODULE_VERSION = ""0""
	       		TSMP_GROUP_API.CREATE_TIME = 現在時間"*/
		TsmpGroupApi ga = new TsmpGroupApi();
		ga.setGroupId(group.getGroupId());
		ga.setApiKey(apikey);
		ga.setModuleName(moduleName);
		ga.setModuleVer("0");
		ga.setCreateTime(DateTimeUtil.now());
		
		getTsmpGroupApiDao().saveAndFlush(ga);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpGroupApi.class.getSimpleName(), TableAct.C.value(), null, ga);
	}
	
	private Map<String, List<AA0214Api>> getRemoveOrNoChangeList(List<AA0214Api> aa0214OriApiList, List<AA0214Api> aa0214NewApiList) {
		List<AA0214Api> needRemoveList = new ArrayList<>();
		List<AA0214Api> noChangeList = new ArrayList<>();
		if(aa0214OriApiList == null)
			aa0214OriApiList = new ArrayList<>();
		if(aa0214NewApiList == null)
			aa0214NewApiList = new ArrayList<>();
		// 整理資料
		Map<String, List<String>> map = new HashMap<>();
		aa0214OriApiList.forEach((aa0214RN_i) -> {
			String aa0214RN_moduleName = aa0214RN_i.getModuleName();
			if(StringUtils.isEmpty(aa0214RN_moduleName)) {
				logger.debug("AA0214Api ModuleName isEmpty !");
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			if (map.get(aa0214RN_moduleName) != null) {
				List<String> aa0214RN_list = map.get(aa0214RN_moduleName);
				if(aa0214RN_list == null || aa0214RN_list.size() == 0) {
					logger.debug("AA0214Api ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				aa0214RN_list.forEach((api) -> {
					if (!aa0214RN_list.contains(api)) {
						aa0214RN_list.add(api);
					}
				});
			} else {
				if(aa0214RN_i.getApiKeyList() == null || aa0214RN_i.getApiKeyList().size() == 0) {
					logger.debug("AA0214Api ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				map.put(aa0214RN_moduleName, aa0214RN_i.getApiKeyList());
			}
		});
		// ================比對資料(被刪除的.無異動的)============================
		if(aa0214NewApiList != null && aa0214NewApiList.size() > 0) {
			aa0214NewApiList.forEach((i) -> {
				List<String> list = map.get(i.getModuleName());
				if (list != null && list.size() > 0) {
					List<String> diffList = difference(list, i.getApiKeyList());
					if (diffList != null && diffList.size() > 0) {
						
						AA0214Api api = new AA0214Api();
						api.setModuleName(i.getModuleName());
						api.setApiKeyList(diffList);
						
						needRemoveList.add(api);
					}
					
					List<String> intersectionList = intersection(list, i.getApiKeyList());
					if(intersectionList != null && intersectionList.size() > 0){
						
						AA0214Api api = new AA0214Api();
						api.setModuleName(i.getModuleName());
						api.setApiKeyList(intersectionList);
						
						noChangeList.add(api);
					}
					
					map.remove(i.getModuleName());
				}
				
			});
			if(map != null && map.size() >0) {
				map.forEach((kay,value)->{
				AA0214Api api = new AA0214Api();
				api.setModuleName(kay);
				api.setApiKeyList(value);
				
				needRemoveList.add(api);
				}); 
			}
			
		}else {
			map.forEach((key,value)->{
				AA0214Api api = new AA0214Api();
				api.setModuleName(key);
				api.setApiKeyList(value);
				needRemoveList.add(api);
			});
		}
		
		Map<String, List<AA0214Api>> resultMap = new HashMap<>();
		resultMap.put("needRemoveList", needRemoveList);
		resultMap.put("noChangeList", noChangeList);
		
		return resultMap;
	}
	
	private List<AA0214Api> getNeedAddList(List<AA0214Api> aa0214OriApiList, List<AA0214Api> aa0214NewApiList) {
		List<AA0214Api> addList = new ArrayList<>();
		if(aa0214OriApiList == null)
			aa0214OriApiList = new ArrayList<>();
		if(aa0214NewApiList == null)
			aa0214NewApiList = new ArrayList<>();
		// 整理資料
		Map<String, List<String>> map = new HashMap<>();
		aa0214NewApiList.forEach((aa0214Add_i) ->{
			String aa0214Add_moduleName = aa0214Add_i.getModuleName();
			if(StringUtils.isEmpty(aa0214Add_moduleName)) {
				logger.debug("AA0214Api ModuleName isEmpty !");
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			if(map.get(aa0214Add_moduleName) != null) {
				List<String> aa0214Add_list = map.get(aa0214Add_moduleName);
				if(aa0214Add_list == null || aa0214Add_list.size() == 0) {
					logger.debug("AA0214Api ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				aa0214Add_list.forEach((api) ->{
					if(!aa0214Add_list.contains(api)) {
						aa0214Add_list.add(api);
					}
				});
			}else {
				if(aa0214Add_i.getApiKeyList() == null || aa0214Add_i.getApiKeyList().size() == 0) {
					logger.debug("AA0214Api ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				map.put(aa0214Add_moduleName, aa0214Add_i.getApiKeyList());
			}
		});
		//================比對資料============================
		if(aa0214OriApiList != null && aa0214OriApiList.size() > 0) {
			
			aa0214OriApiList.forEach((i)->{
				List<String> list = map.get(i.getModuleName());
				
				if(list != null && list.size() > 0){
					List<String> diffList = difference(list, i.getApiKeyList());
					if(diffList != null && diffList.size() > 0){
						
						AA0214Api item = new AA0214Api();
						item.setModuleName(i.getModuleName());
						item.setApiKeyList(diffList);
						
						addList.add(item);
					}
					map.remove(i.getModuleName());
				}
			});
			
			if(map != null && map.size() >0) {
				map.forEach((kay,value)->{
				AA0214Api it = new AA0214Api();
				it.setModuleName(kay);
				it.setApiKeyList(value);
				
				addList.add(it);
				}); 
			}
		}else {
			map.forEach((key,value)->{
				AA0214Api item = new AA0214Api();
				item.setModuleName(key);
				item.setApiKeyList(value);
				
				addList.add(item);
			});
		}
		
		return addList;
	}
	
	/**
     * ltstA對listB差集
	 * @param <R>
     *
     * @param ltstA
     * @param listB
     * @return
     */
    public static <R> List<R> difference(List<R> listA, List<R> listB) {
    	if(listA == null) {
    		listA = new ArrayList<>();
    	}
    	if(listB == null) {
    		listB = new ArrayList<>();
    	}
        List<R> aa0214_list = new ArrayList<>(listA.stream().collect(Collectors.toList()));
        Collections.copy(aa0214_list, listA);
        aa0214_list.removeAll(listB);
        return aa0214_list;
    }

    /**
     * 2個List的交集
     *
     * @param listA
     * @param listB
     * @return
     */
    public static <R> List<R> intersection(List<R> listA, List<R> listB) {
    	if(listA == null) {
    		listA = new ArrayList<>();
    	}
    	if(listB == null) {
    		listB = new ArrayList<>();
    	}
    	List<R> aa0214_list = new ArrayList<>(listA.stream().collect(Collectors.toList()));
        Collections.copy(aa0214_list, listA);
        aa0214_list.retainAll(listB);
        return aa0214_list;
    }
	
	/**
	 * 由 securityLevelId ,使用快取取得SecurityLV
	 * 
	 * @param securityLevelId
	 * @return
	 */
	private TsmpSecurityLevel getSecurityLVById(String securityLevelId) {
		TsmpSecurityLevel i = getSecurityLevelCacheProxy().findById(securityLevelId).orElse(null);
		return i;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupAPiDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return this.tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}

	protected TsmpGroupAuthoritiesCacheProxy getTsmpGroupAuthoritiesCacheProxy() {
		return this.tsmpGroupAuthoritiesCacheProxy;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpSecurityLevelCacheProxy getSecurityLevelCacheProxy() {
		return this.securityLevelCacheProxy;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
