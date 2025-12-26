package tpi.dgrv4.gateway.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

public class CustomUuidEventEvaluator extends EventEvaluatorBase<ILoggingEvent> {

	@Override
    public boolean evaluate(ILoggingEvent event) {

        String message = event.getFormattedMessage();
        
        return message.contains("LOGUUID");
		
    }
}