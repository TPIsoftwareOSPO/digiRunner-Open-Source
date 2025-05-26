package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpGroupAuthoritiesCacheProxy;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpSecurityLevelCacheProxy;
import tpi.dgrv4.dpaa.constant.TsmpDpTimeUnit;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0224Item;
import tpi.dgrv4.dpaa.vo.AA0224Req;
import tpi.dgrv4.dpaa.vo.AA0224Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

@Service
public class AA0224Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpUserDao tsmpUserDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpSecurityLevelCacheProxy tsmpSecurityLevelCacheProxy;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpGroupApiDao tsmpGroupAPiDao;
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy;
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private SeqStoreService seqStoreService;
	private BcryptParamHelper bcryptParamHelper;
	private DgrAuditLogService dgrAuditLogService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	
	@Autowired
	public AA0224Service(TsmpUserDao tsmpUserDao, TsmpApiDao tsmpApiDao,
			TsmpSecurityLevelCacheProxy tsmpSecurityLevelCacheProxy, TsmpGroupDao tsmpGroupDao,
			TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao, TsmpGroupApiDao tsmpGroupAPiDao,
			TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao, TsmpVgroupDao tsmpVgroupDao,
			TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy, TsmpRtnCodeDao tsmpRtnCodeDao,
			TsmpVgroupGroupDao tsmpVgroupGroupDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpClientVgroupDao tsmpClientVgroupDao, TsmpClientGroupDao tsmpClientGroupDao,
			OauthClientDetailsDao oauthClientDetailsDao, SeqStoreService seqStoreService,
			BcryptParamHelper bcryptParamHelper, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpSecurityLevelCacheProxy = tsmpSecurityLevelCacheProxy;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpGroupAPiDao = tsmpGroupAPiDao;
		this.tsmpVgroupAuthoritiesMapDao = tsmpVgroupAuthoritiesMapDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpGroupAuthoritiesCacheProxy = tsmpGroupAuthoritiesCacheProxy;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.seqStoreService = seqStoreService;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}


	@Transactional
	public AA0224Resp updateVGroup(TsmpAuthorization auth, AA0224Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		//寫入 Audit Log M, D在OAuthUtil.updateScope裡
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		
		AA0224Resp resp = new AA0224Resp();
		try {
			checkParams(auth, req);
			checkDataAndUpdateTables(auth, req, locale, iip);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();	// 1220:儲存失敗，資料長度過大
			}  else {
				throw TsmpDpAaRtnCode._1286.throwing();		// 1286:更新失敗
			}	
			
		}
		return resp;
	}

	
	private void checkParams(TsmpAuthorization auth, AA0224Req req) throws Exception {
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		
		if (!StringUtils.hasLength(userNameForQuery)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
		
		// 1231:使用者不存在
		checkUserExists(userNameForQuery, idPType);
			
		// 1273:組織單位ID:必填參數
		String orgId = auth.getOrgId();
		if(StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}
		
		// 1353:[{{0}}] 已存在: {{1}}
		checkVgroupIsExisted(req);
		
		// 1364:安全等級不存在
		checkSecurityLevel(req);
	}
	
	protected void checkUserExists(String userNameForQuery, String idPType) {
		if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				//Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery + ", idp_type: " + idPType);
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
		} else {//以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			//Table 查不到 user
			TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
			if (tsmpUser == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
		}
	}
	
	/**
	 *	1. 新輸入的 虛擬群組代碼 不可在 TSMP_VGROUP 中重複, 否則 throw 1353。 ({{newVgroupName}} 已存在: {{1}})	</br>			
	 *	2. 若有新輸入 虛擬群組名稱 不可在 TSMP_VGROUP 中重複, 否則 throw 1353。 ({{newVgroupAlias}} 已存在: {{1}})				
	 * @param req
	 * @throws Exception
	 */
	private void checkVgroupIsExisted(AA0224Req req) throws Exception {
		String oriVgroupName = nvl(req.getOriVgroupName());
		String oriVgroupAlias = nvl(req.getOriVgroupAlias());
		String vgroupName = nvl(req.getNewVgroupName());
		String vgroupAlias = nvl(req.getNewVgroupAlias());
		List<TsmpVgroup> vgroupNameList = getTsmpVgroupDao().findByVgroupName(vgroupName);
		List<TsmpVgroup> vgroupAliasList = getTsmpVgroupDao().findByVgroupAlias(vgroupAlias);
		TsmpVgroup vg = null;
		
		if(vgroupNameList != null ) {
			for (TsmpVgroup tsmpVgroup : vgroupNameList) {
					if(oriVgroupName.equals(tsmpVgroup.getVgroupName())) {
						vg = tsmpVgroup;
				}
			}
			if(vg != null) {
				vgroupNameList.remove(vg);
			}
			if(vgroupNameList.size() > 0) {
				throw TsmpDpAaRtnCode._1353.throwing("{{newVgroupName}}", vgroupName);
			}
		}
		
		if(!StringUtils.isEmpty(vgroupAlias) && vgroupAliasList != null) {
				for (TsmpVgroup tsmpVgroup : vgroupAliasList) {
						if(oriVgroupAlias.equals(tsmpVgroup.getVgroupAlias())) {
							vg = tsmpVgroup;
					}
				}
				if(vg != null) {
					vgroupAliasList.remove(vg);
				}
			
			if(vgroupAliasList.size() > 0) {
				throw TsmpDpAaRtnCode._1353.throwing("{{newVgroupAlias}}", vgroupAlias);
			}
		}
	}
	
	/**
	 * 檢查新傳入的 newSecurityLevelId 是否存在 TSMP_SECURITY_LEVEL 中, 否則 throw 1364:安全等級不存在。(使用DaoCacheService)
	 * 
	 * @param req
	 * @throws Exception
	 */
	private void checkSecurityLevel(AA0224Req req) throws Exception {
		// 1364:安全等級不存在
		String securityLevelId = req.getNewSecurityLevelId();
		if(!StringUtils.isEmpty(securityLevelId)) {
			TsmpSecurityLevel securityLV = getSecurityLVById(securityLevelId);
			
			if(securityLV == null) {
				throw TsmpDpAaRtnCode._1364.throwing();
			}
		}
	}
	
	
	/**
	 * 1.檢查新傳入的每個 newVgroupAuthoritieIds 是否都存在 TSMP_GROUP_AUTHORITIES 中, 否則 throw 1397:授權核身種類:[{{0}}]不存在。	(使用DaoCacheService)
	 * <br>			
	 * 2.依照 虛擬群組ID 查出全部的核身對應資料(TSMP_VGROUP_AUTHORITIES_MAP), 與 oriVgroupAuthoritieIds 比對確認相符後刪除, 若不相符則 throw 1286
	 * <br>
	 * 3.寫入新的核身代碼對應(newVgroupAuthoritieIds)
	 * @param req
	 * @throws Exception
	 */
	private void checkAuthoritieIdsAndUpdateVgroupAuthoritiesMap(AA0224Req req) {
		// 1397:授權核身種類:[{{0}}]不存在
		String vgroupId = req.getVgroupId();
		List<String> newAuthoritiesIdList = req.getNewVgroupAuthoritieIds();
		List<String> oriAuthoritiesIdList = req.getOriVgroupAuthoritieIds();
		List<TsmpVgroupAuthoritiesMap> vgamList = getTsmpVgroupAuthoritiesMapDao().findByVgroupId(vgroupId);
		
		if(vgamList != null || oriAuthoritiesIdList != null) {
			List<String> oriDbList = null;
			if(vgamList != null) {
				oriDbList = vgamList.stream().map((vgam)->{
					return vgam.getVgroupAuthoritieId();
				}).collect(Collectors.toList());
			}
			
			if(!compare(oriAuthoritiesIdList, oriDbList)) {
				logger.debug("TsmpVgroup Id : "+vgroupId +" TSMP_VGROUP_AUTHORITIES_MAP 與 AA0224Req.getOriVgroupAuthoritieIds 不相符");
				throw TsmpDpAaRtnCode._1286.throwing();
			}else {
				getTsmpVgroupAuthoritiesMapDao().deleteAll(vgamList);
			}
		}
		
		if(newAuthoritiesIdList != null) {
			newAuthoritiesIdList.forEach((authoritiesId)->{
				TsmpGroupAuthorities tsmpGroupAuthorities =	getAuthoritiesyId(authoritiesId);
				if(tsmpGroupAuthorities == null) {
					throw TsmpDpAaRtnCode._1397.throwing(authoritiesId);
				}
				updateVgroupAuthoritiesMap(authoritiesId, vgroupId);
				
			});
		}
	}
	
	
	public <T extends Comparable<T>> boolean compare(List<T> listA, List<T> listB) {
		if(listA == null) {
    		listA = new ArrayList<>();
    	}
		if(listB == null) {
    		listB = new ArrayList<>();
    	}
		if (listA.size() != listB.size())
			return false;
		Collections.sort(listA);
		Collections.sort(listB);
		for (int i = 0; i < listA.size(); i++) {
			if (!listA.get(i).equals(listB.get(i)))
				return false;
		}
		return true;
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
	
	

	
	/**
	 * 1.檢查每一筆新選入的 API (newApiList) 是否存在 TSMP_API 並符合組織原則, 否則 throw 1400、1219
	 *  <br>
	 * 檢查每一筆原始的 API (oriApiList) 是否存在 TSMP_API 並符合組織原則, 否則 throw 1400、1219。
	 *  <br>
	 * 2.每個虛擬群組不能加入超過 205 支 API。判斷 AA0224Item.apiKeyList 的總數若超過 205 則 throw 1402。	
	 * 
	 * 3.異動<b>TSMP_GROUP、TSMP_GROUP_API、TSMP_GROUP_AUTHORITIES_MAP、TSMP_VGROUP_GROUP</b>
	 * 4.異動<b> TSMP_CLIENT_VGROUP、TSMP_CLIENT_GROUP</b>
	 * @param req
	 * @throws Exception
	 */
	private void checkApiKeyIsExistedAndUpdateRelatedTable(TsmpAuthorization auth, AA0224Req req, 
			Integer allowDays,  TsmpVgroup vgroup, InnerInvokeParam iip) throws Exception {
		List<AA0224Item> aa0224OriItemList = req.getOriApiList();
		List<AA0224Item> aa0224NewItemList = req.getNewApiList();
		
		if(aa0224OriItemList == null) {
			aa0224OriItemList = new ArrayList<>();
		}
		if(aa0224NewItemList == null) {
			aa0224NewItemList = new ArrayList<>();
		}
		Map<String , List<AA0224Item>> map = getRemoveOrNoChangeList(aa0224OriItemList, aa0224NewItemList);
		
		List<AA0224Item> needRemoveList = map.get("needRemoveList");		// 被刪除的
		List<AA0224Item> needAddList = getNeedAddList(aa0224OriItemList, aa0224NewItemList);		// 新加入的
		List<AA0224Item> noChangeList = map.get("noChangeList");		// 沒有異動的
		
		//先檢查原本的api是否符合組織原則
		List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(auth.getOrgId(), Integer.MAX_VALUE);		//組織與子組織的orgId
		
		
		// 判斷 AA0224Item.apiKeyList 的總數若超過 205 則 throw 1402:虛擬群組的API數量上限為 [{{0}}]，您選擇 [{{1}}]
		int i = 0;
		i = getApiCount(i, needAddList);
		i = getApiCount(i, noChangeList);
		
		if(i > 205) {
			throw TsmpDpAaRtnCode._1402.throwing("205", i+"");
		}
		
		checkApiKeyIsExistedAndUpdateRelatedTable(needRemoveList, orgList, vgroup, auth, req, allowDays, 1, iip);
	
		checkApiKeyIsExistedAndUpdateRelatedTable(needAddList, orgList, vgroup, auth, req, allowDays, 2, iip);
		checkApiKeyIsExistedAndUpdateRelatedTable(noChangeList, orgList, vgroup, auth, req, allowDays, 3, iip);
	}
	
	private int getApiCount(int i, List<AA0224Item> itemList) {
		if(itemList != null) {
			for (AA0224Item item : itemList) {
				List<String> apikeyList =  item.getApiKeyList();
				if(apikeyList != null) {
					i += apikeyList.size();
				}
			}
			
		}
		return i;
	}
	
	private Map<String, List<AA0224Item>> getRemoveOrNoChangeList(List<AA0224Item> aa0224OriItemList, List<AA0224Item> aa0224NewItemList) {
		List<AA0224Item> needRemoveList = new ArrayList<>();
		List<AA0224Item> noChangeList = new ArrayList<>();
		// 整理資料
		Map<String, List<String>> map = new HashMap<>();
		aa0224OriItemList.forEach((aa0224RN_i) -> {
			String aa0224RN_moduleName = aa0224RN_i.getModuleName();
			if(StringUtils.isEmpty(aa0224RN_moduleName)) {
				logger.debug("AA0224Item ModuleName isEmpty !");
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			if (map.get(aa0224RN_moduleName) != null) {
				List<String> aa0224RN_list = map.get(aa0224RN_moduleName);
				if(aa0224RN_list == null || aa0224RN_list.size() == 0) {
					logger.debug("AA0224Item ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				aa0224RN_list.forEach((api) -> {
					if (!aa0224RN_list.contains(api)) {
						aa0224RN_list.add(api);
					}
				});
			} else {
				if(aa0224RN_i.getApiKeyList() == null || aa0224RN_i.getApiKeyList().size() == 0) {
					logger.debug("AA0224Item ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				map.put(aa0224RN_moduleName, aa0224RN_i.getApiKeyList());
			}
		});
		// ================比對資料(被刪除的.無異動的)============================
		if(aa0224NewItemList != null && aa0224NewItemList.size() > 0) {
			aa0224NewItemList.forEach((i) -> {
				List<String> list = map.get(i.getModuleName());
				
				if (list != null && list.size() > 0) {
					List<String> diffList = difference(list, i.getApiKeyList());
					if (diffList != null && diffList.size() > 0) {
						
						AA0224Item item = new AA0224Item();
						item.setModuleName(i.getModuleName());
						item.setApiKeyList(diffList);
						
						needRemoveList.add(item);
					}
					
					List<String> intersectionList = intersection(list, i.getApiKeyList());
					if(intersectionList != null && intersectionList.size() > 0){
						
						AA0224Item item = new AA0224Item();
						item.setModuleName(i.getModuleName());
						item.setApiKeyList(intersectionList);
						
						noChangeList.add(item);
					}
					map.remove(i.getModuleName());
				}
				
			});
			if(map != null && map.size() >0) {
				map.forEach((kay,value)->{
				AA0224Item item = new AA0224Item();
				item.setModuleName(kay);
				item.setApiKeyList(value);
				
				needRemoveList.add(item);
				}); 
			}
			
		}else {
			map.forEach((key,value)->{
				AA0224Item item = new AA0224Item();
				item.setModuleName(key);
				item.setApiKeyList(value);
				
				needRemoveList.add(item);
			});
		}
		
		Map<String, List<AA0224Item>> resultMap = new HashMap<>();
		resultMap.put("needRemoveList", needRemoveList);
		resultMap.put("noChangeList", noChangeList);
		
		return resultMap;
	}
	
	private List<AA0224Item> getNeedAddList(List<AA0224Item> aa0224OriItemList, List<AA0224Item> aa0224NewItemList) {
		List<AA0224Item> addList = new ArrayList<>();
		// 整理資料
		Map<String, List<String>> map = new HashMap<>();
		aa0224NewItemList.forEach((i) ->{
			String aa0224Add_moduleName = i.getModuleName();
			if(StringUtils.isEmpty(aa0224Add_moduleName)) {
				logger.debug("AA0224Item ModuleName isEmpty !");
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			if(map.get(aa0224Add_moduleName) != null) {
				List<String> aa0224Add_list = map.get(aa0224Add_moduleName);
				if(aa0224Add_list == null || aa0224Add_list.size() == 0) {
					logger.debug("AA0224Item ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				aa0224Add_list.forEach((api) ->{
					if(!aa0224Add_list.contains(api)) {
						aa0224Add_list.add(api);
					}
				});
			}else {
				if(i.getApiKeyList() == null || i.getApiKeyList().size() == 0) {
					logger.debug("AA0224Item ApiKeyList isEmpty !");
					throw TsmpDpAaRtnCode._1286.throwing();
				}
				map.put(aa0224Add_moduleName, i.getApiKeyList());
			}
		});
		//================比對資料============================
		if(aa0224OriItemList != null && aa0224OriItemList.size() > 0) {
			
			aa0224OriItemList.forEach((i)->{
				List<String> list = map.get(i.getModuleName());
				
				if(list != null && list.size() > 0){
					List<String> diffList = difference(list, i.getApiKeyList());
					if(diffList != null && diffList.size() > 0){
						
						AA0224Item item = new AA0224Item();
						item.setModuleName(i.getModuleName());
						item.setApiKeyList(diffList);
						
						addList.add(item);
					}
					map.remove(i.getModuleName());
				}
			});
			if(map != null && map.size() >0) {
				map.forEach((kay,value)->{
				AA0224Item item = new AA0224Item();
				item.setModuleName(kay);
				item.setApiKeyList(value);
				
				addList.add(item);
				}); 
			}
		}else {
			map.forEach((key,value)->{
				AA0224Item item = new AA0224Item();
				item.setModuleName(key);
				item.setApiKeyList(value);
				
				addList.add(item);
			});
		}
		
		
		return addList;
	}
	
	private void checkApiKeyIsExistedAndUpdateRelatedTable(List<AA0224Item> aa0224ItemList, List<String> orgList, TsmpVgroup vgroup,TsmpAuthorization auth, 
			AA0224Req req, Integer allowDays, int flag, InnerInvokeParam iip) throws Exception {
		
		if(aa0224ItemList != null) {
			for (AA0224Item item : aa0224ItemList) {
				List<String> apikeyList =  item.getApiKeyList();
				String moduleName = nvl(item.getModuleName());
				if(apikeyList != null) {
					for (String apikey : apikeyList) {
						TsmpApiId id = new TsmpApiId(apikey,moduleName);
						Optional<TsmpApi> optApi = getTsmpApiDao().findById(id);
						if(!optApi.isPresent()) {
							throw TsmpDpAaRtnCode._1400.throwing(apikey);	// 1400:API: [{{0}}]不存在
						}
						TsmpApi api = optApi.get();
						// 檢查此orgId 是否存在在修改者的向下組織當中
						String apiOrgId = api.getOrgId();
						if (!StringUtils.isEmpty(apiOrgId)) {
							if ( CollectionUtils.isEmpty(orgList) || !orgList.contains(apiOrgId)) {
								throw TsmpDpAaRtnCode._1219.throwing();
							}
						}
						List<String> groupId = getTsmpGroupApiByGroupId(moduleName, apikey, vgroup.getVgroupId());
						if(flag == 1) {
							removeApiHandle(moduleName, apikey, vgroup, auth, groupId, iip);// 被刪除的API
						}else if(flag == 2) {
							addApiHandle( vgroup, auth, req, allowDays, moduleName, apikey, iip);// 新加入的API
						}else if(flag == 3) {
							noChangeApiHandle(vgroup, auth, req, allowDays, groupId);// 沒有異動的API
						}
					}
				}
			}
			
		}
	}
	
	/**
	 * 由moduleName.moduleName找出TsmpGroupApi<br>
	 * 檢查該TsmpGroupApi的groupId是否存在在TsmpVgroupGroup
	 * 
	 * @param moduleName
	 * @param apikey
	 * @return
	 */
	private List<String> getTsmpGroupApiByGroupId(String moduleName, String apikey, String vgroupId){
		
		List<TsmpGroupApi> gaList = getTsmpGroupApiDao().findByApiKeyAndModuleName(apikey, moduleName);
		List<String> groupId = new ArrayList<>();
		gaList.forEach((ga) ->{
			List<TsmpVgroupGroup> vggList = getTsmpVgroupGroupDao().findByGroupId(ga.getGroupId());
			if(vggList != null ) {
				vggList.forEach((vgg)->{
					if(vgroupId.equals(vgg.getVgroupId())) {
						groupId.add(ga.getGroupId());
						
					}
				});
			}
		});
		return groupId;
	}
	
	/**
	 *  2-1. 刪除擁有這些API的群組(TSMP_GROUP), 且群組的 VGROUP_FLAG = '1'; 連帶刪除與群組相關的核身資料(TSMP_GROUP_AUTHORITIES_MAP)	
	 *  2-2. 刪除API與2-1被刪除的群組之間的關聯(TSMP_GROUP_API)
	 *  2-3. 刪除被更新的虛擬群組與2-1被刪除的群組之間的關聯(TSMP_VGROUP_GROUP)	
	 *  2-4. 刪除每個擁有該虛擬群組的用戶端(TSMP_CLIENT_VGROUP)與2-1被刪除的群組之間的關聯(TSMP_CLIENT_GROUP), 注意: 群組必須是虛擬群組
	 *  (存在於 TSMP_VGROUP_GROUP 且 VGROUP_FLAG = '1') 
	 * @param moduleName
	 * @param apikey
	 * @param vgroup
	 * @param auth
	 * @param groupId
	 */
	private void removeApiHandle(String moduleName, String apikey, TsmpVgroup vgroup,TsmpAuthorization auth, List<String> groupId, InnerInvokeParam iip) {
		
		groupId.forEach((id) ->{
			//檢查擁有該虛擬群組的用戶端(TSMP_CLIENT_VGROUP)與2-1被刪除的群組之間的關聯(TSMP_CLIENT_GROUP), 注意: 群組必須是虛擬群組
	        //  (存在於 TSMP_VGROUP_GROUP 且 VGROUP_FLAG = '1')
			/**
			 * select * from tsmp_client_group tcg
			 * where tcg.GROUP_ID  = '2446'
			 * and EXISTS (
			 * 		select 1 from TSMP_CLIENT_VGROUP tcv 
			 *		where tcv.VGROUP_ID = '1059'
			 *		and tcg.CLIENT_ID = tcv.CLIENT_ID 
			 *		and EXISTS (
			 *			select 1 from tsmp_group tg 
			 *			where tg.VGROUP_ID =tcv.VGROUP_ID 
			 *			and tg.VGROUP_FLAG = '1'
			 *			and tcv.VGROUP_ID = '1059'		
			 *		)
			 *)
			 */
			 List<TsmpClientGroup> cgList =getTsmpClientGroupDao().findByGroupId(id);
			cgList.forEach((cg) ->{
				List<TsmpClientVgroup> cvgList = getTsmpClientVgroupDao().findByVgroupIdAndClientId(vgroup.getVgroupId(), cg.getClientId());
				cvgList.forEach((cvg)->{
					List<TsmpGroup> gList = getTsmpGroupDao().findByVgroupIdAndVgroupFlag(vgroup.getVgroupId(), "1");
					if(gList != null && gList.size() > 0 ) {
						getTsmpClientGroupDao().delete(cg);
						String scope = getScope(cg.getClientId());	//TsmpClientGroupDao
						OAuthUtil.updateScope(cg.getClientId(), scope, iip);
					}
				});
			
			});
			
			Optional<TsmpGroup> optGroup = getTsmpGroupDao().findById(id);
			if(optGroup.isPresent()) {
				getTsmpGroupDao().delete(optGroup.get());
				
			}
			List<TsmpGroupAuthoritiesMap> optGam = getTsmpGroupAuthoritiesMapDao().findByGroupId(id);
			getTsmpGroupAuthoritiesMapDao().deleteAll(optGam);
			
			List<TsmpGroupApi>  removeGaList = getTsmpGroupApiDao().findByGroupId(id);
			getTsmpGroupApiDao().deleteAll(removeGaList);
			
			List<TsmpVgroupGroup> vggList = getTsmpVgroupGroupDao().findByGroupId(id);
			getTsmpVgroupGroupDao().deleteAll(vggList);
		});
		
	}
	
	private String getScope(String clientId) {
		List<TsmpClientGroup> clientGroup = getTsmpClientGroupDao().findByClientId(clientId);
		StringBuffer sb = new StringBuffer();
		String scope = "";
		clientGroup.forEach((group) ->{
			sb.append(group.getGroupId()+",");
		});
		if(!"".equals(sb.toString())) {
			scope = sb.toString().substring(0, sb.toString().length()-1);
		}
		return scope;
	}
	
	private void addApiHandle(TsmpVgroup vgroup,TsmpAuthorization auth, AA0224Req req,  Integer allowDays,
			String moduleName, String apikey, InnerInvokeParam iip) throws Exception {
		
		String groupId = updateTsmpGroup(auth, req, allowDays, vgroup);
		updateTsmpGroupApi(groupId, apikey, moduleName);
		updateTsmpGroupAuthoritiesMap(req, groupId);
		updateTsmpVgroupGroup(groupId, req.getVgroupId());
		
		// 如果TsmpClientVgroup存在VgroupId.表示有Client用到此虛擬群組.所以新增API必須新增TsmpClientGroup
		List<TsmpClientVgroup> cvList = getTsmpClientVgroupDao().findByVgroupId(req.getVgroupId());
		if(cvList != null) {
			cvList.forEach((cv)->{
				updateTsmpClientGroup(cv.getClientId(), groupId);
				String scope = getScope(cv.getClientId());
				OAuthUtil.updateScope(cv.getClientId(), scope, iip);
			});
		}
	}

	/**
	 * 1.找出擁有這些API的群組(TSMP_GROUP), 且群組的 VGROUP_FLAG = '1', 並更新 安全等級ID、允許使用時間(秒)、授權次數上限、虛擬群組ID、虛擬群組代碼、更新日期及更新人員。	<br>		
     * 2. 刪除1被更新的群組所對應的核身資料(TSMP_GROUP_AUTHORITIES_MAP), 再寫入新的核身代碼對應(newVgroupAuthoritieIds)				
	 * 
	 * @param vgroup
	 * @param auth
	 * @param req
	 * @param allowDays
	 * @param groupId
	 */
	private void noChangeApiHandle(TsmpVgroup vgroup,TsmpAuthorization auth, 
			AA0224Req req, Integer allowDays, List<String> groupId) {
		// 找出擁有這些API的群組(TSMP_GROUP), 且群組的 VGROUP_FLAG = '1'
		groupId.forEach((id) -> {
			Optional<TsmpGroup> optGroup = getTsmpGroupDao().findById(id);
			if (optGroup.isPresent()) {
				TsmpGroup group = optGroup.get();
				if (nvl(group.getVgroupFlag()).equals("1")) {
					// 1.更新 安全等級ID、允許使用時間(秒)、授權次數上限、虛擬群組ID、虛擬群組代碼、 更新日期及更新人員
					if (!Objects.equals(req.getOriAllowDays(), req.getNewAllowDays())
							|| !Objects.equals(req.getOriAllowTimes(), req.getNewAllowTimes())
							|| req.getOriSecurityLevelId().equals(req.getNewSecurityLevelId())
							|| nvl(req.getOriVgroupAlias()).equals(nvl(req.getNewVgroupAlias()))
							|| nvl(req.getOriVgroupDesc()).equals(nvl(req.getNewVgroupDesc()))
							|| nvl(req.getOriVgroupName()).equals(nvl(req.getNewVgroupName()))) {
						group.setVgroupId(nvl(req.getVgroupId()));
						group.setVgroupName(nvl(req.getNewVgroupName()));
						group.setSecurityLevelId(req.getNewSecurityLevelId());
						group.setAllowTimes(req.getNewAllowTimes());
						group.setAllowDays(allowDays);
						group.setUpdateTime(DateTimeUtil.now());
						group.setUpdateUser(auth.getUserName());
						getTsmpGroupDao().saveAndFlush(group);
					}

					// 2.刪除1被更新的群組所對應的核身資料(TSMP_GROUP_AUTHORITIES_MAP),
					// 再寫入新的核身代碼對應(newVgroupAuthoritieIds)
					List<String> newAuthoritiesIdList = req.getNewVgroupAuthoritieIds();
					List<String> oriAuthoritiesIdList = req.getOriVgroupAuthoritieIds();
					if (oriAuthoritiesIdList != null) {
						oriAuthoritiesIdList.forEach((authId) -> {
							Optional<TsmpGroupAuthoritiesMap> optGam = getTsmpGroupAuthoritiesMapDao()
									.findById(new TsmpGroupAuthoritiesMapId(group.getGroupId(), authId));
							if (optGam.isPresent()) {
								getTsmpGroupAuthoritiesMapDao().delete(optGam.get());
							}
						});
					}

					if (newAuthoritiesIdList != null) {
						newAuthoritiesIdList.forEach((authId) -> {
							TsmpGroupAuthoritiesMap newGam = new TsmpGroupAuthoritiesMap();
							newGam.setGroupAuthoritieId(authId);
							newGam.setGroupId(group.getGroupId());
							getTsmpGroupAuthoritiesMapDao().saveAndFlush(newGam);
						});
					}
				}
			}
		});

	}
	
	private TsmpVgroup checkDataAndUpdateTables(TsmpAuthorization auth, AA0224Req req, String locale, InnerInvokeParam iip) throws Exception { 
		Integer allowDays = getAllowDaysFormat(req, locale);
		TsmpVgroup vgroup = updateTsmpVgroup(auth, req, allowDays);
		new OAuthUtil().setOauthClientDetailsDao(getOauthClientDetailsDao());
		new OAuthUtil().setDgrAuditLogService(getDgrAuditLogService());
		
		checkAuthoritieIdsAndUpdateVgroupAuthoritiesMap(req);
		
		checkApiKeyIsExistedAndUpdateRelatedTable(auth, req, allowDays, vgroup, iip);
		
		return vgroup;
	}
	
	private String updateTsmpGroup(TsmpAuthorization auth, AA0224Req req, Integer allowDays, TsmpVgroup vgroup) throws Exception {
		TsmpGroup group = new TsmpGroup();
		String groupId = getId(TsmpSequenceName.SEQ_TSMP_GROUP_PK);
		group.setGroupId(groupId);
		group.setGroupName(getRandomGroupName());
		group.setCreateTime(DateTimeUtil.now());
		group.setCreateUser(auth.getUserName());
		
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		group.setGroupAlias("A0224Alias"+date);
		group.setGroupDesc("A0224Desc"+date);
		group.setGroupAccess("[]");
		group.setSecurityLevelId(req.getNewSecurityLevelId());
		group.setAllowDays(allowDays);
		group.setAllowTimes(req.getNewAllowTimes());
		group.setVgroupFlag("1");
		group.setVgroupId(req.getVgroupId());
		group.setVgroupName(req.getNewVgroupName());
		getTsmpGroupDao().saveAndFlush(group);
		
		return groupId;
	}
	
	private String getId(TsmpSequenceName seqName) throws Exception {
		String seqId = "";
		
		Long id = getSeqStoreService().nextTsmpSequence(seqName);
		seqId = id+"";
		if (id != null) {
			seqId = id.toString();
		}
		if(StringUtils.isEmpty(seqId)) {
			logger.debug("Get "+ seqName + " error");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return seqId;
	}
	
	private String getRandomGroupName() {
		String randomId = null;		
		UUID uuid = UUID.randomUUID();
		randomId = Long.toString(uuid.getMostSignificantBits(), Character.MAX_RADIX) +
				Long.toString(uuid.getLeastSignificantBits(), Character.MAX_RADIX);
		
		return (randomId.length() > 30 ) ?  randomId.substring(0, 30) : randomId;
		
	}
	
	private Integer getAllowDaysFormat(AA0224Req req, String locale) {
		Integer days = req.getNewAllowDays();
		if(days == null) {
			return 0;
		}
		String encodeTimeUnit =  req.getNewTimeUnit();
		
		String deodeTimeUnit = nvl(getTimeUnitByBcryptParamHelper(encodeTimeUnit, locale));
	
		if(TsmpDpTimeUnit.MINUTE.value().equalsIgnoreCase(deodeTimeUnit)) {
			days = days * 60;
		}else if(TsmpDpTimeUnit.HOUR.value().equalsIgnoreCase(deodeTimeUnit)) {
			days = days * 60 * 60;
		}else if(TsmpDpTimeUnit.DAY.value().equalsIgnoreCase(deodeTimeUnit)) {
			days = days * 60 * 60 * 24;
		}
		
		return days;
	}
	
	private String getTimeUnitByBcryptParamHelper(String encodeTimeUnit, String locale) {
		String decodeTimeUnit = null;
		try {
			decodeTimeUnit = getBcryptParamHelper().decode(encodeTimeUnit, "TIME_UNIT", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decodeTimeUnit;
	}
	
	private String updateTsmpGroupApi(String groupId, String api, String moduleName) {
		TsmpGroupApi groupApi = new TsmpGroupApi();
		groupApi.setGroupId(groupId);
		groupApi.setApiKey(api);
		groupApi.setModuleName(moduleName);
		groupApi.setModuleVer("0");
		groupApi.setCreateTime(DateTimeUtil.now());
		getTsmpGroupApiDao().saveAndFlush(groupApi);
		return groupId;
	}

	private String updateTsmpGroupAuthoritiesMap(AA0224Req req, String groupId) {
		List<String> authoritiesIdList = req.getNewVgroupAuthoritieIds();
		if(authoritiesIdList != null) {
			authoritiesIdList.forEach((authoritiesId)->{
				TsmpGroupAuthoritiesMap gam = new TsmpGroupAuthoritiesMap();
				gam.setGroupAuthoritieId(authoritiesId);
				gam.setGroupId(groupId);
				getTsmpGroupAuthoritiesMapDao().saveAndFlush(gam);
			});
		}
		return groupId;
	}
	
	private TsmpVgroup updateTsmpVgroup(TsmpAuthorization auth, AA0224Req req, Integer allowDays) {
		TsmpVgroup vgroup = null;
		String vgroupId = nvl(req.getVgroupId());
		String vgroupOriName = nvl(req.getOriVgroupName());
		Integer oriAllowDays = req.getOriAllowDays();	//原始資料-允許使用時間
		Integer oriAllowTimes = req.getOriAllowTimes();	//原始資料-授權次數上限
		
		List<TsmpVgroup> vgroupList = getTsmpVgroupDao().findByVgroupIdAndVgroupName(vgroupId, vgroupOriName);
		if(vgroupList == null || vgroupList.size() == 0) {
			logger.debug("updateTsmpVgroup vgroup isEmpty !");
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		if(vgroupList != null && vgroupList.size() > 0) {
			vgroup = vgroupList.get(0);
			if(oriAllowDays == null || oriAllowTimes == null
					|| allowDays.intValue() != oriAllowDays.intValue() 
					|| req.getOriAllowTimes().intValue() != req.getNewAllowTimes().intValue() 
					|| !nvl(req.getOriSecurityLevelId()).equals(nvl(req.getNewSecurityLevelId())) 
					|| !nvl(req.getOriVgroupAlias()).equals(nvl(req.getNewVgroupAlias())) 
					|| !nvl(req.getOriVgroupDesc()).equals(nvl(req.getNewVgroupDesc()))
					|| !nvl(req.getOriVgroupName()).equals(nvl(req.getNewVgroupName()))
			){
				vgroup.setVgroupId(vgroupId);
				vgroup.setVgroupName(nvl(req.getNewVgroupName()));
				vgroup.setVgroupAlias(nvl(req.getNewVgroupAlias()));
				vgroup.setVgroupDesc(nvl(req.getNewVgroupDesc()));
				vgroup.setSecurityLevelId(req.getNewSecurityLevelId());
				vgroup.setAllowTimes(req.getNewAllowTimes());
				vgroup.setAllowDays(allowDays);
				
				vgroup.setUpdateTime(DateTimeUtil.now());
				vgroup.setUpdateUser(auth.getUserName());
				vgroup = getTsmpVgroupDao().saveAndFlush(vgroup);
			}
			
		}
		
		return vgroup;
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
    	List<R> list = new ArrayList<>(listA.stream().collect(Collectors.toList()));
        Collections.copy(list, listA);
        list.removeAll(listB);
        return list;
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
    	List<R> list = new ArrayList<>(listA.stream().collect(Collectors.toList()));
        Collections.copy(list, listA);
        list.retainAll(listB);
        return list;
    }
	
	private void updateVgroupAuthoritiesMap(String vgroupAuthoritieId, String vgroupId) {
		TsmpVgroupAuthoritiesMap vgaMap = new TsmpVgroupAuthoritiesMap();
		vgaMap.setVgroupAuthoritieId(vgroupAuthoritieId);
		vgaMap.setVgroupId(vgroupId);
		getTsmpVgroupAuthoritiesMapDao().saveAndFlush(vgaMap);
	}
	
	private void updateTsmpVgroupGroup(String groupId, String vgroupId) {
		TsmpVgroupGroup vg = new TsmpVgroupGroup();
		vg.setGroupId(groupId);
		vg.setVgroupId(vgroupId);
		vg.setCreateTime(DateTimeUtil.now());
		getTsmpVgroupGroupDao().saveAndFlush(vg);
	}

	private void updateTsmpClientGroup(String clientId, String groupId) {
		TsmpClientGroup cg = new TsmpClientGroup();
		cg.setClientId(clientId);
		cg.setGroupId(groupId);
		getTsmpClientGroupDao().saveAndFlush(cg);
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupAPiDao;
	}
	
	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return this.tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpSecurityLevelCacheProxy getSecurityLevelCacheProxy() {
		return tsmpSecurityLevelCacheProxy;
	}
	
	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return this.tsmpVgroupDao;
	}
	
	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return this.tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpGroupAuthoritiesCacheProxy getTsmpGroupAuthoritiesCacheProxy() {
		return this.tsmpGroupAuthoritiesCacheProxy;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return this.tsmpVgroupGroupDao;
	}
	
	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return this.tsmpClientVgroupDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return this.tsmpClientGroupDao;
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return this.oauthClientDetailsDao;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}
