package tpi.dgrv4.gateway.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ILogbackService;

@Component
public class DgrApplicationEventListener {
	
//	@Autowired(required = false)
	private ILogbackService service;
	
	private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";
	private static final String TPI_DGRV4 = "tpi.dgrv4";
	
	@Autowired
	public DgrApplicationEventListener(@Nullable ILogbackService service) {
		super();
		this.service = service;
	}

	@EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {

		//停用tpi.dgrv4的fileerr
		String rs = "";
		if (service != null) {
			rs = service.stop(TPI_DGRV4, "fileerr");
		} else {
			rs = NO_ENTERPRISE_SERVICE;
		}
		
		TPILogger.tl.info("auto logback fileerr appender as " + rs);
		
		//停用tpi.dgrv4的fileloguuid
		if (service != null) {
			rs = service.stop(TPI_DGRV4, "fileloguuid");
		} else {
			rs = NO_ENTERPRISE_SERVICE;
		}
		TPILogger.tl.info("auto logback fileloguuid appender as " + rs);
		
		try {
			//因為tpi.dgrv4.dpaa.component.TsmpCoreTokenInitializerInit有被用thread印出的LOG是sys_info所需要的LOG,所以等待1分
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
		
		//停用tpi.dgrv4的file_sys_info
		if (service != null) {
			rs = service.stop(TPI_DGRV4, "file_sys_info");
		} else {
			rs = NO_ENTERPRISE_SERVICE;
		}
		
		TPILogger.tl.info("auto logback file_sys_info appender as " + rs);
			
		
    }

}
