package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0204Req;
import tpi.dgrv4.dpaa.vo.DPB0204Resp;
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
public class DPB0204Service {
	private TPILogger logger = TPILogger.tl;

	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;
	private TsmpClientDao tsmpClientDao;

	@Autowired
	public DPB0204Service(DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao, TsmpClientDao tsmpClientDao) {
		super();
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
		this.tsmpClientDao = tsmpClientDao;
	}

	public DPB0204Resp createGtwIdPInfo_jdbc(TsmpAuthorization authorization, DPB0204Req req) {
		DPB0204Resp resp = new DPB0204Resp();
		try {
			// 檢查傳入的參數
			checkParm(req);

			String icon = StringUtils.hasLength(req.getIconFile()) ? req.getIconFile() : IdPHelper.DEFULT_ICON_FILE;
			DgrGtwIdpInfoJdbc infoJdbc = new DgrGtwIdpInfoJdbc();
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
			infoJdbc.setCreateUser(authorization.getUserName());
			infoJdbc.setCreateDateTime(DateTimeUtil.now());

			getDgrGtwIdpInfoJdbcDao().save(infoJdbc);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	private void checkParm(DPB0204Req req) {
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
