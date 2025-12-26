package tpi.dgrv4.gateway.TCP.Packet;

import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ExternalDgrInfoPacket implements Packet_i {

	private ClientKeeper clientKeeper;

	private long lastUpdateTime = System.currentTimeMillis();

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public ClientKeeper getClientKeeper() {
		return clientKeeper;
	}

	public void setClientKeeper(ClientKeeper clientKeeper) {
		this.clientKeeper = clientKeeper;
	}

	public ExternalDgrInfoPacket() {
	}

	public ExternalDgrInfoPacket(ClientKeeper clientKeeper) {
		this.clientKeeper = clientKeeper;
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (CommunicationServer.cs.ExternalDgrInfoMap == null) {
				CommunicationServer.cs.ExternalDgrInfoMap = new ConcurrentHashMap<>();
			}

			CommunicationServer.cs.ExternalDgrInfoMap.put(getClientKeeper().getUsername(), this);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
