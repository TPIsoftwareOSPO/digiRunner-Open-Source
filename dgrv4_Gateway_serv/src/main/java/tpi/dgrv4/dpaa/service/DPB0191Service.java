package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0191Req;
import tpi.dgrv4.dpaa.vo.DPB0191Resp;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0191Service {
	private TPILogger logger = TPILogger.tl;
	private DgrRdbConnectionDao dgrRdbConnectionDao;

	@Autowired
	public DPB0191Service(DgrRdbConnectionDao dgrRdbConnectionDao) {
		super();
		this.dgrRdbConnectionDao = dgrRdbConnectionDao;
	}

	public DPB0191Resp queryRdbConnectionInfoDetail(TsmpAuthorization authorization, DPB0191Req req) {
		DPB0191Resp resp = new DPB0191Resp();
		try {
			String cN = req.getConnectionName();

			if (!StringUtils.hasLength(req.getConnectionName())) {
				throw TsmpDpAaRtnCode._2025.throwing("{{ConnectionName}}");
			}

			DgrRdbConnection info = getDgrRdbConnectionDao().findById(cN).orElse(null);
			if (info == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setConnectionName(info.getConnectionName());
			resp.setConnectionTimeout(info.getConnectionTimeout());
			resp.setCreateDateTime(info.getCreateDateTime());
			resp.setCreateUser(info.getCreateUser());
			resp.setDataSourceProperty(info.getDataSourceProperty());
			resp.setIdleTimeout(info.getIdleTimeout());
			resp.setJdbcUrl(info.getJdbcUrl());
			resp.setMaxLifetime(info.getMaxLifetime());
			resp.setMaxPoolSize(info.getMaxPoolSize());
			resp.setMima(info.getMima());
			resp.setUpdateDateTime(info.getUpdateDateTime());
			resp.setUpdateUser(info.getUpdateUser());
			resp.setUserName(info.getUserName());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}
}
