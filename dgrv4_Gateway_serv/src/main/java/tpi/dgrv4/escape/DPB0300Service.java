package tpi.dgrv4.escape;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0300Req;
import tpi.dgrv4.dpaa.vo.DPB0300Resp;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.grpcproxy.manager.DynamicGrpcProxyManager;
import tpi.dgrv4.grpcproxy.security.TlsCertificateManager;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

@Service
public class DPB0300Service {

    private final TlsCertificateManager tlsCertificateManager;

    @Autowired
    public DPB0300Service(TlsCertificateManager tlsCertificateManager) {
        super();
        this.tlsCertificateManager = tlsCertificateManager;
    }

    public DPB0300Resp testConnGrpc(TsmpAuthorization authorization, DPB0300Req req) {
    	
        try {
            // 檢查基本參數
            checkParam(req);

            // 驗證 TLS 證書（如果有提供）
            validateTlsCertificates(req);

            DPB0300Resp resp = testConn(req);

            return resp;
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }
    
    private DPB0300Resp testConn(DPB0300Req req) {
    	
		String secureMode = req.getSecureMode();
		DPB0300Resp resp = new DPB0300Resp();
		try {
		 	if ("SECURE".equals(secureMode)) {
		        testTlsConnection(req);
		    } else {
		    	testPlaintextConnection(req);
		    }
		 	resp.setConn(true);
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			resp.setConn(false);
			resp.setErrMsg(e.toString());
		}
		return resp;
    }
    
    private void testPlaintextConnection(DPB0300Req req) throws IOException {
    	
    	String targetHostName = req.getTargetHostName();
        int targetPort = Integer.parseInt(req.getTargetPort());
        int connectTimeout = Integer.parseInt(req.getConnectTimeoutMs());
        //checkmarx, SSRF
        CheckmarxCommUtils.sanitizeForCheckmarxConn(targetHostName, targetPort, connectTimeout);
    }
    
    /**
     * Test TLS connection to upstream service, with default trust for self-signed certificates
     *
     * @param mapping The upstream service mapping to test
     * @return TestResult containing success status and message
     * @throws Exception 
     */
    private void testTlsConnection(DPB0300Req req) throws Exception {
    	String targetHostName = req.getTargetHostName();
        int targetPort = Integer.parseInt(req.getTargetPort());
        int connectTimeout = Integer.parseInt(req.getConnectTimeoutMs());
        boolean autoTrustEnabled = "Y".equals(req.getAutoTrustUpstreamCerts());

        if (req.getAutoTrustUpstreamCerts() == null) {
            autoTrustEnabled = true;
        }

        if (autoTrustEnabled) {
             testTlsConnectionWithAutoTrust(targetHostName, targetPort, connectTimeout);
        } else {
             testTlsConnectionWithValidation(req);
        }
    }
    
    /**
     * Test TLS connection with auto-trust enabled (for internal services with self-signed certificates)
     * @throws Exception 
     */
    private void testTlsConnectionWithAutoTrust(String hostname, int port, int connectTimeout) throws Exception {
    	
        
        javax.net.ssl.SSLContext sslContext = HttpsConnectionChecker.createTrustAllSSLContext();
        javax.net.ssl.SSLSocketFactory factory = sslContext.getSocketFactory();
        javax.net.ssl.SSLSocket socket = null;

        try (Socket tempSocket = new Socket()) {
        	//checkmarx, SSRF
            CheckmarxCommUtils.sanitizeForCheckmarxConn(tempSocket, hostname, port, connectTimeout);

            socket = (javax.net.ssl.SSLSocket) factory.createSocket(
                    tempSocket, hostname, port, true);
            socket.setSoTimeout(connectTimeout);

            socket.startHandshake();

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        
    }
    
    /**
     * Test TLS connection with standard certificate validation
     * @throws Exception 
     */
    private void testTlsConnectionWithValidation(DPB0300Req req) throws Exception {
        String hostname = req.getTargetHostName();
        int port = Integer.parseInt(req.getTargetPort());
        int connectTimeout = Integer.parseInt(req.getConnectTimeoutMs());
        String trustedCerts = req.getTrustedCertsContent();

	    try {
	        java.security.KeyStore keyStore = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
	        keyStore.load(null, null);
	
	        java.io.ByteArrayInputStream certStream = new java.io.ByteArrayInputStream(
	                trustedCerts.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
	        int certCount = 0;
	        //checkmarx, Unchecked Input for Loop Condition
	        while (certStream.available() > 0) {
	            java.security.cert.Certificate cert = cf.generateCertificate(certStream);
	            keyStore.setCertificateEntry("cert-" + certCount++, cert);
	        }
	
	        javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(
	                javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
	        tmf.init(keyStore);
	
	        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
	        sslContext.init(null, tmf.getTrustManagers(), null);
	
	        testTlsConnectionWithContext(sslContext, hostname, port, connectTimeout);
	    } catch (Exception e) {
	    	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
	   	 	throw e;
	    }
        
    }
    
    /**
     * Test TLS connection using a specific SSLContext
     * @throws Exception 
     */
    private void testTlsConnectionWithContext(javax.net.ssl.SSLContext sslContext,
                                                    String hostname, int port, int timeout) throws Exception {
        javax.net.ssl.SSLSocket socket = null;

        try {
            javax.net.ssl.SSLSocketFactory factory = sslContext.getSocketFactory();

            try (Socket tempSocket = new Socket()) {
            	//checkmarx, SSRF
                CheckmarxCommUtils.sanitizeForCheckmarxConn(tempSocket, hostname, port, timeout);

                socket = (javax.net.ssl.SSLSocket) factory.createSocket(
                        tempSocket, hostname, port, true);
                socket.setSoTimeout(timeout);
                
                socket.startHandshake();
            }

        } catch (Exception e) {
        	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
	   	 	throw e;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 驗證 TLS 相關的證書內容
     *
     * @param req 請求對象
     * @throws TsmpDpAaException 如果證書無效或缺少必需的配置
     */
    protected void validateTlsCertificates(DPB0300Req req) {

        // 檢查安全模式是否有效
        String secureMode = req.getSecureMode();
        if (secureMode != null && !secureMode.isEmpty() &&
                !secureMode.equals("AUTO") && !secureMode.equals("SECURE") && !secureMode.equals("PLAINTEXT")) {
            throw TsmpDpAaRtnCode._1559.throwing("Invalid secure mode. Must be AUTO, SECURE or PLAINTEXT");
        }

        // 安全模式啟用時檢查證書是否存在
        String trustedCertsContent = req.getTrustedCertsContent();
        if("SECURE".equals(secureMode) && "N".equals(req.getAutoTrustUpstreamCerts())) {
            if(!StringUtils.hasText(trustedCertsContent)) {
            	throw TsmpDpAaRtnCode._1350.throwing("{{trustedCertsContent}}");
            }else {
            	boolean isValid = tlsCertificateManager.validateCertificate(trustedCertsContent);
                if (!isValid) {
                    throw TsmpDpAaRtnCode._1352.throwing("{{trustedCertsContent}}");
                }
            }
        }
    }

    /**
     * 檢查請求參數是否有效
     *
     * @param req 請求對象
     * @throws TsmpDpAaException 如果參數無效
     */
    public void checkParam(DPB0300Req req) {

        String targetHostName = req.getTargetHostName();
        String targetPort = req.getTargetPort();
        String connectTimeout = req.getConnectTimeoutMs();

        if (targetHostName == null || targetHostName.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("{{targetHostName}}");
        }

        if (targetPort == null || targetPort.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("{{targetPort}}");
        }

        if (connectTimeout == null || connectTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("{{connectTimeoutMs}}");
        }

        // 檢查數值型參數是否有效
        try {
            int portNum = Integer.parseInt(targetPort);
            if (portNum <= 0) {
                throw TsmpDpAaRtnCode._1355.throwing("{{targetPort}}", "1", String.valueOf(portNum));
            }
            if (portNum > 65535) {
                throw TsmpDpAaRtnCode._1356.throwing("{{targetPort}}", "65535", String.valueOf(portNum));
            }

            int connectTimeoutMs = Integer.parseInt(connectTimeout);
            if (connectTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1355.throwing("{{connectTimeoutMs}}", "1", String.valueOf(connectTimeoutMs));
            }

        } catch (NumberFormatException e) {
        	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._2021.throwing();
        }
    }

}