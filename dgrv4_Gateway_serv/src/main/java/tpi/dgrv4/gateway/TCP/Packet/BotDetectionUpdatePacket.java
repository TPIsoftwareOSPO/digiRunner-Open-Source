package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.gateway.component.BotDetectionRuleValidator.BotDetectionUpdateType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class BotDetectionUpdatePacket implements Packet_i {

	@Override
	public void runOnServer(LinkerServer ls) {
		CommunicationServer.cs.sendToAll(ls, this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		TPILogger.tl.getBotDetectionRuleValidator().updateFromDb(BotDetectionUpdateType.UPDATE);
	}

}
