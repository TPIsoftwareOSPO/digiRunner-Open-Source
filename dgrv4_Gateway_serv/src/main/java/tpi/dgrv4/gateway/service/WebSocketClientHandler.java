package tpi.dgrv4.gateway.service;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class WebSocketClientHandler extends Endpoint{
	
	private TPILogger logger = TPILogger.tl;
	
	@Override
	public  void onOpen(Session session, EndpointConfig config) {
		
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				//logger.debug("ws client Recive Message:"+message);
		        try {
		        	Session serverSession = WebSocketServer.serverDataMap.get(session.getId());
		        	if(serverSession != null) {
		        		serverSession.getBasicRemote().sendText(message);
		        	}else {
		        		logger.error("server session not found, send message fail, message=" + message);
		        	}
		        }catch(Exception e) {
		        	logger.error(StackTraceUtil.logStackTrace(e));
		        }
			}
		});
    }
	
	@Override
    public void onError(Session session, Throwable t) {
		logger.error(StackTraceUtil.logStackTrace(t));
    }

	@Override
    public void onClose(Session session, CloseReason closeReason) {
		logger.debug("ws client, close=" + session.getId() + ", " +closeReason.toString());
		String clientId = session.getId();
		Session serverSession = WebSocketServer.serverDataMap.get(clientId);
		if(serverSession != null) {
			WebSocketServer.clientDataMap.remove(serverSession.getId());	
		}
		WebSocketServer.serverDataMap.remove(clientId);
		if(serverSession != null && serverSession.isOpen()) {
			try {
				serverSession.close();
			}catch(Exception e) {
				logger.error("server session close error\n" + StackTraceUtil.logStackTrace(e));
			}
		}
		
		logger.debug("ws client close end");
		
    }

}
