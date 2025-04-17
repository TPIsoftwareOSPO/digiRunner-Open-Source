package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0112Req;
import tpi.dgrv4.dpaa.vo.DPB0112Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0112Service {

	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleTxidMapDao tsmpRoleTxidMapDao;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB0112Service(TsmpRoleDao tsmpRoleDao, TsmpRoleTxidMapDao tsmpRoleTxidMapDao,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0112Resp queryRTMapByPk(TsmpAuthorization auth, DPB0112Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		checkParams(req, locale);
		
		String roleId = req.getRoleId();
		String listType = req.getListType();
		List<TsmpRoleTxidMap> dataList = getTsmpRoleTxidMapDao().findByRoleIdAndListType(roleId, listType);
		if (dataList == null || dataList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		String oriRoleName = "";
		String oriRoleAlias = "";
		TsmpRole role = getTsmpRoleDao().findById(roleId).orElse(null);
		if (role != null) {
			oriRoleName = role.getRoleName();
			oriRoleAlias = nvl(role.getRoleAlias());
		}
		
		DPB0112Resp resp = new DPB0112Resp();
		resp.setOriRoleId(roleId);
		resp.setOriRoleName(oriRoleName);
		resp.setOriRoleAlias(oriRoleAlias);
		resp.setOriListType(listType);
		List<String> txIdList = getTxIdList(dataList);
		resp.setOriTxIdList(txIdList);
		resp.setOriTxIdString(String.join(",", txIdList));
		return resp;
	}

	public void checkParams(DPB0112Req req, String locale) {
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

	public List<String> getTxIdList(List<TsmpRoleTxidMap> dataList) {
		return dataList.stream().map((m) -> {
			return m.getTxid();
		}).collect(Collectors.toList());
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}

	protected TsmpRoleTxidMapDao getTsmpRoleTxidMapDao() {
		return this.tsmpRoleTxidMapDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

}