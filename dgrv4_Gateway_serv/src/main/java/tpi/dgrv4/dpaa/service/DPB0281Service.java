package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.sqlserver.jdbc.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0281Field;
import tpi.dgrv4.dpaa.vo.DPB0281Req;
import tpi.dgrv4.dpaa.vo.DPB0281Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.gateway.constant.WebhookFieldEnum;
import tpi.dgrv4.gateway.constant.WebhookType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0281Service {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private BcryptParamHelper bcryptParamHelper;

	public DPB0281Resp createWebhookNotify(TsmpAuthorization auth, DPB0281Req req, ReqHeader header) {
		DPB0281Resp resp = new DPB0281Resp();
		try {
			checkParam(req);
			
			DgrWebhookNotify vo = new DgrWebhookNotify();
			vo.setNotifyName(req.getNotifyName());
			vo.setNotifyType(req.getNotifyType());
			vo.setEnable(getBcryptParamHelper().decode(req.getEnable(), "ENABLE_FLAG", BcryptFieldValueEnum.PARAM2, header.getLocale()));			
			vo.setMessage(req.getMessage());
			vo.setPayloadFlag(req.getPayloadFlag());
			vo.setCreateDateTime(DateTimeUtil.now());
			vo.setCreateUser(auth.getUserName());
			
			vo = getDgrWebhookNotifyDao().save(vo);
			
			if(req.getFieldList()!=null) {
				for(DPB0281Field field : req.getFieldList()) {
					DgrWebhookNotifyField fieldVo = new DgrWebhookNotifyField();
					fieldVo.setWebhookNotifyId(vo.getWebhookNotifyId());
					fieldVo.setFieldKey(field.getKey());
					fieldVo.setFieldValue(field.getValue());
					fieldVo.setFieldType(field.getType());
					fieldVo.setMappingUrl(field.getMappingUrl());
					fieldVo.setCreateDateTime(DateTimeUtil.now());
					fieldVo.setCreateUser(auth.getUserName());
					getDgrWebhookNotifyFieldDao().save(fieldVo);
				}
			}
			resp.setWebhookNotifyId(vo.getWebhookNotifyId()+"");
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0281Req req) {
		if(StringUtils.isEmpty(req.getNotifyName()) || 
			StringUtils.isEmpty(req.getNotifyType()) || 
			StringUtils.isEmpty(req.getEnable()) ||
			req.getPayloadFlag() == null){
			throw TsmpDpAaRtnCode._1296.throwing();
		}
				
		switch(req.getNotifyType().toUpperCase()) {
			case WebhookType.LINE:
				for(DPB0281Field field : req.getFieldList()) {
					if(WebhookFieldEnum.AUTHORIZATION.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
				}				
				break;
			case WebhookType.EMAIL:
				for(DPB0281Field field : req.getFieldList()) {
					if(WebhookFieldEnum.SUBJECT.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
					if(WebhookFieldEnum.RECIPIENTS.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
				}
				break;
			case WebhookType.SLACK:
				for(DPB0281Field field : req.getFieldList()) {
					if(WebhookFieldEnum.URL.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
				}
				break;
			case WebhookType.DISCORD:
				for(DPB0281Field field : req.getFieldList()) {
					if(WebhookFieldEnum.URL.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
				}
				break;
			default:
				for(DPB0281Field field : req.getFieldList()) {
					if(WebhookFieldEnum.URL.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
					if(WebhookFieldEnum.PERCENT.getCode().equals(field.getKey()) && StringUtils.isEmpty(field.getValue())) {
						throw TsmpDpAaRtnCode._1296.throwing();
					}
				}
				break;
		}
		
		//查詢DgrWebhookNotify資料表，條件NOTIFY_NAME = DPB0281Req.notifyName，若查到資料則throw RTN Code _1353。
		/* Query the DgrWebhookNotify table with the condition NOTIFY_NAME = DPB0281Req.notifyName. 
		 * If a record is found, throw an exception with return code RTN Code _1353. */
		Optional<DgrWebhookNotify> notify = getDgrWebhookNotifyDao().findFirstByNotifyName(req.getNotifyName());
		if(notify.isPresent()) {
			//[{{0}}] 已存在: {{1}}
			throw TsmpDpAaRtnCode._1353.throwing("{{notifyName}}", String.valueOf(req.getNotifyName()));
		}
		
		// must 100 percent
		if(null != req.getFieldList() && WebhookType.HTTP.equals(req.getNotifyType().toUpperCase())) {
			int total = 0;
			for(DPB0281Field field : req.getFieldList()) {
				if(WebhookFieldEnum.PERCENT.getCode().equals(field.getKey())) {
					int percent = Integer.parseInt(field.getValue());
					total += percent;
				}
			}
			if (total != 100) {
				this.logger.error("DPB0281Req - Probability total is not equal to 100, which does not meet the restriction.");
				throw TsmpDpAaRtnCode._1528.throwing(total+"");
			}
		}		
	}
}
