package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.keeper.server.CommunicationServerConfig;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class TPILogInfoPacket implements Packet_i {

	TPILogInfo _logInfo;

	public TPILogInfoPacket() {
	}
	
	public TPILogInfoPacket(TPILogInfo logInfo) {
		super();
		this._logInfo = logInfo;
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		
		try {
			while (CommunicationServerConfig.logPool.offer(_logInfo) == false) {
				CommunicationServerConfig.logPool.poll();
			}
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		
		
	}

}
