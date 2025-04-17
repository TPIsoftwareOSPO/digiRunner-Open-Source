package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0176Req;
import tpi.dgrv4.dpaa.vo.DPB0176Resp;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0176Service {

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0176Service(DgrWebSocketMappingDao dgrWebSocketMappingDao) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
	}

	public DPB0176Resp createWs(TsmpAuthorization auth, DPB0176Req req) {
		DPB0176Resp resp = new DPB0176Resp();
		try {
			checkParam(req);
			
			DgrWebSocketMapping vo = new DgrWebSocketMapping();
			vo.setMemo(req.getMemo());
			vo.setSiteName(req.getSiteName());
			vo.setTargetWs(req.getTargetWs());
			vo.setAuth(req.getAuth());
			vo.setCreateUser(auth.getUserName());
			
			getDgrWebSocketMappingDao().save(vo);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0176Req req) {
		DgrWebSocketMapping vo = getDgrWebSocketMappingDao().findFirstBySiteName(req.getSiteName());
		if(vo != null) {
			throw TsmpDpAaRtnCode._1353.throwing("{{siteName}}", req.getSiteName());
		}
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

}
