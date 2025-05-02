package tpi.dgrv4.tcp.utils.packets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class DoSetUserName implements Packet_i {
	private static Logger logger = LoggerFactory.getLogger(DoSetUserName.class);
	
	String name;
	
	public final static Object waitKey = new Object();

	public DoSetUserName() {
	}	
	
	public DoSetUserName(String name) {
		super();
		this.name = name;
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		logger.info("\n\n...runOnClient 2: SET NAME OK :" + name + "\n");
		synchronized (DoSetUserName.waitKey) {
			DoSetUserName.waitKey.notify();
		}
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		ls.userName = name;
		logger.info("\n\n...runOnServer 1: SET NAME OK :" + name + "\n");
		ls.send(this);
	}
}
