package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0094Req;
import tpi.dgrv4.dpaa.vo.DPB0094Resp;
import tpi.dgrv4.dpaa.vo.DPB0094RespItem;
import tpi.dgrv4.entity.daoService.OpenApiKeyService;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0094Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao; 
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao; 
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;

	@Autowired
	public DPB0094Service(TsmpClientDao tsmpClientDao, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0094Resp queryOpenApiKeyByDateAndLike(TsmpAuthorization authorization, DPB0094Req req, ReqHeader reqHeader) {
		DPB0094Resp resp = new DPB0094Resp();

		try {
			//chk param		
			// 檢查日期格式
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
			Date startDate = opt_s.get();
			Date endDate = opt_e.get();
			if (startDate.compareTo(endDate) > 0) {
				throw TsmpDpAaRtnCode._1295.throwing();
			} else {
				/* 假設查詢同一天(1911/01/01~1911/01/01) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/02 00:00:00
				 * 不同天(1911/01/01~1911/01/03) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/04 00:00:00
				 * 因為SQL條件是 createDateTime >= :startDate and createDateTime < :endDate
				 */
				endDate = plusDay(endDate, 1);
			}
			
			TsmpOpenApiKey lastRecord = getLastRecordFromPrevPage(req.getOpenApiKeyId());
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			List<TsmpOpenApiKey> openApiKeyList = getTsmpOpenApiKeyDao().query_dpb0094Service(startDate, endDate, 
					lastRecord, words, getPageSize());
			if (openApiKeyList == null || openApiKeyList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<DPB0094RespItem> dataList = getRespItem(openApiKeyList, resp, reqHeader.getLocale());
			resp.setDataList(dataList);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private List<DPB0094RespItem> getRespItem(List<TsmpOpenApiKey> openApiKeyList, DPB0094Resp resp, String locale) {
		List<DPB0094RespItem> dataList = resp.getDataList();
		if(dataList == null) {
			dataList = new ArrayList<DPB0094RespItem>();
		}
		for (TsmpOpenApiKey oak : openApiKeyList) {			
			
			String clientId = oak.getClientId();
			Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
			String clientName = "";
			String clientAlias = "";
			if(opt_client.isPresent()) {
				TsmpClient client = opt_client.get();
				clientName = client.getClientName();
				clientAlias = client.getClientAlias();
			}
			
			String createDateTime = "";
			Optional<String> opt_date = DateTimeUtil.dateTimeToString(oak.getCreateDateTime(),
					DateTimeFormatEnum.西元年月日_2);// yyyy/MM/dd
			if (opt_date.isPresent()) {
				createDateTime = opt_date.get();
			}		
			String rolloverFlag = oak.getRolloverFlag();
			String expiredAtStr = getDateTime(oak.getExpiredAt());
			String status = oak.getOpenApiKeyStatus();// 狀態,為 0 或 1
			String statusName = OpenApiKeyService.getStatusName(expiredAtStr, status, rolloverFlag, locale);
			
			DPB0094RespItem item = new DPB0094RespItem();
			item.setOpenApiKeyId(oak.getOpenApiKeyId());
			item.setClientId(clientId);
			item.setClientName(clientName);
			item.setClientAlias(clientAlias);
			item.setOpenApiKey(oak.getOpenApiKey());
			item.setOpenApiKeyAlias(oak.getOpenApiKeyAlias());
			item.setCreateDateTime(createDateTime);
			item.setExpiredAt(getDateTime(oak.getExpiredAt()));// 格式: yyyy/MM/dd
			item.setRevokedAt(getDateTime(oak.getRevokedAt()));// 格式: yyyy/MM/dd
			item.setOpenApiKeyStatus(status);
			item.setOpenApiKeyStatusName(statusName);
			
			dataList.add(item);
		}
		return dataList;
	}

	private TsmpOpenApiKey getLastRecordFromPrevPage(Long id) {
		if (id != null) {
			Optional<TsmpOpenApiKey> opt = getTsmpOpenApiKeyDao().findById(id);
			return opt.orElseThrow(() -> {return TsmpDpAaRtnCode._1297.throwing();});
		}
		return null;
	}

	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(null);// yyyy/MM/dd
		return dtStr;
	}
	
	private Date plusDay(Date dt, int days) {
		LocalDateTime ldt = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		ldt = ldt.plusDays(days);
		return Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0094");
		return this.pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
}
