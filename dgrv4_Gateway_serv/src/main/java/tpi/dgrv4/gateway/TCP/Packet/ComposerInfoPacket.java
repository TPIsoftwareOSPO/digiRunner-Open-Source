package tpi.dgrv4.gateway.TCP.Packet;

import java.util.HashMap;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ComposerInfoData;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class ComposerInfoPacket implements Packet_i{

	public static final String composerInfo = "composerInfo";
	
	public ComposerInfoData composerInfoData;
	
	public ComposerInfoPacket() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (ls.paramObj.containsKey(composerInfo) == false) {
				//因為使用ConcurrentHashMap有發生封包不完全,造成資料錯亂的情況
				ls.paramObj.put(composerInfo, new HashMap<String, ComposerInfoData>());
			}
			HashMap<String, ComposerInfoData> composerInfoHM = (HashMap<String, ComposerInfoData>) ls.paramObj.get(composerInfo);
			synchronized (composerInfoHM) {
				composerInfoHM.put(composerInfoData.getComposerID(), composerInfoData);
			}
			
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}

}
