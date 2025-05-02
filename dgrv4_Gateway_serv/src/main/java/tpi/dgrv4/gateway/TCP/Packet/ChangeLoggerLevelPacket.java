package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ChangeLoggerLevelPacket implements Packet_i {

	public String loggerLevel;

	public ChangeLoggerLevelPacket() {
		super();
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			// 此流程於 TPILogger init phase 也有使用, 用於系統啟動時
			TPILogger.initLoggerLevel(loggerLevel);
				
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			//TPILogger.tl.info("...[KP Start] change log level: " + loggerLevel);
			CommunicationServer.cs.sendToAll(ls, this);
			//TPILogger.tl.info("...[KP End] change log level: " + loggerLevel);
			TPILogger.tl.debug("Switch to logger level: " + loggerLevel + " ");
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
