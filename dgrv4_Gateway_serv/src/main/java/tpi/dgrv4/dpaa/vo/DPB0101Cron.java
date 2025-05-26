package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0101Cron {
	
	/** [BEGIN] 以下是簡易版設定表單所使用的欄位 */

	/** [簡易版] 頻率, 0="每一天", 1="每個月_號", 2="每週星期_" */
	private Integer frequency;

	/** [簡易版] 時 */
	private Integer hour;

	/** [簡易版] 分 */
	private Integer minute;
	
	/** [END] 以下是簡易版設定表單所使用的欄位 */

	/** "月"群組, 0="每一個月", 1="_月", 2="從_月起，每隔_個月" */
	private Integer monthGroup;
	
	/** "月"範圍值 */
	private List<Integer> monthRange;
	
	/** "月"間隔 */
	private Integer monthInterval;
	
	/** "月"起始值 */
	private Integer monthStart;
	
	/** "日"群組, 0="每一天"(下拉選單), 1="_號", 2="從_號起，每隔_天", 3="每月倒數第_天", 4="最接近_號的週間日", 5="星期_", 6="從星期_起，每隔_天", 7="每月最後一個星期_", 8="每月第_個星期_" */
	private Integer dayGroup;
	
	/** "日"類別, 0="每一天", 1="每月最後一天", 2="每月最後一個週間日" */
	private Integer dayType;
	
	/** "日"的日期範圍值 */
	private List<Integer> dayRange;
	
	/** "日"的起始值 */
	private Integer dayStart;
	
	/** "日"的間隔 */
	private Integer dayInterval;
	
	/** "日"的倒數值 */
	private Integer dayCountdown;
	
	/** "日"的接近值 */
	private Integer dayNearest;
	
	/** "日"的星期範圍值 */
	private List<Integer> weekRange;
	
	/** "日"的星期起始值 */
	private Integer weekStart;
	
	/** "日"的星期間隔 */
	private Integer weekInterval;
	
	/** "日"的星期最後 */
	private Integer weekLast;
	
	/** "日"的第幾個星期 */
	private Integer weekTh1;
	
	/** "日"的第幾個星期幾 */
	private Integer weekTh2;
	
	/** "時"群組, 0="每一小時", 1="第_時", 2="從第_時起，每隔_小時" */
	private Integer hourGroup;
	
	/** "時"範圍值 */
	private List<Integer> hourRange;
	
	/** "時"間隔 */
	private Integer hourInterval;
	
	/** "時"起始值 */
	private Integer hourStart;
	
	/** "分"群組, 0="每一分", 1="第_分", 2="從第_分起，每隔_分" */
	private Integer minuteGroup;
	
	/** "分"範圍值 */
	private List<Integer> minuteRange;
	
	/** "分"間隔 */
	private Integer minuteInterval;
	
	/** "分"起始值 */
	private Integer minuteStart;
	
	/** "秒"群組, 0="每一秒", 1="第_秒", 2="從第_秒起，每隔_秒" */
	private Integer secondGroup;
	
	/** "秒"範圍值 */
	private List<Integer> secondRange;
	
	/** "秒"間隔 */
	private Integer secondInterval;
	
	/** "秒"起始值 */
	private Integer secondStart;

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	public Integer getMonthGroup() {
		return monthGroup;
	}

	public void setMonthGroup(Integer monthGroup) {
		this.monthGroup = monthGroup;
	}

	public List<Integer> getMonthRange() {
		return monthRange;
	}

	public void setMonthRange(List<Integer> monthRange) {
		this.monthRange = monthRange;
	}

	public Integer getMonthInterval() {
		return monthInterval;
	}

	public void setMonthInterval(Integer monthInterval) {
		this.monthInterval = monthInterval;
	}

	public Integer getMonthStart() {
		return monthStart;
	}

	public void setMonthStart(Integer monthStart) {
		this.monthStart = monthStart;
	}

	public Integer getDayGroup() {
		return dayGroup;
	}

	public void setDayGroup(Integer dayGroup) {
		this.dayGroup = dayGroup;
	}

	public Integer getDayType() {
		return dayType;
	}

	public void setDayType(Integer dayType) {
		this.dayType = dayType;
	}

	public List<Integer> getDayRange() {
		return dayRange;
	}

	public void setDayRange(List<Integer> dayRange) {
		this.dayRange = dayRange;
	}

	public Integer getDayStart() {
		return dayStart;
	}

	public void setDayStart(Integer dayStart) {
		this.dayStart = dayStart;
	}

	public Integer getDayInterval() {
		return dayInterval;
	}

	public void setDayInterval(Integer dayInterval) {
		this.dayInterval = dayInterval;
	}

	public Integer getDayCountdown() {
		return dayCountdown;
	}

	public void setDayCountdown(Integer dayCountdown) {
		this.dayCountdown = dayCountdown;
	}

	public Integer getDayNearest() {
		return dayNearest;
	}

	public void setDayNearest(Integer dayNearest) {
		this.dayNearest = dayNearest;
	}

	public List<Integer> getWeekRange() {
		return weekRange;
	}

	public void setWeekRange(List<Integer> weekRange) {
		this.weekRange = weekRange;
	}

	public Integer getWeekStart() {
		return weekStart;
	}

	public void setWeekStart(Integer weekStart) {
		this.weekStart = weekStart;
	}

	public Integer getWeekInterval() {
		return weekInterval;
	}

	public void setWeekInterval(Integer weekInterval) {
		this.weekInterval = weekInterval;
	}

	public Integer getWeekLast() {
		return weekLast;
	}

	public void setWeekLast(Integer weekLast) {
		this.weekLast = weekLast;
	}

	public Integer getWeekTh1() {
		return weekTh1;
	}

	public void setWeekTh1(Integer weekTh1) {
		this.weekTh1 = weekTh1;
	}

	public Integer getWeekTh2() {
		return weekTh2;
	}

	public void setWeekTh2(Integer weekTh2) {
		this.weekTh2 = weekTh2;
	}

	public Integer getHourGroup() {
		return hourGroup;
	}

	public void setHourGroup(Integer hourGroup) {
		this.hourGroup = hourGroup;
	}

	public List<Integer> getHourRange() {
		return hourRange;
	}

	public void setHourRange(List<Integer> hourRange) {
		this.hourRange = hourRange;
	}

	public Integer getHourInterval() {
		return hourInterval;
	}

	public void setHourInterval(Integer hourInterval) {
		this.hourInterval = hourInterval;
	}

	public Integer getHourStart() {
		return hourStart;
	}

	public void setHourStart(Integer hourStart) {
		this.hourStart = hourStart;
	}

	public Integer getMinuteGroup() {
		return minuteGroup;
	}

	public void setMinuteGroup(Integer minuteGroup) {
		this.minuteGroup = minuteGroup;
	}

	public List<Integer> getMinuteRange() {
		return minuteRange;
	}

	public void setMinuteRange(List<Integer> minuteRange) {
		this.minuteRange = minuteRange;
	}

	public Integer getMinuteInterval() {
		return minuteInterval;
	}

	public void setMinuteInterval(Integer minuteInterval) {
		this.minuteInterval = minuteInterval;
	}

	public Integer getMinuteStart() {
		return minuteStart;
	}

	public void setMinuteStart(Integer minuteStart) {
		this.minuteStart = minuteStart;
	}

	public Integer getSecondGroup() {
		return secondGroup;
	}

	public void setSecondGroup(Integer secondGroup) {
		this.secondGroup = secondGroup;
	}

	public List<Integer> getSecondRange() {
		return secondRange;
	}

	public void setSecondRange(List<Integer> secondRange) {
		this.secondRange = secondRange;
	}

	public Integer getSecondInterval() {
		return secondInterval;
	}

	public void setSecondInterval(Integer secondInterval) {
		this.secondInterval = secondInterval;
	}

	public Integer getSecondStart() {
		return secondStart;
	}

	public void setSecondStart(Integer secondStart) {
		this.secondStart = secondStart;
	}

}