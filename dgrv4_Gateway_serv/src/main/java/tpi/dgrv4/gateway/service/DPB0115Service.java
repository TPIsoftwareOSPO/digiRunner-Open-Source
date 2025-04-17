package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpRoleTxidMapListType;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.gateway.component.cache.proxy.AuthoritiesCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpRoleTxidMapCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.DPB0115Item;
import tpi.dgrv4.gateway.vo.DPB0115Req;
import tpi.dgrv4.gateway.vo.DPB0115Resp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0115Service {

	private TPILogger logger;
	
	private AuthoritiesCacheProxy authoritiesCacheProxy;
	private TsmpRoleTxidMapCacheProxy tsmpRoleTxidMapCacheProxy;
	
	@Autowired
	public DPB0115Service(AuthoritiesCacheProxy authoritiesCacheProxy,
			TsmpRoleTxidMapCacheProxy tsmpRoleTxidMapCacheProxy) {
		super();
		this.authoritiesCacheProxy = authoritiesCacheProxy;
		this.tsmpRoleTxidMapCacheProxy = tsmpRoleTxidMapCacheProxy;
	}

	public DPB0115Resp queryRTMapByUk(TsmpAuthorization auth, DPB0115Req req) {
		checkParams(auth, req);
		
		// 取得角色清單
		List<String> roleIdList = req.getRoleIdList();
		if (roleIdList == null || roleIdList.isEmpty()) {
			String userName = auth.getUserNameForQuery();
			roleIdList = getAuthoritiesByUserName(userName);
		}

		// 取得該角色所設定的對應檔
		List<TsmpRoleTxidMap> roleTxidMapping = getRoleTxidMapByRoleIdList(roleIdList);
		
		// 整理出要回傳的交易代碼清單: 如果沒有指定要查詢的交易代碼, 則會dump出設定檔既有資料
		List<String> txidList = getTxidList(roleTxidMapping, req.getTxIdList());

		// 依交易代碼清單判斷是否可用
		Map<String, Boolean> mappingResult = getMappingResult(txidList, roleTxidMapping);
		
		DPB0115Resp resp = new DPB0115Resp();
		List<DPB0115Item> dpb0115Items = getDPB0115Items(mappingResult);
		resp.setDataList(dpb0115Items);
		return resp;
	}

	public void checkParams(TsmpAuthorization auth, DPB0115Req req) {
		String userName = auth.getUserName();
		List<String> roleIdList = req.getRoleIdList();
		if (!StringUtils.hasLength(userName) && (roleIdList == null || roleIdList.isEmpty())) {
			throw DgrRtnCode._1296.throwing();
		}
	}

	/**
	 * * Using cache<br>
	 * 依照使用者名稱取得角色ID清單
	 * @param userName
	 * @return
	 */
	public List<String> getAuthoritiesByUserName(String userName) {
		if ("DGRK".equals(userName)) {
			return null;
		}
		
		List<Authorities> authorities = getAuthoritiesCacheProxy().findByUsername(userName);
		if (authorities == null || authorities.isEmpty()) {
			getLogger().debug("User [" + userName + "] has no roles");
			throw DgrRtnCode._1264.throwing();
		}
		return authorities.stream().map((a) -> {
			return a.getAuthority();	// role_id
		}).collect(Collectors.toList());
	}
	
	/**
	 * * Using cache<br>
	 * 使用角色ID取得交易代碼設定
	 * @param roleIdList
	 * @return
	 */
	public List<TsmpRoleTxidMap> getRoleTxidMapByRoleIdList(List<String> roleIdList) {
		if (roleIdList == null || roleIdList.isEmpty()) {
			return new ArrayList<>();
		}
		List<TsmpRoleTxidMap> roleTxidMapping = getTsmpRoleTxidMapCacheProxy().findByRoleIdIn(roleIdList);
		return roleTxidMapping;
	}

	/**
	 * 整理出要回傳的交易代碼清單
	 * @param roleTxidMapping
	 * @param txidList
	 * @return
	 */
	public List<String> getTxidList(List<TsmpRoleTxidMap> roleTxidMapping, List<String> txidList) {
		if (txidList == null || txidList.isEmpty()) {
			if (roleTxidMapping != null && !roleTxidMapping.isEmpty()) {
				Set<String> txidSet = new HashSet<>();
				for (TsmpRoleTxidMap m : roleTxidMapping) {
					txidSet.add(m.getTxid());
				}
				txidList = new ArrayList<>(txidSet);
			}
		}
		return txidList;
	}

	public Map<String, Boolean> getMappingResult(List<String> txidList, List<TsmpRoleTxidMap> roleTxidMapping) {
		if(CollectionUtils.isEmpty(txidList)) {
			return null;
		}
		
		Map<String, Boolean> mappingResult = new HashMap<>();
		Map<String, Object> analyzingResult = analyzeMapping(roleTxidMapping);	// 透過這個分析結果加快判斷速度
		Boolean available = null;
		for (String txid : txidList) {
			available = getAvailable(analyzingResult, txid);
			mappingResult.put(txid, available);
		}
		return mappingResult;
	}

	public Boolean getAvailable(Map<String, Object> analyzingResult, String txid) {
		// 如果設定檔是空的, 表示可以無條件使用該交易代碼
		if (a_hasNoMapping(analyzingResult)) {
			return Boolean.TRUE;
		}
		
		Boolean available = null;
		
		Boolean hasWhite = a_hasWhite(analyzingResult); // 白名單是否有資料
		Boolean hasBlack = a_hasBlack(analyzingResult); // 黑名單是否有資料
		Boolean inWhite = a_inWhite(analyzingResult, txid); // 在白名單中
		Boolean inBlack = a_inBlack(analyzingResult, txid); // 在黑名單中

		if (hasWhite) {
			if (inWhite) {
				available = Boolean.TRUE;
			} else {
				available = Boolean.FALSE;
			}
		} else if (hasBlack) {
			if (inBlack) {
				available = Boolean.FALSE;
			} else {
				available = Boolean.TRUE;
			}
		} else {
			available = Boolean.TRUE;
		}
		
		return available;
	}

	public Map<String, Object> analyzeMapping(List<TsmpRoleTxidMap> roleTxidMapping) {
		Map<String, Object> analyzingResult = new HashMap<>();
		Boolean hasNoMapping = Boolean.TRUE;
		Boolean hasWhite = Boolean.FALSE; // 白名單是否有資料
		Boolean hasBlack = Boolean.FALSE; // 黑名單是否有資料
		Set<String> whiteList = new HashSet<String>();
		Set<String> blackList = new HashSet<String>();

		if (roleTxidMapping != null && !roleTxidMapping.isEmpty()) {
			hasNoMapping = Boolean.FALSE;

			for (TsmpRoleTxidMap m : roleTxidMapping) {
				if (TsmpRoleTxidMapListType.WHITE_LIST.value().equals(m.getListType())) {
					hasWhite = Boolean.TRUE;
					whiteList.add(m.getTxid());
				} else if (TsmpRoleTxidMapListType.BLACK_LIST.value().equals(m.getListType())) {
					hasBlack = Boolean.TRUE;
					blackList.add(m.getTxid());
				}
			}
		}
		
		analyzingResult.put("hasNoMapping", hasNoMapping);
		analyzingResult.put("hasWhite", hasWhite);
		analyzingResult.put("hasBlack", hasBlack);
		analyzingResult.put("whiteList", whiteList);
		analyzingResult.put("blackList", blackList);
		return analyzingResult;
	}

	public Boolean a_hasNoMapping(Map<String, Object> analyzingResult) {
		return (Boolean) analyzingResult.get("hasNoMapping");
	}

	public Boolean a_hasWhite(Map<String, Object> analyzingResult) {
		return (Boolean) analyzingResult.get("hasWhite");
	}

	public Boolean a_hasBlack(Map<String, Object> analyzingResult) {
		return (Boolean) analyzingResult.get("hasBlack");
	}

	@SuppressWarnings("unchecked")
	public Boolean a_inWhite(Map<String, Object> analyzingResult, String txid) {
		Set<String> whiteList = (HashSet<String>) analyzingResult.get("whiteList");
		return whiteList.contains(txid);
	}

	@SuppressWarnings("unchecked")
	public Boolean a_inBlack(Map<String, Object> analyzingResult, String txid) {
		Set<String> blackList = (HashSet<String>) analyzingResult.get("blackList");
		return blackList.contains(txid);
	}

	public List<DPB0115Item> getDPB0115Items(Map<String, Boolean> mappingResult) {
		if (mappingResult == null || mappingResult.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<DPB0115Item> dpb0115Items = new ArrayList<>();
		DPB0115Item dpb0115Item = null;
		for (Map.Entry<String, Boolean> entry : mappingResult.entrySet()) {
			dpb0115Item = new DPB0115Item();
			dpb0115Item.setTxId(entry.getKey());
			dpb0115Item.setAvailable(entry.getValue());
			dpb0115Items.add(dpb0115Item);
		}
		return dpb0115Items;
	}

	protected AuthoritiesCacheProxy getAuthoritiesCacheProxy() {
		return this.authoritiesCacheProxy;
	}

	protected TsmpRoleTxidMapCacheProxy getTsmpRoleTxidMapCacheProxy() {
		return this.tsmpRoleTxidMapCacheProxy;
	}

	protected TPILogger getLogger() {
		return this.logger;
	}

}