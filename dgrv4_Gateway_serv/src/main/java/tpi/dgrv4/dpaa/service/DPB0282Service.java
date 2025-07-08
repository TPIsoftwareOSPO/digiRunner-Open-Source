package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0281Field;
import tpi.dgrv4.dpaa.vo.DPB0282Req;
import tpi.dgrv4.dpaa.vo.DPB0282Resp;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0282Service {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;

	public DPB0282Resp getWebhookNotify(TsmpAuthorization auth, DPB0282Req req) {
		DPB0282Resp resp = new DPB0282Resp();
		try {
			checkParam(req);
			
			Long webhookNotifyId = Long.parseLong(req.getWebhookNotifyId());
			
			Optional<DgrWebhookNotify> vo = getDgrWebhookNotifyDao().findById(webhookNotifyId);
			if(vo.isPresent()) {
				BeanUtils.copyProperties(vo.get(), resp);
				List<DPB0281Field> respFieldList = new ArrayList<>();
				List<DgrWebhookNotifyField> fieldList = getDgrWebhookNotifyFieldDao().findByWebhookNotifyId(webhookNotifyId);								
				fieldList.forEach(e -> {
					DPB0281Field f = new DPB0281Field();
					f.setKey(e.getFieldKey());
					f.setValue(e.getFieldValue());
					f.setType(e.getFieldType());
					f.setMappingUrl(e.getMappingUrl());
					respFieldList.add(f);
				});
				
				// parse Long to String
				resp.setWebhookNotifyId(vo.get().getWebhookNotifyId()+"");				
				resp.setFieldList(respFieldList);
			} else {
				throw TsmpDpAaRtnCode._1298.throwing();
			}				
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0282Req req) {
		if(!StringUtils.hasText(req.getWebhookNotifyId())) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}
}
