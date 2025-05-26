package tpi.dgrv4.gateway.service;

import java.net.Socket;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;

@Component
@DependsOn("staticResourceInitializer")
public class ComposerWebSocketClientConn {
	private TsmpSettingService tsmpSettingService;
	
	private static String SSL_KEY="server.ssl.key-store";
	private static String SSL_MIMA="server.ssl.key-store-password";
	private static String SSL_TYPE="server.ssl.keyStoreType";
	
	public static Session session;
	private static Environment env;
	
	@Autowired
	public ComposerWebSocketClientConn(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}
	
	/*@PostConstruct
	public void init() {
		this.startWS();
	}*/

	public void startWS() {
        try {
        	
        	TPILogger.tl.debug("prepare composer ws client connection to server");
            if (ComposerWebSocketClientConn.session != null && ComposerWebSocketClientConn.session.isOpen()) {
            	TPILogger.tl.debug("composer websocket client already open");
                return;
            	 //WebSocketClient.session.close();
            }
            
            List<String> caList = getTsmpSettingService().getVal_TSMP_COMPOSER_ADDRESS();
            
            String composerAddress = null;
    		if(!CollectionUtils.isEmpty(caList)) {
    			composerAddress = caList.get(0);
    		}
            
    		if(composerAddress == null) {
    			TPILogger.tl.error("TsmpSetting TSMP_COMPOSER_ADDRESS is empty, no websocket");
    			return;
    		}
            
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            //設定message最大10M
            container.setDefaultMaxBinaryMessageBufferSize(10*1024*1024);
            container.setDefaultMaxTextMessageBufferSize(10*1024*1024);

            composerAddress = composerAddress.replace("http", "ws");
            String uri = composerAddress + "/editor/comms";
            
            //Session session = container.connectToServer(WebSocketClientHandler.class, URI.create(uri));
            //Session session = container.connectToServer(new PojoEndpointClient(WebSocketClientHandler.class, new ArrayList<>()), createClientConfig(), URI.create(uri));
       
            
            /*String keystoreType = getEnv().getProperty(WebSocketClient.SSL_TYPE);
            InputStream keystoreLocation = new FileInputStream(getEnv().getProperty(WebSocketClient.SSL_KEY));
            char [] keystorePassword = getEnv().getProperty(WebSocketClient.SSL_MIMA).toCharArray();
            char [] keyPassword = getEnv().getProperty(WebSocketClient.SSL_MIMA).toCharArray();

            KeyStore keystore = KeyStore.getInstance(keystoreType);
            keystore.load(keystoreLocation, keystorePassword);
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, keyPassword);

            InputStream truststoreLocation = new FileInputStream(getEnv().getProperty(WebSocketClient.SSL_KEY));
            char [] truststorePassword = getEnv().getProperty(WebSocketClient.SSL_MIMA).toCharArray();
            String truststoreType = getEnv().getProperty(WebSocketClient.SSL_TYPE);

            KeyStore truststore = KeyStore.getInstance(truststoreType);
            truststore.load(truststoreLocation, truststorePassword);
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(truststore);

            KeyManager[] keymanagers = kmfactory.getKeyManagers();
            TrustManager[] trustmanagers =  tmfactory.getTrustManagers();*/

//            TrustManager[] trustmanagers = new TrustManager[]{new X509ExtendedTrustManager() {
//
//				@Override
//				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public X509Certificate[] getAcceptedIssuers() {
//					// TODO Auto-generated method stub
//					return null;
//				}
//
//				@Override
//				public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
//					// TODO Auto-generated method stub
//					
//				}}};
//            
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustmanagers, new SecureRandom());
//            SSLContext.setDefault(sslContext);
            
            //System.setProperty("javax.net.ssl.trustStorePassword", getEnv().getProperty(WebSocketClient.SSL_MIMA));
            //System.setProperty("javax.net.ssl.trustStoreType", getEnv().getProperty(WebSocketClient.SSL_TYPE));
            //System.setProperty("javax.net.ssl.trustStore", getEnv().getProperty(WebSocketClient.SSL_KEY));
            
           
            
            
            ClientEndpointConfig.Configurator configurator =
                    new ClientEndpointConfig.Configurator() {
                    };
           ClientEndpointConfig clientEndpointConfig =
                    ClientEndpointConfig.Builder.create().configurator(configurator).build();
           /*List<String> protocolList = new ArrayList<>();
           protocolList.add("wss");
           ClientEndpointConfig clientEndpointConfig =
                   ClientEndpointConfig.Builder.create().preferredSubprotocols(protocolList).build();*/
            
            /*ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder
                    .create().configurator(new ClientEndpointConfig.Configurator(){
                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {
                            super.beforeRequest(headers);
                            List<String> values = new ArrayList<String>();
                            values.add("v");
                            headers.put("k",values);
                        }
                    }).build();*/

          /* SSLContext sslContext = SSLContext.getInstance("TLS");
           sslContext.init(null, new TrustManager[]{new X509TrustManager() {
               public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
               public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
               public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                   //return null;
                   return new java.security.cert.X509Certificate[0];
               }
           }}, null);
           HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
           HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);*/
           
            // 取消 wss 安全性驗證
            SSLContext sslContext = HttpUtil.disableWssValidation();
            clientEndpointConfig.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", sslContext);

            /*Session session = container.connectToServer(
            		new PojoEndpointClient(WebSocketClientHandler.class, new ArrayList<>()),
                    clientEndpointConfig,
                    URI.create(uri));*/
            TPILogger.tl.debug("composer ws uri:" + uri);
            container.connectToServer(ComposerWebSocketClientHandler.class,  clientEndpointConfig, URI.create(uri));
            TPILogger.tl.debug("setting composer ws connect to server OK");
            
        } catch (Exception e) {
        	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
        }
    }
    
    public void restart() {
    	try {
	    	if (ComposerWebSocketClientConn.session != null && ComposerWebSocketClientConn.session.isOpen()) {
	            ComposerWebSocketClientConn.session.close();
	            TPILogger.tl.debug("composer websocket client close");
	            ComposerWebSocketClientConn.session = null;
	        }
	    	this.startWS();
    	}catch(Exception e) {
    		TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
    	}
    }

	public static Environment getEnv() {
		return env;
	}

	public static void setEnv(Environment env) {
		ComposerWebSocketClientConn.env = env;
	}

	public TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}	
}
