package tpi.dgrv4.dpaa.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

public class TimestampConverterUtil {

	private static final long MILLIS_PER_DAY = 86400000L;
	private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
	private static final AtomicLong ZONE_OFFSET_MILLIS = new AtomicLong();

	// 定義合法的時間範圍
	private static final long MIN_TIMESTAMP = LocalDate.of(100, 1, 1).atStartOfDay(SYSTEM_ZONE).toInstant()
			.toEpochMilli();
	private static final long MAX_TIMESTAMP = LocalDate.of(9999, 12, 31).atStartOfDay(SYSTEM_ZONE).toInstant()
			.toEpochMilli();

	static {
		// 初始化時計算當前時區的偏移量（毫秒）
		updateZoneOffset();
	}

	/**
	 * 更新時區偏移量
	 */
	private static void updateZoneOffset() {
		ZONE_OFFSET_MILLIS.set(SYSTEM_ZONE.getRules().getOffset(Instant.now()).getTotalSeconds() * 1000L);
	}

	/**
	 * 將時間戳記轉換為該日期的 0 點 0 分 0 秒的時間戳記。
	 *
	 * @param time 原始的時間戳記
	 * @return 該日期的 0 點 0 分 0 秒的時間戳記
	 */
	public static long getDateOnlyTimestamp(long time) {

		if (time == 0) {
			return time;
		}

		if (time < MIN_TIMESTAMP || time > MAX_TIMESTAMP) {
			throw new IllegalArgumentException("Timestamp must be between 0100-01-01 and 9999-12-31");
		}

		long offset = ZONE_OFFSET_MILLIS.get();
		return ((time + offset) / MILLIS_PER_DAY) * MILLIS_PER_DAY - offset;
	}
}
