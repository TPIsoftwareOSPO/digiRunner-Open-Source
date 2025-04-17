package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0113Req;
import tpi.dgrv4.dpaa.vo.DPB0113Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpRoleCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
@Transactional
public class DPB0113Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRoleCacheProxy tsmpRoleCacheProxy;
	private TsmpRoleTxidMapDao tsmpRoleTxidMapDao;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB0113Service(TsmpRoleCacheProxy tsmpRoleCacheProxy, TsmpRoleTxidMapDao tsmpRoleTxidMapDao,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRoleCacheProxy = tsmpRoleCacheProxy;
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0113Resp updateRTMap(TsmpAuthorization auth, DPB0113Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		String[] newTxIdAry = checkParams(auth, req, locale);
		return doUpdate(auth, req, newTxIdAry);
	}

	/**
	 * 檢查Request, 並回傳split後的新交易代碼清單
	 * @param auth
	 * @param req
	 * @return
	 */
	public String[] checkParams(TsmpAuthorization auth, DPB0113Req req, String locale) {
		List<String> oriTxIdList = req.getOriTxIdList();
		String roleId = req.getNewRoleId();
		String txId = req.getNewTxId();
		String listType = req.getNewListType();

		// 原始交易代碼清單必填
		if (oriTxIdList == null || oriTxIdList.isEmpty()) {
			throw TsmpDpAaRtnCode._1350.throwing("{{oriTxIdList}}");
		}

		// 角色ID必填, 長度限制10
		if (StringUtils.isEmpty(roleId)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{roleId}}");
		} else if (roleId.length() > 10) {
			throw TsmpDpAaRtnCode._1351.throwing("{{roleId}}", String.valueOf(10), String.valueOf(roleId.length()));
		} else {
			TsmpRole tsmpRole = getTsmpRoleById(roleId);
			if (tsmpRole == null) {
				throw TsmpDpAaRtnCode._1354.throwing("{{roleId}}", roleId);
			}
		}
		
		// 名單類型必填, bcrypt 解密
		if (StringUtils.isEmpty(listType)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{listType}}");
		}
		try {
			listType = getBcryptParamHelper().decode(listType, "RT_MAP_LIST_TYPE", locale);
			req.setNewListType(listType);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		// [新] 交易代碼必填
		if (StringUtils.isEmpty(txId)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{txId}}");
		}
		
		Set<String> txIdSet = new HashSet<>();
		String[] txIdAry = txId.split(",");
		for (int i = 0; i < txIdAry.length; i++) {
			// 新-交易代碼不能重複, 否則更新時會違反UK(roleId + txId)
			if (txIdSet.contains(txIdAry[i])) {
				throw TsmpDpAaRtnCode._1284.throwing("{{newTxId}}");
			// 每一筆交易代碼 maxLength = 10
			} else if (txIdAry[i].length() > 10) {
				throw TsmpDpAaRtnCode._1351.throwing("{{txId}}", String.valueOf(10), String.valueOf(txIdAry[i].length()));
			} else {
				txIdSet.add(txIdAry[i]);
			}
			
			txIdAry[i] = txIdAry[i].trim();
		}
		return txIdAry;
	}

	public DPB0113Resp doUpdate(TsmpAuthorization auth, DPB0113Req req, String[] newTxIdAry) {
		String oriRoleId = req.getOriRoleId();
		List<String> oriTxIdList = req.getOriTxIdList();
		String oriListType = req.getOriListType();
		
		// 檢查傳入的資料是否與資料庫相同, 同時刪除舊資料
		checkAndDeleteOriData(oriRoleId, oriListType, oriTxIdList);
		
		// 新增資料
		List<Long> roleTxidMapIds = new ArrayList<>();
		
		TsmpRoleTxidMap m = null;
		List<TsmpRoleTxidMap> dataList = null;
		for (int i = 0; i < newTxIdAry.length; i++) {
			// [新] (角色ID + 交易代碼) 不可重複
			dataList = getTsmpRoleTxidMapDao().findByRoleIdAndTxid(req.getNewRoleId(), newTxIdAry[i]);
			if (dataList != null && !dataList.isEmpty()) {
				throw TsmpDpAaRtnCode._1353.throwing("{{newRoleId}} + {{newTxId}}", "(" + req.getNewRoleId() + " + " + newTxIdAry[i] + ")");
			}
			
			m = new TsmpRoleTxidMap();
			m.setRoleId(req.getNewRoleId());
			m.setTxid(newTxIdAry[i]);
			m.setListType(req.getNewListType());
			m.setCreateDateTime(DateTimeUtil.now());
			m.setCreateUser(auth.getUserName());
			m = getTsmpRoleTxidMapDao().save(m);
			roleTxidMapIds.add(m.getRoleTxidMapId());
		}
		
		DPB0113Resp resp = new DPB0113Resp();
		resp.setRoleTxidMapIds(roleTxidMapIds);
		return resp;
	}

	public void checkAndDeleteOriData(String roleId, String listType, List<String> oriTxIdList) {
		List<TsmpRoleTxidMap> dataList = getTsmpRoleTxidMapDao().findByRoleIdAndListType(roleId, listType);
		// 若傳入的原始交易代碼數量與資料庫不符, 則跳錯
		if (dataList == null || dataList.size() != oriTxIdList.size()) {
			throw TsmpDpAaRtnCode._1290.throwing();
		}
		// 比對原始交易代碼是否與資料庫相同
		Boolean isInList = null;
		for (TsmpRoleTxidMap data : dataList) {
			isInList = isInOriTxIdList(data.getTxid(), oriTxIdList);
			if (!isInList) {
				this.logger.debug(String.format("交易代碼[%s] 未包含在傳入的原始交易代碼清單中: %s", data.getTxid(), oriTxIdList));
				throw TsmpDpAaRtnCode._1290.throwing();
			} else {
				// 刪除資料
				getTsmpRoleTxidMapDao().delete(data);
			}
		}
	}

	private Boolean isInOriTxIdList(String txId, List<String> oriTxIdList) {
		for (String oriTxId : oriTxIdList) {
			if (oriTxId.equals(txId)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Using Cache
	 * @param roleId
	 * @return
	 */
	protected TsmpRole getTsmpRoleById(String roleId) {
		return getTsmpRoleCacheProxy().findById(roleId);
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

}