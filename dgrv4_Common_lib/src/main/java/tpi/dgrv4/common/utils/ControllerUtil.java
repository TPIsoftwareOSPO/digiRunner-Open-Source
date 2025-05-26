package tpi.dgrv4.common.utils;

import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.keeper.ITPILogger;

public class ControllerUtil {
	
	private static ITPILogger logger;
	
	/**
	 * Locale 修改成符合資料表中的資料格式, ex. zh-TW
	 * 
	 * @param locale
	 * @return
	 */
	public static String getLocale(String locale) {
		try {
			//若傳入值為空,則預設為英文
			if(!StringUtils.hasLength(locale)) {
				// 不使用 Locale.getDefault(), 因為 pipelines 測試主機取出來的語系不一定存在TsmpRtnCode
				//Locale currentLocale = Locale.getDefault();
				locale = "en-US";
			};
 
			//修改成符合DB中的資料格式, ex.zh-TW
			if(StringUtils.hasLength(locale)) {
				locale = locale.replace("_", "-"); 
			}
			
			String[] arr = locale.split("-");
			if(StringUtils.hasLength(arr[0])) { //language
				arr[0] = arr[0].toLowerCase();
			}
			
			if(StringUtils.hasLength(arr[1])) { //country
				arr[1] = arr[1].toUpperCase();
			}
			
			locale = arr[0].concat("-").concat(arr[1]);
			
			// 若不是EN_US或ZH_TW，預設為EN_US
//			if (!LocaleType.EN_US.matches(locale)&&!LocaleType.ZH_TW.matches(locale)) {
//				locale = LocaleType.EN_US;
//			}
			
		} catch (Exception e) {
			if(logger != null) {
				logger.error(StackTraceUtil.logStackTrace(e));
			}else {
				locale = "en-US";
			}
		}
		
		return locale;
	}
	
	public final static String buildContent(String template, Map<String, String> params) {
		String value;
		for(String key : params.keySet()) {
			value = "";
			if (params.get(key) != null) {
				value = params.get(key);
			}
			template = template.replaceAll("\\{\\{" + key + "\\}\\}", value);
		}
		return template;
	}
	
	public static void setLogger(ITPILogger logger) {
		ControllerUtil.logger = logger;
	}
	
}

