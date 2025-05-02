package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.controller.AA0325Controller;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class UpdateComposerTSPacket implements Packet_i {

	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			AA0325Controller.ts = System.currentTimeMillis();
			TPILogger.tl.debug(" [" + lc.userName + "] update Composer ts (AA0325Controller.ts) ");
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			CommunicationServer.cs.sendToAll(ls, this);
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
