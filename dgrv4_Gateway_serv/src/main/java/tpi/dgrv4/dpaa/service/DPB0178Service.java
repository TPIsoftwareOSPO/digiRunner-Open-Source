package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0178Req;
import tpi.dgrv4.dpaa.vo.DPB0178Resp;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0178Service {

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0178Service(DgrWebSocketMappingDao dgrWebSocketMappingDao) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
	}

	public DPB0178Resp deleteWs(TsmpAuthorization auth, DPB0178Req req) {
		DPB0178Resp resp = new DPB0178Resp();
		try {
			checkParam(req);
			Long id = Long.valueOf(req.getLongId());
			getDgrWebSocketMappingDao().deleteById(id);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0178Req req) {
		if(!StringUtils.hasText(req.getLongId())) {
			throw TsmpDpAaRtnCode._2025.throwing("longId");
		}
		
		if(!getDgrWebSocketMappingDao().existsById(Long.valueOf(req.getLongId()))) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

	
}
