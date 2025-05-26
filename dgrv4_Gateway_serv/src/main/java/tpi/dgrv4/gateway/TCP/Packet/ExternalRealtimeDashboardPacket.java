package tpi.dgrv4.gateway.TCP.Packet;

import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.RealtimeDashboardPacket;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ExternalRealtimeDashboardPacket implements Packet_i {

	private RealtimeDashboardPacket realtimeDashboardPacket;

	private long lastUpdateTime = System.currentTimeMillis();

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public RealtimeDashboardPacket getRealtimeDashboardPacket() {
		return realtimeDashboardPacket;
	}

	public void setRealtimeDashboardPacket(RealtimeDashboardPacket realtimeDashboardPacket) {
		this.realtimeDashboardPacket = realtimeDashboardPacket;
	}

	public ExternalRealtimeDashboardPacket() {
	}

	public ExternalRealtimeDashboardPacket(RealtimeDashboardPacket realtimeDashboardPacket) {
		this.realtimeDashboardPacket = realtimeDashboardPacket;
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		// Do nothing
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (CommunicationServer.cs.ExternalRealtimeDashboardInfoMap == null) {
				CommunicationServer.cs.ExternalRealtimeDashboardInfoMap = new ConcurrentHashMap<>();
			}

			CommunicationServer.cs.ExternalRealtimeDashboardInfoMap.put(getRealtimeDashboardPacket().getName(), this);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
