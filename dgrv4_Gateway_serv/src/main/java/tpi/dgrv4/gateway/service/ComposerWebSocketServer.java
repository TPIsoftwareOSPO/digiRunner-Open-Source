package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
@ServerEndpoint("/website/composer/comms")
public class ComposerWebSocketServer {

	private static int onlineCount = 0;
	// concurrent包的線程安全Set，用來存放每個客戶端對應的MyWebSocket對象。
	private static CopyOnWriteArraySet<ComposerWebSocketServer> webSocketSet = new CopyOnWriteArraySet<ComposerWebSocketServer>();

	// 與某個客戶端的連接會話，需要通過它來給客戶端發送數據
	private Session session;

	/**
	 * 連接建立成功調用的方法
	 */
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		webSocketSet.add(this); // 加入set中
		addOnlineCount(); // 在線數加1
		TPILogger.tl.debug("composer ws sever, open action, online number:" + ComposerWebSocketServer.onlineCount);
	}

	/**
	 * 連接關閉調用的方法
	 */
	@OnClose
	public void onClose() {
		webSocketSet.remove(this); // 從set中刪除
		subOnlineCount(); // 在線數減1
		TPILogger.tl.debug("composer ws server, colse action, online number:" + ComposerWebSocketServer.onlineCount);

	}

	/**
	 * 收到客戶端消息後調用的方法 @ Param message 客戶端發送過來的消息
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		
		//logger.debug("composer ws server Recive Message:"+message);
		try {
			if (ComposerWebSocketClientConn.session != null) {
				ComposerWebSocketClientConn.session.getBasicRemote().sendText(message);
			} else {
				TPILogger.tl.error("--no session, send out composer websocket error----");
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

	}

	/**
	 * @ Param session @ Param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		TPILogger.tl.error(StackTraceUtil.logStackTrace(error));
	}

	/**
	 * 實現服務器主動推送
	 */
	public void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
	}

	/**
	 * 群發
	 */
	public void sendAllUserMessage(String message) throws IOException {

		for (ComposerWebSocketServer item : webSocketSet) {
			try {
				item.sendMessage(message);
			} catch (IOException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
		}

	}


	public static synchronized void addOnlineCount() {
		ComposerWebSocketServer.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		ComposerWebSocketServer.onlineCount--;
	}

	public static CopyOnWriteArraySet<ComposerWebSocketServer> getWebSocketSet() {
		return webSocketSet;
	}
}