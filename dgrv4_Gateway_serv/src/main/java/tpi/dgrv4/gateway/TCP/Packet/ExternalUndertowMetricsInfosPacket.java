package tpi.dgrv4.gateway.TCP.Packet;

import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.UndertowMetricsPacket;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ExternalUndertowMetricsInfosPacket implements Packet_i {

	private UndertowMetricsPacket undertowMetricsPacket;

	private long lastUpdateTime = System.currentTimeMillis();

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public UndertowMetricsPacket getUndertowMetricsPacket() {
		return undertowMetricsPacket;
	}

	public void setUndertowMetricsPacket(UndertowMetricsPacket undertowMetricsPacket) {
		this.undertowMetricsPacket = undertowMetricsPacket;
	}

	public ExternalUndertowMetricsInfosPacket() {
	}

	public ExternalUndertowMetricsInfosPacket(UndertowMetricsPacket undertowMetricsPacket) {
		this.undertowMetricsPacket = undertowMetricsPacket;
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}
	
	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (CommunicationServer.cs.ExternalUndertowMetricsInfoMap == null) {
				CommunicationServer.cs.ExternalUndertowMetricsInfoMap = new ConcurrentHashMap<>();
			}

			CommunicationServer.cs.ExternalUndertowMetricsInfoMap.put(getUndertowMetricsPacket().getName(), this);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}
}
