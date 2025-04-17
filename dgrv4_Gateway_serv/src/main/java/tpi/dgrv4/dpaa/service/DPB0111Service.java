package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0111Item;
import tpi.dgrv4.dpaa.vo.DPB0111Req;
import tpi.dgrv4.dpaa.vo.DPB0111Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpRoleCacheProxy;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0111Service {

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpRoleCacheProxy tsmpRoleCacheProxy;
	private TsmpRoleTxidMapDao tsmpRoleTxidMapDao;
	private BcryptParamHelper bcryptParamHelper;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0111Service(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpRoleCacheProxy tsmpRoleCacheProxy,
			TsmpRoleTxidMapDao tsmpRoleTxidMapDao, BcryptParamHelper bcryptParamHelper, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpRoleCacheProxy = tsmpRoleCacheProxy;
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.serviceConfig = serviceConfig;
	}

	public DPB0111Resp queryRTMapList(TsmpAuthorization auth, DPB0111Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());

		checkParams(req, locale);

		String p_roleId = req.getP_roleId();
		String p_listType = req.getP_listType();
		String[] keywords = getKeywords(req.getKeyword(), " ");
		String listType = req.getListType();
		List<TsmpRoleTxidMap> dataList = getTsmpRoleTxidMapDao().query_dpb0111Service_01(p_roleId, p_listType, //
			listType, keywords);
		if (dataList == null || dataList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		// 整理資料 (groupBy)
		Map<TsmpRole, Map<String, List<TsmpRoleTxidMap>>> mapping = groupByTsmpRoleAndListType(dataList);
		
		DPB0111Resp resp = new DPB0111Resp();
		List<DPB0111Item> dpb0111Items = getDPB0111Items(mapping, getPageSize(), reqHeader.getLocale());
		resp.setDataList(dpb0111Items);
		return resp;
	}

	public void checkParams(DPB0111Req req, String locale) {
		String listType = req.getListType();
		if (listType != null) {
			try {
				listType = getBcryptParamHelper().decode(listType, "RT_MAP_LIST_TYPE", locale);
				req.setListType(listType);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
	}

	public List<DPB0111Item> getDPB0111Items(Map<TsmpRole, Map<String, List<TsmpRoleTxidMap>>> maps, //
			Integer pageSize, String locale) {
		List<DPB0111Item> dpb0111Items = new ArrayList<>();
		
		TsmpRole tsmpRole = null;
		Map<String, List<TsmpRoleTxidMap>> innerData = null;
		DPB0111Item dpb0111Item = null;
		String roleId = null;
		String roleName = null;
		String roleAlias = null;
		String listType = null;
		String listTypeName = null;
		String oriTxId = null;
		String txId = null;
		Boolean isTxIdTruncated = null;
		for (Map.Entry<TsmpRole, Map<String, List<TsmpRoleTxidMap>>> entry : maps.entrySet()) {
			tsmpRole = entry.getKey();
			innerData = entry.getValue();
			for (Map.Entry<String, List<TsmpRoleTxidMap>> innerEntry : innerData.entrySet()) {
				dpb0111Item = new DPB0111Item();

				// 角色
				roleId = new String();
				roleName = new String();
				roleAlias = new String();
				if (tsmpRole != null) {
					roleId = tsmpRole.getRoleId();
					roleName = tsmpRole.getRoleName();
					roleAlias = nvl(tsmpRole.getRoleAlias());
				}
				dpb0111Item.setRoleId(roleId);
				dpb0111Item.setRoleName(roleName);
				dpb0111Item.setRoleAlias(roleAlias);
				// 名單類型
				listType = nvl(innerEntry.getKey());
				listTypeName = getListTypeName(listType, locale);
				dpb0111Item.setListType(listType);
				dpb0111Item.setListTypeName(listTypeName);
				// 交易代碼
				oriTxId = getOriTxId(innerEntry.getValue());
				txId = truncateTxId(oriTxId);
				isTxIdTruncated = !txId.equals(oriTxId);
				dpb0111Item.setOriTxId(oriTxId);
				dpb0111Item.setTxId(txId);
				dpb0111Item.setIsTxIdTruncated(isTxIdTruncated);
				
				dpb0111Items.add(dpb0111Item);
			}
		}
		
		// 資料排序: roleId asc, listType asc (因為從資料庫撈出來後有再整理過資料, 所以要重新排序)
		Collections.sort(dpb0111Items, (i1, i2) -> {
			int c = i1.getRoleId().compareTo(i2.getRoleId());
			if (c == 0) {
				return i1.getListType().compareTo(i2.getListType());
			}
			return c;
		});
		
		// 分頁
		if (dpb0111Items.size() > pageSize) {
			return dpb0111Items.subList(0,  pageSize);
		} else {
			return dpb0111Items;
		}
	}

	public String getOriTxId(List<TsmpRoleTxidMap> maps) {
		if (maps == null || maps.isEmpty()) {
			return new String();
		}
		return String.join(",", //
			maps.stream().map((m) -> {
				return m.getTxid();
			}).collect(Collectors.toList())
		);
	}

	/**
	 * 將資料整理成: Map<TsmpRole, Map<listType, List<TsmpRoleTxidMap>>
	 * @param dataList
	 * @return
	 */
	public Map<TsmpRole, Map<String, List<TsmpRoleTxidMap>>> groupByTsmpRoleAndListType(List<TsmpRoleTxidMap> dataList) {
		Map<TsmpRole, Map<String, List<TsmpRoleTxidMap>>> mapping = new HashMap<>();
		
		if (dataList == null || dataList.isEmpty()) return mapping;
		
		TsmpRole tsmpRole = null;
		String listType = null;
		Map<String, List<TsmpRoleTxidMap>> innerMapping = null;
		List<TsmpRoleTxidMap> innerList = null;
		for (TsmpRoleTxidMap data : dataList) {
			tsmpRole = getTsmpRoleById(data.getRoleId());
			if (tsmpRole == null) {
				// 角色不存在
				throw TsmpDpAaRtnCode._1230.throwing();
			}
			listType = data.getListType();

			innerMapping = mapping.get(tsmpRole) == null ? new HashMap<>() : mapping.get(tsmpRole);
			innerList = innerMapping.get(listType) == null ? new ArrayList<>() : innerMapping.get(listType);
			innerList.add(data);
			innerMapping.put(listType, innerList);
			mapping.put(tsmpRole, innerMapping);
		}
		
		return mapping;
	}

	public String truncateTxId(String txId) {
		if (StringUtils.isEmpty(txId)) return new String();
		if (txId.length() > 30) {
			return txId.substring(0, 30) + "...";
		}
		return txId;
	}

	public String getListTypeName(String listType, String locale) {
		if (StringUtils.isEmpty(listType)) return new String();
		TsmpDpItems items = getItemsById("RT_MAP_LIST_TYPE", listType, locale);
		if (items == null) return new String();
		return items.getSubitemName();
	}

	/**
	 * Using Cache
	 * @param roleId
	 * @return
	 */
	protected TsmpRole getTsmpRoleById(String roleId) {
		return getTsmpRoleCacheProxy().findById(roleId);
	}

	/**
	 * Using Cache
	 * @param itemNo
	 * @param subitemNo
	 * @return
	 */
	protected TsmpDpItems getItemsById(String itemNo, String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		return i;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpRoleCacheProxy getTsmpRoleCacheProxy() {
		return this.tsmpRoleCacheProxy;
	}

	protected TsmpRoleTxidMapDao getTsmpRoleTxidMapDao() {
		return this.tsmpRoleTxidMapDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0111");
		return this.pageSize;
	}

}