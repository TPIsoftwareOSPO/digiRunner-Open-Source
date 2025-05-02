package tpi.dgrv4.tcp.utils.packets;

import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class Liveness   implements Packet_i, java.io.Serializable {

	@Override
	public void runOnClient(LinkerClient lc) {
		//System.out.println("Hello .....runOnClient() = " + lc.userName);
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		
	}
}
