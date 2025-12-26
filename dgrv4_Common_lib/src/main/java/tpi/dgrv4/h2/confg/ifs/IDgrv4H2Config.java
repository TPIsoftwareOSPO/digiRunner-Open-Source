package tpi.dgrv4.h2.confg.ifs;

import org.h2.tools.Server;

import tpi.dgrv4.common.keeper.ITPILogger;

public interface IDgrv4H2Config {

	Server createTcpServer(String string, String string2, String string3, String portStr, ITPILogger tl);

}
