package tpi.dgrv4.tcp.utils.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;

import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class CommunicationServer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(CommunicationServer.class);
	
	ServerSocket server = null;

	public static CommunicationServer cs;

	public int port = 6666;

	public final int MAX_Connection_Number = 1000000;

	// ------ static ServerTable 表格 = new ServerTable();
	// ------ private String 會員編號;

	// 共用資料, 目前連線的客戶資料
	public LinkedList<LinkerServer> connClinet = null;
	public HashMap<IP_PortKey, LinkerServer> connMap = null;
	public ArrayList<Notifier> notifiers = null;
	public ConcurrentHashMap<String, Packet_i> ExternalDgrInfoMap;
	public ConcurrentHashMap<String, Packet_i> ExternalUndertowMetricsInfoMap;
	public ConcurrentHashMap<String, Packet_i> ExternalUrlStatusInfoMap;
	public ConcurrentHashMap<String, Packet_i> ExternalRealtimeDashboardInfoMap;
	public ConcurrentHashMap<String, Packet_i> httpNodeInfo;
	public UndertowMetricsInfoMap undertowMetricsInfos = new UndertowMetricsInfoMap();
	public UrlStatusInfoMap urlStatusInfos = new UrlStatusInfoMap();
	public RealtimeDashboardInfoMap realtimeDashboardInfos = new RealtimeDashboardInfoMap();

	public CommunicationServer(int port) {
		this(port, null);
	}

	public CommunicationServer(int port, Notifier notifier) {
		cs = this;
		this.port = port;
		connClinet = new LinkedList<LinkerServer>();
		connMap = new HashMap<IP_PortKey, LinkerServer>();
		httpNodeInfo = new ConcurrentHashMap<>();
		addNotifier(notifier);
		try {
			server = new ServerSocket(port);
		} catch (Exception e) {
			System.err.println("無法建立server : " + e.getMessage());
			System.exit(1);
		}
		Thread t = new Thread(this);
		t.start();
	}

	public void addNotifier(Notifier notifier) {
		if (this.notifiers == null) {
			notifiers = new ArrayList<Notifier>();
		}
		if (notifier != null) {
			this.notifiers.add(notifier);
		}
	}

	public void run() {
		Socket user = null;
		boolean isRunning = true;
		while (isRunning) {
			try {
				user = server.accept();
				doConnectionProc(user);
			} catch (Exception e) {
//				e.printStackTrace();
				System.err.println("TCP server.accept()結束");
				isRunning = false;
			}
		}
	}

	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public synchronized void doConnectionProc(Socket user) {
		while (MAX_Connection_Number == connClinet.size()) {
			try {
				wait();
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}
		}

		LinkerServer con = new LinkerServer(user, notifiers);
		synchronized (connClinet) {
			connClinet.add(con);
			String remoteIp = con.getRemoteIP();
			int remotePort = con.getRemotePort();
			connMap.put(new IP_PortKey(remoteIp, remotePort), con);
			for (Notifier noti : notifiers) {
				noti.runConnection(con);
			}
			Thread t = new Thread(con);
			t.start();
		}
	}

	public synchronized void doDisconnectProc(LinkerServer conn) {
		synchronized (connClinet) {
			conn.close();
			int remotePort = conn.getRemotePort();
			String remoteIp = conn.getRemoteIP();
			connMap.remove(new IP_PortKey(remoteIp, remotePort));
			connClinet.remove(conn);
			for (Notifier noti : notifiers) {
				noti.runDisconnect(conn);
			}
			notifyAll();
		}
	}

	public void sendToAll(LinkerServer conn, Packet_i data) {
		synchronized (connClinet) {
			for (LinkerServer ll : connClinet) {
				ll.send(data);
//				switch (ll.get身份()) {
//				case admin:
//				case MasterController:
//				case DataNode:
//					continue;
//				case web:
//					ll.send(data);
//				}
			}
		}
	}

	public void sendToAll_NotSelf(LinkerServer conn, Packet_i data) {
		synchronized (connClinet) {
			for (LinkerServer ll : connClinet) {
				if (ll == conn) {
					continue;
				}
				ll.send(data);
			}
		}
	}

	public void sendToSpecUserNames(LinkerServer conn, Packet_i data, String names[]) {
		synchronized (connClinet) {
			int i = 0;
			for (LinkerServer ll : connClinet) {
				if (ll == conn) {
					continue;
				}

				for (int j = 0; j < names.length; j++) {
					if (names[j].equals(ll.userName)) {
						ll.send(data);
						System.err.println("server send to " + ll.userName);
						break;
					}
				}
			}
		}
	}

	// 创建一个 Kryo 对象池，每个线程都从池中获取一个 Kryo 对象进行序列化和反序列化
	public static final ThreadLocal<Kryo> kryoLocal = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
//		System.out.println("Kryo object creation");
		// 免註冊
//		kryo.register(TcpIdentity.class);
//		kryo.register(DoSetUserName.class);
//		kryo.register(java.util.Date.class);
//		kryo.register(java.lang.StringBuffer.class);
//		kryo.register(java.util.Date.class);
//		kryo.register(java.lang.StringBuffer.class);
//		kryo.register(tpi.dgrv4.tcp.utils.communication.Role.class);
		kryo.setRegistrationRequired(false);
		// 設置警告未註冊的類型
//		kryo.setWarnUnregisteredClasses(true); 
		return kryo;
	});

}
