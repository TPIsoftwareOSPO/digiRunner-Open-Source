package tpi.dgrv4.tcp.utils.packets;

import java.util.UUID;

import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class UrlStatusPacket implements Packet_i {

	private String name;
	private String apiLogs;
	private long updateTime;
	
	public String uuid = "(__" + UUID.randomUUID().toString() + "__)";
	
	public UrlStatusPacket() {
	}

	public UrlStatusPacket(String name, String apiLogs) {
		this.name = name;
		this.apiLogs = apiLogs;
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

	public String getApiLogs() {
		return apiLogs;
	}

	public void setApiLogs(String apiLogs) {
		this.apiLogs = apiLogs;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public void runOnServer(LinkerServer ls) {

		CommunicationServer.cs.urlStatusInfos.putStatus(name, this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}

}
