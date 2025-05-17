package tpi.dgrv4.tcp.utils.packets;

import java.util.UUID;

import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class UndertowMetricsPacket implements Packet_i {

	private String name;
	private String metrics;
	private long updateTime;
	
	public String uuid = "(__" + UUID.randomUUID().toString() + "__)";
	
	public UndertowMetricsPacket() {
	}

	public UndertowMetricsPacket(String name, String metrics) {
		this.name = name;
		this.metrics = metrics;
		this.updateTime = System.currentTimeMillis();
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMetrics() {
		return metrics;
	}

	public void setMetrics(String metrics) {
		this.metrics = metrics;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public void runOnServer(LinkerServer ls) {

		CommunicationServer.cs.undertowMetricsInfos.putMetric(name, this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}

}
