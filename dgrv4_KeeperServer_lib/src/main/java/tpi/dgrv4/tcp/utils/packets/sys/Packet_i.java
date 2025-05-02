package tpi.dgrv4.tcp.utils.packets.sys;

import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;

public interface Packet_i {
	public void runOnServer(LinkerServer ls);

	public void runOnClient(LinkerClient lc);
}
