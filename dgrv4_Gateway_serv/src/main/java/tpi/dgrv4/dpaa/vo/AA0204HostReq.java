package tpi.dgrv4.dpaa.vo;
	
public class AA0204HostReq {

	/**主機序號*/
	private String hostSeq;
	
	/**主機名稱*/
	private String hostName;
	
	/**主機IP*/
	private String hostIP;

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostIP == null) ? 0 : hostIP.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + ((hostSeq == null) ? 0 : hostSeq.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AA0204HostReq other = (AA0204HostReq) obj;
		if (hostIP == null) {
			if (other.hostIP != null)
				return false;
		} else if (!hostIP.equals(other.hostIP))
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (hostSeq == null) {
			if (other.hostSeq != null)
				return false;
		} else if (!hostSeq.equals(other.hostSeq))
			return false;
		return true;
	}

	public String getHostSeq() {
		return hostSeq;
	}

	public void setHostSeq(String hostSeq) {
		this.hostSeq = hostSeq;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getHostName() {
		return hostName;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	
	
}
