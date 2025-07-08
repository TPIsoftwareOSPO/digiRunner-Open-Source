package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.UrlCodecHelper;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0044NewsItem;
import tpi.dgrv4.dpaa.vo.DPB0044Req;
import tpi.dgrv4.dpaa.vo.DPB0044Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class DPB0044Service {
	private final TsmpDpNewsDao tsmpDpNewsDao;
	private final TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private final TsmpOrganizationDao tsmpOrganizationDao;
	private final ServiceConfig serviceConfig;
	private final BcryptParamHelper helper;

	private Integer pageSize;
	
	public DPB0044Resp queryNewsLike_v3_4(TsmpAuthorization tsmpAuthorization, DPB0044Req req, ReqHeader reqHeader) {
		DPB0044Resp resp = new DPB0044Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			TsmpDpNews lastRecord = getLastRecordFromPrevPage(req.getNewsId());
			String typeItemNo = req.getTypeItemNo();
			String enFlagEncode = req.getEnFlagEncode();//啟用/停用	ex:1 / 0 , 使用BcryptParam設計
			String fbTypeEncode = req.getFbTypeEncode();//前後台分類	ex:FRONT / BACK , 使用BcryptParam設計, itemNo="FB_FLAG"
			
			//chk param
			if (!StringUtils.hasLength(enFlagEncode)){
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (!StringUtils.hasLength(fbTypeEncode)){
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			// 檢查日期格式
			String startDateStr = req.getQueryStartDate();
			String endDateStr = req.getQueryEndDate();
			Optional<Date> dpb0044_opt_s = DateTimeUtil.stringToDateTime(startDateStr, DateTimeFormatEnum.西元年月日_2);
			Optional<Date> opt_e = DateTimeUtil.stringToDateTime(endDateStr, DateTimeFormatEnum.西元年月日_2);
			if ( !(dpb0044_opt_s.isPresent() && opt_e.isPresent()) ) {
				throw TsmpDpAaRtnCode._1295.throwing();
			}
			
			// 檢查日期邏輯
			// 使用DateTimeUtil轉出來的Date, 時間都是00:00:00
			final Date dpb0044_startDate = dpb0044_opt_s.orElse(null);
			Date endDate = opt_e.orElse(null);
			if (dpb0044_startDate.compareTo(endDate) > 0) {
				throw TsmpDpAaRtnCode._1295.throwing();
			} else {
				/* 假設查詢同一天(1911/01/01~1911/01/01) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/02 00:00:00
				 * 不同天(1911/01/01~1911/01/03) 變成查詢 1911/01/01 00:00:00 ~ 1911/01/04 00:00:00
				 * 因為SQL條件是 createDateTime >= :startDate and createDateTime < :endDate
				 */
				endDate = plusDay(endDate, 1);//加一天
			}
			
			TPILogger.tl.debug(String.format("DPB0044Service: %s ~ %s", debugDate(dpb0044_startDate), debugDate(endDate)));
			
//			String nowDateStr = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_2);
//
//
//			Optional<Date> opt_n = DateTimeUtil.stringToDateTime(nowDateStr, DateTimeFormatEnum.西元年月日_2);
//			if(opt_n.isEmpty()) {
//				throw TsmpDpAaRtnCode._1295.throwing();
//			}
			Date nowDate = DateTimeUtil.now();
			nowDate = plusDay(nowDate, 1);//加一天
			
			
			String enFlag = getDecode(enFlagEncode, "ENABLE_FLAG", reqHeader.getLocale());//解碼
			String fbType = getDecode(fbTypeEncode, "FB_FLAG", reqHeader.getLocale());//解碼
 
			List<TsmpDpNews> news = getTsmpDpNewsDao().queryLike(lastRecord, words, dpb0044_startDate, endDate, nowDate, 
					typeItemNo,	enFlag, fbType, getPageSize());

			if (CollectionUtils.isEmpty(news)) {
				throw TsmpDpAaRtnCode._1198.throwing();
			}

			List<DPB0044NewsItem> dataList = getDPB0044NewsItemList(news, reqHeader.getLocale());
			resp.setDataList(dataList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private Date plusDay(Date dt, int days) {
		LocalDateTime ldt = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		ldt = ldt.plusDays(days);
		return Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}

	private String debugDate(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
	}
	
	private TsmpDpNews getLastRecordFromPrevPage(Long newsId) {
		if (newsId != null) {
			Optional<TsmpDpNews> opt = getTsmpDpNewsDao().findById(newsId);
			return opt.orElseThrow(() -> {return TsmpDpAaRtnCode._1297.throwing();});
		}
		return null;
	}
	
	protected String getDecode(String encode, String itemNo, String locale) {
		String decode = null;
		try {
			decode = getBcryptParamHelper().decode(encode, itemNo, locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decode;
	}
 
	private List<DPB0044NewsItem> getDPB0044NewsItemList(List<TsmpDpNews> tsmpDpNewsList, String locale) throws Exception {
		List<DPB0044NewsItem> dataList = new ArrayList<DPB0044NewsItem>();
		for (TsmpDpNews tsmpDpNews : tsmpDpNewsList) {
			DPB0044NewsItem vo = getDPB0044NewsItem(tsmpDpNews, locale);
			dataList.add(vo);
		}
		return dataList;
	}

	private DPB0044NewsItem getDPB0044NewsItem(TsmpDpNews tsmpDpNews, String locale) throws Exception {
		DPB0044NewsItem data = new DPB0044NewsItem();
		data.setNewsId(tsmpDpNews.getNewsId());
		data.setNewTitle(tsmpDpNews.getNewTitle());
		
		// DB Save plain text 需要做 UrlEncode
		String newContent = tsmpDpNews.getNewContent();
		// DB 為明文時, Encode String 回傳
		newContent = UrlCodecHelper.getEncode(newContent);
//		this.logger.debug("=== [ DPB0044內容 ] Encode ===");		
//		this.logger.debug("{}",newContent);		
		data.setNewContent(newContent);

		String status = tsmpDpNews.getStatus();// 啟用狀態,為 0 或 1
		String statusName = TsmpDpDataStatus.text(status); // 啟用狀態中文
		data.setStatus(status);
		data.setStatusName(statusName);
		Optional<String> opt_date = DateTimeUtil.dateTimeToString(tsmpDpNews.getPostDateTime(),
				DateTimeFormatEnum.西元年月日_2);
		if (opt_date.isPresent()) {
			data.setPostDateTime(opt_date.get());
		}

		String subitemNo = tsmpDpNews.getRefTypeSubitemNo();// 公告類型代碼
		String subitemName = getSubitemName(subitemNo, locale);// 公告類型中文
		data.setTypeItemNo(subitemNo);
		data.setTypeItemNoName(subitemName);
		String orgId = tsmpDpNews.getOrgId();// 組織單位ID
		String orgName = getOrgName(orgId);// 組織單位名稱
		data.setOrgId(orgId);
		data.setOrgName(orgName);
		data.setLv(tsmpDpNews.getVersion());

		return data;
	}

	/**
	 * 取得 公告類型code 對映的 公告類型中文
	 * 
	 * @param subitemNo
	 * @return
	 */
	private String getSubitemName(String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId("NEWS_TYPE", subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo == null) {
			return null;
		}

		String dpb0044_subitemName = vo.getSubitemName();

		return dpb0044_subitemName;
	}

	/**
	 * 取得 組織單位ID 對映的 組織單位名稱
	 * 
	 * @param orgId
	 * @return
	 */
	private String getOrgName(String orgId) {
		Optional<TsmpOrganization> opt = getTsmpOrganizationDao().findById(orgId);
		if (!opt.isPresent()) {
			return null;
		}

		TsmpOrganization n = opt.get();
		String dpb0044_orgName = n.getOrgName();

		return dpb0044_orgName;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
	
	protected TsmpDpNewsDao getTsmpDpNewsDao() {
		return this.tsmpDpNewsDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0044");
		return this.pageSize;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
}
