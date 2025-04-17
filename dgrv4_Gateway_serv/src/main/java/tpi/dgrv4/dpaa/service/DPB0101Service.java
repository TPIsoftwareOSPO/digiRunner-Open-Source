package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0101Cron;
import tpi.dgrv4.dpaa.vo.DPB0101Items;
import tpi.dgrv4.dpaa.vo.DPB0101Req;
import tpi.dgrv4.dpaa.vo.DPB0101Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptRjobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

import static tpi.dgrv4.dpaa.util.ServiceUtil.*;

@Service
public class DPB0101Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private BcryptParamHelper bcryptParamHelper;
	private ApptRjobDispatcher apptRjobDispatcher;

	private ObjectMapper om = new ObjectMapper();

	@Autowired
	public DPB0101Service(TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy, TsmpDpApptRjobDao tsmpDpApptRjobDao,
			TsmpDpApptRjobDDao tsmpDpApptRjobDDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			BcryptParamHelper bcryptParamHelper, ApptRjobDispatcher apptRjobDispatcher) {
		super();
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpApptRjobDDao = tsmpDpApptRjobDDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.bcryptParamHelper = bcryptParamHelper;
		this.apptRjobDispatcher = apptRjobDispatcher;
	}

	public DPB0101Resp createRjob(TsmpAuthorization auth, DPB0101Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		String userName = checkReq(auth, req);
		Long effDateTime = checkPastDate( req.getEffDateTime() );
		Long invDateTime = checkPastDate( req.getInvDateTime() );
		this.logger.debug(String.format("eff ~ inv = %s ~ %s", req.getEffDateTime(), req.getInvDateTime()));
		if (effDateTime != null && invDateTime != null && invDateTime.compareTo(effDateTime) < 0) {
			this.logger.debug("結束時間不得小於起始時間");
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		checkItems(req.getRjobItems(), locale);
		Map<String, String> checkCronResult = checkCron(req.getCronJson(), locale);
		this.logger.debug("cron expression = " + checkCronResult.get("cronExpression"));
		this.logger.debug("cron desc = " + checkCronResult.get("cronDesc"));
		
		return doSave(userName, req, checkCronResult, effDateTime, invDateTime, locale);
	}

	private String checkReq(TsmpAuthorization auth, DPB0101Req req) {
		String userName = auth.getUserName();
		String rjobName = req.getRjobName();
		if (
			StringUtils.isEmpty(userName) ||
			StringUtils.isEmpty(rjobName)
		) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		/**
		 * 不分中、英文,
		 * 名稱: 最長30字元;
		 * 備註: 最常100字元;
		 */
		String remark = req.getRemark();
		if (
			(rjobName.length() > 30) ||
			(!StringUtils.isEmpty(remark) && remark.length() > 100)
		) {
			throw TsmpDpAaRtnCode._1220.throwing();
		}
		return userName;
	}

	private Long checkPastDate(String dtStr) {
		if (StringUtils.isEmpty(dtStr)) {
			return null;
		}
		Date dt = getDate(dtStr, new TsmpDpAaException(TsmpDpAaRtnCode._1295));
		if (dt == null) {
			return null;
		}
		if (dt.compareTo(DateTimeUtil.now()) < 0) {
			throw TsmpDpAaRtnCode._1227.throwing();
		}
		return dt.getTime();
	}

	private Date getDate(String dtStr, TsmpDpAaException exception) {
		Optional<Date> opt = DateTimeUtil.stringToDateTime(dtStr, DateTimeFormatEnum.西元年月日時分秒_2);
		if (!opt.isPresent() && exception != null) {
			throw exception;
		}
		return opt.orElse(null);
	}

	private void checkItems(List<DPB0101Items> items, String locale) {
		if (items == null || items.isEmpty()) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String dpb0101_refItemNo = null;
		String refSubitemNo = null;
		Integer dpb0101_sortBy = null;
		Set<Integer> sorts = new HashSet<>();	// 用來檢查是否有重複的[執行順序]
		for (DPB0101Items item : items) {
			dpb0101_refItemNo = item.getRefItemNo();
			refSubitemNo = item.getRefSubitemNo();
			dpb0101_sortBy = item.getSortBy();
			if (StringUtils.isEmpty(dpb0101_refItemNo) || dpb0101_sortBy == null || dpb0101_sortBy < 0) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			if (sorts.contains(dpb0101_sortBy)) {
				throw TsmpDpAaRtnCode._1284.throwing("SORT_BY");
			} else {
				sorts.add(dpb0101_sortBy);
			}
			
			try {
				dpb0101_refItemNo = getBcryptParamHelper().decode(dpb0101_refItemNo, "SCHED_CATE1", locale);
				item.setRefItemNo(dpb0101_refItemNo);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
			
			if (!StringUtils.isEmpty(refSubitemNo)) {
				try {
					refSubitemNo = getBcryptParamHelper().decode(refSubitemNo, dpb0101_refItemNo, locale);
					item.setRefSubitemNo(refSubitemNo);
				} catch (BcryptParamDecodeException e) {
					throw TsmpDpAaRtnCode._1299.throwing();
				}
			}

			checkSchedCate(dpb0101_refItemNo, refSubitemNo, locale);
		}
	}

	private void checkSchedCate(String itemNo, String subitemNo, String locale) {
		TsmpDpItems dpb0101_items = getItemsById("SCHED_CATE1", itemNo, false, locale);
		if (dpb0101_items == null) {
			throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
		}
		// 判斷是否有設定子項目
		if (!StringUtils.isEmpty(subitemNo)) {
			String param1 = dpb0101_items.getParam1();
			if ("-1".equals(param1)) {
				// 不應該有子項目
				throw TsmpDpAaRtnCode._1290.throwing();
			}
			dpb0101_items = getItemsById(itemNo, subitemNo, false, locale);
			if (dpb0101_items == null) {
				throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
			}
		}
	}

	protected Map<String, String> checkCron(DPB0101Cron cron, String localeStr) {
		if (cron == null) {
			this.logger.error("沒有傳入表單物件");
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		
		/**
		 * 如果有設定 frequency, 表示為[簡易版]表單,<br>
		 * 需要再翻譯成[完整版]表單再 parsing
		 */
		StringBuffer cronDesc_1 = new StringBuffer();
		Integer frequency = cron.getFrequency();
		if (frequency != null) {
			cron = transitSimpleForm(cron, cronDesc_1, localeStr);
		}
		
		/**
		 * 對應 spring cron expression:
		 * [0]=秒, [1]=分, [2]=時, [3]=日, [4]=月, [5]=週
		 */
		StringBuffer cronDesc_2 = new StringBuffer();
		String[] cronExpression = new String[6];
		checkSecond(cron, cronExpression, cronDesc_2, localeStr);
		checkMinute(cron, cronExpression, cronDesc_2, localeStr);
		checkHour(cron, cronExpression, cronDesc_2, localeStr);
		checkDay(cron, cronExpression, cronDesc_2, localeStr);
		checkMonth(cron, cronExpression, cronDesc_2, localeStr);
		
		Map<String, String> checkCronResult = new HashMap<>();
		checkCronResult.put("cronExpression", String.join(" ", cronExpression));
		checkCronResult.put("cronDesc", (cronDesc_1.length() > 0 ? cronDesc_1.toString() : cronDesc_2.toString()));
		
		// 驗證組出來的表達式是否正確
		try {
			CronExpression.isValidExpression(checkCronResult.get("cronExpression"));
		} catch (Exception e) {
			this.logger.debug("表達式組成錯誤: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		
		return checkCronResult;
	}

	private DPB0101Cron transitSimpleForm(DPB0101Cron cron, StringBuffer cronDesc, String localeStr) {
		DPB0101Cron newCron = new DPB0101Cron();
		Integer frequency = cron.getFrequency();
		switch(frequency) {
			// 每一天
			case 0:
				newCron.setMonthGroup(0);
				newCron.setDayGroup(0);
				newCron.setDayType(0);
				//cronDesc.append("每一天");
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2013, localeStr) );
				break;
			// 日期範圍
			case 1:
				newCron.setMonthGroup(0);
				newCron.setDayGroup(1);
				newCron.setDayRange(cron.getDayRange());
				//cronDesc.append("每個月" + join("、", cron.getDayRange()) + "號");
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2014, localeStr, join(", ", cron.getDayRange())) );
				break;
			// 星期範圍
			case 2:
				newCron.setMonthGroup(0);
				newCron.setDayGroup(5);
				newCron.setWeekRange(cron.getWeekRange());
				//cronDesc.append("每星期" + String.join("、", toWeekRangeNames(cron.getWeekRange(), localeStr)));
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2015, localeStr, String.join(", ", toWeekRangeNames(cron.getWeekRange(), localeStr))) );
				break;
			// 每十分鐘
			case 3:
				newCron.setDayGroup(0);
				newCron.setDayType(0);
				newCron.setMonthGroup(0);
				// 時
				newCron.setHourGroup(0);
				// 分
				newCron.setMinuteStart(0);
				newCron.setMinuteInterval(10);
				newCron.setMinuteGroup(2);
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2017, localeStr) );
				break;
			// 每半小時
			case 4:
				newCron.setMonthGroup(0);
				newCron.setDayType(0);
				newCron.setDayGroup(0);
				// 時
				newCron.setHourGroup(0);
				// 分
				newCron.setMinuteStart(0);
				newCron.setMinuteGroup(2);
				newCron.setMinuteInterval(30);
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2018, localeStr) );
				break;
			// 每一小時
			case 5:
				newCron.setDayType(0);
				newCron.setMonthGroup(0);
				newCron.setDayGroup(0);
				// 時
				newCron.setHourInterval(1);
				newCron.setHourGroup(2);
				newCron.setHourStart(0);
				// 分
				newCron.setMinuteGroup(1);
				newCron.setMinuteRange(Arrays.asList(new Integer[] {0}));
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2019, localeStr) );
				break;
			// 每 2 小時
			case 6:
				setFrequency_every_N_hours(newCron, 2);
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2020, localeStr, String.valueOf(2)) );
				break;
			// 每 6 小時
			case 7:
				setFrequency_every_N_hours(newCron, 6);
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2020, localeStr, String.valueOf(6)) );
				break;
			// 每 12 小時
			case 8:
				setFrequency_every_N_hours(newCron, 12);
				cronDesc.append( getMessage(TsmpDpAaRtnCode._2020, localeStr, String.valueOf(12)) );
				break;
			default:
				this.logger.debug("頻率參數值錯誤: " + frequency);
				throw TsmpDpAaRtnCode._1290.throwing();
		}
		// 0~2
		if (frequency > -1 && frequency < 3) {
			// 時
			newCron.setHourGroup(1);
			if (cron.getHour() == null) cron.setHour(0);		// 簡單版若沒有傳入 hour 就預設為 0
			newCron.setHourRange(Arrays.asList(new Integer[] {cron.getHour()}));
			// 分
			newCron.setMinuteGroup(1);
			if (cron.getMinute() == null) cron.setMinute(0);	// 簡單版若沒有傳入 minute 就預設為 0
			newCron.setMinuteRange(Arrays.asList(new Integer[] {cron.getMinute()}));
			
			String timeStr = String.format("%02d:%02d", cron.getHour(), cron.getMinute());
			cronDesc.append( getMessage(TsmpDpAaRtnCode._2016, localeStr, timeStr) );
		}
		// 秒 (簡單版並沒有設定"秒", 以習慣設定為"0")
		newCron.setSecondGroup(1);
		newCron.setSecondRange(Arrays.asList(new Integer[] {0}));

		return newCron;
	}

	private List<String> toWeekRangeNames(List<Integer> weekRange, String localeStr) {
		if (weekRange != null && !weekRange.isEmpty()) {
			Locale locale = parseLocale(localeStr);
			
			List<String> names = new ArrayList<>();
			// 0=日, 1=一...
			for (Integer i : weekRange) {
				names.add( toDayOfWeekDisplayName(i, locale) );
			}
			return names;
		}
		return Collections.emptyList();
	}

	private String toDayOfWeekDisplayName(Integer i, String localeStr) {
		Locale locale = parseLocale(localeStr);
		return toDayOfWeekDisplayName(i, locale);
	}

	/**
	 * 轉換星期為名稱
	 * @param i: 0=日, 1=一...
	 * @param locale
	 * @return
	 */
	private String toDayOfWeekDisplayName(Integer i, Locale locale) {
		if (i == null || i < 0 || i > 6) {
			this.logger.error("錯誤的星期值(0~6): " + i);
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		DayOfWeek dow = DayOfWeek.of( (i == 0 ? 7 : i) );
		// 繁中範例: 一、二..., 英文範例: Mon, Tue...
		TextStyle ts = TextStyle.SHORT;
		if (Locale.CHINESE.getLanguage().equals(locale.getLanguage())) {
			ts = TextStyle.NARROW;
		}
		return dow.getDisplayName(ts, locale);
	}

	protected void setFrequency_every_N_hours(DPB0101Cron cron, Integer hourInterval) {
		cron.setMonthGroup(0);
		cron.setDayGroup(0);
		cron.setDayType(0);
		// 時
		cron.setHourGroup(2);
		cron.setHourStart(0);
		cron.setHourInterval(hourInterval);
		// 分
		cron.setMinuteGroup(1);
		cron.setMinuteRange(Arrays.asList(new Integer[] {0}));
	}

	protected void checkSecond(DPB0101Cron cron, String[] cronExp, StringBuffer cronDesc, String localeStr) {
		Integer group = cron.getSecondGroup();
		if (group == null || !(group.equals(0) || group.equals(1) || group.equals(2))) {
			this.logger.error("[秒]錯誤的群組值: " + group);
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		cronDesc.append("秒: ");
		switch(group) {
			case 0:
				cronExp[0] = "*";
				cronDesc.append("每1秒");
				break;
			case 1:
				List<Integer> ranges = cron.getSecondRange();
				if (ranges == null || ranges.isEmpty()) {
					this.logger.error("[秒]未傳入範圍值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[0] = combineRanges(ranges, 0, 59, new TsmpDpAaException(TsmpDpAaRtnCode._1242));
				cronDesc.append("第" + join("、", ranges) + "秒");
				break;
			case 2:
				Integer dpb0101Second_start = cron.getSecondStart();
				Integer interval = cron.getSecondInterval();
				if (dpb0101Second_start == null || interval == null) {
					this.logger.error("[秒]未傳入間歇值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (dpb0101Second_start < 0 || dpb0101Second_start > 59) {
					this.logger.error("[秒]錯誤的起始值: " + dpb0101Second_start);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (interval < 1 || interval > 60) {
					this.logger.error("[秒]錯誤的間隔值: " + interval);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[0] = dpb0101Second_start + "/" + interval;
				cronDesc.append("從第" + dpb0101Second_start + "秒起，每隔" + interval + "秒");
				break;
			default:
				cronExp[0] = "*";
				cronDesc.append("每1秒");
				break;
		}
		this.logger.debug("[秒]: " + cronExp[0]);
	}

	protected void checkMinute(DPB0101Cron cron, String[] cronExp, StringBuffer cronDesc, String localeStr) {
		Integer group = cron.getMinuteGroup();
		if (group == null || !(group.equals(0) || group.equals(1) || group.equals(2))) {
			this.logger.error("[分]錯誤的群組值: " + group);
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		cronDesc.append("\n分: ");
		switch(group) {
			case 0:
				cronExp[1] = "*";
				cronDesc.append("每1分");
				break;
			case 1:
				List<Integer> ranges = cron.getMinuteRange();
				if (ranges == null || ranges.isEmpty()) {
					this.logger.error("[分]未傳入範圍值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[1] = combineRanges(ranges, 0, 59, new TsmpDpAaException(TsmpDpAaRtnCode._1242));
				cronDesc.append("第" + join("、", ranges) + "分");
				break;
			case 2:
				Integer dpb0101Minute_start = cron.getMinuteStart();
				Integer interval = cron.getMinuteInterval();
				if (dpb0101Minute_start == null || interval == null) {
					this.logger.error("[分]未傳入間歇值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (dpb0101Minute_start < 0 || dpb0101Minute_start > 59) {
					this.logger.error("[分]錯誤的起始值: " + dpb0101Minute_start);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (interval < 1 || interval > 60) {
					this.logger.error("[分]錯誤的間隔值: " + interval);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[1] = dpb0101Minute_start + "/" + interval;
				cronDesc.append("從第" + dpb0101Minute_start + "分起，每隔" + interval + "分");
				break;
			default:
				cronExp[1] = "*";
				cronDesc.append("每1分");
				break;
		}
		this.logger.debug("[分]: " + cronExp[1]);
	}

	protected void checkHour(DPB0101Cron cron, String[] cronExp, StringBuffer cronDesc, String localeStr) {
		Integer group = cron.getHourGroup();
		if (group == null || !(group.equals(0) || group.equals(1) || group.equals(2))) {
			this.logger.error("[時]錯誤的群組值: " + group);
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		cronDesc.append("\n時: ");
		switch(group) {
			case 0:
				cronExp[2] = "*";
				cronDesc.append("每1小時");
				break;
			case 1:
				List<Integer> ranges = cron.getHourRange();
				if (ranges == null || ranges.isEmpty()) {
					this.logger.error("[時]未傳入範圍值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[2] = combineRanges(ranges, 0, 23, new TsmpDpAaException(TsmpDpAaRtnCode._1242));
				cronDesc.append("第" + join("、", ranges) + "時");
				break;
			case 2:
				Integer dpb0101Hour_start = cron.getHourStart();
				Integer interval = cron.getHourInterval();
				if (dpb0101Hour_start == null || interval == null) {
					this.logger.error("[時]未傳入間歇值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (dpb0101Hour_start < 0 || dpb0101Hour_start > 23) {
					this.logger.error("[時]錯誤的起始值: " + dpb0101Hour_start);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (interval < 1 || interval > 24) {
					this.logger.error("[時]錯誤的間隔值: " + interval);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[2] = dpb0101Hour_start + "/" + interval;
				cronDesc.append("從第" + dpb0101Hour_start + "時起，每隔" + interval + "小時");
				break;
			default:
				cronExp[2] = "*";
				cronDesc.append("每1小時");
				break;
		}
		this.logger.debug("[時]: " + cronExp[2]);
	}

	protected void checkDay(DPB0101Cron cron, String[] cronExp, StringBuffer cronDesc, String localeStr) {
		Integer group = cron.getDayGroup();
		if (group == null || group < 0 || group > 8 //
			|| (group == 3 || group == 4 || group == 7 || group == 8)) {	// Spring的Cron不支援"L"、"W"、"#"在dayOfMonth及dayOfWeek欄位
			this.logger.error("[日]錯誤的群組值: " + group);
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		cronDesc.append("\n日: ");
		switch(group) {
			case 0:
				Integer dayType = cron.getDayType();
				if (dayType == null || dayType < 0 || dayType > 2 //
					|| (dayType == 1 || dayType == 2)) {	// Spring的Cron不支援"L"、"W"、"#"在dayOfMonth及dayOfWeek欄位
					//this.logger.error("[日]必須指定(0=每一天, 1=每月最後一天, 2=每月最後一個週間日): " + dayType);
					this.logger.error("[日]必須指定為(0=每一天): " + dayType);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				// 每1天
				if (dayType.equals(0)) {
					cronExp[3] = "?";
					cronExp[5] = "*";
					cronDesc.append("每1天");
				// 每月最後一天
				} else if (dayType.equals(1)) {
					cronExp[3] = "L";
					cronExp[5] = "?";
					cronDesc.append("每月最後一天");
				// 每月最後一個週間日 (weekday)
				} else if (dayType.equals(2)) {
					cronExp[3] = "LW";
					cronExp[5] = "?";
					cronDesc.append("每月最後一個週間日");
				}
				break;
			// __號
			case 1:
				List<Integer> ranges = cron.getDayRange();
				if (ranges == null || ranges.isEmpty()) {
					this.logger.error("[日]未傳入日期範圍值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = combineRanges(ranges, 1, 31, new TsmpDpAaException(TsmpDpAaRtnCode._1242));
				cronExp[5] = "?";
				cronDesc.append(join("、", ranges) + "號");
				break;
			// 從__號起，每隔__天
			case 2:
				Integer dpb0101Day_start = cron.getDayStart();
				Integer interval = cron.getDayInterval();
				if (dpb0101Day_start == null || interval == null) {
					this.logger.error("[日]未傳入日期的起始值或間隔值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (dpb0101Day_start < 1 || dpb0101Day_start > 31) {
					this.logger.error("[日]錯誤的日期起始值: " + dpb0101Day_start);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (interval < 1 || interval > 31) {
					this.logger.error("[日]錯誤的日期間隔值: " + interval);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = dpb0101Day_start + "/" + interval;
				cronExp[5] = "?";
				cronDesc.append("從" + dpb0101Day_start + "號起，每隔" + interval + "天");
				break;
			// 每月倒數第__天
			case 3:
				Integer countdown = cron.getDayCountdown();
				if (countdown == null) {
					this.logger.error("[日]未指定每月倒數第幾天");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (countdown < 1 || countdown > 31) {
					this.logger.error("[日]錯誤的日期倒數值: " + countdown);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = "L-" + countdown;
				cronExp[5] = "?";
				cronDesc.append("每月倒數第" + countdown + "天");
				break;
			// 最接近__號的週間日
			case 4:
				Integer nearest = cron.getDayNearest();
				if (nearest == null) {
					this.logger.error("[日]未指定最接近幾號的週間日");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (nearest < 1 || nearest > 31) {
					this.logger.error("[日]錯誤的日期接近值: " + nearest);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = nearest + "W";
				cronExp[5] = "?";
				cronDesc.append("最接近" + nearest + "號的週間日");
				break;
			// 星期__
			case 5:
				ranges = cron.getWeekRange();
				if (ranges == null || ranges.isEmpty()) {
					this.logger.error("[日]未傳入星期範圍值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = "?";
				cronExp[5] = combineRanges(ranges, 0, 6, new TsmpDpAaException(TsmpDpAaRtnCode._1242));
				cronDesc.append("星期" + String.join("、", toWeekRangeNames(ranges, localeStr)));
				break;
			// 從星期__起，每隔__天
			case 6:
				dpb0101Day_start = cron.getWeekStart();
				interval = cron.getWeekInterval();
				if (dpb0101Day_start == null || interval == null) {
					this.logger.error("[日]未傳入星期的起始值或間隔值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (dpb0101Day_start < 0 || dpb0101Day_start > 6) {
					this.logger.error("[日]錯誤的星期起始值: " + dpb0101Day_start);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (interval < 1 || interval > 7) {
					this.logger.error("[日]錯誤的星期間隔值: " + interval);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = "?";
				cronExp[5] = dpb0101Day_start + "/" + interval;
				cronDesc.append("從星期" + toDayOfWeekDisplayName(dpb0101Day_start, localeStr) + "起，每隔" + interval + "天");
				break;
			// 每月最後一個星期__
			case 7:
				Integer last = cron.getWeekLast();
				if (last == null) {
					this.logger.error("[日]未指定每月最後一個星期幾");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (last < 0 || last > 6) {
					this.logger.error("[日]每月最後一個星期幾，不可指定為: " + last);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = "?";
				cronExp[5] = last + "L";
				cronDesc.append("每月最後一個星期" + toDayOfWeekDisplayName(last, localeStr));
				break;
			// 每月第__個星期__
			case 8:
				Integer weekTh1 = cron.getWeekTh1();
				Integer weekTh2 = cron.getWeekTh2();
				if (weekTh1 == null || weekTh2 == null) {
					this.logger.error("[日]未指定每月第幾個星期幾");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (weekTh1 < 1 || weekTh1 > 5) {
					this.logger.error(String.format("[日]不能指定第%d個星期幾", weekTh1));
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (weekTh2 < 0 || weekTh2 > 6) {
					this.logger.error("[日]不能指定第幾個星期" + weekTh2);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[3] = "?";
				cronExp[5] = weekTh2 + "#" + weekTh1;
				cronDesc.append("每月第" + weekTh1 + "個星期" + toDayOfWeekDisplayName(weekTh2, localeStr));
				break;
			default:
				cronExp[3] = "?";
				cronExp[5] = "*";
				cronDesc.append("每1天");
				break;
		}
		this.logger.debug(String.format("[日]: %s, %s", cronExp[3], cronExp[5]));
	}

	protected void checkMonth(DPB0101Cron cron, String[] cronExp, StringBuffer cronDesc, String localeStr) {
		Integer group = cron.getMonthGroup();
		if (group == null || !(group.equals(0) || group.equals(1) || group.equals(2))) {
			this.logger.error("[月]錯誤的群組值: " + group);
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		cronDesc.append("\n月: ");
		switch(group) {
			case 0:
				cronExp[4] = "*";
				cronDesc.append("每1個月");
				break;
			case 1:
				List<Integer> ranges = cron.getMonthRange();
				if (ranges == null || ranges.isEmpty()) {
					this.logger.error("[月]未傳入範圍值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[4] = combineRanges(ranges, 1, 12, new TsmpDpAaException(TsmpDpAaRtnCode._1242));
				cronDesc.append(join("、", ranges) + "月");
				break;
			case 2:
				Integer dpb0101Month_start = cron.getMonthStart();
				Integer interval = cron.getMonthInterval();
				if (dpb0101Month_start == null || interval == null) {
					this.logger.error("[月]未傳入間歇值");
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (dpb0101Month_start < 1 || dpb0101Month_start > 12) {
					this.logger.error("[月]錯誤的起始值: " + dpb0101Month_start);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				if (interval < 1 || interval > 12) {
					this.logger.error("[月]錯誤的間隔值: " + interval);
					throw TsmpDpAaRtnCode._1242.throwing();
				}
				cronExp[4] = dpb0101Month_start + "/" + interval;
				cronDesc.append("從" + dpb0101Month_start + "月起，每隔" + interval + "個月");
				break;
			default:
				cronExp[4] = "*";
				cronDesc.append("每1個月");
				break;
		}
		this.logger.debug("[月]: " + cronExp[4]);
	}

	/**
	 * 轉換成"value1-value2"的範圍表達式<br>
	 * 若非連續區段, 則返回多值表達式"value1,value3"
	 * @param ranges
	 * @param min 有傳入則會驗證每個元素是否>=min, 否則拋出exeption的錯誤
	 * @param max 有傳入則會驗證每個元素是否<=max, 否則拋出exeption的錯誤
	 * @param exception
	 * @return
	 */
	protected String combineRanges(List<Integer> ranges, Integer min, Integer max, TsmpDpAaException exception) {
		if (ranges == null || ranges.isEmpty()) {
			return new String();
		}
		
		// 先排序
		try {
			Collections.sort(ranges);
		} catch (Exception e) {
			this.logger.error("List排序失敗: " + ranges);
			throw exception;
		}

		List<String> rangeExpression = new ArrayList<>();
		String delimiter = "-";

		Integer range = null;
		for (int i = 0; i < ranges.size(); i++) {
			range = ranges.get(i);
			
			if (range == null) {
				this.logger.error("範圍內不可指定空值");
				throw exception;
			}

			if ( (min != null && range < min) || (max != null && range > max) ) {
				this.logger.error(String.format("\"%d\"已超出範圍值%d~%d", range, min, max));
				throw exception;
			}
			
			rangeExpression.add(String.valueOf(range));

			if (i == 0) continue;
			
			if ( Math.abs(range - ranges.get(i - 1)) > 1 ) {
				delimiter = ",";
			}
			
			if ("-".equals(delimiter) && i == ranges.size() - 1) {
				// 只留頭尾
				rangeExpression.clear();
				rangeExpression.add( String.valueOf(ranges.get(0)) );
				rangeExpression.add( String.valueOf(ranges.get( ranges.size() - 1 )) );
			}
		}
		
		return String.join(delimiter, rangeExpression);
	}

	@Transactional
	public DPB0101Resp doSave(String userName, DPB0101Req req, Map<String, String> checkCronResult, Long effDateTime, Long invDateTime, String locale) {
		String cronJson = getCronJson(req.getCronJson());
		String cronExpression = checkCronResult.get("cronExpression");
		String cronDesc = nvl(checkCronResult.get("cronDesc"));
		
		TsmpDpApptRjob rjob = new TsmpDpApptRjob();
		rjob.setRjobName(req.getRjobName());
		rjob.setCronExpression(cronExpression);
		rjob.setCronJson(cronJson);
		rjob.setCronDesc(cronDesc);
		rjob.setNextDateTime(DateTimeUtil.now().getTime());	// 排程會自動調整執行時間到週期上
		rjob.setLastDateTime(null);
		rjob.setEffDateTime(effDateTime);
		rjob.setInvDateTime(invDateTime);
		rjob.setRemark(req.getRemark());
		rjob.setCreateDateTime(DateTimeUtil.now());
		rjob.setCreateUser(userName);
		
		// 先存排程工作項目檔, 因為呼叫 saveRjob() 時就會排入背景作業, 必須要先有工作項目才能排入
		List<Map<Long, Long>> apptRjobDIds = saveRjobD(rjob, req.getRjobItems());
		// 再存週期排程設定檔(同時在背景啟動)
		rjob = saveRjob(rjob);
		
		DPB0101Resp resp = new DPB0101Resp();
		resp.setApptRjobId(rjob.getApptRjobId());
		resp.setLv(rjob.getVersion());
		resp.setCronExpression(rjob.getCronExpression());
		String nextDateTime = DateTimeUtil.dateTimeToString(new Date(rjob.getNextDateTime()), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
		resp.setNextDateTime(nextDateTime);
		resp.setStatus(rjob.getStatus());
		String statusName = getStatusName(resp.getStatus(), locale);
		resp.setStatusName(statusName);
		resp.setApptRjobDIds(apptRjobDIds);
		return resp;
	}

	public String getCronJson(DPB0101Cron cron) {
		try {
			return this.om.writeValueAsString(cron);
		} catch (JsonProcessingException e) {
			this.logger.error("無法轉換表單為JSON字串");
			throw TsmpDpAaRtnCode._1242.throwing();
		}
	}

	public List<Map<Long, Long>> saveRjobD(TsmpDpApptRjob rjob, List<DPB0101Items> dpb0101Items) {
		List<Map<Long, Long>> apptRjobDIds = new ArrayList<>();

		TsmpDpApptRjobD rjobD = null;
		Map<Long, Long> apptRjobDId = null;
		for (DPB0101Items dpb0101Item : dpb0101Items) {
			rjobD = new TsmpDpApptRjobD();
			apptRjobDId = new HashMap<>();
			rjobD.setApptRjobId(rjob.getApptRjobId());
			rjobD.setRefItemNo(dpb0101Item.getRefItemNo());
			rjobD.setRefSubitemNo(dpb0101Item.getRefSubitemNo());
			rjobD.setInParams(dpb0101Item.getInParams());
			rjobD.setIdentifData(dpb0101Item.getIdentifData());
			rjobD.setSortBy(dpb0101Item.getSortBy());
			rjobD.setCreateDateTime(DateTimeUtil.now());
			rjobD.setCreateUser(rjob.getCreateUser());
			rjobD = getTsmpDpApptRjobDDao().save(rjobD);
			apptRjobDId.put(rjobD.getApptRjobDId(), rjobD.getVersion());
			apptRjobDIds.add(apptRjobDId);
		}

		return apptRjobDIds;
	}

	public TsmpDpApptRjob saveRjob(TsmpDpApptRjob rjob) {
		if (rjob == null) {
			throw TsmpDpAaRtnCode._1242.throwing();
		}
		rjob = getTsmpDpApptRjobDao().save(rjob);
		
		// 將排程加入背景作業
		rjob = getApptRjobDispatcher().activate(rjob.getApptRjobId(), rjob.getCreateUser());
		return rjob;
	}

	public String getStatusName(String status, String locale) {
		if (!StringUtils.isEmpty(status)) {
			TsmpDpItems items = getItemsById("RJOB_STATUS", status, false, locale);
			if (items != null) {
				return items.getSubitemName();
			}
		}
		return new String();
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return i;
	}

	protected String getMessage(TsmpDpAaRtnCode tsmpDpAaRtnCode, String locale, Object... params) {
		String rtnCode = tsmpDpAaRtnCode.getCode();
		Optional<TsmpRtnCode> opt = getTsmpRtnCodeCacheProxy().findById(rtnCode, locale);
		if (opt.isPresent()) {
			TsmpRtnCode tsmpRtnCode = opt.get();
			if (ObjectUtils.isEmpty(params)) {
				return tsmpRtnCode.getTsmpRtnMsg();
			} else {
				Map<String, String> map = new HashMap<>();
				for (int i = 0; i < params.length; i++) {
					map.put(String.valueOf(i), params[i].toString());
				}
				return MailHelper.buildContent(tsmpRtnCode.getTsmpRtnMsg(), map);
			}
		}
		return new String();
	}

	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpApptRjobDDao getTsmpDpApptRjobDDao() {
		return this.tsmpDpApptRjobDDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ApptRjobDispatcher getApptRjobDispatcher() {
		return this.apptRjobDispatcher;
	}

}