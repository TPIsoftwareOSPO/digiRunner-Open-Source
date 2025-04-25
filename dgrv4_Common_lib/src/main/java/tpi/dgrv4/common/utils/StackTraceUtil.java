package tpi.dgrv4.common.utils;

import java.io.ByteArrayOutputStream;

import org.springframework.util.StringUtils;

public class StackTraceUtil {

	/**
	 * 效能比 getLineNumer() 佳, Java 9 之後提供的方法
	 * */
    public static synchronized String getLineNumber() {
    	final String splitStr = "__v4__\n";
    	// Java 9 之後提供的新方法, 取最近的 5 筆, filter後加入 sb
		StringBuffer sb = StackWalker.getInstance().walk(frames -> frames
			.limit(5)
			.filter(f -> f.getClassName().contains("tpi.dgrv4"))
		    .map(frame -> frame.getClassName() + "." + frame.getMethodName() + "() Line:" + frame.getLineNumber() + splitStr)
		    .collect(StringBuffer::new, StringBuffer::append, StringBuffer::append));
		// 將 sb 分割成 Array
		String[] lines = sb.toString().split(splitStr);
		
		if (lines.length > 2) {
			return lines[1].toString();
		} else {
			return lines[lines.length - 1].toString();
		}
    }
    
//    private static final Throwable throwable = new Throwable();
    /**
     * 效能不好, 請改換 getLineNumber2()
     * */
//    public static synchronized String getLineNumber() {
//    	throwable.fillInStackTrace();
//    	StackTraceElement[] elements = throwable.getStackTrace();
//    	return (elements == null || elements.length <= 1) ? "N/A" : elements[1].toString();
//    }
	
	/**
	 * 取出 exception info String
	 * @param e
	 * @return
	 */
	public static String logStackTrace(Throwable e) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		e.printStackTrace(new java.io.PrintWriter(buf, true));
		String msg = buf.toString();
		try {
			buf.close();
		} catch (Exception t) {
			return e.getMessage();
		}
		return msg;
	}
	
	public static String logTpiShortStackTrace(Throwable e) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		e.printStackTrace(new java.io.PrintWriter(buf, true));
		String msg = buf.toString();
		try {
			buf.close();
		} catch (Exception t) {
			return e.getMessage();
		}
//		return msg;
		return getShortErrMsg(msg);
	}
	
	/**
	 * 業務邏輯錯誤應該不要印出所有 stack trace 把定義明確的 exception, logger Level 改為 INFO
	 * @param errStr
	 * @return
	 */
	private static String getShortErrMsg(String errStr) {
		String errMsg = "";
		String keyword = "tpi.dgrv4";
		if (StringUtils.hasText(errStr)) {
			String[] errStrArr = errStr.split("\n");
			if (errStrArr != null && errStrArr.length > 0) {
				//errMsg = errStrArr[0];
				for (String str : errStrArr) {
					// 只印出有包含以下文字的錯誤訊息
					if (str.contains(keyword) || str.contains("Exception:") || str.contains("Throwable:")) {
						errMsg += "\n" + str;
					}
				}
			}
		}
		return errMsg;
	}
	
	/**
	 * 獲取當前執行線程的呼叫堆疊作為字串
	 * @return
	 */
    public static String getStackTraceAsString() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder("=== [Current Thread: " + Thread.currentThread().getName() + "] ===\n");
        Thread.currentThread().getName();        
        // 從索引 2 開始，跳過 getStackTrace() 和 getStackTraceAsString() 方法
        for (int i = 2; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            sb.append("\t").append(element.getClassName()).append(".")
              .append(element.getMethodName()).append("(")
              .append(element.getFileName()).append(":")
              .append(element.getLineNumber()).append(")\n");
        }
        sb.append("===================");
        return sb.toString();
    }
	
}

