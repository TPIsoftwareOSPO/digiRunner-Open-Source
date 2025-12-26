package tpi.dgrv4.gateway.service;

import java.io.IOException;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

@ClientEndpoint
@Component
public class ComposerWebSocketClientHandler extends Endpoint{
	
	
	@Override
	public  void onOpen(Session session, EndpointConfig config) {
		ComposerWebSocketClientConn.session = session;
		TPILogger.tl.debug("composer ws client create success");
		
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				//logger.debug("composer ws client Recive Message:"+message);
		        try {
		        	for (ComposerWebSocketServer item : ComposerWebSocketServer.getWebSocketSet()) {
		    			try {
		    				item.sendMessage(message);
		    			} catch (IOException e) {
		    				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		    			}
		    		}
		        }catch(Exception e) {
		        	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		        }
				
			}
		});
    }
	
	@Override
    public void onError(Session session, Throwable t) {
		TPILogger.tl.error(StackTraceUtil.logStackTrace(t));
    }

	@Override
    public void onClose(Session session, CloseReason closeReason) {
		TPILogger.tl.debug("composer ws client, close=" + session.getId() + ", " +closeReason.toString());
    }

}
