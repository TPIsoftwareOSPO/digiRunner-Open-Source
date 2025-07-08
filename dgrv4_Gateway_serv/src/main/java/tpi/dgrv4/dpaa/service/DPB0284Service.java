package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0284Req;
import tpi.dgrv4.dpaa.vo.DPB0284Resp;
import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;
import tpi.dgrv4.entity.repository.DgrWebhookApiMapDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0284Service {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookApiMapDao dgrWebhookApiMapDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyLogDao dgrWebhookNotifyLogDao;

	public DPB0284Resp deleteWebhookNotify(TsmpAuthorization auth, DPB0284Req req) {
		DPB0284Resp resp = new DPB0284Resp();
		try {
			checkParam(req);
			
			Long id = Long.parseLong(req.getWebhookNotifyId());
			DgrWebhookNotify vo = getDgrWebhookNotifyDao().findById(id).orElse(null);
			if(vo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<DgrWebhookNotifyLog> logList = getDgrWebhookNotifyLogDao().findByWebhookNotifyId(vo.getWebhookNotifyId());
			if(!logList.isEmpty()) {
				throw TsmpDpAaRtnCode._1287.throwing();
			}
			
			// 刪除關聯的 DgrWebhookNotifyField 資料
			// Delete related DgrWebhookNotifyField records
			List<DgrWebhookNotifyField> fields = getDgrWebhookNotifyFieldDao().findByWebhookNotifyId(id);
			getDgrWebhookNotifyFieldDao().deleteAll(fields);			
			
			// 刪除關聯的 DgrWebhookApiMap 資料
			// Delete related DgrWebhookApiMap records
			List<DgrWebhookApiMap> apiMaps = getDgrWebhookApiMapDao().findByWebhookNotifyId(id);
			getDgrWebhookApiMapDao().deleteAll(apiMaps);
			
			getDgrWebhookNotifyDao().delete(vo);
										
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0284Req req) {
		if(!StringUtils.hasText(req.getWebhookNotifyId())) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}
}
