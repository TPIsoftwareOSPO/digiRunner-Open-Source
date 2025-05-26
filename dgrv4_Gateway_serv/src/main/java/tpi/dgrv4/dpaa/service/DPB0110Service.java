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
import tpi.dgrv4.dpaa.vo.DPB0110Req;
import tpi.dgrv4.dpaa.vo.DPB0110Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpRoleCacheProxy;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
@Transactional
public class DPB0110Service {

	private TsmpRoleCacheProxy tsmpRoleCacheProxy;
	private AuthoritiesDao authoritiesDao;
	private TsmpRoleTxidMapDao tsmpRoleTxidMapDao;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB0110Service(TsmpRoleCacheProxy tsmpRoleCacheProxy, AuthoritiesDao authoritiesDao,
			TsmpRoleTxidMapDao tsmpRoleTxidMapDao, BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRoleCacheProxy = tsmpRoleCacheProxy;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0110Resp createRTMap(TsmpAuthorization auth, DPB0110Req req, ReqHeader reqHeader) {
		String local = ServiceUtil.getLocale(reqHeader.getLocale());
		checkParams(auth, req.getRoleId(), req.getTxId(), req.getListType(), req, local);
		return doSave(auth, req);
	}

	public void checkParams(TsmpAuthorization auth, String roleId, String txId, String listType, DPB0110Req req, String locale) {
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
		try {
			listType = getBcryptParamHelper().decode(listType, "RT_MAP_LIST_TYPE", locale);
			req.setListType(listType);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}

		// 交易代碼必填
		if (StringUtils.isEmpty(txId)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{txId}}");
		}
		
		Set<String> txIdSet = new HashSet<>();
		String[] txIdAry = txId.split(",");
		List<TsmpRoleTxidMap> mapping = null;
		for (int i = 0; i < txIdAry.length; i++) {
			if (txIdSet.contains(txIdAry[i])) {
				throw TsmpDpAaRtnCode._1284.throwing("{{txId}}");
			}
			// 每一筆交易代碼 maxLength = 10
			if (txIdAry[i].length() > 10) {
				throw TsmpDpAaRtnCode._1351.throwing("{{txId}}", String.valueOf(10), String.valueOf(txIdAry[i].length()));
			}
			// (角色ID + 交易代碼) 不可重複
			mapping = getTsmpRoleTxidMapDao().findByRoleIdAndTxid(roleId, txIdAry[i]);
			if (mapping != null && !mapping.isEmpty()) {
				throw TsmpDpAaRtnCode._1353.throwing("{{roleId}} + {{txId}}", "(" + roleId + " + " + txIdAry[i] + ")");
			}
			
			txIdSet.add(txIdAry[i]);
			
			txIdAry[i] = txIdAry[i].trim();
		}
		req.setTxId(String.join(",", txIdAry));
	}

	public DPB0110Resp doSave(TsmpAuthorization auth, DPB0110Req req) {
		List<Long> roleTxidMapIds = new ArrayList<>();
		
		String txId = req.getTxId();
		String[] txIdAry = txId.split(",");
		TsmpRoleTxidMap m = null;
		for (int i = 0; i < txIdAry.length; i++) {
			m = new TsmpRoleTxidMap();
			m.setRoleId(req.getRoleId());
			m.setTxid(txIdAry[i]);
			m.setListType(req.getListType());
			m.setCreateDateTime(DateTimeUtil.now());
			m.setCreateUser(auth.getUserName());
			m = getTsmpRoleTxidMapDao().save(m);
			roleTxidMapIds.add(m.getRoleTxidMapId());
		}
		
		DPB0110Resp resp = new DPB0110Resp();
		resp.setRoleTxidMapIds(roleTxidMapIds);
		return resp;
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

	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}

	protected TsmpRoleTxidMapDao getTsmpRoleTxidMapDao() {
		return this.tsmpRoleTxidMapDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

}