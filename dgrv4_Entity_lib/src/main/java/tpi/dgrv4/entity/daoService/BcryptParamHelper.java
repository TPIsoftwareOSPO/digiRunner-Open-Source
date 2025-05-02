package tpi.dgrv4.entity.daoService;

import java.util.Base64;
import java.util.List;

import org.springframework.security.crypto.bcrypt.copy.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.component.cache.proxy.ITsmpDpItemsCacheProxy;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.entity.entity.ITsmpDpItems;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class BcryptParamHelper {
 
	private final ITsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	public String decode(final String bcryptParamString, final String itemNo, final String locale) throws BcryptParamDecodeException {
		return decode(bcryptParamString, itemNo, BcryptFieldValueEnum.SUBITEM_NO, locale);
	}
	
	public String decode(final String bcryptParamString, final String itemNo, final BcryptFieldValueEnum fieldValue, final String locale) throws BcryptParamDecodeException {
		if ("".equals(bcryptParamString)) {
			return new String();
		}

		try {
			if (bcryptParamString.indexOf(",") == -1) {
				throw new IllegalArgumentException();
			}
			final String[] bcryptParams = bcryptParamString.split(",");
			final int index = Integer.parseInt(bcryptParams[1]);

			final List<ITsmpDpItems> items = getItems(itemNo, locale);
			if (items == null || items.isEmpty()) {
				throw new Exception("ItemNo '" + itemNo + "' doesn't exist!");
			} else if ((items.size() - 1) < index) {
				throw new NumberFormatException(index + ", data size = " + items.size());
			}

			// 比對 bcrypt 值
			final ITsmpDpItems item = items.get(index);
			final String bcryptEncodedSubitemNo = new String(base64Decode(bcryptParams[0]));
			
			final String value;
			if (BcryptFieldValueEnum.PARAM1.equals(fieldValue)) {
				value = item.getParam1();
			} else if(BcryptFieldValueEnum.PARAM2.equals(fieldValue)) {
				value = item.getParam2();
			} else if(BcryptFieldValueEnum.PARAM3.equals(fieldValue)) {
				value = item.getParam3();
			} else if(BcryptFieldValueEnum.PARAM4.equals(fieldValue)) {
				value = item.getParam4();
			} else if(BcryptFieldValueEnum.PARAM5.equals(fieldValue)) {
				value = item.getParam5();
			} else { 
				value = item.getSubitemNo();
			}

			
			final boolean isMatched = bCryptPasswordCheck(value, bcryptEncodedSubitemNo);
			if (isMatched) {
				return value;
			} else {
				throw new Exception("Mismatched values.");
			}
		} catch (NumberFormatException e) {
			throw new BcryptParamDecodeException("Invalid index " + e.getLocalizedMessage(), bcryptParamString);
		} catch (IllegalArgumentException e) {
			ITPILogger.tl.error("bcryptParamString: " + bcryptParamString + "\n" + "itemNo: " + itemNo + "\n"
					+ "fieldValue: " + fieldValue + "\n" + "locale: " + locale);
			throw new BcryptParamDecodeException("Invalid parameter format, missing \",\"", bcryptParamString);
		} catch (Exception e) {
			throw new BcryptParamDecodeException(e.getLocalizedMessage(), bcryptParamString);
		}
	}

	private List<ITsmpDpItems> getItems(String itemNo, String locale) {
		return getTsmpDpItemsCacheProxy().queryBcryptParam(itemNo, locale);
	}

	public String encode(String value, int index) {
		String bcryptEncode = bCryptEncode(value);
		byte[] bcryptEncodeBytes = bcryptEncode.getBytes();
		String base64Encode = base64Encode(bcryptEncodeBytes);
		return base64Encode + "," + index;
	}

	/**
	 * 驗證使用者密碼是否正確
	 * @param str
	 * @param encode
	 * @return true / false
	 */
	public static boolean bCryptPasswordCheck(String str, String encode){
		//移除spring-security相關
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.matches(str,encode);
	}

	/**
	 * 密碼編碼
	 * @param str
	 * @return
	 */
	public static String bCryptEncode(String str) {
		//移除spring-security相關
		// 安全層級可以在 constructor 中調整
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.encode(str);
	}

	protected ITsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
	
	private byte[] base64Decode(String base64EncodedStr) {
		return Base64.getDecoder().decode(base64EncodedStr);
	}
	
	private String base64Encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
}