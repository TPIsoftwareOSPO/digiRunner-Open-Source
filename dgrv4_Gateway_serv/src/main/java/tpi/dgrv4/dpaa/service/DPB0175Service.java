package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0175Req;
import tpi.dgrv4.dpaa.vo.DPB0175Resp;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0175Service {

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0175Service(DgrWebSocketMappingDao dgrWebSocketMappingDao) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
	}

	public DPB0175Resp queryWsDetail(TsmpAuthorization auth, DPB0175Req req) {
		DPB0175Resp resp = new DPB0175Resp();
		try {
			checkParam(req);
			
			DgrWebSocketMapping vo = getDgrWebSocketMappingDao().findById(Long.valueOf(req.getLongId())).orElse(null);
			if(vo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			resp.setCreateDateTime(DateTimeUtil.dateTimeToString(vo.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分秒).orElse(null));
			resp.setCreateUser(vo.getCreateUser());
			resp.setHexId(RandomSeqLongUtil.toHexString(vo.getWsMappingId(), RandomLongTypeEnum.YYYYMMDD));
			resp.setLongId(vo.getWsMappingId().toString());
			resp.setMemo(vo.getMemo());
			resp.setSiteName(vo.getSiteName());
			resp.setTargetWs(vo.getTargetWs());
			resp.setAuth(vo.getAuth());
			resp.setUpdateDateTime(vo.getUpdateDateTime()!= null ? DateTimeUtil.dateTimeToString(vo.getUpdateDateTime(), DateTimeFormatEnum.西元年月日時分秒).orElse(null) : null);
			resp.setUpdateUser(vo.getUpdateUser());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0175Req req) {
		if(!StringUtils.hasText(req.getLongId())) {
			throw TsmpDpAaRtnCode._2025.throwing("longId");
		}
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

}
