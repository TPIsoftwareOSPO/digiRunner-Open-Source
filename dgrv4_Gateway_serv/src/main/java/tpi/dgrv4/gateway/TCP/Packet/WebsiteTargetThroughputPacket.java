package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class WebsiteTargetThroughputPacket implements Packet_i {

	public static final String websiteTargetThroughput = "websiteTargetThroughput";

	public String targetThroughputJson; 
	
	
	@Override
	public void runOnClient(LinkerClient lc) {

	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			
			ls.paramObj.put(websiteTargetThroughput, targetThroughputJson);
			
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
