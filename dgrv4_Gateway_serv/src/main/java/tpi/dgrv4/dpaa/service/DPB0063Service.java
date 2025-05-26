package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0063PkReq;
import tpi.dgrv4.dpaa.vo.DPB0063Req;
import tpi.dgrv4.dpaa.vo.DPB0063Resp;
import tpi.dgrv4.dpaa.vo.DPB0063SaveItem;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayer;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayerId;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0063Service {
	
	private TPILogger logger = TPILogger.tl;

	private SeqStoreService seqStoreService;
	private TsmpDpChkLayerDao tsmpDpChkLayerDao;
	
	@Autowired
	public DPB0063Service(SeqStoreService seqStoreService, TsmpDpChkLayerDao tsmpDpChkLayerDao) {
		super();
		this.seqStoreService = seqStoreService;
		this.tsmpDpChkLayerDao = tsmpDpChkLayerDao;
	}

	@Transactional
	public DPB0063Resp saveLayer(TsmpAuthorization authorization, DPB0063Req req) {
		DPB0063Resp resp = new DPB0063Resp();
		try {
			//check param
			Map<String, Map<Integer, List<DPB0063PkReq>>> dataMap = req.getDataMap();
			if(dataMap == null) {
				throw TsmpDpAaRtnCode._1210.throwing();
			}
			
			String userName = authorization.getUserName();
			
			//1.取得Table中所有資料id
			Map<String, TsmpDpChkLayerId> idMap = getAllDataIdMap();
			
			//2.把Reuest的設為啟用,Create或Update進Table中
			resp = createOrUpdateData(dataMap, userName, idMap);
			
			//3.把在Table中,但不在Request中的資料改為停用
			updateStatue(idMap); 
			
		} catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private Map<String, TsmpDpChkLayerId> getAllDataIdMap() {
		Map<String, TsmpDpChkLayerId> dataMap = new HashMap<String, TsmpDpChkLayerId>();
		
		List<TsmpDpChkLayer> chkLayerList = getTsmpDpChkLayerDao().findAll();		
		for (TsmpDpChkLayer c : chkLayerList) {
			String reviewType = c.getReviewType();
			Integer layer  = c.getLayer();
			String roleId = c.getRoleId();
			String key = reviewType + "|" + layer + "|" + roleId;
			TsmpDpChkLayerId id = new TsmpDpChkLayerId(reviewType, layer, roleId);
			dataMap.put(key, id);
		}
		
		return dataMap;
	}
	
	private DPB0063Resp createOrUpdateData(Map<String, Map<Integer, List<DPB0063PkReq>>> dataMap, 
			String userName, Map<String, TsmpDpChkLayerId> idMap) {
		DPB0063Resp resp = new DPB0063Resp();
		Map<String, Map<Integer, List<DPB0063SaveItem>>> reviewTypeMap = new HashMap<>();  
		resp.setDataMap(reviewTypeMap);
		
		//Map<reviewType:String, Map<layer:Integer, List<DPB0063PkReq>>>		
		for (Map.Entry<String, Map<Integer, List<DPB0063PkReq>>> entry : dataMap.entrySet()) {
			String reviewType = entry.getKey();
			//Map<layer:Integer, List<DPB0063PkReq>
			Map<Integer, List<DPB0063PkReq>> pkReqMap = entry.getValue();			
			for (Map.Entry<Integer, List<DPB0063PkReq>> layerEntry : pkReqMap.entrySet()) {
				Integer layer = layerEntry.getKey();
				List<DPB0063PkReq> pkReqList = layerEntry.getValue();				
				for (DPB0063PkReq pkReq : pkReqList) {
					String roleId = pkReq.getRoleId();
					Long lv = pkReq.getLv();
					// 查詢
					String key = reviewType + "|" + layer + "|" + roleId;
					idMap.remove(key);//將已更新的移除
					
					TsmpDpChkLayerId id = new TsmpDpChkLayerId(reviewType, layer, roleId);
					Optional<TsmpDpChkLayer> opt = getTsmpDpChkLayerDao().findById(id);
					TsmpDpChkLayer c = null;
					
					if(opt.isPresent()) {//Update
						/* 在Table中已存在,若request傳來的lv如果是null,視為新增,直接"啟用",不處理雙欄位更新 */
						
						// 深層拷貝
						TsmpDpChkLayer orig = opt.get();						
						c = ServiceUtil.deepCopy(orig, TsmpDpChkLayer.class);
						
						c.setUpdateDateTime(DateTimeUtil.now());
						c.setUpdateUser(userName);
						if(lv != null) {
							c.setVersion(lv);
						}
						c.setStatus(TsmpDpDataStatus.ON.value());//啟用
						try {
							c = getTsmpDpChkLayerDao().saveAndFlush(c);
						} catch (ObjectOptimisticLockingFailureException e) {
							throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
						}
						resp = getResp(resp, c);//只放入啟用的資料
						
					}else {//Create
						c = new TsmpDpChkLayer();
						Long chkLayerId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_DP_CHK_LAYER);
						c.setChkLayerId(chkLayerId);
						c.setReviewType(reviewType);
						c.setLayer(layer);
						c.setRoleId(roleId);
						c.setCreateDateTime(DateTimeUtil.now());
						c.setCreateUser(userName);
						c.setStatus(TsmpDpDataStatus.ON.value());//啟用
						c = getTsmpDpChkLayerDao().saveAndFlush(c);					
						resp = getResp(resp, c);//只放入啟用的資料
					}
				}
			}
		}
	
		return resp;
	}
 
	private void updateStatue(Map<String, TsmpDpChkLayerId> idMap) {
		for (Map.Entry<String,TsmpDpChkLayerId> entry : idMap.entrySet()) {
			TsmpDpChkLayerId id = entry.getValue();
			Optional<TsmpDpChkLayer> opt = getTsmpDpChkLayerDao().findById(id);
			if(opt.isPresent()) {
				TsmpDpChkLayer c = opt.get();
				c.setStatus(TsmpDpDataStatus.OFF.value());//停用
				c = getTsmpDpChkLayerDao().saveAndFlush(c);
			}
		}
	}
	
	private DPB0063Resp getResp(DPB0063Resp resp, TsmpDpChkLayer c) {
		DPB0063SaveItem saveItem = new DPB0063SaveItem();
		saveItem.setChkLayerId(c.getChkLayerId());
		saveItem.setReviewType(c.getReviewType());
		saveItem.setLayer(c.getLayer());
		saveItem.setRoleId(c.getRoleId());
		saveItem.setStatus(c.getStatus());
		saveItem.setLv(c.getVersion());
		
		Map<String, Map<Integer, List<DPB0063SaveItem>>> reviewTypeMap = resp.getDataMap() == null ? //
				new HashMap<>() : resp.getDataMap();
		resp.setDataMap(reviewTypeMap);
		
		Map<Integer , List<DPB0063SaveItem>> layerMap = reviewTypeMap.get(c.getReviewType()) == null ? //
				new HashMap<Integer , List<DPB0063SaveItem>>() : reviewTypeMap.get(c.getReviewType());
		reviewTypeMap.put(c.getReviewType(), layerMap);
				
		List<DPB0063SaveItem> saveItemList = layerMap.get(c.getLayer()) == null ? //
				new ArrayList<DPB0063SaveItem>() : layerMap.get(c.getLayer());
		layerMap.put(c.getLayer(), saveItemList);		
		saveItemList.add(saveItem);
		
		return resp;
	}

	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}
	
	protected TsmpDpChkLayerDao getTsmpDpChkLayerDao() {
		return this.tsmpDpChkLayerDao;
	}
}
