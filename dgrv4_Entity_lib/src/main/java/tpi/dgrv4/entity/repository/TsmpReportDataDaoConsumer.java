package tpi.dgrv4.entity.repository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.entity.vo.AA1201ReportDateTimeRange;

public class TsmpReportDataDaoConsumer {

	public Consumer<AA1201ReportDateTimeRange> consumer = (
			AA1201ReportDateTimeRange aa1201ReportDateTimeRange) -> {
		Date now = aa1201ReportDateTimeRange.getNow();
		ReportDateTimeRangeTypeEnum dateTimeRangeType = aa1201ReportDateTimeRange.getDateTimeRangeType();
		Optional<LocalDate> opt_startDate = aa1201ReportDateTimeRange.getOpt_startDate();
		Optional<LocalDate> opt_endDate = aa1201ReportDateTimeRange.getOpt_endDate();
		Map<String, Object> params = aa1201ReportDateTimeRange.getParams();
		StringBuffer sb = aa1201ReportDateTimeRange.getSb();
		Optional<Date> opt_startDateTime = aa1201ReportDateTimeRange.getOpt_startDateTime();
		Optional<Date> opt_endDateTime = aa1201ReportDateTimeRange.getOpt_endDateTime();
		
		// <===查詢時間單位"分"
		if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {

			Date startDateTime = opt_startDateTime.get();

			Date endDateTime = opt_endDateTime.get();

			// <===統計邏輯：1天內的10分鐘資料。
				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :minuteType ");
				sb.append("    AND r.lastRowDateTime >= :startDateTime ");
				sb.append("    AND r.lastRowDateTime <= :endDateTime ");
				sb.append("   )   ");
				params.put("minuteType", ReportDateTimeRangeTypeEnum.MINUTE.value());
				params.put("startDateTime", startDateTime);
				params.put("endDateTime", endDateTime);

		
			// ===>統計邏輯：1天內的10分鐘資料。
		}
		// ===>查詢時間單位"分"		

		// <===查詢時間單位"天"
		if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.DAY) {

			GregorianCalendar gc_startDate = GregorianCalendar
					.from(opt_startDate.get().atStartOfDay(ZoneId.systemDefault()));

			GregorianCalendar gc_endDate = GregorianCalendar
					.from(opt_endDate.get().atStartOfDay(ZoneId.systemDefault()));

			// <===統計邏輯：若結束時間為今天，則查詢出今天資料(1小時資料+10分鐘資料)。
			SimpleDateFormat fmt = new SimpleDateFormat(DateTimeFormatEnum.西元年月日_2.value());
			if (fmt.format(gc_endDate.getTime()).equals(fmt.format(now))) {

				GregorianCalendar gc_hour = new GregorianCalendar();

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :hourType ");
				sb.append("    AND r.lastRowDateTime >= :startHour ");
				sb.append("    AND r.lastRowDateTime < :endHour ");
				sb.append("   )   ");
				params.put("hourType", ReportDateTimeRangeTypeEnum.HOUR.value());

				gc_hour.setTime(now);
				gc_hour.set(GregorianCalendar.SECOND, 0);
				gc_hour.set(GregorianCalendar.MINUTE, 0);
				params.put("endHour", gc_hour.getTime());

				gc_hour.set(GregorianCalendar.HOUR_OF_DAY, 0);
				params.put("startHour", gc_hour.getTime());

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :minuteType ");
				sb.append("    AND r.lastRowDateTime >= :startMinute ");
				sb.append("    AND r.lastRowDateTime < :endMinute ");
				sb.append("   )   ");
				params.put("minuteType", ReportDateTimeRangeTypeEnum.MINUTE.value());

				GregorianCalendar gc_minute = new GregorianCalendar();
				gc_minute.setTime(now);
				gc_minute.set(GregorianCalendar.SECOND, 0);

				gc_minute.add(GregorianCalendar.MINUTE, -(gc_minute.get(GregorianCalendar.MINUTE) % 10));
				params.put("endMinute", gc_minute.getTime());

				gc_minute.set(GregorianCalendar.MINUTE, 0);
				params.put("startMinute", gc_minute.getTime());

			}
			// ===>統計邏輯：若結束時間為今天，則查詢出今天資料(1小時資料+10分鐘資料)。

			// <===統計邏輯：查詢出昨天以前資料(1天資料)
			if (gc_startDate.before(gc_endDate) || (!fmt.format(gc_endDate.getTime()).equals(fmt.format(now))
					&& fmt.format(gc_startDate.getTime()).equals(fmt.format(gc_endDate.getTime())))) {

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :dayType ");
				sb.append("    AND r.lastRowDateTime >= :startDay ");
				sb.append("    AND r.lastRowDateTime < :endDay ");
				sb.append("   )   ");
				params.put("dayType", ReportDateTimeRangeTypeEnum.DAY.value());

				params.put("startDay", gc_startDate.getTime());

				if (fmt.format(gc_endDate.getTime()).equals(fmt.format(now))) {

				} else {
					gc_endDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
				}
				params.put("endDay", gc_endDate.getTime());
			}
			// ===>統計邏輯：查詢出昨天以前資料(1天資料)
		}
		// ===>查詢時間單位"天"

		// <===查詢時間單位"月"
		if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MONTH) {

			GregorianCalendar gc_startDate = GregorianCalendar
					.from(opt_startDate.get().atStartOfDay(ZoneId.systemDefault()));

			GregorianCalendar gc_endDate = GregorianCalendar
					.from(opt_endDate.get().atStartOfDay(ZoneId.systemDefault()));

			// <===統計邏輯：查詢出這個月資料資料(1天資料+1小時資料+10分鐘資料)。
			SimpleDateFormat fmt = new SimpleDateFormat(DateTimeFormatEnum.西元年月.value());
			if (fmt.format(gc_endDate.getTime()).equals(fmt.format(now))) {

				GregorianCalendar gc_hour = new GregorianCalendar();

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :hourType ");
				sb.append("    AND r.lastRowDateTime >= :startHour ");
				sb.append("    AND r.lastRowDateTime < :endHour ");
				sb.append("   )   ");
				params.put("hourType", ReportDateTimeRangeTypeEnum.HOUR.value());

				gc_hour.setTime(now);
				gc_hour.set(GregorianCalendar.SECOND, 0);
				gc_hour.set(GregorianCalendar.MINUTE, 0);
				params.put("endHour", gc_hour.getTime());

				gc_hour.set(GregorianCalendar.HOUR_OF_DAY, 0);
				params.put("startHour", gc_hour.getTime());

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :minuteType ");
				sb.append("    AND r.lastRowDateTime >= :startMinute ");
				sb.append("    AND r.lastRowDateTime < :endMinute ");
				sb.append("   )   ");
				params.put("minuteType", ReportDateTimeRangeTypeEnum.MINUTE.value());

				GregorianCalendar gc_minute = new GregorianCalendar();
				gc_minute.setTime(now);
				gc_minute.set(GregorianCalendar.SECOND, 0);

				gc_minute.add(GregorianCalendar.MINUTE, -(gc_minute.get(GregorianCalendar.MINUTE) % 10));
				params.put("endMinute", gc_minute.getTime());

				gc_minute.set(GregorianCalendar.MINUTE, 0);
				params.put("startMinute", gc_minute.getTime());

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :dayType ");
				sb.append("    AND r.lastRowDateTime >= :startDay ");
				sb.append("    AND r.lastRowDateTime < :endDay ");
				sb.append("   )   ");
				params.put("dayType", ReportDateTimeRangeTypeEnum.DAY.value());

				GregorianCalendar gc_startDay1 = new GregorianCalendar();
				gc_startDay1.setTime(gc_endDate.getTime());
				gc_startDay1.set(GregorianCalendar.DAY_OF_MONTH, 1);
				params.put("startDay", gc_startDay1.getTime());

				params.put("endDay", gc_endDate.getTime());

			}
			// ===>統計邏輯：查詢出這個月資料資料(1天資料+1小時資料+10分鐘資料)。

			// <===統計邏輯：查詢出這個月以前資料，不包含這個月
			if (!fmt.format(gc_startDate.getTime()).equals(fmt.format(now))
					|| (!fmt.format(gc_endDate.getTime()).equals(fmt.format(now))
							&& fmt.format(gc_startDate.getTime()).equals(fmt.format(gc_endDate.getTime())))) {

				sb.append("    OR  ");

				sb.append("  (    ");
				sb.append("    r.dateTimeRangeType = :monthType ");
				sb.append("    AND r.lastRowDateTime >= :startMonth ");
				sb.append("    AND r.lastRowDateTime < :endMonth ");
				sb.append("   )   ");
				params.put("monthType", ReportDateTimeRangeTypeEnum.MONTH.value());

				// gc_startDate.set(GregorianCalendar.DAY_OF_MONTH, 1);
				params.put("startMonth", gc_startDate.getTime());
				if (fmt.format(gc_endDate.getTime()).equals(fmt.format(now))) {

				} else {
					gc_endDate.add(GregorianCalendar.MONTH, 1);
				}
				gc_endDate.set(GregorianCalendar.DAY_OF_MONTH, 1);

				params.put("endMonth", gc_endDate.getTime());
			}
			// ===>統計邏輯：查詢出這個月以前資料，不包含這個月

		}
		// ===>查詢時間單位"月"

	};

}
