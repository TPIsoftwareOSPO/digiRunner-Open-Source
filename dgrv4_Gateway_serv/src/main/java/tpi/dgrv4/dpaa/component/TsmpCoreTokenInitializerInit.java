package tpi.dgrv4.dpaa.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tpi.dgrv4.dpaa.vo.UndertowConfigInfo;
import tpi.dgrv4.entity.component.ITsmpCoreTokenInitializerInit;
import tpi.dgrv4.entity.component.IVersionService;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.LinkedList;

@Component
public class TsmpCoreTokenInitializerInit implements ITsmpCoreTokenInitializerInit {

	private IVersionService versionService;

	@Autowired
	public TsmpCoreTokenInitializerInit(IVersionService versionService) {
		super();
		this.versionService = versionService;
	}

	@Override
	public StringBuffer init(ContextRefreshedEvent e, StringBuffer info) {

	    if (info == null) {
	        info = new StringBuffer();
	    }
		
		String s = "\n      _       ____                                      _  _   \r\n"
				+ "   __| | __ _|  _ \\ _   _ _ __  _ __   ___ _ __  __   __ || |  \r\n"
				+ "  / _` |/ _` | |_) | | | | '_ \\| '_ \\ / _ \\ '__| \\ \\ / / || |_ \r\n"
				+ " | (_| | (_| |  _ <| |_| | | | | | | |  __/ |     \\ V /|__   _|\r\n"
				+ "  \\__,_|\\__, |_| \\_\\\\__,_|_| |_|_| |_|\\___|_|      \\_/    |_|  \r\n"
				+ "        |___/                                                  ";
		info.append(s);
		Environment env = e.getApplicationContext().getEnvironment();
		String webServerPort = env.getProperty("server.port");
		String context_path = env.getProperty("server.servlet.context-path");
		String sslFlag = env.getProperty("server.ssl.enabled");
		info.append("\n========== dgRv4 web server info ============");
		info.append("\n ...dgR VERSION = " + versionService.getVersion().strVersion);
		info.append("\n ...server.servlet.context-path = " + context_path);
		info.append("\n ...spring.profiles.active = " + env.getProperty("spring.profiles.active"));
		info.append("\n ...NodeName = " + TPILogger.lc.userName);
		TPILogger.lc.param.put("server.port", webServerPort);
		TPILogger.lc.param.put("server.servlet.context-path", context_path);
		TPILogger.lc.param.put("server.ssl.enabled", sslFlag);
		TPILogger.lc.param.put("spring.profiles.active", env.getProperty("spring.profiles.active"));

		// 添加 Undertow 配置資訊
		String undertowInfo = UndertowConfigInfo.getUndertowInfo();
		if (undertowInfo != null && !undertowInfo.isEmpty()) {
			String[] lines = undertowInfo.split("\n");
			for (int i = 1; i < lines.length; i++) {
				// 移除原始的 "- " 前綴，並加上 "..."
				String line = lines[i].trim();
				if (line.startsWith("-")) {
					line = line.substring(1).trim();
				}
				info.append("\n ..." + line);
			}
		}

		LinkedList<String> logStartingMsg = tpi.dgrv4.gateway.keeper.TPILogger.logStartingMsg;
		for (String msg : logStartingMsg) {
			info.append("\n ..." + msg);
		}
	    info.append("\n_____________________________________________");
	    info.append("\n");

	    // 載入完成後, 可以重新連線 from RDB keeper server ip
	    closeConnection();
	    
		return info;
	}

	private void closeConnection() {
		TPILogger.tl.info("\n\n........... close-connection start...............\n\n");
		
		TPILogger.lc.close(); // 它會重連
		TPILogger.hasSecondConnectionStarting = true; //不用讀10秒, 加速連線
		
		TPILogger.tl.info("\n\n........... close-connection end  ...............\n\n");
	}

}
