package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0064Items;
import tpi.dgrv4.dpaa.vo.DPB0064Req;
import tpi.dgrv4.dpaa.vo.DPB0064Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayer;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0064Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpChkLayerDao tsmpDpChkLayerDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired 
	public DPB0064Service(TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpRoleDao tsmpRoleDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpChkLayerDao = tsmpDpChkLayerDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0064Resp queryAllLayer(TsmpAuthorization authorization, DPB0064Req req, ReqHeader reqHeader) {
		DPB0064Resp resp = new DPB0064Resp();
		resp.setDataMap(new HashMap<>());
		resp.setTypeMap(new HashMap<>());
		
		try {
			// 找出所有 REVIEW_TYPE
			List<TsmpDpItems> reviewTypes = getTsmpDpItemsCacheProxy().findByItemNoAndLocale(TsmpDpReqReviewType.ITEM_NO, reqHeader.getLocale());
			if (reviewTypes == null || reviewTypes.isEmpty()) {
				this.logger.error("沒有設定任何簽核類型(TSMP_DP_ITEMS)");
				throw TsmpDpAaRtnCode._1211.throwing();
			}
			
			String reviewType = null;
			String reviewTypeName = null;
			for (TsmpDpItems tsmpDpItems : reviewTypes) {
				reviewType = tsmpDpItems.getSubitemNo();
				reviewTypeName = tsmpDpItems.getSubitemName();
				
				// 設定關卡層數資料
				resp.getDataMap().put(reviewType, new HashMap<>());
				setLayersMapping(reviewType, resp.getDataMap().get(reviewType));
				// 設定簽核類型對應
				resp.getTypeMap().put(reviewType, reviewTypeName);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}

	private void setLayersMapping(String reviewType, Map<Integer, List<DPB0064Items>> layersMapping) {
		List<TsmpDpChkLayer> chkLayers = getTsmpDpChkLayerDao().findByReviewTypeAndStatus( //
			reviewType, TsmpDpDataStatus.ON.value());
		if(chkLayers == null || chkLayers.isEmpty()) {
			this.logger.error(String.format("簽核類型: %s, 未設定關卡簽核角色", reviewType));
			return;
		}

		Integer layer = null;
		List<DPB0064Items> dpb0064ItemsList = null;
		DPB0064Items dpb0064Items = null;
		for (TsmpDpChkLayer c : chkLayers) {
			layer = c.getLayer();
			dpb0064Items = getDPB0064Items(c);
			dpb0064ItemsList = layersMapping.get(layer) == null ? new ArrayList<>() : layersMapping.get(layer);
			dpb0064ItemsList.add(dpb0064Items);
			layersMapping.put(layer, dpb0064ItemsList);
		}
	}

	private DPB0064Items getDPB0064Items(TsmpDpChkLayer c) {
		String roleName = getRoleName(c.getRoleId());
		
		DPB0064Items item = new DPB0064Items();
		item.setChkLayerId(c.getChkLayerId());
		item.setReviewType(c.getReviewType());
		item.setLayer(c.getLayer());
		item.setRoleId(c.getRoleId());
		item.setStatus(c.getStatus());
		item.setLv(c.getVersion());
		item.setRoleName(roleName);
		
		return item;
	}

	// 取角色
	private String getRoleName(String roleId) {
		Optional<TsmpRole> opt = getTsmpRoleDao().findById(roleId);
		if(opt.isPresent()) {
			return opt.get().getRoleName();
		}
		return new String();
	}
	
	/* 2020/06/30; Kim; 應該要看 tsmp_dp_items 有設定哪些 REVIEW_TYPE, 就要能讓前端設定那些關卡的審核人員
	public DPB0064Resp queryAllLayer(TsmpAuthorization authorization, DPB0064Req req) {
		DPB0064Resp resp = new DPB0064Resp();
		
		try {
			//查詢 status = 啟用
			List<TsmpDpChkLayer> layerList = getTsmpDpChkLayerDao().findByStatus(TsmpDpDataStatus.ON.value());
			if(layerList == null || layerList.isEmpty()) {
				throw TsmpDpAaRtnCode._1211.throwing();
			}
			
			for (TsmpDpChkLayer c : layerList) {
				String roleId = c.getRoleId();
				String reviewType = c.getReviewType();
				
				// 取角色
				Optional<TsmpRole> opt = getTsmpRoleDao().findById(roleId);
				String roleName = null;
				if(opt.isPresent()) {
					TsmpRole org = opt.get();
					roleName = org.getRoleName();
				}
				
				// 取類型
				String itemName = null;
				List<TsmpDpItems> itemList = getTsmpDpItemsDao().findByItemNo(reviewType);
				if(itemList != null && !itemList.isEmpty()) {
					TsmpDpItems item = itemList.get(0);
					itemName = item.getItemName();
				}
				resp = getResp(resp, c, roleName, itemName);
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp; 
	}
	
	private DPB0064Resp getResp(DPB0064Resp resp, TsmpDpChkLayer c, String roleName, String itemName) {
		
		String reviewType = c.getReviewType();
		Integer layer = c.getLayer();
		
		DPB0064Items item = new DPB0064Items();
		item.setChkLayerId(c.getChkLayerId());
		item.setReviewType(reviewType);
		item.setLayer(layer);
		item.setRoleId(c.getRoleId());
		item.setStatus(c.getStatus());
		item.setLv(c.getVersion());
		item.setRoleName(roleName);
		
		// Map<reviewType:String, Map<layer:Integer, List<DPB0064Items>>>
		Map<String, Map<Integer, List<DPB0064Items>>> dataMap = resp.getDataMap() == null ? //
				new HashMap<String, Map<Integer, List<DPB0064Items>>>() : resp.getDataMap();
		resp.setDataMap(dataMap);
		
		Map<Integer, List<DPB0064Items>> layerMap = dataMap.get(reviewType) == null ? //
				new HashMap<Integer, List<DPB0064Items>>() : dataMap.get(reviewType);
		dataMap.put(reviewType, layerMap);	
		
		List<DPB0064Items> itemList = layerMap.get(layer) == null ? //
				new ArrayList<DPB0064Items>() : layerMap.get(layer);
		layerMap.put(layer, itemList);
		itemList.add(item);
				
		// Map<itemNo:String, itemName:String>
		Map<String, String> typeMap = resp.getTypeMap() == null ? //
				new HashMap<String, String>() : resp.getTypeMap();
		resp.setTypeMap(typeMap);
		
		typeMap.put(reviewType, itemName);
		
		return resp;
	}
	*/
	
	protected TsmpDpChkLayerDao getTsmpDpChkLayerDao() {
		return this.tsmpDpChkLayerDao;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
}
