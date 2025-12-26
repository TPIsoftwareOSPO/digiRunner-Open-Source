package tpi.dgrv4.gateway.keeper;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TPILogInfo implements Serializable{
	
	public String userName = "";
	long mstime = System.currentTimeMillis();
	long nstime = System.nanoTime();
	String level;
	StringBuffer logMsg = new StringBuffer();
	String line = getLineNumber2();
	private Date date = new Date();

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public long getMstime() {
		return mstime;
	}

	public void setMstime(long mstime) {
		this.mstime = mstime;
	}

	public long getNstime() {
		return nstime;
	}

	public void setNstime(long nstime) {
		this.nstime = nstime;
	}

	public StringBuffer getLogMsg() {
		return logMsg;
	}

	public void setLogMsg(StringBuffer logMsg) {
		this.logMsg = logMsg;
	}

	public String getFormatDate() {
		date.setTime(mstime);
		SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss.SSS");
		String dateText = df2.format(date);
		return dateText;
	}

//	private static final Throwable throwable = new Throwable();

	public static boolean stackTraceFlag = true;
	public static Object lockKey = new Object();
	private static final String splitStr = "__v4__\n";
	
	public static String getLineNumber2() {
		if (TPILogInfo.stackTraceFlag == false) {
			return "N/A";
		}

		
		// Java 9 之後提供的新方法, 取最近的 5 筆, filter後加入 sb
		StringBuffer sb = StackWalker.getInstance().walk(frames -> frames
			.limit(5)
			.filter(f -> f.getClassName().contains("tpi.dgrv4"))
		    .map(frame -> frame.getClassName() + "." + frame.getMethodName() + "() Line:" + frame.getLineNumber() + splitStr)
		    .collect(StringBuffer::new, StringBuffer::append, StringBuffer::append));
		// 將 sb 分割成 Array
		String[] lines = sb.toString().split(splitStr);
		
		if (lines[1].toString().indexOf("debugDelay2sec") > 0) {
			if (lines.length > 2) {
				return lines[2].toString();
			} else {
				return lines[lines.length - 1].toString();
			}
		}else {
			if (lines.length > 3) {
				return lines[3].toString();
			} else {
				return lines[lines.length - 1].toString();
			}
		}
		
		// 舊作法
//		StackTraceElement[] elements = null;
//		synchronized (lockKey) {
//			throwable.fillInStackTrace();
//			elements = throwable.getStackTrace();
//		}
//
//		if (elements == null) {
//			return "N/A";
//		}
		
		// 舊作法
//		if (elements[1].toString().indexOf("debugDelay2sec") > 0) {
//			if (elements.length > 2) {
//				return elements[2].toString();
//			} else {
//				return elements[elements.length - 1].toString();
//			}
//		} else {
//			if (elements.length > 3) {
//				return elements[3].toString();
//			} else {
//				return elements[elements.length - 1].toString();
//			}
//		}
	}

	@Override
	public String toString() {
		return "" + getFormatDate()  + "," + level + ", " + logMsg + ", "
				+ line ;
	}
	

}
