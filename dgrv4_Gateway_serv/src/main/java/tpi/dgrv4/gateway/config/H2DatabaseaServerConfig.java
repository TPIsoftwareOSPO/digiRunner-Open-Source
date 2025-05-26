package tpi.dgrv4.gateway.config;

import java.net.ServerSocket;
import java.sql.SQLException;

import jakarta.annotation.PostConstruct;

//import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.h2.confg.ifs.IDgrv4H2Config;

/**
 * 2024 / 6/ 16 完成 IoC 注入, git commit:"74ceb80" 
 *  
 * @author John Chen
 *
 */
@Component
public class H2DatabaseaServerConfig {
	
	@Value("${digi.h2.port:9090}")
	private String h2Port ;

//	@Autowired(required = false)
	private IDgrv4H2Config dgrv4H2Config;
	
	@Autowired
	public H2DatabaseaServerConfig(@Nullable IDgrv4H2Config dgrv4h2Config) {
		super();
		dgrv4H2Config = dgrv4h2Config;
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public Object inMemoryH2DatabaseaServer() throws SQLException {
		StringBuffer buf = new StringBuffer();
		buf.append("\n======================== [H2 Config START] ==========================\n");
		buf.append("\n ... * [H2 Config] Lib IoC Object Status: " + dgrv4H2Config + " * ...\n");
		
		Object h2Server = null;
		int port = Integer.parseInt(h2Port);
		String portStr = String.valueOf(port);
		if (isPortAvailable(port) && dgrv4H2Config!=null) {
			h2Server = dgrv4H2Config.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", portStr, TPILogger.tl);
			buf.append("\n ... * H2 server Start Complete on Port [" + portStr + "] * ...\n");
		} else {
			buf.append("\n ... * Port" + portStr + " is already in use OR IoC FAILED. H2 server will not be started. * ...\n");
		}
		buf.append("\n======================== [H2 Config END] ============================\n");
		TPILogger.tl.info(buf.toString());
		return h2Server;
	}

	private boolean isPortAvailable(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			serverSocket.setReuseAddress(true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
