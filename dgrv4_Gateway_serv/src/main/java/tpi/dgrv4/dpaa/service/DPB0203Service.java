package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0203Req;
import tpi.dgrv4.dpaa.vo.DPB0203Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0203Service {
	private TPILogger logger = TPILogger.tl;

	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;

	@Autowired
	public DPB0203Service(DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao) {
		super();
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
	}

	public DPB0203Resp queryGtwIdPInfoDetail_jdbc(TsmpAuthorization authorization, DPB0203Req req) {
		DPB0203Resp resp = new DPB0203Resp();
		try {
			String id = req.getId();
			if (!StringUtils.hasLength(id)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
			}

			long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoJdbc infoJdbc = getDgrGtwIdpInfoJdbcDao().findById(longId).orElse(null);
			if (infoJdbc == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			String icon = StringUtils.hasLength(infoJdbc.getIconFile()) ? infoJdbc.getIconFile()
					: IdPHelper.DEFULT_ICON_FILE;

			resp.setId(id);
			resp.setLongId(String.valueOf(longId));
			resp.setClientId(infoJdbc.getClientId());
			resp.setStatus(infoJdbc.getStatus());
			resp.setRemark(infoJdbc.getRemark());
			resp.setConnectionName(infoJdbc.getConnectionName());
			resp.setSqlPtmt(infoJdbc.getSqlPtmt());
			resp.setSqlParams(infoJdbc.getSqlParams());
			resp.setUserMimaAlg(infoJdbc.getUserMimaAlg());
			resp.setUserMimaColName(infoJdbc.getUserMimaColName());
			resp.setIdtSub(infoJdbc.getIdtSub());
			resp.setIdtName(infoJdbc.getIdtName());
			resp.setIdtEmail(infoJdbc.getIdtEmail());
			resp.setIdtPicture(infoJdbc.getIdtPicture());
			resp.setIconFile(icon);
			resp.setPageTitle(infoJdbc.getPageTitle());
			resp.setCreateDateTime(infoJdbc.getCreateDateTime());
			resp.setCreateUser(infoJdbc.getCreateUser());
			resp.setUpdateDateTime(infoJdbc.getUpdateDateTime());
			resp.setUpdateUser(infoJdbc.getUpdateUser());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}
}
