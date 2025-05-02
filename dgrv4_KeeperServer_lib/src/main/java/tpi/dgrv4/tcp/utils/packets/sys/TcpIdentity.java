package tpi.dgrv4.tcp.utils.packets.sys;

import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.communication.Role;


public class TcpIdentity implements Packet_i {
	public Role f_identity;

	@Override
	public void runOnClient(LinkerClient lc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runOnServer(LinkerServer ls) {
		// TODO Auto-generated method stub
		ls.setIdentity(f_identity);
	}

}
