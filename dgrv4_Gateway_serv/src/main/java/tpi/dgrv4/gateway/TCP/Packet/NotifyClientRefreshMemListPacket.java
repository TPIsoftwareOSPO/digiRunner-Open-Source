package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class NotifyClientRefreshMemListPacket implements Packet_i {
    public NotifyClientRefreshMemListPacket() {
        super();
    }

    @Override
    public void runOnClient(LinkerClient lc) { // 2. server 通知 所有 client 刷新
    	try {
	        synchronized (TPILogger.lc) {// 刷新mem
	            ApptJobDispatcher a = (ApptJobDispatcher) lc.paramObj.get("RefreshMem");
	            if(a != null) {
		            a.resetRefreshSchedule();
		            TPILogger.tl.trace("Client ["+  lc.userName + "] ApptJob has been refreshed.");
	            }
	        }
    	}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
    }

    @Override
    public void runOnServer(LinkerServer ls) { // 1.client 通知 server 刷新
    	try {
    		CommunicationServer.cs.sendToAll(ls, this);
    	}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

    }
}
