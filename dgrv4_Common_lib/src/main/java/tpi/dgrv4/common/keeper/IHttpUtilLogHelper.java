package tpi.dgrv4.common.keeper;

import java.util.concurrent.atomic.AtomicReference;

public interface IHttpUtilLogHelper {

	ConfigHelper instance = new ConfigHelper();

	boolean isLogResponseTime();

	class ConfigHelper {
        public static final AtomicReference<IHttpUtilLogHelper> h1 = new AtomicReference<>();
	}
}
