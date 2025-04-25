package tpi.dgrv4.gateway.keeper.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.DgrNodeLostContact;
import tpi.dgrv4.entity.repository.DgrNodeLostContactDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.keeper.TPILogInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.communication.Notifier;
import tpi.dgrv4.tcp.utils.communication.Role;

/**
 * Initialized dgR-Keeper Server
 * 
 * @author John
 */
@Component
@DependsOn(value = "autoInitSQL")
public class CommunicationServerConfig {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private TsmpSettingService tsmpSettingService;
	private DgrNodeLostContactDao dgrNodeLostContactDao;

	// Connected Notifier
	public Notifier notify;

	// 用來放最近的 n 條訊息
	public static LinkedBlockingQueue<TPILogInfo> logPool = new LinkedBlockingQueue<>(8);
	
	// 提供給刪除資料使用
//	ExecutorService threadPool = Executors.newFixedThreadPool(1);

	@Autowired
	public CommunicationServerConfig(TsmpSettingService tsmpSettingService,
			DgrNodeLostContactDao dgrNodeLostContactDao) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.dgrNodeLostContactDao = dgrNodeLostContactDao;
	}
	
	@PostConstruct
	public void init() {
		// keeper server go live with [Notifier]
		notify = new Notifier() {
			@Override
			public void runConnection(LinkerServer conn) {
				try(ExecutorService executor = Executors.newFixedThreadPool(10)) {
					executor.submit(() -> {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						printoutAllClient();
					});
				} catch (Exception e) {
				    TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				    Thread.currentThread().interrupt();
				}
			}

			@Override
			public void runDisconnect(LinkerServer conn) {
				
				// 埋 log 找出是在哪裡斷線的
//				String whereDisconnection =  StackTraceUtil.logTpiShortStackTrace(new Exception("是哪裡斷線的"));
//				logger_error(whereDisconnection);
				
				printoutAllClient();
				if ("".equals(conn.userName)) {
					return ;
				}
				
				StringBuffer msgbuf = new StringBuffer();
				String s = "\r\n"
						+ " __  ___ .______       ______  __       __  .___________.\r\n"
						+ "|  |/  / |   _  \\     /      ||  |     |  | |           |\r\n"
						+ "|  '  /  |  |_)  |   |  ,----'|  |     |  | `---|  |----`\r\n"
						+ "|    <   |   ___/    |  |     |  |     |  |     |  |     \r\n"
						+ "|  .  \\  |  |        |  `----.|  `----.|  |     |  |     \r\n"
						+ "|__|\\__\\ | _|         \\______||_______||__|     |__|     \r\n"
						+ "                                                         \r\n"
						+ ""
						;
				msgbuf.append(s);
				msgbuf.append("\n...["+ conn.userName +"] Client Disconnect to dgr-keeper server .....FAIL !!!");
				msgbuf.append("\n______________________________________________________");
				msgbuf.append("\n");
				logger_error(msgbuf.toString());

				// 刪除 14 天前的資料
				try {
					Long timestamp = System.currentTimeMillis();
					DgrNodeLostContact vo = new DgrNodeLostContact();
					vo.setIp(conn.getRemoteIP());
					vo.setLostTime(TimeZoneUtil.long2UTCstring(timestamp));
					vo.setNodeName(conn.userName);
					vo.setPort(conn.getRemotePort());
					vo.setCreateTimestamp(timestamp);
					dgrNodeLostContactDao.saveAndFlush(vo);
					
					LocalDateTime beforeDateTime = LocalDateTime.now().minusDays(14);
					Timestamp beforeTimestampVo = Timestamp.valueOf(beforeDateTime);
					long beforeTimestamp = beforeTimestampVo.getTime();
					dgrNodeLostContactDao.deleteByCreateTimestampLessThan(beforeTimestamp);
				}catch(Exception e) {
					logger.error(StackTraceUtil.logStackTrace(e));
				}
			}

			@Override
			public void setRole(Role role, LinkerServer ls) {
				// no Effect, Don't implements
			}
		};
		logger.info("......Keeper Server bind port START.....");
		//適用 localhost 不會啟動 2 次
		
		boolean canConnect = telenetPort("127.0.0.1", getTsmpSettingService().getVal_DGRKEEPER_PORT());
		if ( !canConnect ) {
			new CommunicationServer(getTsmpSettingService().getVal_DGRKEEPER_PORT(), notify);
			StringBuffer msgbuf = new StringBuffer();
			String s = "\r\n"
					+ " __  ___ .______          _______.____    ____  _______ .______      \r\n"
					+ "|  |/  / |   _  \\        /       |\\   \\  /   / |   ____||   _  \\     \r\n"
					+ "|  '  /  |  |_)  |      |   (----` \\   \\/   /  |  |__   |  |_)  |    \r\n"
					+ "|    <   |   ___/        \\   \\      \\      /   |   __|  |      /     \r\n"
					+ "|  .  \\  |  |        .----)   |      \\    /    |  |____ |  |\\  \\----.\r\n"
					+ "|__|\\__\\ | _|        |_______/        \\__/     |_______|| _| `._____|\r\n"
					+ "                                                                     \r\n"
					+ "";
			msgbuf.append(s);
			msgbuf.append("\n...dgr-keeper server [go LIVE] ");
			msgbuf.append("\n...KeeperServer IP   = " + getTsmpSettingService().getVal_DGRKEEPER_IP());
			msgbuf.append("\n...KeeperServer PORT = " + getTsmpSettingService().getVal_DGRKEEPER_PORT());
			msgbuf.append("\n........................................");
			msgbuf.append("\n");
			
			logger_info(msgbuf.toString());
			
			//System.err.println(msgbuf.toString().contains("keeper server"));
		}

		logger.info("......Keeper Server bind port END.....");
	}

	public boolean telenetPort(String ip, int port) {
		try (Socket server = new Socket()){
			InetSocketAddress address = new InetSocketAddress(ip, port);
			server.connect(address, 5000);
			//checkmarx, Missing HSTS Header
			CheckmarxUtils.sanitizeForCheckmarx(server);
			server.getOutputStream().flush();
			
			byte[] socket_read_buffer = new byte[1024*1024*10];
			int bytesRead = server.getInputStream().read(socket_read_buffer);
			ByteArrayOutputStream baos_4_readObj = new ByteArrayOutputStream();
			// copy to ByteArrayOS
			baos_4_readObj.write(socket_read_buffer, 0, bytesRead);
			baos_4_readObj.flush();
			boolean endFlag = "Yes, I am dgRunner".equals(baos_4_readObj.toString());
		} catch (UnknownHostException e) {
			logger.info("...[telenet Keeper Server] UnknownHostException !...\n");
			return false;
		} catch (IOException e) {
			logger.info("...[telenet Keeper Server] IOException !...\n");
			return false;
		} finally {
		}
		logger.info("...[telenet Keeper Server] already exists !...\n");
		return true;
	}

	/**
	 * print All client info
	 */
	public void printoutAllClient() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n\n ...LinkerClient Status ... ");
		sb.append("\n ...................................................... ");
		sb.append("\n ...getRemoteIP()\t");
		sb.append("isConnected()\t");
		sb.append("getRemotePort()\t");
		sb.append("userName\n");
		for (LinkerServer server : CommunicationServer.cs.connClinet) {
			sb.append(" ..." + server.getRemoteIP() + "\t\t");
			sb.append("" + server.isConnected() + "\t\t");
			sb.append("" + server.getRemotePort() + "\t\t");
			sb.append("".equals(server.userName) ? "*" : server.userName);
			sb.append("\n");
		}		
		sb.append(" ...................................................... \n");
		logger.info(sb.toString()); 
	}
	
	public void logger_info(String logMsg) {
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("INFO");
			log.getLogMsg().append(Thread.currentThread().getName()+ "::" + logMsg);
			log.userName = "Keeper Server";
		} catch (Exception e) {
		}
		//System.out.println(log.toString());
		logger.info("[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		
		sendLogPacket(log);
	}
	
	public void logger_error(String logMsg) {
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("ERROR");
			log.getLogMsg().append(logMsg);
			log.userName = "Keeper Server";
		} catch (Exception e) {
		}
		//System.out.println(log.toString());
		logger.error("[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		
		sendLogPacket(log);
	}
	
	private void sendLogPacket(TPILogInfo log) {
//		當 logPool 已滿(32筆)時,丟棄最舊的 log (poll)
//		然後重試加入新的 log (offer)
//		以維持固定大小的 log 隊列
		while (CommunicationServerConfig.logPool.offer(log) == false) {
			CommunicationServerConfig.logPool.poll();
		}
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
