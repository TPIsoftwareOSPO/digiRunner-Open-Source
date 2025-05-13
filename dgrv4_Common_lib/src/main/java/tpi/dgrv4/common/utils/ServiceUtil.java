package tpi.dgrv4.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.keeper.ITPILogger;

public class ServiceUtil {

	private static ITPILogger logger;

	public static String buildContent(String template, Map<String, String> params) {
		String value;
		for(String key : params.keySet()) {
			value = "";
			if (params.get(key) != null) {
				value = params.get(key);
			}
			// replaceAll() 方法的第一個參數是作為正則表達式處理的，如果 key 變數包含正則表達式的特殊字符，
			// 可能會導致意外行為或安全問題。
			// 對於 replaceAll() 方法，正確的修正方式是使用 Pattern.quote() 包裝正則表達式部分
			template = template.replaceAll("\\{\\{" + Pattern.quote(key) + "\\}\\}", value);
		}
		return template;
	}
	
	/**
	 * base 64解密
	 * @param base64EncodedStr
	 * @return
	 */
	public static byte[] base64Decode(String base64EncodedStr) {
		byte[] bdata = null;
		try {
			bdata = Base64.getDecoder().decode(base64EncodedStr);
		} catch (IllegalArgumentException e) {
			bdata = Base64.getUrlDecoder().decode(base64EncodedStr);
		}
		return bdata;
	}
	
	/**
	 * Locale 修改成符合資料表中的資料格式, ex. zh-TW
	 * 
	 * @param gateway_locale
	 * @return
	 */
	public static String getLocale(String gateway_locale) {
		try {
			//若傳入值為空,則取得系統的 locale
			if(!StringUtils.hasText(gateway_locale)) {
				// 不使用 Locale.getDefault(), 因為 pipelines 測試主機取出來的語系不一定存在TsmpRtnCode
				gateway_locale = LocaleType.ZH_TW;
			};
 
			//修改成符合DB中的資料格式, ex.zh-TW
			if(StringUtils.hasText(gateway_locale)) {
				gateway_locale = gateway_locale.replace("_", "-"); 
			}
			
			String[] arr = gateway_locale.split("-");
			if(StringUtils.hasText(arr[0])) { //language
				arr[0] = arr[0].toLowerCase();
			}
			
			if(StringUtils.hasText(arr[1])) { //country
				arr[1] = arr[1].toUpperCase();
			}
			
			gateway_locale = arr[0].concat("-").concat(arr[1]);
			
			//tom, 20240411, 因為之後會越來越多語系,所以不再限制語系,以免每次都要修正
			// 若不是EN_US或ZH_TW，預設為EN_US
//			if (!LocaleType.EN_US.matches(gateway_locale) && !LocaleType.ZH_TW.matches(gateway_locale)) {
//				gateway_locale = LocaleType.EN_US;
//			}
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
		
		return gateway_locale;
	}

	/**
	 * 如果 input 為 null, 則返回 alternatives 中第一個非 null 的值;<br>
	 * 若 alternatives 為空值, 則返回 Integer.MIN_VALUE
	 * @param gateway_input
	 * @param alternatives
	 * @return
	 * @throws Exception
	 */
	public static Integer nvl(Integer gateway_input, Integer ... alternatives) {
		return nvl(Integer.class, () -> {
			return Integer.MIN_VALUE;
		}, (i) -> {
			return i;
		}, gateway_input, alternatives);
	}

	/**
	 * 如果 input 為 null, 則返回 alternatives 中第一個非 null 的值;<br>
	 * 若 alternatives 為空值, 則返回空字串
	 * @param gateway_input
	 * @param alternatives
	 * @return
	 * @throws Exception
	 */
	public static String nvl(String gateway_input, String ... alternatives) {
		return nvl(String.class, () -> {
			return new String();
		}, (s) -> {
			return String.valueOf(s);
		}, gateway_input, alternatives);
	}

	@SafeVarargs
	private static <R> R nvl(Class<R> clazz, Supplier<R> initializer, Function<R, R> caster, R gateway_input, R ... alternatives) {
		if (gateway_input == null) {
			if (alternatives == null || alternatives.length == 0) {
				return initializer.get();
			} else {
				gateway_input = alternatives[0];
				alternatives = Arrays.copyOfRange(alternatives, 1, alternatives.length);
				return nvl(clazz, initializer, caster, gateway_input, alternatives);
			}
		}

		return caster.apply(gateway_input);
	}
	
	/**
	 * 檢查email格式
	 * 可以多個mail,以「,」分隔
	 * 
	 * @param input
	 * @return
	 */
	public static boolean checkEmail(String input) {
		String rule = RegexpConstant.EMAIL;
		return checkDataByPattern(input, rule);
			
	}
	
	/**
	 * 主機路徑,注意只有/是不合法
	 * 
	 * @param input
	 * @return
	 */
	public static boolean checkHostPath(String input) {
		String rule = RegexpConstant.HOST_PATH;
		return checkDataByPattern(input, rule);
			
	}
	
	/**
	 * IP4和IP6的格式
	 * 
	 * @param input
	 * @return
	 */
	public static boolean checkIP(String input) {
		String rule1 = RegexpConstant.IP;
		String rule2 = RegexpConstant.IP6_1;
		String rule3 = RegexpConstant.IP6_2;
		String rule4 = RegexpConstant.IP6_3;
		return (checkDataByPattern(input, rule1) || checkDataByPattern(input, rule2) || checkDataByPattern(input, rule3) || checkDataByPattern(input, rule4));
			
	}
	
	/**
	 * 1或0的格式
	 * 
	 * @param input
	 * @return
	 */
	public static boolean checkOneZero(String input) {
		String rule = RegexpConstant.ONE_ZERO;
		return checkDataByPattern(input, rule);
			
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
		Matcher m = p.matcher(input); 
		return m.matches();
			
	}

	public static <T> T deepCopy(T source, Class<T> clazz) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readValue( //
				om.writeValueAsString(source), //
				clazz //
			);
		} catch (Exception e) {
			logger.debug("deepCopy error:\n" + StackTraceUtil.logStackTrace(e));
		}
		return null;
	}

	public static String truncateExceptionMessage(Exception e, int maxLength) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		StringBuffer sb = sw.getBuffer();
		if (sb.length() > maxLength) {
			sb.setLength(maxLength);
		}
		return sb.toString();
	}
	
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
	
	/**
	 * 只能輸入數字英文
	 * 
	 * @param gateway_input
	 * @return
	 */
	public static boolean isNumericOrAlphabetic(String gateway_input) {
		String rule = RegexpConstant.ENGLISH_NUMBER;
		return checkDataByPattern(gateway_input, rule);
			
	}
	
	public static String join(CharSequence gateway_delimiter, Integer... elements) {
		Objects.requireNonNull(gateway_delimiter);
		Objects.requireNonNull(elements);
		// Number of elements not likely worth Arrays.stream overhead.
		StringJoiner joiner = new StringJoiner(gateway_delimiter);
		for (Integer i: elements) {
		    joiner.add(String.valueOf(i));
		}
		return joiner.toString();
	}

	public static String join(CharSequence gateway_delimiter, Iterable<? extends Integer> elements) {
		Objects.requireNonNull(gateway_delimiter);
		Objects.requireNonNull(elements);
		StringJoiner joiner = new StringJoiner(gateway_delimiter);
		for (Integer i : elements) {
			joiner.add(String.valueOf(i));
		}
		return joiner.toString();
	}
	
	public static Locale parseLocale(String gateway_localeStr) {
		try {
			String[] a = gateway_localeStr.split("_");
			String[] a1 = gateway_localeStr.split("-");
			if (a1.length > a.length) {
				a = deepCopy(a1, String[].class);
				if (a == null) {
					a = LocaleType.ZH_TW.split("-");
				}
			}

			Locale.Builder gateway_builder = new Locale.Builder();
			if (a.length > 2) {
				gateway_builder.setLanguage(a[0]);
				gateway_builder.setRegion(a[1]);
				gateway_builder.setVariant(a[2]);
			} else if (a.length > 1) {
				gateway_builder.setLanguage(a[0]);
				gateway_builder.setRegion(a[1]);
			} else if (a.length > 0) {
				gateway_builder.setLanguage(a[0]);
			}
			return gateway_builder.build();
		} catch (Exception e) {
			return Locale.getDefault();
		}
	}
	
	public static boolean isChainedExceptionMessagesContainIgnoreCase(Throwable gateway_t, String ...inputs) {
		if (inputs == null || inputs.length == 0) {
			return true;
		}

		String gateway_detailMessage = null;
		while(gateway_t != null) {
			gateway_detailMessage = gateway_t.getMessage();
			if (StringUtils.hasLength(gateway_detailMessage)) {
				gateway_detailMessage = gateway_detailMessage.toLowerCase();
				
				for(String input : inputs) {
					if (StringUtils.hasLength(input)) {
						input = input.toLowerCase();
						if (gateway_detailMessage.contains(input)) {
							return true;
						}
					}
				}
			}
			gateway_t = gateway_t.getCause();
		}
		return false;
	}
	
	public static boolean isValueTooLargeException(Throwable gateway_t) {
		return isChainedExceptionMessagesContainIgnoreCase(gateway_t, 
			"Value too long",					// H2
			"value too large",					// Oracle
			"Data too long",					// MySQL
			"Text was truncated", "會被截斷",		// SQLServer
			"value too long");					// PostgreSQL
	}
	
	/**
     * 文字打碼,只顯示前後N個字,中間其餘隱藏加星號(*) <br>
     * 引數異常直接返回""
     *
     * @param str		要處理的資料文字
     * @param front     需要顯示前幾位
     * @param end       需要顯示末幾位
     * @return 處理完成的資料
     */
    public static String dataMask(String str, int front, int end) {
        //資料不能為空
        if (!StringUtils.hasLength(str)) {
            return "";
        }
        
        //需要擷取的不能小於0
        if (front < 0 || end < 0) {
        	return "";
        }
        
        //需要擷取的長度,若大於資料,全取代為*
        if ((front + end) > str.length()) {
        	StringBuffer strBuffer = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
            	strBuffer.append("*");
            }
            return strBuffer.toString();
        }
        
        //計算*的數量
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

	// https://blog.csdn.net/qq_35387940/article/details/84391784
	public static String getIpAddress(HttpServletRequest request) {
		String ipAddress = null;
		try {
			ipAddress = request.getHeader("x-forwarded-for");
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
				if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
					// 根據網卡取本機配置的IP
					try {
						InetAddress inet = InetAddress.getLocalHost();
						if (inet != null) {
							ipAddress = inet.getHostAddress();
						}
					} catch (UnknownHostException e) {
						logger.debug("getIpAddress error:\n" + StackTraceUtil.logStackTrace(e));
					}
				}
			}
			// 對於通過多個代理的情況，第一個IP為客戶端真實IP，多個IP按照','分割
			if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
				// = 15
				if (ipAddress.indexOf(",") > 0) {
					ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
				}
			}
		} catch (Exception e) {
			ipAddress = "";
		}

		return ipAddress;
	}

	public static String getFQDN(HttpServletRequest request) {
		String dn = null;
		try {
			dn = request.getHeader("X-Forwarded-Host");
		} catch (Exception e) {
			dn = "";
		}
		return dn;
	}
	
	
	/**
	 * [ZH] 判斷IP是否在網段中 <br>
	 * 例如: <br>
	 * "192.168.1.127", "192.168.1.64/26"  <br>
	 * [EN] Determine whether the IP is in the network segment <br>
	 * For example: <br>
	 * "192.168.1.127", "192.168.1.64/26" <br>
	 */
	public static boolean isIpInNetwork(String ip, String gateway_cidr) {
		try {
			// [ZH] 1.特殊情況處理：
			// 不限網域（接受任何 IP）
			// [EN] 1.Special Situation Handling：
			// Unlimited domain (accept any IP)
			if("0.0.0.0/0".equals(gateway_cidr)) return true;
			
			// [ZH] 2.IP 地址轉換：
			// 將點分十進制的 IP 地址（例如 "192.168.1.1"）分割成四個部分
			// [EN] 2.IP address conversion：
			// Split a dotted decimal IP address (e.g. "192.168.1.1") into four parts
			String[] ips = ip.split("\\.");
			// [ZH] 將這四個部分轉換為一個 32 位整數，使用位移運算（<<）進行組合
			// [EN] CConvert these four parts into a 32-bit integer and combine them using the bit shift operation (<<)
			int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16)
					| (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
			
			// [ZH] 3.CIDR 處理：
			// 從 CIDR 表示法（例如 "192.168.1.0/24"）中提取掩碼位數（例如 "24"）
			// [EN] 3.CIDR processing:
			// Extract the number of mask bits (for example, "24") from CIDR notation (for example, "192.168.1.0/24")
			String[] cidrParts = gateway_cidr.split("/");
			int type = Integer.parseInt(cidrParts[1]);
			
			// [ZH] 計算對應的網路掩碼值（mask）
			// [EN] Calculate the corresponding network mask value (mask)
			int mask = 0xFFFFFFFF << (32 - type);
			
			// [ZH] 從 CIDR 表示法中提取網路地址部分
			// [EN] Extract the network address portion from CIDR notation
			String cidrIp = cidrParts[0];
			
			// [ZH] 網路地址轉換為整數值
			// [EN] Convert network address to integer value
			String[] cidrIps = cidrIp.split("\\.");
			int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16)
					| (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);
			
			// [ZH] 4.網段判斷：
			// 使用位運算 &（AND）運算符將 IP 地址和網路掩碼進行運算
			// 同樣對網路地址和網路掩碼進行運算
			// 比較這兩個結果是否相等，相等則表示 IP 在該網段內
			// [EN] 4.Network segment determination:
			// Use the bitwise & (AND) operator to perform operations on the IP address and network mask
			// Also perform operations on the network address and network mask
			// Compare the two results to see if they are equal. If they are equal, it means the IP is in the network segment.
			return (ipAddr & mask) == (cidrIpAddr & mask);
			
		} catch (Exception e) {
			logger.debug("ip: " + ip + ", cidr: " + gateway_cidr);
			logger.debug("isIpInNetwork error:\n" + StackTraceUtil.logStackTrace(e));
			return false;
		}
	}
	
    public static boolean isIpInNetworkForIpv6(String ip, String cidr) {
        try {
            // Split CIDR into address and prefix length
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid CIDR format");
            }

            // Parse address and prefix length
            Inet6Address cidrAddress = (Inet6Address) InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            // Parse the IP to check
            Inet6Address checkAddress = (Inet6Address) InetAddress.getByName(ip);

            // Convert addresses to BigInteger
            BigInteger cidrBigInt = new BigInteger(1, cidrAddress.getAddress());
            BigInteger checkBigInt = new BigInteger(1, checkAddress.getAddress());

            // Calculate the mask
            BigInteger mask = BigInteger.valueOf(-1).shiftLeft(128 - prefixLength);

            // Apply mask to both addresses
            BigInteger cidrsubnet = cidrBigInt.and(mask);
            BigInteger checkSubnet = checkBigInt.and(mask);

            // Compare the results
            return cidrsubnet.equals(checkSubnet);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address or CIDR", e);
        }
    }

	public static boolean isIpInCidr(String ip, String cidr) {
		try {
			// 分割 CIDR
			String[] parts = cidr.split("/");
			String cidrIp = parts[0];
			int prefixLength = Integer.parseInt(parts[1]);

			// 驗證 prefixLength
			if (prefixLength < 0 || (cidrIp.contains(":") && prefixLength > 128) || (!cidrIp.contains(":") && prefixLength > 128)) {
				throw new IllegalArgumentException("Invalid prefix length");
			}
			// 取得 IP 位址的位元組陣列
			InetAddress inetAddress = InetAddress.getByName(cidrIp);
			byte[] cidrBytes = inetAddress.getAddress();
			byte[] ipBytes = InetAddress.getByName(ip).getAddress();

			// 處理IPv4和IPv6位址的長度差異
			if (cidrBytes.length != ipBytes.length) {
				return false;
			}

			// 計算子網遮罩
			byte[] mask = new byte[cidrBytes.length];
			for (int i = 0; i < mask.length; i++) {
				int maskBits = Math.min(prefixLength - (i * 8), 8);
				mask[i] = (byte) (maskBits > 0 ? 0xFF << (8 - maskBits) : 0);
			}

			// 進行位元 AND 操作並比較
			for (int i = 0; i < cidrBytes.length; i++) {
				if ((cidrBytes[i] & mask[i]) != (ipBytes[i] & mask[i])) {
					return false;
				}

			}
			return true;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid IP address or CIDR", e);
		}
	}

	public static boolean isIpInWhitelist(String ipOrFqdn, Set<String> whitelist) {
		try {
			InetAddress addr = InetAddress.getByName(ipOrFqdn);
			String ip = addr.getHostAddress();
			for (String entry : whitelist) {
				//當白名單內有 ::/0 或0.0.0.0/0 代表全部放行，直接不用判斷後面
				if ("::/0".equalsIgnoreCase(entry) || "0.0.0.0/0".equalsIgnoreCase(entry)) {
					return true;
				} else if ("localhost".equalsIgnoreCase(entry) || "::1".equalsIgnoreCase(entry) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(entry) || "127.0.0.1".equalsIgnoreCase(entry)) {
					//特殊處理localhost情況
					//白名單中有本機位址情況下，所有類型的本機位址都要放
					if (addr.isLoopbackAddress()) {
						return true;
					}
				} else if (entry.contains("/")) {
					if (isIpInCidr(ip, entry)) {
						return true;
					}
				} else if (entry.equals(ip) || entry.equalsIgnoreCase(ipOrFqdn)) {
					return true;
				}
			}
		} catch (UnknownHostException e) {
			logger.error("ipOrFqdn :　"+ipOrFqdn +"\n whitelist" + whitelist.toString() +"\n" + e);
		}

		return false;
	}

	public static void setLogger(ITPILogger logger) {
		ServiceUtil.logger = logger;
	}

	/**
	 * base 64 加密(無後綴)
	 */
	public static String base64EncodeWithoutPadding(byte[] bytes) {
		return Base64.getEncoder().withoutPadding().encodeToString(bytes);
	}

	/**
	 * base 64 加密(有後綴)
	 */
	public static String base64Encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static Long parseSequenceToLong(Object seq) {
		Long result = null;
		if (seq.getClass().equals(Long.class)) {
			result = (Long) seq;
		} else if (seq.getClass().equals(BigInteger.class)) {
			BigInteger seq2 = (BigInteger) seq;
			result = seq2.longValue();
		} else if(seq.getClass().equals(Number.class)){
			Number seq2 = (Number) seq;
			result = seq2.longValue();
		} else if (seq.getClass().equals(BigDecimal.class)) {
			BigDecimal seq2 = (BigDecimal) seq;
			result = seq2.longValue();
		} 
		return result;
	}
	
	
}