package tpi.dgrv4.gateway.TCP.Packet;

import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ExternalUrlStatusPacket implements Packet_i {

	private UrlStatusPacket urlStatusPacket;

	private long lastUpdateTime = System.currentTimeMillis();

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public UrlStatusPacket getUrlStatusPacket() {
		return urlStatusPacket;
	}

	public void setUrlStatusPacket(UrlStatusPacket urlStatusPacket) {
		this.urlStatusPacket = urlStatusPacket;
	}

	public ExternalUrlStatusPacket() {
	}

	public ExternalUrlStatusPacket(UrlStatusPacket urlStatusPacket) {
		this.urlStatusPacket = urlStatusPacket;
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		// Do nothing
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (CommunicationServer.cs.ExternalUrlStatusInfoMap == null) {
				CommunicationServer.cs.ExternalUrlStatusInfoMap = new ConcurrentHashMap<>();
			}

			CommunicationServer.cs.ExternalUrlStatusInfoMap.put(getUrlStatusPacket().getName(), this);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
