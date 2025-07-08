package tpi.dgrv4.dpaa.service;


import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0280Req;
import tpi.dgrv4.dpaa.vo.DPB0280Resp;
import tpi.dgrv4.dpaa.vo.DPB0280WebhookNotify;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0280Service {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private BcryptParamHelper bcryptParamHelper;

	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	public DPB0280Resp queryWebhookNotify(TsmpAuthorization auth, DPB0280Req req, ReqHeader header) {

		DPB0280Resp resp = new DPB0280Resp();

		try {
			// 分頁用，DGR_WEBHOOK_NOTIFY.DGR_WEBHOOK_NOTIFY_ID，是當頁資料的最後一筆 id，按下 [查看更多] 時才需要帶入。
			// For pagination, DGR_WEBHOOK_NOTIFY.DGR_WEBHOOK_NOTIFY_ID is the last record ID of the current page.  
			// It should be provided only when clicking [Load More].
			String id = req.getWebhookNotifyId();
			Long longId = null;
			if(StringUtils.hasLength(id)) {
				longId = Long.parseLong(id);
			} 			
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String local = header.getLocale();
			String reqEnable = req.getEnable();
			// 當enable為-1，表示「全部」。不需進行解密。
			// When enable is -1, it means "all" and decryption is not required.
			if (StringUtils.hasText(reqEnable) && !"-1".equals(reqEnable)) {
				reqEnable = getBcryptParamHelper().decode(req.getEnable(), "ENABLE_FLAG", BcryptFieldValueEnum.PARAM2, header.getLocale());
			}
			
			List<DgrWebhookNotify> webhookNotifyList = null;
			if ("N".equals(req.getPaging())) {
				// 不分頁
				webhookNotifyList = getDgrWebhookNotifyDao().findByDgrWebhookNotifyIdAndKeyword(longId, reqEnable,
						words, Integer.MAX_VALUE);
			} else {
				// 分頁
				webhookNotifyList = getDgrWebhookNotifyDao().findByDgrWebhookNotifyIdAndKeyword(longId, reqEnable,
						words, getPageSize());
			}			
			
			List<DPB0280WebhookNotify> respWebhookNotifyList = new ArrayList<>();
			if (!webhookNotifyList.isEmpty()) {
				webhookNotifyList.forEach(e -> {
					Long webhookNotifyId = e.getWebhookNotifyId();
					String notifyName = e.getNotifyName();
					String notifyType = e.getNotifyType();
					String enable = e.getEnable();					
					String createDateTime = DateTimeUtil.dateTimeToString(e.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分_2).orElse(new String());					
					String createUser = e.getCreateUser();

					DPB0280WebhookNotify notify = new DPB0280WebhookNotify();
					notify.setWebhookNotifyId(webhookNotifyId+"");
					notify.setNotifyName(notifyName);
					notify.setNotifyType(notifyType);
					notify.setEnable(enable);
					notify.setEnableName(getSubitemNameByParam2("ENABLE_FLAG", enable, local));					
					notify.setCreateDateTime(createDateTime);
					notify.setCreateUser(createUser);

					respWebhookNotifyList.add(notify);
				});
			}else {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setWebhookNotifyList(respWebhookNotifyList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.PARAM2, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	private String getSubitemNameByParam2(String itemNo, String param2, String locale) {
		TsmpDpItems n = getTsmpDpItemsCacheProxy().findByItemNoAndParam2AndLocale(itemNo, param2, locale);
		if (n==null) {
			return null;
		}
		String subitemName = n.getSubitemName();
		return subitemName;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("DPB0280");
		return this.pageSize;
	}
}
