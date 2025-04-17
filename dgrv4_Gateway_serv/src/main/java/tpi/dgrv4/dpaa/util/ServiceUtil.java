package tpi.dgrv4.dpaa.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpaaStaticResourceInitializer;
import tpi.dgrv4.dpaa.constant.RegexpConstant;
import tpi.dgrv4.dpaa.vo.ConvertTimeUnitRst;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class ServiceUtil {

	/**
	 * Injected by {@link DpaaStaticResourceInitializer}
	 */
	private static TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private static TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private static TPILogger logger = TPILogger.tl;

	/**
	 * 取代多個分隔字元為單一字元，並依分隔字元切割字串
	 * 
	 * @param keyword
	 * @param delimiter
	 * @return
	 */
	public static String[] getKeywords(String keyword, String delimiter) {
		String[] words = null;
		if (keyword != null && !keyword.isEmpty()) {
			keyword = keyword.trim().replaceAll(delimiter + "+", delimiter);
			words = keyword.split(delimiter);
		}
		return words;
	}
//
//	/**
//	 * Request 中若未包含 "nextPage" 欄位，表示要求第一頁資料, 若要求第二頁，則應傳入 "2", 依此類推; 未符合
//	 * PageRequest.of() 的規格, 可利用此方法轉換合法頁碼
//	 * 
//	 * @return
//	 */
//	public static int getPage(int requestNextPage) {
//		if (requestNextPage < 2) {
//			return 0;
//		} else {
//			return requestNextPage - 1;
//		}
//	}
//
//	/**
//	 * 計算 SHA-256 值後轉成 base64 字串
//	 * 
//	 * @param input
//	 * @return
//	 */
//	public static String getSHA256ToBase64(String input) {
//		String toReturn = null;
//
//		try {
//			byte[] input_byte = SHA256Util.getSHA256(input.getBytes());
//			toReturn = base64EncodeWithoutPadding(input_byte);
//		} catch (Exception e) {
//			logger.debug("getSHA256ToBase64 error: " + e);
//		}
//
//		return toReturn;
//	}

	/**
	 * 計算 SHA-256 值後轉成 base64 URL 無後綴 字串
	 * 
	 * @param input
	 * @return
	 */
	public static String getSHA256ToBase64UrlEncodeWithoutPadding(String input) {
		String toReturn = null;

		try {
			byte[] input_byte = SHA256Util.getSHA256(input.getBytes());
			toReturn = Base64Util.base64URLEncode(input_byte);

		} catch (Exception e) {
			TPILogger.tl.debug("getSHA256ToBase64UrlEncodeWithoutPadding error: " + StackTraceUtil.logStackTrace(e));
		}

		return toReturn;
	}

	/**
	 * 計算 SHA-512值後轉成 base64 字串
	 * 
	 * @param input
	 * @return
	 */
	public static String getSHA512ToBase64(String input) {
		String toReturn = null;

		try {
			byte[] input_byte = SHA256Util.getSHA512(input.getBytes());
			toReturn = Base64Util.base64EncodeWithoutPadding(input_byte);
		} catch (Exception e) {
			TPILogger.tl.debug("getSHA512ToBase64 error: " + e);
		}

		return toReturn;
	}

//	/**
//	 * base 64 加密(無後綴)
//	 */
//	public static String base64EncodeWithoutPadding(byte[] bytes) {
//		return Base64.getEncoder().withoutPadding().encodeToString(bytes);
//	}
	/**
	 * base 64 加密(有後綴)
	 */
	public static String base64Encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * base 64解密
	 * 
	 * @param base64EncodedStr
	 * @return
	 */
	public static byte[] base64Decode(String base64EncodedStr) {
		byte[] bdata = null;
		try {
			bdata = Base64.getDecoder().decode(base64EncodedStr);
		} catch (IllegalArgumentException e) {
			bdata = Base64Util.base64URLDecode(base64EncodedStr);
		}
		return bdata;
	}

//	/**
//	 * 將 UUID 轉成 byte[]
//	 * 
//	 * @param uuid
//	 * @return
//	 */
//	public static byte[] asByteArray(UUID uuid) {
//
//		long msb = uuid.getMostSignificantBits();
//		long lsb = uuid.getLeastSignificantBits();
//		byte[] buffer = new byte[16];
//
//		for (int i = 0; i < 8; i++) {
//			buffer[i] = (byte) (msb >>> 8 * (7 - i));
//		}
//		for (int i = 8; i < 16; i++) {
//			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
//		}
//
//		return buffer;
//
//	}
//
//	public static String toChineseTimeExpression(Long seconds) {
//		String exp = "";
//		if (seconds < 60) {
//			exp = seconds + "秒";
//		} else if (seconds < 3600) {
//			Long m = seconds / 60;
//			Long s = seconds % 60;
//			exp = m + "分";
//			if (s > 0) {
//				exp += s + "秒";
//			}
//		} else if (seconds < 86400) {
//			Long h = seconds / 3600;
//			Long m = seconds % 3600 / 60;
//			Long s = seconds % 3600 % 60;
//			exp = h + "小時";
//			if (m > 0) {
//				exp += m + "分";
//			}
//			if (s > 0) {
//				exp += s + "秒";
//			}
//		} else {
//			Long d = seconds / 86400;
//			Long h = seconds % 86400 / 3600;
//			Long m = seconds % 86400 % 3600 / 60;
//			Long s = seconds % 86400 % 3600 % 60;
//			exp = d + "天";
//			if(h > 0) {
//				exp += h + "小時"	;
//			}
//			if (m > 0) {
//				exp += m + "分";
//			}
//			if (s > 0) {
//				exp += s + "秒";
//			}
//		}
//		return exp;
//	}

	public static String decodeBase64URL(String input) {

		try {

			byte[] inputByte = Base64Util.base64URLDecode(input);
			input = new String(inputByte, StandardCharsets.UTF_8);

		} catch (Exception e) {
			TPILogger.tl.debug("The Input : " + input);
			TPILogger.tl.debug("Decode Base64URL Error: " + e.getMessage());
		}

		return input;
	}

	public static String encodeBase64URL(String input) {

		try {

			input = Base64Util.base64URLEncode(input.getBytes(StandardCharsets.UTF_8));

		} catch (Exception e) {
			TPILogger.tl.debug("The Input : " + input);
			TPILogger.tl.debug("Encode Base64URL Error: " + e.getMessage());
		}

		return input;
	}

	public static <T> T deepCopy(T source, Class<T> clazz) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readValue( //
					om.writeValueAsString(source), //
					clazz //
			);
		} catch (Exception e) {
			TPILogger.tl.debug("deepCopy error: " + StackTraceUtil.logStackTrace(e));
		}
		return null;
	}
//
//	// https://blog.csdn.net/qq_35387940/article/details/84391784
//	public static String getIpAddress(HttpServletRequest request) {
//		String ipAddress = null;
//        try {
//            ipAddress = request.getHeader("x-forwarded-for");
//            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//                ipAddress = request.getHeader("Proxy-Client-IP");
//            }
//            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//                ipAddress = request.getHeader("WL-Proxy-Client-IP");
//            }
//            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//                ipAddress = request.getRemoteAddr();
//                if (ipAddress.equals("127.0.0.1")) {
//                    // 根据网卡取本机配置的IP
//                    try {
//                    	InetAddress inet = InetAddress.getLocalHost();
//                    	if (inet != null) {
//                    		ipAddress = inet.getHostAddress();
//                    	}
//                    } catch (UnknownHostException e) {
//                        logger.debug("getIpAddress error: " + e);
//                    }
//                }
//            }
//            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
//            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
//                // = 15
//                if (ipAddress.indexOf(",") > 0) {
//                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
//                }
//            }
//        } catch (Exception e) {
//            ipAddress="";
//        }
// 
//        return ipAddress;
//	}
//
//	public static String truncateExceptionMessage(Exception e, int maxLength) {
//		StringWriter sw = new StringWriter();
//		PrintWriter pw = new PrintWriter(sw);
//		e.printStackTrace(pw);
//
//		StringBuffer sb = sw.getBuffer();
//		if (sb.length() > maxLength) {
//			sb.setLength(maxLength);
//		}
//		return sb.toString();
//	}

	public static boolean isChainedExceptionMessagesContainIgnoreCase(Throwable t, String... inputs) {
		if (inputs == null || inputs.length == 0) {
			return true;
		}

		String detailMessage = null;
		while (t != null) {
			detailMessage = t.getMessage();
			if (StringUtils.hasLength(detailMessage)) {
				detailMessage = detailMessage.toLowerCase();

				for (String input : inputs) {
					if (StringUtils.hasLength(input)) {
						input = input.toLowerCase();
						if (detailMessage.contains(input)) {
							return true;
						}
					}
				}
			}
			t = t.getCause();
		}
		return false;
	}

	public static boolean isValueTooLargeException(Throwable t) {
		return isChainedExceptionMessagesContainIgnoreCase(t, "Value too long", // H2
				"value too large", "ORA-12899", // Oracle
				"Data too long", // MySQL
				"Text was truncated", "會被截斷", // SQLServer
				"value too long"); // PostgreSQL
	}

	/**
	 * 如果 input 為 null, 則返回 alternatives 中第一個非 null 的值;<br>
	 * 若 alternatives 為空值, 則返回 Integer.MIN_VALUE
	 * 
	 * @param input
	 * @param alternatives
	 * @return
	 * @throws Exception
	 */
	public static Integer nvl(Integer input, Integer... alternatives) {
		return nvl(Integer.class, () -> {
			return Integer.MIN_VALUE;
		}, (i) -> {
			return i;
		}, input, alternatives);
	}

	/**
	 * 如果 input 為 null, 則返回 alternatives 中第一個非 null 的值;<br>
	 * 若 alternatives 為空值, 則返回空字串
	 * 
	 * @param input
	 * @param alternatives
	 * @return
	 * @throws Exception
	 */
	public static String nvl(String input, String... alternatives) {
		return nvl(String.class, () -> {
			return new String();
		}, (s) -> {
			return String.valueOf(s);
		}, input, alternatives);
	}

	@SafeVarargs
	private static <R> R nvl(Class<R> clazz, Supplier<R> initializer, Function<R, R> caster, R input,
			R... alternatives) {
		if (input == null) {
			if (alternatives == null || alternatives.length == 0) {
				return initializer.get();
			} else {
				input = alternatives[0];
				alternatives = Arrays.copyOfRange(alternatives, 1, alternatives.length);
				return nvl(clazz, initializer, caster, input, alternatives);
			}
		}

		return caster.apply(input);
	}

	/**
	 * 使用正則表示式檢查字串
	 * 
	 * @param input 需被檢查
	 * @param rule
	 * @return
	 */
	public static boolean checkDataByPattern(String input, String rule) {
		Pattern p = Pattern.compile(rule);
		if (input == null) {
			input = "";
		}
		Matcher m = p.matcher(input); // 不接受 null
		return m.matches();

	}

	/**
	 * 檢查email格式 可以多個mail,以「,」分隔
	 * 
	 * @param input
	 * @return
	 */
	public static boolean checkEmail(String input) {
		String rule = RegexpConstant.EMAIL;
		return checkDataByPattern(input, rule);

	}

	/**
	 * 只能輸入數字英文
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isNumericOrAlphabetic(String input) {
		String rule = RegexpConstant.ENGLISH_NUMBER;
		return checkDataByPattern(input, rule);

	}

	/**
	 * 只能輸入數字英文-_空白
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isNumericOrAlphabeticOrWhitespace(String input) {
		String rule = RegexpConstant.ENGLISH_NUMBER_WHITESPACE;
		return checkDataByPattern(input, rule);

	}

	public static String join(CharSequence delimiter, Integer... elements) {
		Objects.requireNonNull(delimiter);
		Objects.requireNonNull(elements);
		// Number of elements not likely worth Arrays.stream overhead.
		StringJoiner joiner = new StringJoiner(delimiter);
		for (Integer i : elements) {
			joiner.add(String.valueOf(i));
		}
		return joiner.toString();
	}

	public static String join(CharSequence delimiter, Iterable<? extends Integer> elements) {
		Objects.requireNonNull(delimiter);
		Objects.requireNonNull(elements);
		StringJoiner joiner = new StringJoiner(delimiter);
		for (Integer i : elements) {
			joiner.add(String.valueOf(i));
		}
		return joiner.toString();
	}

	public static Locale parseLocale(String localeStr) {
		try {
			String[] a = localeStr.split("_");
			String[] a1 = localeStr.split("-");
			if (a1.length > a.length) {
				a = deepCopy(a1, String[].class);
				if (a == null) {
					a = LocaleType.ZH_TW.split("-");
				}
			}

			Locale.Builder builder = new Locale.Builder();
			if (a.length > 2) {
				builder.setLanguage(a[0]);
				builder.setRegion(a[1]);
				builder.setVariant(a[2]);
			} else if (a.length > 1) {
				builder.setLanguage(a[0]);
				builder.setRegion(a[1]);
			} else if (a.length > 0) {
				builder.setLanguage(a[0]);
			}
			return builder.build();
		} catch (Exception e) {
			return Locale.getDefault();
		}
	}

	/**
	 * 計算傳入時間(秒)是否可以進位成單一單位(ex: N小時、N分鐘) <br>
	 * 若無法完整進位(ex: N小時N分鐘、N分鐘N秒), 則不進位, 單位預設為"秒"，並前綴 "大約" 字樣；若可完整進位，則此欄位填入空字串
	 * 
	 * <li>ex : 輸入59秒
	 * <li>輸出結果 :
	 * <table border="1">
	 * <tbody>
	 * <tr>
	 * <td>ConvertTimeUnitRst.time</td>
	 * <td>59</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnit</td>
	 * <td>s</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnitName</td>
	 * <td>秒</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.approximateTimeUnit</td>
	 * <td></td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * <br>
	 * <li>ex : 輸入120秒 (2分鐘)
	 * <li>輸出結果 :
	 * <table border="1">
	 * <tbody>
	 * <tr>
	 * <td>ConvertTimeUnitRst.time</td>
	 * <td>2</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnit</td>
	 * <td>m</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnitName</td>
	 * <td>分鐘</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.approximateTimeUnit</td>
	 * <td></td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * <br>
	 * <li>ex : 輸入5400秒 (1小時30分鐘)
	 * <li>輸出結果 :
	 * <table border="1">
	 * <tbody>
	 * <tr>
	 * <td>ConvertTimeUnitRst.time</td>
	 * <td>90</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnit</td>
	 * <td>m</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnitName</td>
	 * <td>分鐘</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.approximateTimeUnit</td>
	 * <td>大約 1 小時</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * <br>
	 * <li>ex : 輸入5500秒 (1小時32分鐘30秒)
	 * <li>輸出結果 :
	 * <table border="1">
	 * <tbody>
	 * <tr>
	 * <td>ConvertTimeUnitRst.time</td>
	 * <td>5500</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnit</td>
	 * <td>s</td>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.timeUnitName</td>
	 * <td>秒</th>
	 * </tr>
	 * <tr>
	 * <td>ConvertTimeUnitRst.approximateTimeUnit</td>
	 * <td>大約 1 小時</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 * @param seconds
	 * @return ConvertTimeUnitRst
	 * @throws IllegalArgumentException
	 * 
	 */

	public static ConvertTimeUnitRst convertTimeUnit(int seconds, String locale) throws IllegalArgumentException {
		if (seconds < 0) {
			throw new IllegalArgumentException("Param(" + seconds + ") is no less than 0 ");
		}
		List<TsmpDpItems> timeUnitList = getTsmpDpItemsCacheProxy().findByItemNoAndLocaleOrderBySortByAsc("TIME_UNIT",
				locale);
		Map<String, Integer> map = convertTimeUnit(timeUnitList, seconds, 0);
		ConvertTimeUnitRst object = new ConvertTimeUnitRst();
		object.setApproximateTimeUnit("");
		int count = 0;
		for (String unit : map.keySet()) {
			Integer value = map.get(unit);
			if (count == 0) {

				object.setTime(nvl(value));
				object.setTimeUnit(nvl(unit));
				object.setTimeUnitName(getTimeUnitName(timeUnitList, unit));
			} else {
				TsmpRtnCode trn = getTsmpRtnCodeCacheProxy().findById(TsmpDpAaRtnCode._2012.getCode(), locale)
						.orElse(null);
				String msg = "";
				if (trn == null) {
					msg = "Approximately";
				} else {
					if (StringUtils.isEmpty(trn.getTsmpRtnMsg())) {
						msg = "Approximately";
					} else {
						msg = trn.getTsmpRtnMsg();

					}
				}
				object.setApproximateTimeUnit(msg + " " + value + " " + getTimeUnitName(timeUnitList, unit));

			}
			count++;
		}
		return object;
	}

	private static Map<String, Integer> convertTimeUnit(List<TsmpDpItems> timeUnitList, int time,
			int currentTimeUnitIndex) {
		Map<String, Integer> allowDays = null;
		int nextTimeUnitIndex = currentTimeUnitIndex + 1;
		if (timeUnitList.size() > nextTimeUnitIndex) {
			TsmpDpItems nextTimeUnit = timeUnitList.get(nextTimeUnitIndex);

			int basic = Integer.valueOf(nextTimeUnit.getParam1());

			int q = time / basic; // quotient 商數
			int r = time % basic; // remainder 餘數
			if (q > 0 && r == 0) {
				allowDays = convertTimeUnit(timeUnitList, q, nextTimeUnitIndex);
			} else {
				TsmpDpItems currentTimeUnit = timeUnitList.get(currentTimeUnitIndex);
				allowDays = new LinkedHashMap<>();
				allowDays.put(currentTimeUnit.getSubitemNo(), time);
				if (q > 0) {
					Map<String, Integer> ballParkMap = convertTimeUnit(timeUnitList, q, nextTimeUnitIndex);
					allowDays.putAll(ballParkMap);
				}

			}
		} else {
			TsmpDpItems currentTimeUnit = timeUnitList.get(currentTimeUnitIndex);
			allowDays = new HashMap<>();
			allowDays.put(currentTimeUnit.getSubitemNo(), time);
		}

		return allowDays;
	}

	private static String getTimeUnitName(List<TsmpDpItems> timeUnitList, String subItemNo) {
		String subitemName = "";
		Optional<TsmpDpItems> result = timeUnitList.stream().filter(obj -> obj.getSubitemNo().equals(subItemNo))
				.findFirst();
		if (result.isPresent()) {
			subitemName = nvl(result.get().getSubitemName());
		}

		return subitemName;
	}

	/**
	 * 文字打碼,只顯示前後N個字,中間其餘隱藏加星號(*) <br>
	 * 引數異常直接返回""
	 *
	 * @param str   要處理的資料文字
	 * @param front 需要顯示前幾位
	 * @param end   需要顯示末幾位
	 * @return 處理完成的資料
	 */
	public static String dataMask(String str, int front, int end) {
		// 資料不能為空
		if (!StringUtils.hasLength(str)) {
			return "";
		}
		// 需要擷取的長度不能大於資料
		if ((front + end) > str.length()) {
			return "";
		}
		// 需要擷取的不能小於0
		if (front < 0 || end < 0) {
			return "";
		}
		// 計算*的數量
		int asteriskCount = str.length() - (front + end);
		StringBuffer asteriskStr = new StringBuffer();
		for (int i = 0; i < asteriskCount; i++) {
			asteriskStr.append("*");
		}
		String regex = "(.{" + String.valueOf(front) + "})(.+)(.{" + String.valueOf(end) + "})";
		String replacement = "$1" + asteriskStr + "$3";
		String mark = str.replaceAll(regex, replacement);
		return mark;
	}

	/**
	 * 取得後面有加特殊字0x80,再經 Base64Encode 的 字串
	 * 
	 * @return
	 */
	public static String getAdd0x80ToBase64Encoede(String origStr) {
		byte[] data1 = origStr.getBytes();
		byte[] data2 = new byte[data1.length + 1];
		System.arraycopy(data1, 0, data2, 0, data1.length);// 陣列複製:參數分別是來源陣列、來源起始索引、目的陣列、目的起始索引、複製長度
		data2[data2.length - 1] = (byte) 0x80;// 加入特殊字0x80
		String base64EnStr = base64Encode(data2);

		return base64EnStr;
	}

	/**
	 * Locale 修改成符合資料表中的資料格式, ex. zh-TW
	 * 
	 * @param locale
	 * @return
	 */
	public static String getLocale(String locale) {
		try {
			// 若傳入值為空,則取得系統的 locale
			if (!StringUtils.hasText(locale)) {
				// 不使用 Locale.getDefault(), 因為 pipelines 測試主機取出來的語系不一定存在TsmpRtnCode
				locale = LocaleType.ZH_TW;
			}
			;

			// 修改成符合DB中的資料格式, ex.zh-TW
			if (StringUtils.hasText(locale)) {
				locale = locale.replace("_", "-");
			}

			String[] arr = locale.split("-");
			if (StringUtils.hasText(arr[0])) { // language
				arr[0] = arr[0].toLowerCase();
			}

			if (StringUtils.hasText(arr[1])) { // country
				arr[1] = arr[1].toUpperCase();
			}

			locale = arr[0].concat("-").concat(arr[1]);

			// 若不是EN_US、ZH_CN或ZH_TW，預設為EN_US
			if (!LocaleType.EN_US.matches(locale) && !LocaleType.ZH_TW.matches(locale)
					&& !LocaleType.ZH_CN.matches(locale)) {
				locale = LocaleType.EN_US;
			}
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}

		return locale;
	}

//	
//	public static void main(String[] args) {
//		boolean b = isIpInNetwork( //
//			String.format("%d.%d.%d.%d", 192, 168, 30, 194), // 192.168.30.194
//			String.format("%d.%d.%d.%d/%d", 192, 0, 0, 0, 16));	// 192.0.0.0/16
//		System.out.println(b);
//	}
//	
	/**
	 * 判斷IP是否在網段中 例如: "192.168.1.127", "192.168.1.64/26"
	 */
	public static boolean isIpInNetwork(String ip, String cidr) {
		try {
			// 不限網域
			if ("0.0.0.0/0".equals(cidr))
				return true;

			String[] ips = ip.split("\\.");
			int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16)
					| (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
			// 這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
			int type = Integer.parseInt(cidr.replaceAll(".*/", "")); // NOSONAR
			int mask = 0xFFFFFFFF << (32 - type);
			String cidrIp = cidr.replaceAll("/.*", "");
			String[] cidrIps = cidrIp.split("\\.");
			int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16)
					| (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);
			return (ipAddr & mask) == (cidrIpAddr & mask);

		} catch (Exception e) {
			logger.debug(String.format("ip: %s, cidr: %s", ip, cidr));
			logger.debug("isIpInNetwork error: " + e);
			return false;
		}
	}

	public static String buildContent(String template, Map<String, String> params) {
		String value;
		for (String key : params.keySet()) {
			value = "";
			if (params.get(key) != null) {
				value = params.get(key);
			}
			template = template.replaceAll("\\{\\{" + key + "\\}\\}", value);
		}
		return template;
	}

	/**
	 * 將{X}轉成{p},呼叫時fromIndex固定帶0
	 */
	public static String replaceParameterToP(String str) {
		Pattern p = Pattern.compile("\\{[^\\{\\}/]*\\}");
		if (str == null) {
			str = "";
		}
		Matcher m = p.matcher(str); // 不接受 null
		str = m.replaceAll("{p}");
		return str;
	}

	public static TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	public static void setTsmpDpItemsCacheProxy(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		ServiceUtil.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public static TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return tsmpRtnCodeCacheProxy;
	}

	public static void setTsmpRtnCodeCacheProxy(TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy) {
		ServiceUtil.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
	}

}
