package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0285Req;
import tpi.dgrv4.dpaa.vo.DPB0285Resp;
import tpi.dgrv4.dpaa.vo.DPB0285WebhookLog;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyLogDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0285Service {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyLogDao dgrWebhookNotifyLogDao;	
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;

	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	public DPB0285Resp queryWebhookLogs(TsmpAuthorization auth, DPB0285Req req, ReqHeader header) {

		DPB0285Resp resp = new DPB0285Resp();

		try {			
			// 檢查日期格式
			// Check date format
			String startDateStr = req.getStartDate();
			String endDateStr = req.getEndDate();
			Optional<Date> opt_s = DateTimeUtil.stringToDateTime(startDateStr, DateTimeFormatEnum.西元年月日_2);
			Optional<Date> opt_e = DateTimeUtil.stringToDateTime(endDateStr, DateTimeFormatEnum.西元年月日_2);
			
			if(!opt_s.isPresent()) {
				throw TsmpDpAaRtnCode._1295.throwing();
			}		
			if(!opt_e.isPresent()) {
				throw TsmpDpAaRtnCode._1295.throwing();
			}
			
			// 檢查日期邏輯
			// 使用DateTimeUtil轉出來的Date, 時間都是00:00:00
			// Check date logic
			// The Date converted by DateTimeUtil always has the time set to 00:00:00
			Date startDate = opt_s.get();
			Date endDate = opt_e.get();
			if (startDate.compareTo(endDate) > 0) {
				throw TsmpDpAaRtnCode._1295.throwing();
			} else {
				/* 假設查詢同一天(1911/01/01~1911/01/01) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/02 00:00:00
				 * 不同天(1911/01/01~1911/01/03) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/04 00:00:00
				 * 因為SQL條件是 createDateTime >= :startDate and createDateTime < :endDate
				 */
				/* If querying the same day (1911/01/01~1911/01/01), 
			     * it will be adjusted to 1911/01/01 00:00:00 ~ 1911/01/02 00:00:00.
			     * If querying different days (1911/01/01~1911/01/03), 
			     * it will be adjusted to 1911/01/01 00:00:00 ~ 1911/01/04 00:00:00.
			     * This is because the SQL condition is 
			     * createDateTime >= :startDate and createDateTime < :endDate.
			     */
				endDate = plusDay(endDate, 1);
			}
			
			
			// 分頁用，DGR_WEBHOOK_NOTIFY_LOG.WEBHOOK_NOTIFY_LOG_ID，是當頁資料的最後一筆 id，按下 [查看更多] 時才需要帶入。
			// For pagination, DGR_WEBHOOK_NOTIFY_LOG.WEBHOOK_NOTIFY_LOG_ID is the last record ID of the current page.  
			// It should be provided only when clicking [Load More].			
			Long id = null;
			DgrWebhookNotifyLog lastRecord = null;
			if(StringUtils.hasLength(req.getWebhookNotifyLogId())) {
				id = Long.parseLong(req.getWebhookNotifyLogId());
				lastRecord = getLastRecordFromPrevPage(id);
			}
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");	
			
			
			List<DgrWebhookNotifyLog> webhookNotifyLogList = null;
			if ("N".equals(req.getPaging())) {
				// 不分頁
				webhookNotifyLogList = getDgrWebhookNotifyLogDao().query_dpb0285Service(startDate, endDate, 
						lastRecord, words, Integer.MAX_VALUE);
			} else {
				// 分頁
				webhookNotifyLogList = getDgrWebhookNotifyLogDao().query_dpb0285Service(startDate, endDate, 
						lastRecord, words, getPageSize());
			}	
			
			
			List<DPB0285WebhookLog> respWebhookNotifyLogList = new ArrayList<>();
			if (!webhookNotifyLogList.isEmpty()) {
				webhookNotifyLogList.forEach(e -> {
					Long webhookNotifyLogId = e.getWebhookNotifyLogId();					
					DgrWebhookNotify notify = getDgrWebhookNotifyDao().findById(e.getWebhookNotifyId()).orElse(null);
					
					String notifyName = notify.getNotifyName();
					String notifyType = notify.getNotifyType();
					String clientId = e.getClientId();
					String content = e.getContent();
					String remark = e.getRemark();
					String createDateTime = DateTimeUtil.dateTimeToString(e.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分_2).orElse(new String());					

					DPB0285WebhookLog webhookLog = new DPB0285WebhookLog();
					webhookLog.setWebhookNotifyLogId(webhookNotifyLogId+"");
					webhookLog.setNotifyName(notifyName);
					webhookLog.setNotifyType(notifyType);
					webhookLog.setClientId(clientId);					
					webhookLog.setContent(content);
					webhookLog.setRemark(remark);
					webhookLog.setCreateDateTime(createDateTime);
					webhookLog.setResult(StringUtils.hasText(remark)?"N":"Y");

					respWebhookNotifyLogList.add(webhookLog);
				});
			}else {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			resp.setWebhookLogList(respWebhookNotifyLogList);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private DgrWebhookNotifyLog getLastRecordFromPrevPage(Long webhookNotifyLogId) {
		if (webhookNotifyLogId != null) {
			Optional<DgrWebhookNotifyLog> opt = getDgrWebhookNotifyLogDao().findById(webhookNotifyLogId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}
	
	private Date plusDay(Date dt, int days) {
		LocalDateTime ldt = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		ldt = ldt.plusDays(days);
		return Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("DPB0285");
		return this.pageSize;
	}
}
