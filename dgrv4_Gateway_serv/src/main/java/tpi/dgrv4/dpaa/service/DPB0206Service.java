package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0206Req;
import tpi.dgrv4.dpaa.vo.DPB0206Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * @author Mini <br>
 * 刪除 GTW IdP (JDBC) 的資料
 */
@Service
public class DPB0206Service {
	private TPILogger logger = TPILogger.tl;

	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;

	@Autowired
	public DPB0206Service(DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao) {
		super();
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
	}

	public DPB0206Resp deleteGtwIdPInfo_jdbc(TsmpAuthorization authorization, DPB0206Req req) {
		DPB0206Resp resp = new DPB0206Resp();

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

			getDgrGtwIdpInfoJdbcDao().delete(infoJdbc);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		return resp;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}
}
