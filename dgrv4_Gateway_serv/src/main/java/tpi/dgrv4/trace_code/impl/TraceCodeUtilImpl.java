package tpi.dgrv4.trace_code.impl;

import org.springframework.stereotype.Component;

import tpi.dgrv4.common.ifs.TraceCodeUtilIfs;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TraceCodeUtilImpl implements TraceCodeUtilIfs{
	
	/**
	 *  說明 dgr-v4 流程使用
	 *  // 說明 dgr-v4 流程使用, 沒有注入就不會引用
		if (traceCodeUtil != null) traceCodeUtil.logger(this);
	 */
	public void logger(Object thisClass) {
		TPILogger.tl.debug(StackTraceUtil.logStackTrace(new Throwable(thisClass.getClass().getName() + " Trace...")));
	}
}
