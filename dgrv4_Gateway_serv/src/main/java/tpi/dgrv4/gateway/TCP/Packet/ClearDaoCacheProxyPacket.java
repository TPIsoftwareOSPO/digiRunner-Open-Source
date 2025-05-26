package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.component.cache.core.IDaoGenericCache;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ClearDaoCacheProxyPacket implements Packet_i{

	public ClearDaoCacheProxyPacket() {
		super();
	}

	// 2. Client 收到此封包, 開始清空 Cache Proxy V3
	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			synchronized (TPILogger.lc) {
				IDaoGenericCache<?, ?> igc = (IDaoGenericCache<?, ?>) lc.paramObj.get("DaoGenericCache");
				igc.clear();
				TPILogger.tl.info("Client [" + lc.userName + "] IDaoGenericCache is totally cleared.");
			}
			TPILogger.tl.updateESLogFlag();
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	// 1. Server 轉送此封包給所有 Client
	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			CommunicationServer.cs.sendToAll_NotSelf(ls, this);
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
