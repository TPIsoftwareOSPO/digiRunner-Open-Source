package tpi.dgrv4.gateway.component;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Ssl;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "server")
public class ServerConfigProperties {
	private int port;
	private String shutdown;

	private Ssl ssl;
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getShutdown() {
		return shutdown;
	}

	public void setShutdown(String shutdown) {
		this.shutdown = shutdown;
	}

	/**
	 * @return the ssl
	 */
	public Ssl getSsl() {
		return ssl;
	}

	/**
	 * @param ssl the ssl to set
	 */
	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}


	


}
