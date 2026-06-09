package tpi.dgrv4.common.exceptions;

import java.util.HashMap;
import java.util.Map;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;

public interface ITsmpDpAaError<ThrowableType extends Throwable> {

	public TsmpDpModule getModule();

    public String getSeq();

    public String getDefaultMessage();

    public default String getCode() {
        return getModule().getGroupId() + getSeq();
    }

    public default ThrowableType throwing() {
        throw new TsmpDpAaException(this);
    }
    
    public default ThrowableType throwing(String...args) {
    	// 參數值只能放入英數字, 不可放入中文, 否則 Return code 參數不符合多國語系定義
    	Map<String, String> params = new HashMap<>();
    	String rtnCode = this.getCode();	
    	// index 從0開始
    	for(int i = 0; i < args.length; i++) {
    		String str = args[i];
    		if("1499".equals(rtnCode) || "1559".equals(rtnCode) ) {
    			//rtnCode 1499 , 1599 為特例,不做中文檢查,為了能真實反應介接得到的訊息
    		}else {
    			checkParam(str);
    		}
    		params.put(String.valueOf(i), String.valueOf(str));
    	}
    	throw new TsmpDpAaException(this, params);
    }
	/**
	 *  關掉中文檢查		
	 *  false 關	
	 *  true  開		
	 */
    public default ThrowableType throwing(Boolean state, String...args) {
    	// 參數值只能放入英數字, 不可放入中文, 否則 Return code 參數不符合多國語系定義
    	Map<String, String> params = new HashMap<>();
    	// index 從0開始
    	for(int i = 0; i < args.length; i++) {
    		String str = args[i];
    		if(state) {
    			checkParam(str);
    		}
    		params.put(String.valueOf(i), String.valueOf(str));
    	}
    	throw new TsmpDpAaException(this, params);
    }

    public static String getLocalizationMessage(ITsmpDpAaError<?> rtn) {
        // Return default message
        return String.format("%s - %s", rtn.getCode(), rtn.getDefaultMessage());
    }
    
    public static void checkParam(String str) {
		String regex = "[^\\u4e00-\\u9fa5]*"; //不能為中文		
        if(!str.matches(regex)) {
        	throw TsmpDpAaRtnCode._1285.throwing();
        }
    }
    
    /**
     * 建立例外物件但不拋出
     */
    public default TsmpDpAaException toException() {
        return new TsmpDpAaException(this);
    }
    
    /**
     * 建立例外物件但不拋出（帶參數）
     */
    public default TsmpDpAaException toException(String...args) {
        Map<String, String> params = new HashMap<>();
        String rtnCode = this.getCode();	
        for(int i = 0; i < args.length; i++) {
            String str = args[i];
            if("1499".equals(rtnCode) || "1559".equals(rtnCode) ) {
                //rtnCode 1499 , 1599 為特例,不做中文檢查
            }else {
                checkParam(str);
            }
            params.put(String.valueOf(i), String.valueOf(str));
        }
        return new TsmpDpAaException(this, params);
    }
    
    /**
     * 建立例外物件但不拋出（帶參數和中文檢查開關）
     */
    public default TsmpDpAaException toException(Boolean state, String...args) {
        Map<String, String> params = new HashMap<>();
        for(int i = 0; i < args.length; i++) {
            String str = args[i];
            if(state) {
                checkParam(str);
            }
            params.put(String.valueOf(i), String.valueOf(str));
        }
        return new TsmpDpAaException(this, params);
    }


}
