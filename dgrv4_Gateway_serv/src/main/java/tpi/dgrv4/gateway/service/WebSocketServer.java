package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.config.WebSocketConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
@ServerEndpoint(value = "/website/{siteName}", configurator = WebSocketConfig.class)
public class WebSocketServer {

	private TPILogger logger = TPILogger.tl;
	// key為server session id, data為client session
	protected static Map<String, Session> clientDataMap = new ConcurrentHashMap<>();

	// key為client session id, data為server session
	protected static Map<String, Session> serverDataMap = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(@PathParam("siteName") String siteName, Session session) {
		@SuppressWarnings("unchecked")
		List<String> auth = (List<String>) session.getUserProperties().get("Authorization");
		Session clientSession = null;
		try {
			clientSession = WebSocketClientConn.startWS(siteName, auth);
			if (clientSession == null) {
				logger.error("client session is null");
				throw new Exception("client session is null");
			}
			serverDataMap.put(clientSession.getId(), session);
			clientDataMap.put(session.getId(), clientSession);

			logger.debug("ws sever, open action, serverDataSize:" + serverDataMap.size() + ", clientDataSize:"
					+ clientDataMap.size());

		} catch (Exception e) {
			//發生任何問題都要中斷連線
			try {
				session.close();
			} catch (IOException e1) {
				logger.error(StackTraceUtil.logStackTrace(e));
			}

		}

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.debug("ws server, close=" + session.getId() + ", " + closeReason.toString());
		String serverId = session.getId();
		Session clientSession = clientDataMap.get(serverId);
		if (clientSession != null) {
			serverDataMap.remove(clientSession.getId());
		}
		clientDataMap.remove(serverId);
		if (clientSession != null && clientSession.isOpen()) {
			try {
				clientSession.close();
			} catch (Exception e) {
				logger.error("client session close error\n" + StackTraceUtil.logStackTrace(e));
			}
		}

		logger.debug("ws server, colse action, serverDataSize:" + serverDataMap.size() + ", clientDataSize:"
				+ clientDataMap.size());
	}

	@OnMessage
	public void onMessage(String message, Session session) {

		// logger.debug("ws server Recive Message:"+message);
		try {
			Session clientSession = clientDataMap.get(session.getId());
			if (clientSession != null) {
				clientSession.getBasicRemote().sendText(message);
			} else {
				logger.error("client session not found, send message fail, message=" + message);
			}
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}

	}

	@OnError
	public void onError(@PathParam("siteName") String siteName, Session session, Throwable error) {
		logger.error("siteName=" + siteName + " error\n" + StackTraceUtil.logStackTrace(error));
	}

}