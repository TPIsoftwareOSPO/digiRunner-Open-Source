package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
import tpi.dgrv4.dpaa.vo.AA0221Item;
import tpi.dgrv4.dpaa.vo.AA0221Req;
import tpi.dgrv4.dpaa.vo.AA0221Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.TsmpVgroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0221Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpUserDao tsmpUserDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpSecurityLevelCacheProxy securityLevelCacheProxy;
	private TsmpGroupApiDao tsmpGroupAPiDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy;
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private SeqStoreService seqStoreService;
	private BcryptParamHelper bcryptParamHelper;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	
	@Autowired
	public AA0221Service(TsmpUserDao tsmpUserDao, TsmpApiDao tsmpApiDao,
			TsmpSecurityLevelCacheProxy securityLevelCacheProxy, TsmpGroupApiDao tsmpGroupAPiDao,
			TsmpVgroupGroupDao tsmpVgroupGroupDao, TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao,
			TsmpVgroupDao tsmpVgroupDao, TsmpGroupDao tsmpGroupDao,
			TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao,
			TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy, TsmpRtnCodeDao tsmpRtnCodeDao,
			SeqStoreService seqStoreService, BcryptParamHelper bcryptParamHelper, DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpApiDao = tsmpApiDao;
		this.securityLevelCacheProxy = securityLevelCacheProxy;
		this.tsmpGroupAPiDao = tsmpGroupAPiDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpVgroupAuthoritiesMapDao = tsmpVgroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesCacheProxy = tsmpGroupAuthoritiesCacheProxy;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.seqStoreService = seqStoreService;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	@Transactional
	public AA0221Resp createVGroup(TsmpAuthorization auth, AA0221Req req, ReqHeader reqHeader) {
		AA0221Resp resp = new AA0221Resp();
		TsmpVgroup vgroup = null;
		try {
			checkParams(auth, req);
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			vgroup = checkDataAndUpdateTables(auth, req, locale);
 
			resp.setVgroupId(vgroup.getVgroupId());

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
		
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();	// 1220:儲存失敗，資料長度過大
			} else {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		return resp;
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
	
	private void checkParams(TsmpAuthorization auth, AA0221Req req) throws Exception {
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		
		if (!StringUtils.hasLength(userNameForQuery)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
		
		// 1231:使用者不存在
		checkUserExists(userNameForQuery, idPType);
			
		// 1273:組織單位ID:必填參數
		String orgId = auth.getOrgId();
		if(!StringUtils.hasLength(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}
		
		// 1353:[{{0}}] 已存在: {{1}}
		checkVgroupIsExisted(req);
		
		// 1364:安全等級不存在
		checkSecurityLevel(req);
		
	}
	
	/**
	 * 	1. 輸入的 虛擬群組代碼 不可在 TSMP_VGROUP 中重複, 否則 throw 1353。 ({{vgroupName}} 已存在: {{1}})				
	 *	2. 若有輸入 虛擬群組名稱 不可在 TSMP_VGROUP 中重複, 否則 throw 1353。 ({{vgroupAlias}} 已存在: {{1}})	
	 *	
	 * @param req
	 * @throws Exception
	 */
	private void checkVgroupIsExisted(AA0221Req req) throws Exception {
		String vgroupName = nvl(req.getVgroupName());
		String vgroupAlias = nvl(req.getVgroupAlias());
		List<TsmpVgroup> vgroupNameList = getTsmpVgroupDao().findByVgroupName(vgroupName);
		List<TsmpVgroup> vgroupAliasList = getTsmpVgroupDao().findByVgroupAlias(vgroupAlias);
		
		if(vgroupNameList != null && vgroupNameList.size() > 0) {
			throw TsmpDpAaRtnCode._1353.throwing("{{vgroupName}}", vgroupName);
		}
		
		if(!StringUtils.isEmpty(vgroupAlias) && vgroupAliasList != null && vgroupAliasList.size() > 0) {
			throw TsmpDpAaRtnCode._1353.throwing("{{vgroupAlias}}", vgroupAlias);
		}
	}
	
	/**
	 * 檢查傳入的 securityLevelId 是否存在 TSMP_SECURITY_LEVEL 中, 否則 throw 1364:安全等級不存在。(使用DaoCacheService)
	 * 				
	 * @param req
	 * @throws Exception
	 */
	private void checkSecurityLevel(AA0221Req req) throws Exception {
		// 1364:安全等級不存在
		String securityLevelId = req.getSecurityLevelId();
		if(!StringUtils.isEmpty(securityLevelId)) {
			TsmpSecurityLevel securityLV = getSecurityLVById(securityLevelId);
			
			if(securityLV == null) {
				throw TsmpDpAaRtnCode._1364.throwing();
			}
		}
	}
	
	
	/**
	 * 1.檢查傳入的每個 vgroupAuthoritiesIds 是否都存在 TSMP_GROUP_AUTHORITIES 中, 否則 throw 1397:授權核身種類:[{{0}}]不存在。				
	 * <br>
	 * 2.寫入<b>TSMP_GROUP_AUTHORITIES_MAP</b>
	 * @param req
	 * @throws Exception
	 */
	private void checkAuthoritieIdsAndUpdateVgroupAuthoritiesMap(AA0221Req req, TsmpVgroup vgroup) {
		// 1397:授權核身種類:[{{0}}]不存在
		List<String> authoritiesIdList = req.getVgroupAuthoritieIds();
		
		String vgroupId = vgroup.getVgroupId();
		if(authoritiesIdList != null) {
			authoritiesIdList.forEach((authoritiesId)->{
				TsmpGroupAuthorities tsmpGroupAuthorities =	getAuthoritiesyId(authoritiesId);
				if(tsmpGroupAuthorities == null) {
					throw TsmpDpAaRtnCode._1397.throwing(authoritiesId);
				}
				
				updateVgroupAuthoritiesMap(authoritiesId, vgroupId);
				
			});
		}
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
	 * 1.檢查傳入的每一個 API Key (apiKey + moduleName) 是否存在 TSMP_API 中, 否則 throw 1400:API: [{{0}}]不存在
	 * <br>
	 * 2.每個虛擬群組不能加入超過 205 支 API。
	 * <br>
	 * 3.寫入<b>TSMP_GROUP、TSMP_GROUP_API、TSMP_GROUP_AUTHORITIES_MAP</b>
	 * 4.寫入<b> TSMP_VGROUP_GROUP</b>
	 * @param req
	 * @throws Exception
	 */
	private void checkApiKeyIsExistedAndUpdateRelatedTable(TsmpAuthorization auth, AA0221Req req, 
			Integer allowDays,  TsmpVgroup vgroup) throws Exception {
		
		int i =0;
		List<AA0221Item> aa0221ItemList = req.getDataList();
		String vgroupId = vgroup.getVgroupId();
		if(aa0221ItemList != null) {
			for (AA0221Item item : aa0221ItemList) {
				List<String> apikeyList =  item.getApiKeyList();
				if(apikeyList != null) {
					i = i + apikeyList.size();
				}
			}
			
		}
		// 判斷 AA0221Item.apiKeyList 的總數若超過 205 則 throw 1402:虛擬群組的API數量上限為 [{{0}}]，您選擇 [{{1}}]
		if(i > 205) {
			throw TsmpDpAaRtnCode._1402.throwing("205", i+"");
		}
		
		if(aa0221ItemList != null) {
			for (AA0221Item item : aa0221ItemList) {
				List<String> apikeyList =  item.getApiKeyList();
				String moduleName = nvl(item.getModuleName());
				if(apikeyList != null) {
					for (String api : apikeyList) {
						TsmpApiId id = new TsmpApiId(api,moduleName);
						Optional<TsmpApi> optApi = getTsmpApiDao().findById(id);
						if(!optApi.isPresent()) {
							throw TsmpDpAaRtnCode._1400.throwing(api);	// 1400:API: [{{0}}]不存在
						}
//						i++;
						String groupId = updateTsmpGroup(auth, req, allowDays, vgroup);
						updateTsmpGroupApi(groupId, api, moduleName);
						updateTsmpGroupAuthoritiesMap(req, groupId);
						updateTsmpVgroupGroup(groupId, vgroupId);
					}
				}
			}
			
		}
		
		
	}
	
	private TsmpVgroup checkDataAndUpdateTables(TsmpAuthorization auth, AA0221Req req, String locale) throws Exception { 
		Integer allowDays = getAllowDaysFormat(req, locale);
		TsmpVgroup vgroup = updateTsmpVgroup(auth, req, allowDays);
		
		checkAuthoritieIdsAndUpdateVgroupAuthoritiesMap(req, vgroup);
		
		checkApiKeyIsExistedAndUpdateRelatedTable(auth, req, allowDays, vgroup);
		
		return vgroup;
	}
	
	private String updateTsmpGroup(TsmpAuthorization auth, AA0221Req req, Integer allowDays, TsmpVgroup vgroup) throws Exception {
		TsmpGroup aa0221_group = new TsmpGroup();
		String groupId = getId(TsmpSequenceName.SEQ_TSMP_GROUP_PK);
		aa0221_group.setGroupId(groupId);
		aa0221_group.setGroupName(getRandomGroupName());
		aa0221_group.setCreateTime(DateTimeUtil.now());
		aa0221_group.setCreateUser(auth.getUserName());
		
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		aa0221_group.setGroupAlias("A0221Alias"+date);
		aa0221_group.setGroupDesc("A0221Desc"+date);
		aa0221_group.setGroupAccess("[]");
		aa0221_group.setSecurityLevelId(req.getSecurityLevelId());
		aa0221_group.setAllowDays(allowDays);
		aa0221_group.setAllowTimes(req.getAllowTimes());
		aa0221_group.setVgroupFlag("1");
		aa0221_group.setVgroupId(vgroup.getVgroupId());
		aa0221_group.setVgroupName(vgroup.getVgroupName());
		getTsmpGroupDao().saveAndFlush(aa0221_group);
		
		return groupId;
	}
	
	private String getId(TsmpSequenceName seqName) throws Exception {
		String aa0221_seqId = "";
		
		Long id = getSeqStoreService().nextTsmpSequence(seqName);
		aa0221_seqId = id+"";
		if (id != null) {
			aa0221_seqId = id.toString();
		}
		if(StringUtils.isEmpty(aa0221_seqId)) {
			logger.debug("Get "+ seqName + " error");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return aa0221_seqId;
	}
	
	private String getRandomGroupName() {
		String aa0221_randomId = null;		
		UUID aa0221_uuid = UUID.randomUUID();
		aa0221_randomId = Long.toString(aa0221_uuid.getMostSignificantBits(), Character.MAX_RADIX) +
				Long.toString(aa0221_uuid.getLeastSignificantBits(), Character.MAX_RADIX);
		
		return (aa0221_randomId.length() > 30 ) ?  aa0221_randomId.substring(0, 30) : aa0221_randomId;
		
	}
	
	private Integer getAllowDaysFormat(AA0221Req req, String locale) {
		Integer days = req.getAllowDays();
		if(days == null) {
			return 0;
		}
		String encodeTimeUnit =  req.getTimeUnit();
		
		String aa0221_deodeTimeUnit = nvl(getTimeUnitByBcryptParamHelper(encodeTimeUnit, locale));
	
		if(TsmpDpTimeUnit.MINUTE.value().equalsIgnoreCase(aa0221_deodeTimeUnit)) {
			days = days * 60;
		}else if(TsmpDpTimeUnit.HOUR.value().equalsIgnoreCase(aa0221_deodeTimeUnit)) {
			days = days * 60 * 60;
		}else if(TsmpDpTimeUnit.DAY.value().equalsIgnoreCase(aa0221_deodeTimeUnit)) {
			days = days * 60 * 60 * 24;
		}
		
		return days;
	}
	
	private String getTimeUnitByBcryptParamHelper(String encodeTimeUnit, String locale) {
		String aa0221_decodeTimeUnit = null;
		try {
			aa0221_decodeTimeUnit = getBcryptParamHelper().decode(encodeTimeUnit, "TIME_UNIT", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return aa0221_decodeTimeUnit;
	}
	
	private String updateTsmpGroupApi(String groupId, String api, String moduleName) {
		TsmpGroupApi aa0221_groupApi = new TsmpGroupApi();
		aa0221_groupApi.setGroupId(groupId);
		aa0221_groupApi.setApiKey(api);
		aa0221_groupApi.setModuleName(moduleName);
		aa0221_groupApi.setModuleVer("0");
		aa0221_groupApi.setCreateTime(DateTimeUtil.now());
		getTsmpGroupApiDao().saveAndFlush(aa0221_groupApi);
		return groupId;
	}

	private String updateTsmpGroupAuthoritiesMap(AA0221Req req, String groupId) {
		List<String> authoritiesIdList = req.getVgroupAuthoritieIds();
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
	
	private TsmpVgroup updateTsmpVgroup(TsmpAuthorization auth, AA0221Req req, Integer allowDays) throws Exception {
		TsmpVgroup vgroup = new TsmpVgroup();
		
		String vgroupId = getId(TsmpSequenceName.SEQ_TSMP_VGROUP_PK);
		vgroup.setVgroupId(vgroupId);
		vgroup.setVgroupName(req.getVgroupName());
		vgroup.setCreateTime(DateTimeUtil.now());
		vgroup.setCreateUser(auth.getUserName());
		vgroup.setVgroupAlias(req.getVgroupAlias());
		vgroup.setVgroupDesc(req.getVgroupDesc());
		vgroup.setVgroupAccess("[]");
		vgroup.setSecurityLevelId(req.getSecurityLevelId());
		vgroup.setAllowDays(allowDays);
		vgroup.setAllowTimes(req.getAllowTimes());
		getTsmpVgroupDao().saveAndFlush(vgroup);

		return vgroup;
	}
	
	private void updateVgroupAuthoritiesMap(String vgroupAuthoritieId, String vgroupId) {
		TsmpVgroupAuthoritiesMap aa0221_vgaMap = new TsmpVgroupAuthoritiesMap();
		aa0221_vgaMap.setVgroupAuthoritieId(vgroupAuthoritieId);
		aa0221_vgaMap.setVgroupId(vgroupId);
		getTsmpVgroupAuthoritiesMapDao().saveAndFlush(aa0221_vgaMap);
	}
	
	private void updateTsmpVgroupGroup(String groupId, String vgroupId) {
		TsmpVgroupGroup aa0221_vg = new TsmpVgroupGroup();
		aa0221_vg.setGroupId(groupId);
		aa0221_vg.setVgroupId(vgroupId);
		aa0221_vg.setCreateTime(DateTimeUtil.now());
		getTsmpVgroupGroupDao().saveAndFlush(aa0221_vg);
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
		return this.securityLevelCacheProxy;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return this.tsmpVgroupDao;
	}
	
	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return this.tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}
	
	protected TsmpGroupAuthoritiesCacheProxy getTsmpGroupAuthoritiesCacheProxy() {
		return this.tsmpGroupAuthoritiesCacheProxy;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return this.tsmpVgroupGroupDao;
	}
	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}
