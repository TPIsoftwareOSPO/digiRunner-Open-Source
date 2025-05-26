package tpi.dgrv4.entity.exceptions;

import java.util.HashMap;
import java.util.Map;

public interface DgrError<ThrowableType extends Throwable> {

	public DgrModule getModule();

    public String getSeq();

    public String getDefaultMessage();

    public default String getCode() {
        return getModule().getGroupId() + getSeq();
    }

    public default ThrowableType throwing() {
        throw new DgrException(this);
    }
    
	public default ThrowableType throwing(String... args) {
		// 參數值只能放入英數字, 不可放入中文, 否則 Return code 參數不符合多國語系定義
		Map<String, String> params = new HashMap<>();
		// index 從0開始
		for (int i = 0; i < args.length; i++) {
			String str = args[i];
			checkParam(str);
			params.put(String.valueOf(i), String.valueOf(str));
		}
		throw new DgrException(this, params);
	}

    public static String getLocalizationMessage(DgrError<?> rtn) {
        // Return default message
        return String.format("%s - %s", rtn.getCode(), rtn.getDefaultMessage());
    }
    
    public static void checkParam(String str) {
		String regex = "[^\\u4e00-\\u9fa5]*"; //不能為中文		
        if(!str.matches(regex)) {
        	throw DgrRtnCode._1285.throwing();
        }
    }

}
