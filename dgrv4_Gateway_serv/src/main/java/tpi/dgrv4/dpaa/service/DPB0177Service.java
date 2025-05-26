package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0177Req;
import tpi.dgrv4.dpaa.vo.DPB0177Resp;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0177Service {

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0177Service(DgrWebSocketMappingDao dgrWebSocketMappingDao) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
	}

	public DPB0177Resp updateWs(TsmpAuthorization auth, DPB0177Req req) {
		DPB0177Resp resp = new DPB0177Resp();
		try {
			DgrWebSocketMapping vo = checkParam(req);
			
			vo.setMemo(req.getMemo());
			vo.setTargetWs(req.getTargetWs());
			vo.setAuth(req.getAuth());
			vo.setUpdateUser(auth.getUserName());
			vo.setUpdateDateTime(DateTimeUtil.now());
			
			getDgrWebSocketMappingDao().save(vo);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return resp;
	}
	
	private DgrWebSocketMapping checkParam(DPB0177Req req) {
		if(!StringUtils.hasText(req.getLongId())) {
			throw TsmpDpAaRtnCode._2025.throwing("longId");
		}
		
		DgrWebSocketMapping vo = getDgrWebSocketMappingDao().findById(Long.valueOf(req.getLongId())).orElse(null);
		if(vo == null) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		return vo;
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

}
