package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0185Req;
import tpi.dgrv4.dpaa.vo.DPB0185Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0185Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;

	@Autowired
	public DPB0185Service(DgrGtwIdpInfoADao dgrGtwIdpInfoADao) {
		super();
		this.dgrGtwIdpInfoADao = dgrGtwIdpInfoADao;
	}

	public DPB0185Resp queryGtwIdPInfoDetail_api(TsmpAuthorization authorization, DPB0185Req req) {
		DPB0185Resp resp = new DPB0185Resp();
		try {
			String id = req.getId();
			if (!StringUtils.hasLength(id)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
			}
			long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoA a = getDgrGtwIdpInfoADao().findById(longId).orElse(null);
			if (a == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			resp.setApiMethod(a.getApiMethod());
			resp.setApiUrl(a.getApiUrl());
			resp.setClientId(a.getClientId());
			resp.setCreateDateTime(a.getCreateDateTime());
			resp.setCreateUser(a.getCreateUser());
			resp.setId(id);
			resp.setIdtEmail(a.getIdtEmail());
			resp.setIdtName(a.getIdtName());
			resp.setIdtPicture(a.getIdtPicture());
			resp.setLongId(String.valueOf(longId));
			resp.setRemark(a.getRemark());
			resp.setReqBody(a.getReqBody());
			resp.setReqBodyType(a.getReqBodyType());
			resp.setReqHeader(a.getReqHeader());
			resp.setStatus(a.getStatus());
			resp.setSucByField(a.getSucByField());
			resp.setSucByType(a.getSucByType());
			resp.setSucByValue(a.getSucByValue());
			resp.setUpdateDateTime(a.getUpdateDateTime());
			resp.setUpdateUser(a.getUpdateUser());
			String icon = StringUtils.hasLength(a.getIconFile()) ? a.getIconFile() : IdPHelper.DEFULT_ICON_FILE;
			resp.setIconFile(icon);
			resp.setPageTitle(a.getPageTitle());
			resp.setIdtLightId(a.getIdtLightId());
			resp.setIdtRoleName(a.getIdtRoleName());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {

		return dgrGtwIdpInfoADao;
	}
}
