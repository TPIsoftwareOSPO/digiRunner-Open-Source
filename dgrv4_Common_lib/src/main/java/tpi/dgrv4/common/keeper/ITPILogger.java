package tpi.dgrv4.common.keeper;

public abstract class ITPILogger {
	
	public static ITPILogger tl = null;

	public abstract void debug(String m);

	public abstract void error(String m);

	public abstract void debugDelay2sec(String m);

	public abstract void info(String shortErrMsg);
	
	public abstract void warn(String logMsg);
}
