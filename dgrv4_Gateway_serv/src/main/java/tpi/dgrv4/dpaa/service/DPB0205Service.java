package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0205Req;
import tpi.dgrv4.dpaa.vo.DPB0205Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0205Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;
	private TsmpClientDao tsmpClientDao;

	@Autowired
	public DPB0205Service(DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao, TsmpClientDao tsmpClientDao) {
		super();
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
		this.tsmpClientDao = tsmpClientDao;
	}

	public DPB0205Resp updateGtwIdPInfo_jdbc(TsmpAuthorization authorization, DPB0205Req req) {
		DPB0205Resp resp = new DPB0205Resp();
		try {
			checkParm(req);

			String id = req.getId();
			long longId = RandomSeqLongUtil.toLongValue(id);

			DgrGtwIdpInfoJdbc infoJdbc = getDgrGtwIdpInfoJdbcDao().findById(longId).orElse(null);
			if (infoJdbc == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			String icon = StringUtils.hasLength(req.getIconFile()) ? req.getIconFile() : IdPHelper.DEFULT_ICON_FILE;

			infoJdbc.setClientId(req.getClientId());
			infoJdbc.setStatus(req.getStatus());
			infoJdbc.setRemark(req.getRemark());
			infoJdbc.setConnectionName(req.getConnectionName());
			infoJdbc.setSqlPtmt(req.getSqlPtmt());
			infoJdbc.setSqlParams(req.getSqlParams());
			infoJdbc.setUserMimaAlg(req.getUserMimaAlg());
			infoJdbc.setUserMimaColName(req.getUserMimaColName());
			infoJdbc.setIdtSub(req.getIdtSub());
			infoJdbc.setIdtName(req.getIdtName());
			infoJdbc.setIdtEmail(req.getIdtEmail());
			infoJdbc.setIdtPicture(req.getIdtPicture());
			infoJdbc.setIconFile(icon);
			infoJdbc.setPageTitle(req.getPageTitle());
			infoJdbc.setUpdateDateTime(DateTimeUtil.now());
			infoJdbc.setUpdateUser(authorization.getUserName());

			infoJdbc = getDgrGtwIdpInfoJdbcDao().saveAndFlush(infoJdbc);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		return resp;
	}

	private void checkParm(DPB0205Req req) {
		String id = req.getId();
		if (!StringUtils.hasLength(id)) {
			throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
		}

		String clientId = req.getClientId();
		if (!StringUtils.hasLength(clientId)) {
			throw TsmpDpAaRtnCode._2025.throwing("{{clientId}}");
		}

		TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
		if (client == null) {
			throw TsmpDpAaRtnCode._1344.throwing();
		}
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
}
