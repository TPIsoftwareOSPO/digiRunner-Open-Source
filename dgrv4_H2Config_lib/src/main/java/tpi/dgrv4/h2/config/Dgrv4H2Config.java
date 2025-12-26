package tpi.dgrv4.h2.config;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.h2.confg.ifs.IDgrv4H2Config;

@Component
public class Dgrv4H2Config implements IDgrv4H2Config {

	@Override
	public Server createTcpServer(String string, String string2, String string3, String portStr, ITPILogger tl) {
		Server h2Server = null;
		try {
			h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", 
					"-tcpPort", portStr);
		} catch (SQLException e) {
			tl.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		return h2Server;
	}

}
