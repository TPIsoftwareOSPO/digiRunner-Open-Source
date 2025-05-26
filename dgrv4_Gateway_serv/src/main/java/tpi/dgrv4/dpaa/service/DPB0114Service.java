package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0114Req;
import tpi.dgrv4.dpaa.vo.DPB0114Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
@Transactional
public class DPB0114Service {

	private TsmpRoleTxidMapDao tsmpRoleTxidMapDao;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB0114Service(TsmpRoleTxidMapDao tsmpRoleTxidMapDao, BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0114Resp deleteRTMap(TsmpAuthorization auth, DPB0114Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		checkParams(req, locale);
		
		String roleId = req.getRoleId();
		String listType = req.getListType();
		List<TsmpRoleTxidMap> dataList = getTsmpRoleTxidMapDao().findByRoleIdAndListType(roleId, listType);
		if (dataList == null || dataList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		return doDelete(roleId, listType, dataList);
	}

	public void checkParams(DPB0114Req req, String locale) {
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

	public DPB0114Resp doDelete(String roleId, String listType, List<TsmpRoleTxidMap> dataList) {
		List<String> oriTxIdList = new ArrayList<>();
		for (TsmpRoleTxidMap data : dataList) {
			getTsmpRoleTxidMapDao().delete(data);
			oriTxIdList.add(data.getTxid());
		}
		
		DPB0114Resp resp = new DPB0114Resp();
		resp.setOriRoleId(roleId);
		resp.setOriListType(listType);
		resp.setOriTxIdList(oriTxIdList);
		return resp;
	}

	protected TsmpRoleTxidMapDao getTsmpRoleTxidMapDao() {
		return this.tsmpRoleTxidMapDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

}