package tpi.dgrv4.gateway.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.constant.DgrDeployRole;

@Component
public class DigiRunnerGtwDeployProperties {
 
	@Value("${digiRunner.gtw.deploy.role:#{null}}")
	private String deployRole;

	@Value("${digiRunner.gtw.deploy.id:#{null}}")
	private String deployId;

	@Value("${digiRunner.gtw.deploy.interval.ms:1000}")
	private Long deployIntervalMs;
 
	@Value("${digiRunner.gtw.deploy.landing.ip.port:#{null}}")
	private String deployLandingIpPort;

	// 預設 https
	@Value("${digiRunner.gtw.deploy.landing.scheme:https}")
	private String deployLandingScheme;
	
	public boolean isMemoryRole() {
		return DgrDeployRole.MEMORY.value().equalsIgnoreCase(getDeployRole());
	}

	public String getDeployRole() {
		return deployRole;
	}

	public void setDeployRole(String deployRole) {
		this.deployRole = deployRole;
	}

	public String getDeployId() {
		return deployId;
	}

	public void setDeployId(String deployId) {
		this.deployId = deployId;
	}

	public long getDeployIntervalMs() {
		return deployIntervalMs;
	}

	public void setDeployIntervalMs(long deployIntervalMs) {
		this.deployIntervalMs = deployIntervalMs;
	}

	public String getDeployLandingIpPort() {
		return deployLandingIpPort;
	}

	public void setDeployLandingIpPort(String deployLandingIpPort) {
		this.deployLandingIpPort = deployLandingIpPort;
	}

	public String getDeployLandingScheme() {
		return deployLandingScheme;
	}

	public void setDeployLandingScheme(String deployLandingScheme) {
		this.deployLandingScheme = deployLandingScheme;
	}
}
