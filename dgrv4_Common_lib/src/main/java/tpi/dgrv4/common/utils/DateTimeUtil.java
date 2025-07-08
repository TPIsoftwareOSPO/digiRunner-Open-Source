package tpi.dgrv4.common.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;

public class DateTimeUtil {

	/** 時間格式轉字串 */
	public static Optional<String> dateTimeToString(Date input, DateTimeFormatEnum format, String timezone) {
		if (input != null) {
			if (format == null) {
				format = DateTimeFormatEnum.西元年月日時分秒;
			}

			final SimpleDateFormat sdf = new SimpleDateFormat(format.value());
			sdf.setTimeZone(TimeZone.getTimeZone(timezone));
			return Optional.ofNullable(sdf.format(input));
		}
		return Optional.empty();
	}

	/** 時間格式轉字串 */
	public static Optional<String> dateTimeToString(Date input, DateTimeFormatEnum format) {
		if (input != null) {
			if (format == null) {
				format = DateTimeFormatEnum.西元年月日時分秒;
			}

			final SimpleDateFormat sdf = new SimpleDateFormat(format.value());
			return Optional.ofNullable(sdf.format(input));
		}
		return Optional.empty();
	}

	/** 時間格式轉字串 */
	public static Optional<String> dateTimeToString(Date input, DateTimeFormatEnum format, Locale locale) {
		if (input != null) {
			if (format == null) {
				format = DateTimeFormatEnum.西元年月日時分秒;
			}

			final SimpleDateFormat sdf = new SimpleDateFormat(format.value(), locale);
			return Optional.ofNullable(sdf.format(input));
		}
		return Optional.empty();
	}

	/** 時間格式轉字串 */
	public static Optional<String> dateTimeToString(LocalDate input, DateTimeFormatEnum format) {
		if (input == null) {
			return Optional.empty();
		}

		String formatDateTime = null;

		if (format == null) {
			format = DateTimeFormatEnum.西元年月日;
		}

		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format.value());
			formatDateTime = input.format(dtf);
			return Optional.ofNullable(formatDateTime);
		} catch (Exception e) {
		}

		return Optional.empty();
	}

	/** 時間格式轉字串 */
	public static Optional<String> dateTimeToString(LocalDateTime input, DateTimeFormatEnum format) {
		if (input == null) {
			return Optional.empty();
		}

		String formatDateTime = null;

		if (format == null) {
			format = DateTimeFormatEnum.西元年月日時分秒;
		}

		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format.value());
			formatDateTime = input.format(dtf);
			return Optional.ofNullable(formatDateTime);
		} catch (Exception e) {
		}

		return Optional.empty();
	}

	/**
	 * 字串格式轉日期(含時間)，若輸入參數與格式不符則回傳 Optional.empty()<br>
	 * 時間資訊不足時則補0，ex: 輸入"2020/01/01"，則輸出"2020/01/01 00:00:00.000"
	 */
	public static final Optional<Date> stringToDateTime(String input, DateTimeFormatEnum format) {
		Date date = null;

		Optional<LocalDateTime> optDateTime = stringToLocalDateTime(input, format);
		if (optDateTime.isPresent()) {
			LocalDateTime localDateTime = optDateTime.get();
			ZoneId zone = ZoneId.systemDefault();
			ZonedDateTime zonedDateTime = localDateTime.atZone(zone);
			date = Date.from(zonedDateTime.toInstant());
			return Optional.ofNullable(date);
		}

		Optional<LocalDate> optDate = stringToLocalDate(input, format);
		if (optDate.isPresent()) {
			LocalDate localDate = optDate.get();
			ZoneId zone = ZoneId.systemDefault();
			ZonedDateTime zonedDateTime = localDate.atStartOfDay(zone);
			date = Date.from(zonedDateTime.toInstant());
			return Optional.ofNullable(date);
		}

		return Optional.empty();
	}

	/** 字串格式轉日期 */
	public static Optional<LocalDate> stringToLocalDate(String input, DateTimeFormatEnum format) {
		if (input == null) {
			return Optional.empty();
		}

		LocalDate formatDate = null;

		if (format == null) {
			format = DateTimeFormatEnum.西元年月日;
		}

		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format.value());
			formatDate = LocalDate.parse(input, dtf);
			return Optional.ofNullable(formatDate);
		} catch (Exception e) {
		}

		return Optional.empty();
	}

	/**
	 * 字串格式轉日期(含時間)<br>
	 * 若輸入參數與格式不符，或是未傳入時間資訊，則回傳 Optional.empty()
	 */
	public static Optional<LocalDateTime> stringToLocalDateTime(String input, DateTimeFormatEnum format) {
		if (input == null) {
			return Optional.empty();
		}

		LocalDateTime formatDateTime = null;

		if (format == null) {
			format = DateTimeFormatEnum.西元年月日時分秒;
		}

		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format.value());
			formatDateTime = LocalDateTime.parse(input, dtf);
			return Optional.ofNullable(formatDateTime);
		} catch (Exception e) {
		}

		return Optional.empty();
	}

	/**
	 * 取得精度只到"秒"的現在時間
	 * 
	 * @return
	 */
	public static final Date now() {
		final ZonedDateTime zdt = ZonedDateTime.now().withNano(0);
		return Date.from(zdt.toInstant());
	}

	/***
	 * 
	 * 將"秒"轉換成 8 days, 6 hours, 6 minutes, 40 seconds
	 * 
	 * @param seconds
	 * @return
	 */
	public static final String secondsToDaysHoursMinutesSeconds(long seconds) {
		seconds = seconds / 1000;

		int day = (int) TimeUnit.SECONDS.toDays(seconds);
		long hoursL = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
		long minuteL = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
		long secondL = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

		String timeStr = "";
		if (day >= 1) {
			timeStr = timeStr + day + " days, ";
		}
		if (hoursL >= 1) {
			timeStr = timeStr + hoursL + " hours, ";
		}
		if (minuteL >= 1) {
			timeStr = timeStr + minuteL + " minutes, ";
		}
		if (secondL >= 1) {
			timeStr = timeStr + secondL + " seconds ";
		}
		return timeStr;
	}

	/**
	 * Returns a date one year from the given date as a Long value.
	 * 
	 * @param dateTime the initial date
	 * @return the date one year from the initial date as a Long value
	 */
	public static final Long getOneYearLater(LocalDateTime dateTime) {
		return dateTime.plusYears(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	/**
	 * Converts a given timestamp (as Long) to a date string in the format
	 * "yyyy-MM-dd".
	 * 
	 * <p>
	 * This method takes a timestamp representing the number of milliseconds since
	 * the Unix epoch (1970-01-01T00:00:00Z) and returns its corresponding date as a
	 * string in the format "yyyy-MM-dd".
	 * </p>
	 * 
	 * @param timestamp the timestamp to be converted, in milliseconds since the
	 *                  Unix epoch
	 * @return the corresponding date as a string in the format "yyyy-MM-dd"
	 */
	public static final String convertLongToDateStr(Long timestamp) {

		if (timestamp == null) {
			timestamp = 0L;
		}

		// Convert the timestamp to an Instant
		Instant instant = Instant.ofEpochMilli(timestamp);

		// Convert the Instant to a LocalDate
		LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

		// Use DateTimeFormatter to format the date as "yyyy-MM-dd"
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		return date.format(formatter);
	}
}
