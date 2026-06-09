package tpi.dgrv4.gateway.config;

import java.net.ServerSocket;
import java.sql.SQLException;


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

	@Value("${digi.h2.web.port:9092}")
    private int webPort;
	
	@Value("${digi.h2.server.enable:true}")
	private boolean h2serverEnable ;
	
	@Value("${spring.datasource.url:}")
	private String datasourceUrl;

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

		if (h2serverEnable == false) {
			buf.append("\n ... * H2 server enable = [" + h2serverEnable + "] * ...\n");
		} else if (isRemoteTcpMode()) {
			buf.append("\n ... * datasource url is remote TCP mode [" + datasourceUrl + "], skip starting H2 server. * ...\n");
		} else if (isPortAvailable(port) && dgrv4H2Config != null) {
			h2Server = dgrv4H2Config.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", portStr, TPILogger.tl);
			buf.append("\n ... * H2 server Start Complete on Port [" + portStr + "] * ...\n");
		} else {
			buf.append("\n ... * Port [" + portStr + "] is already in use OR IoC FAILED. H2 server will not be started. * ...\n");
		}

		buf.append("\n======================== [H2 Config END] ============================\n");
		TPILogger.tl.info(buf.toString());
		return h2Server;
	}


    /**
     * H2 Web Console
     * Browser console
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Object h2WebServer() throws SQLException {
        StringBuffer buf = new StringBuffer();
        buf.append("\n======================== [H2 Web Console START] ==========================\n");

        Object webServer = null;

        if (!h2serverEnable) {
            buf.append("\n ... * H2 Web Console skipped, server enable = [false] * ...\n");
        } else if (isRemoteTcpMode()) {
            buf.append("\n ... * datasource url is remote TCP mode [" + datasourceUrl + "], skip starting H2 Web Console. * ...\n");
        } else if (!isPortAvailable(webPort)) {
            buf.append("\n ... * Port [" + webPort + "] is already in use. H2 Web Console will not be started. * ...\n");
        } else if (dgrv4H2Config == null) {
            buf.append("\n ... * IoC FAILED (dgrv4H2Config is null). H2 Web Console will not be started. * ...\n");
        } else {
            webServer = dgrv4H2Config.createWebServer(
                    "-web", "-webAllowOthers", "-webPort",
                    String.valueOf(webPort), TPILogger.tl  
            );
            buf.append("\n ... * H2 Web Console Start Complete on Port [" + webPort + "] * ...\n");
            buf.append("\n ... * URL  : http://localhost:" + webPort + " * ...\n");
            buf.append("\n ... * JDBC : " + buildTcpJdbcUrl(Integer.parseInt(h2Port)) + " * ...\n");
            buf.append("\n ... * CLI  : java -cp /opt/dgr-v4/libsext/party-3rd-jar/h2-2.3.232.jar org.h2.tools.Shell \\\n");
            buf.append(" ...            -url \"" + buildTcpJdbcUrl(Integer.parseInt(h2Port)) + "\" \\\n");
            buf.append(" ...            -user sa \\\n");
            buf.append(" ...            -password \"\" \n");
        }

        buf.append("\n======================== [H2 Web Console END] ============================\n");
        TPILogger.tl.info(buf.toString());
        return webServer;
    }

    /**
     * 若 datasource url 已經是 tcp:// 格式
     * 代表本機是 H2 客戶端，連接遠端 H2 Server，不應在本機啟動 H2 Server
     */
    private boolean isRemoteTcpMode() {
        if (datasourceUrl == null || datasourceUrl.isEmpty()) {
            return false;
        }
        String withoutPrefix = datasourceUrl.replaceFirst("(?i)^jdbc:h2:", "");
        return withoutPrefix.toLowerCase().startsWith("tcp://");
    }

    private String buildTcpJdbcUrl(int tcpPort) {
        if (datasourceUrl == null || datasourceUrl.isEmpty()) {
            return "jdbc:h2:tcp://localhost:" + tcpPort + "/ (無法取得 datasource url)";
        }

        // 去掉 jdbc:h2: 前綴，並去掉 ; 之後的參數
        // jdbc:h2:./h2dir/dgrdb;NON_KEYWORDS=VALUE  → ./h2dir/dgrdb
        // jdbc:h2:mem:dgrdb;NON_KEYWORDS=VALUE      → mem:dgrdb
        String withoutPrefix = datasourceUrl.replaceFirst("(?i)^jdbc:h2:", "");
        String dbPath = withoutPrefix.contains(";") 
                        ? withoutPrefix.substring(0, withoutPrefix.indexOf(";")) 
                        : withoutPrefix;

        return "jdbc:h2:tcp://localhost:" + tcpPort + "/" + dbPath;
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