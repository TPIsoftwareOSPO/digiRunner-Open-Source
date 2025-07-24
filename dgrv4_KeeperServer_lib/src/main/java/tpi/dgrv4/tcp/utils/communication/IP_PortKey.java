package tpi.dgrv4.tcp.utils.communication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class IP_PortKey {
	public String ip;
	public int port;
	
	public IP_PortKey(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof IP_PortKey))
			return false;
		IP_PortKey castOther = (IP_PortKey) other;
		return new EqualsBuilder().append(ip, castOther.ip).append(port,
				castOther.port).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(ip).append(port).toHashCode();
	}
	
}
