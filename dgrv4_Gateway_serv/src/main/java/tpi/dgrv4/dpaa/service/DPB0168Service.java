package tpi.dgrv4.dpaa.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0168Req;
import tpi.dgrv4.dpaa.vo.DPB0168Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0168Service {

	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0168Service(DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao) {
		super();
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
	}

	public DPB0168Resp deleteGtwIdPInfo_ldap(TsmpAuthorization authorization, DPB0168Req req) {
		DPB0168Resp resp = new DPB0168Resp();
		try {
			cheakParm(req);
			String id = req.getId();
			Long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoL dgrGtwIdpInfoL = getDgrGtwIdpInfoLDao().findById(longId).orElse(null);
			if (dgrGtwIdpInfoL == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			getDgrGtwIdpInfoLDao().delete(dgrGtwIdpInfoL);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}

	private void cheakParm(DPB0168Req req) {
		if (StringUtils.isBlank(req.getId()))
			throw TsmpDpAaRtnCode._2025.throwing("id");
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}
}
