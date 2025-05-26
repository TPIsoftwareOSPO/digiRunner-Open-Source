package tpi.dgrv4.dpaa.component.rjob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.DPB0101Service;
import tpi.dgrv4.dpaa.vo.DPB0101Cron;
import tpi.dgrv4.dpaa.vo.DPB0101Items;
import tpi.dgrv4.dpaa.vo.DPB0101Req;
import tpi.dgrv4.dpaa.vo.DPB0101Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * <h2>系統預設的週期排程設定器</h2><br>
 * 系統若有需要預設啟動排程工作，可在此類別註冊。<br>
 * 參考規格: <a href=
 * "https://docs.google.com/spreadsheets/d/1Y6zdsAOyYR5CCTWkXbJY06Y9q4I-tKYisG8CHzMaLuM/edit#gid=2001869163&range=B83">DPB0101Req</a>
 * 
 * @author Kim
 */
public class SystemDefaultRjobInitializer {

	private TPILogger logger = TPILogger.tl;

	private final String REMARK_STAMP = "[SYS]";

	private DPB0101Service dpb0101Service;

	private BcryptParamHelper bcryptParamHelper;

	private TsmpDpApptRjobDao tsmpDpApptRjobDao;

	private List<DPB0101Req> defaultRjobList;

	/**
	 * 把要註冊的 Rjob 加入 defaultRjobList
	 */
	public SystemDefaultRjobInitializer(DPB0101Service dpb0101Service, TsmpDpApptRjobDao tsmpDpApptRjobDao, //
			BcryptParamHelper bcryptParamHelper) {
		this.dpb0101Service = dpb0101Service;
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.bcryptParamHelper = bcryptParamHelper;

		// [報表排程]: 預設每一天的0點10分執行一次，為了統計到前一天的允許數值
		//20250407 no execute
		/*register( //
				"Report Schedule", "預設排程報表", //
				new DPB0101CronBuilder() //
						.frequency(3) // 每10分
						.hour(0) //
						.minute(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("REPORT_BATCH", 8) //
						.sortBy(0) //
						.build());*/

		// [Open API Key 快到期檢查]: 預設每一天的12點1分執行一次
		register( //
				"Open API Key 快到期檢查", "預設Open API Key 快到期檢查", //
				new DPB0101CronBuilder() //
						.frequency(0) // 每一天
						.hour(12) //
						.minute(1) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("OAK_CHK_EXPI", 6) //
						.sortBy(0) //
						.build());

		// [JWE/TLS憑證到期提醒]: 預設每一天的09點00分執行一次
		register( //
				"JWE/TLS Cert Exp", "預設JWE/TLS憑證到期提醒", //
				new DPB0101CronBuilder() //
						.frequency(0) // 每一天
						.hour(9) //
						.minute(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("NOTICE_EXP_CERT", 10) //
						.refSubitemNo("JWE", 0) //
						.sortBy(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("NOTICE_EXP_CERT", 10) //
						.refSubitemNo("TLS", 1) //
						.sortBy(1) //
						.build());

		// [Housekeeping排程]: 預設每一天的2點0分執行一次
		register( //
				"Housekeeping排程", "預設Housekeeping排程", //
				new DPB0101CronBuilder() //
						.frequency(0) // 每一天
						.hour(2) //
						.minute(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("HOUSEKEEPING_BATCH", 9) //
						.sortBy(0) //
						.build());

		// [自動上下架排程]: 預設每一天的1點0分執行一次
		register( //
				"API 自動上架下架排程", "預設 API 自動上架下架排程", //
				new DPB0101CronBuilder() //
						.frequency(0) // 每一天
						.hour(1) //
						.minute(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("API_SCHEDULED", 19) //
						.refSubitemNo("API_LAUNCH", 2) //
						.sortBy(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("API_SCHEDULED", 19) //
						.refSubitemNo("API_REMOVAL", 3) //
						.sortBy(1) //
						.build());

		// [自動上下架排程]: 預設每一天的1點0分執行一次
		register( //
				"API 自動啟用停用排程", "預設 API 自動啟用停用排程", //
				new DPB0101CronBuilder() //
						.frequency(0) // 每一天
						.hour(1) //
						.minute(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("API_SCHEDULED", 19) //
						.refSubitemNo("API_ENABLE", 0) //
						.sortBy(0) //
						.build(), //
				new DPB0101ItemsBuilder(this.bcryptParamHelper) //
						.refItemNo("API_SCHEDULED", 19) //
						.refSubitemNo("API_DISABLE", 1) //
						.sortBy(1) //
						.build());
	}

	public List<DPB0101Req> getDefaultRjobList() {
		if (CollectionUtils.isEmpty(this.defaultRjobList)) {
			return this.defaultRjobList;
		}
		// 檢查是否已有相同的系統預設排程(系統預設的週期排程，會在Remark欄位押上 [SYS] 開頭的標記)
		List<TsmpDpApptRjob> existingRjobs;
		List<DPB0101Req> rjobList = null;
		for (DPB0101Req req : this.defaultRjobList) {
			existingRjobs = this.tsmpDpApptRjobDao.findByRemark(req.getRemark());
			if (CollectionUtils.isEmpty(existingRjobs)) {
				if (rjobList == null) {
					rjobList = new ArrayList<>();
				}
				rjobList.add(req);
			} else {
				this.logger.debug(String.format("[%s] 已有相同的系統預設週期排程: %s", req.getRjobName(), //
						existingRjobs.stream().map((rjob) -> {
							return rjob.getApptRjobId();
						}).collect(Collectors.toList()).toString()));
			}
		}
		return rjobList;
	}

	public void createRjob(DPB0101Req req) {
		TsmpAuthorization auth = getManagerAuth();
		ReqHeader reqHeader = getDefaultReqHeader();
		DPB0101Resp resp = this.dpb0101Service.createRjob(auth, req, reqHeader);
		this.logger.debug("已建立預設的週期排程: " + resp.getApptRjobId());
	}

	private void register(String rjobName, String remark, DPB0101Cron cronJson, DPB0101Items... items) {
		DPB0101Req req = getDPB0101Req(rjobName, remark, cronJson, items);
		register(req);
	}

	private void register(String rjobName, String remark, DPB0101Cron cronJson, String effDateTime, String invDateTime, //
			DPB0101Items... items) {
		DPB0101Req req = getDPB0101Req(rjobName, remark, cronJson, effDateTime, invDateTime, items);
		register(req);
	}

	private void register(DPB0101Req req) {
		if (this.defaultRjobList == null) {
			this.defaultRjobList = new ArrayList<>();
		}
		this.defaultRjobList.add(req);
	}

	private DPB0101Req getDPB0101Req(String rjobName, String remark, DPB0101Cron cronJson, DPB0101Items... items) {
		return getDPB0101Req(rjobName, remark, cronJson, null, null, items);
	}

	private DPB0101Req getDPB0101Req(String rjobName, String remark, DPB0101Cron cronJson, String effDateTime,
			String invDateTime, //
			DPB0101Items... items) {
		List<DPB0101Items> rjobItems = null;
		if (items != null) {
			rjobItems = Arrays.asList(items);
		}

		DPB0101Req req = new DPB0101Req();
		req.setRjobName(rjobName);
		req.setRemark(String.format("%s%s", this.REMARK_STAMP, remark));
		req.setCronJson(cronJson);
		req.setEffDateTime(effDateTime);
		req.setInvDateTime(invDateTime);
		req.setRjobItems(rjobItems);
		return req;
	}

	private TsmpAuthorization getManagerAuth() {
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName("manager");
		return auth;
	}

	private ReqHeader getDefaultReqHeader() {
		ReqHeader reqHeader = new ReqHeader();
		reqHeader.setLocale(LocaleType.EN_US);
		return reqHeader;
	}

	private class DPB0101CronBuilder extends DPB0101Cron {

		public DPB0101CronBuilder frequency(Integer frequency) {
			setFrequency(frequency);
			return this;
		}

		public DPB0101CronBuilder hour(Integer hour) {
			setHour(hour);
			return this;
		}

		public DPB0101CronBuilder minute(Integer minute) {
			setMinute(minute);
			return this;
		}

		public DPB0101CronBuilder monthGroup(Integer monthGroup) {
			setMonthGroup(monthGroup);
			return this;
		}

		public DPB0101CronBuilder monthRange(List<Integer> monthRange) {
			setMonthRange(monthRange);
			return this;
		}

		public DPB0101CronBuilder monthInterval(Integer monthInterval) {
			setMonthInterval(monthInterval);
			return this;
		}

		public DPB0101CronBuilder monthStart(Integer monthStart) {
			setMonthStart(monthStart);
			return this;
		}

		public DPB0101CronBuilder dayGroup(Integer dayGroup) {
			setDayGroup(dayGroup);
			return this;
		}

		public DPB0101CronBuilder dayType(Integer dayType) {
			setDayType(dayType);
			return this;
		}

		public DPB0101CronBuilder dayRange(List<Integer> dayRange) {
			setDayRange(dayRange);
			return this;
		}

		public DPB0101CronBuilder dayStart(Integer dayStart) {
			setDayStart(dayStart);
			return this;
		}

		public DPB0101CronBuilder dayInterval(Integer dayInterval) {
			setDayInterval(dayInterval);
			return this;
		}

		public DPB0101CronBuilder dayCountdown(Integer dayCountdown) {
			setDayCountdown(dayCountdown);
			return this;
		}

		public DPB0101CronBuilder dayNearest(Integer dayNearest) {
			setDayNearest(dayNearest);
			return this;
		}

		public DPB0101CronBuilder weekRange(List<Integer> weekRange) {
			setWeekRange(weekRange);
			return this;
		}

		public DPB0101CronBuilder weekStart(Integer weekStart) {
			setWeekStart(weekStart);
			return this;
		}

		public DPB0101CronBuilder weekInterval(Integer weekInterval) {
			setWeekInterval(weekInterval);
			return this;
		}

		public DPB0101CronBuilder weekLast(Integer weekLast) {
			setWeekLast(weekLast);
			return this;
		}

		public DPB0101CronBuilder weekTh1(Integer weekTh1) {
			setWeekTh1(weekTh1);
			return this;
		}

		public DPB0101CronBuilder weekTh2(Integer weekTh2) {
			setWeekTh2(weekTh2);
			return this;
		}

		public DPB0101CronBuilder hourGroup(Integer hourGroup) {
			setHourGroup(hourGroup);
			return this;
		}

		public DPB0101CronBuilder hourRange(List<Integer> hourRange) {
			setHourRange(hourRange);
			return this;
		}

		public DPB0101CronBuilder hourInterval(Integer hourInterval) {
			setHourInterval(hourInterval);
			return this;
		}

		public DPB0101CronBuilder hourStart(Integer hourStart) {
			setHourStart(hourStart);
			return this;
		}

		public DPB0101CronBuilder minuteGroup(Integer minuteGroup) {
			setMinuteGroup(minuteGroup);
			return this;
		}

		public DPB0101CronBuilder minuteRange(List<Integer> minuteRange) {
			setMinuteRange(minuteRange);
			return this;
		}

		public DPB0101CronBuilder minuteInterval(Integer minuteInterval) {
			setMinuteInterval(minuteInterval);
			return this;
		}

		public DPB0101CronBuilder minuteStart(Integer minuteStart) {
			setMinuteStart(minuteStart);
			return this;
		}

		public DPB0101CronBuilder secondGroup(Integer secondGroup) {
			setSecondGroup(secondGroup);
			return this;
		}

		public DPB0101CronBuilder secondRange(List<Integer> secondRange) {
			setSecondRange(secondRange);
			return this;
		}

		public DPB0101CronBuilder secondInterval(Integer secondInterval) {
			setSecondInterval(secondInterval);
			return this;
		}

		public DPB0101CronBuilder secondStart(Integer secondStart) {
			setSecondStart(secondStart);
			return this;
		}

		public DPB0101Cron build() {
			DPB0101Cron cron = new DPB0101Cron();
			cron.setFrequency(getFrequency());
			cron.setHour(getHour());
			cron.setMinute(getMinute());
			cron.setMonthGroup(getMonthGroup());
			cron.setMonthRange(getMonthRange());
			cron.setMonthInterval(getMonthInterval());
			cron.setMonthStart(getMonthStart());
			cron.setDayGroup(getDayGroup());
			cron.setDayType(getDayType());
			cron.setDayRange(getDayRange());
			cron.setDayStart(getDayStart());
			cron.setDayInterval(getDayInterval());
			cron.setDayCountdown(getDayCountdown());
			cron.setDayNearest(getDayNearest());
			cron.setWeekRange(getWeekRange());
			cron.setWeekStart(getWeekStart());
			cron.setWeekInterval(getWeekInterval());
			cron.setWeekLast(getWeekLast());
			cron.setWeekTh1(getWeekTh1());
			cron.setWeekTh2(getWeekTh2());
			cron.setHourGroup(getHourGroup());
			cron.setHourRange(getHourRange());
			cron.setHourInterval(getHourInterval());
			cron.setHourStart(getHourStart());
			cron.setMinuteGroup(getMinuteGroup());
			cron.setMinuteRange(getMinuteRange());
			cron.setMinuteInterval(getMinuteInterval());
			cron.setMinuteStart(getMinuteStart());
			cron.setSecondGroup(getSecondGroup());
			cron.setSecondRange(getSecondRange());
			cron.setSecondInterval(getSecondInterval());
			cron.setSecondStart(getSecondStart());
			return cron;
		}
	}

	private class DPB0101ItemsBuilder extends DPB0101Items {

		private int refItemNoIdx;

		private int refSubitemNoIdx;

		private BcryptParamHelper bcryptParamHelper;

		public DPB0101ItemsBuilder(BcryptParamHelper bcryptParamHelper) {
			this.bcryptParamHelper = bcryptParamHelper;
		}

		public DPB0101ItemsBuilder refItemNo(String refItemNo, int refItemNoIdx) {
			setRefItemNo(refItemNo);
			this.refItemNoIdx = refItemNoIdx;
			return this;
		}

		public DPB0101ItemsBuilder refSubitemNo(String refSubitemNo, int refSubitemNoIdx) {
			setRefSubitemNo(refSubitemNo);
			this.refSubitemNoIdx = refSubitemNoIdx;
			return this;
		}

		public DPB0101ItemsBuilder inParams(String inParams) {
			setInParams(inParams);
			return this;
		}

		public DPB0101ItemsBuilder identifData(String identifData) {
			setIdentifData(identifData);
			return this;
		}

		public DPB0101ItemsBuilder sortBy(Integer sortBy) {
			setSortBy(sortBy);
			return this;
		}

		public DPB0101Items build() {
			DPB0101Items items = new DPB0101Items();
			if (!StringUtils.isEmpty(getRefItemNo())) {
				items.setRefItemNo(this.bcryptParamHelper.encode(getRefItemNo(), this.refItemNoIdx));
			}
			if (!StringUtils.isEmpty(getRefSubitemNo())) {
				items.setRefSubitemNo(this.bcryptParamHelper.encode(getRefSubitemNo(), this.refSubitemNoIdx));
			}
			items.setInParams(getInParams());
			items.setIdentifData(getIdentifData());
			items.setSortBy(getSortBy());
			return items;
		}
	}

}