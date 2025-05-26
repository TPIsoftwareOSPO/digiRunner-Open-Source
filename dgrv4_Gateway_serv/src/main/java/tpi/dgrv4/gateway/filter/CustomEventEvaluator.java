package tpi.dgrv4.gateway.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class CustomEventEvaluator extends EventEvaluatorBase<ILoggingEvent> {
	
	@Override
    public boolean evaluate(ILoggingEvent event) {
		String message = event.getFormattedMessage();
		
		// [ZH] 檢查啟動標誌,
		// 避免使用正則表達式,改用contains(),因為實際只需檢查消息中是否包含特定字符串,而不需要完整的模式匹配
		// [EN] Checking the startup flag,
		// Avoid using regular expressions and use contains() instead, because you only need to check whether the message contains a specific string, without requiring a complete pattern match
		boolean isContain = message.contains("Starting DgrApplication") && message.contains("using Java");
	    boolean logStartFlag = isContain
		        || message.contains("Running with Spring Boot")
		        || message.contains("seconds") 
		        || message.contains("profile is active");
	        
	    // [ZH] 如果是啟動消息，添加到 TPILogger
	    // [EN] If it is a startup message, add it to TPILogger
	    if (logStartFlag) {
	        TPILogger.logStartingMsg.add(message);
	    }
	    
	    return 
	    logStartFlag
	    || message.contains("dgr-keeper server [go LIVE]")
	    || message.contains("This Client Connect to dgr-keeper server")
	    || message.contains("dgRv4 web server info")
	    ;

    }
	
}