package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.keeper.server.CommunicationServerConfig;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.util.concurrent.LinkedBlockingQueue;

public class RequireTPILogInfoPacket implements Packet_i {

	// 用來放最近的 n 條訊息
	public LinkedBlockingQueue<TPILogInfo> logPool;
	
	public Long currentTime;

	public RequireTPILogInfoPacket() {
		super();
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			lc.paramObj.put("logPool", logPool);
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			this.logPool = new LinkedBlockingQueue<TPILogInfo>();
			for (TPILogInfo tpiLogInfo : CommunicationServerConfig.logPool) {
				if (currentTime.longValue() < tpiLogInfo.getMstime()) {
					this.logPool.add(tpiLogInfo);
				}
			}
			ls.send(this);
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
