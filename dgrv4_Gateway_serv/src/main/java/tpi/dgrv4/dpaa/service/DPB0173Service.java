package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0173Req;
import tpi.dgrv4.dpaa.vo.DPB0173Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0173Service {

	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;

	@Autowired
	public DPB0173Service(DgrGtwIdpInfoODao dgrGtwIdpInfoODao) {
		super();
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
	}

	public DPB0173Resp deleteGtwIdPInfo_oauth2(TsmpAuthorization authorization, DPB0173Req req) {
		DPB0173Resp resp = new DPB0173Resp();
		try {
			String id = req.getId();
			cheakParm(id);

			Long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoO dgrGtwIdpInfoO = getDgrGtwIdpInfoODao().findById(longId).orElse(null);
			if (dgrGtwIdpInfoO == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			getDgrGtwIdpInfoODao().delete(dgrGtwIdpInfoO);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}

	private void cheakParm(String id) {
		if (!StringUtils.hasLength(id))
			throw TsmpDpAaRtnCode._2025.throwing("id");
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
}
