package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0196Req;
import tpi.dgrv4.dpaa.vo.DPB0196Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoApiDao;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0196Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao;

	@Autowired
	public DPB0196Service(DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao) {
		super();
		this.dgrAcIdpInfoApiDao = dgrAcIdpInfoApiDao;
	}

	public DPB0196Resp queryIdPInfoDetail_api(TsmpAuthorization authorization, DPB0196Req req) {
		DPB0196Resp resp = new DPB0196Resp();

		try {
			String id = req.getId();
			if (!StringUtils.hasLength(id)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
			}
			long longId = RandomSeqLongUtil.toLongValue(id);
			DgrAcIdpInfoApi a = getDgrAcIdpInfoApiDao().findById(longId).orElse(null);
			if (a == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			resp.setApiMethod(a.getApiMethod());
			resp.setApiUrl(a.getApiUrl());
			resp.setCreateDateTime(a.getCreateDateTime());
			resp.setCreateUser(a.getCreateUser());
			resp.setId(id);
			resp.setIdtEmail(a.getIdtEmail());
			resp.setIdtName(a.getIdtName());
			resp.setIdtPicture(a.getIdtPicture());
			resp.setLongId(String.valueOf(longId));
			resp.setApprovalResultMail(a.getApprovalResultMail());
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
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrAcIdpInfoApiDao getDgrAcIdpInfoApiDao() {
		return dgrAcIdpInfoApiDao;
	}
}
