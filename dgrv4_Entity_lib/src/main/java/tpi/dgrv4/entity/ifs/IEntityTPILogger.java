package tpi.dgrv4.entity.ifs;

public interface IEntityTPILogger {
	// 靜態內部類作為持有者
    class Holder {
        private static IEntityTPILogger instance = null;
        
        public static IEntityTPILogger getInstance() {
            return instance;
        }
        
        public static void setInstance(IEntityTPILogger logger) {
            instance = logger;
        }
    }
    
    // 透過靜態方法作為捷徑
    static IEntityTPILogger getInstance() {
        return Holder.getInstance();
    }
    
    static void setInstance(IEntityTPILogger logger) {
        Holder.setInstance(logger);
    }
    
    void debug(String m);
    void error(String m);
    void debugDelay2sec(String m);
    void info(String shortErrMsg);
    void warn(String logMsg);
}
