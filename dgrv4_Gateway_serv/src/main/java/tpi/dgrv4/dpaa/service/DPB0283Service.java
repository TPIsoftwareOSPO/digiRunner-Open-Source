package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
import tpi.dgrv4.dpaa.vo.DPB0283Req;
import tpi.dgrv4.dpaa.vo.DPB0283Resp;
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
public class DPB0283Service {

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

	public DPB0283Resp updateWebhookNotify(TsmpAuthorization auth, DPB0283Req req, ReqHeader header) {
		DPB0283Resp resp = new DPB0283Resp();
		try {
			checkParam(req);	
			Long id = Long.parseLong(req.getWebhookNotifyId());
			
			Optional<DgrWebhookNotify> foundData = getDgrWebhookNotifyDao().findById(id);
			if(foundData.isPresent()) {
				DgrWebhookNotify vo = foundData.get();
				vo.setEnable(getBcryptParamHelper().decode(req.getEnable(), "ENABLE_FLAG", BcryptFieldValueEnum.PARAM2, header.getLocale()));			
				vo.setMessage(req.getMessage());
				vo.setPayloadFlag(req.getPayloadFlag());
				vo.setUpdateDateTime(DateTimeUtil.now());
				vo.setUpdateUser(auth.getUserName());			
				vo = getDgrWebhookNotifyDao().save(vo);
							
				//先刪除，後新增
				// Delete first, then add 
				List<DgrWebhookNotifyField> oldFields = getDgrWebhookNotifyFieldDao().findByWebhookNotifyId(vo.getWebhookNotifyId());
				getDgrWebhookNotifyFieldDao().deleteAll(oldFields);
				
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
						fieldVo.setUpdateDateTime(DateTimeUtil.now());
						fieldVo.setUpdateUser(auth.getUserName());
						getDgrWebhookNotifyFieldDao().save(fieldVo);
					}
				}
				resp.setWebhookNotifyId(vo.getWebhookNotifyId()+"");
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0283Req req) {
		if(!StringUtils.hasText(req.getWebhookNotifyId()) || !StringUtils.hasText(req.getEnable()) || req.getPayloadFlag() == null){
				throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		Long id = Long.parseLong(req.getWebhookNotifyId());		
		Optional<DgrWebhookNotify> foundData = getDgrWebhookNotifyDao().findById(id);
		
		if(foundData.isPresent()) {
			switch(foundData.get().getNotifyType().toUpperCase()) {
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
			
			// must 100 percent
			if(null != req.getFieldList() && WebhookType.HTTP.equals(foundData.get().getNotifyType().toUpperCase())) {
				int total = 0;
				for(DPB0281Field field : req.getFieldList()) {
					if(WebhookFieldEnum.PERCENT.getCode().equals(field.getKey())) {
						int percent = Integer.parseInt(field.getValue());
						total += percent;
					}
				}
				if (total != 100) {
					this.logger.error("DPB0283Req - Probability total is not equal to 100, which does not meet the restriction.");
					throw TsmpDpAaRtnCode._1528.throwing(total+"");
				}
			}
		} else {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
	}
}
