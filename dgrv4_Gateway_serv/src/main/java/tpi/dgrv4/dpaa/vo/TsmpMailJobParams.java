package tpi.dgrv4.dpaa.vo;

public class TsmpMailJobParams {
	private String mailType;//內文相同/內文不同
	
	private String mailFileName;

	public String getMailType() {
		return mailType;
	}

	public void setMailType(String mailType) {
		this.mailType = mailType;
	}

	public String getMailFileName() {
		return mailFileName;
	}

	public void setMailFileName(String mailFileName) {
		this.mailFileName = mailFileName;
	}

	@Override
	public String toString() {
		return "TsmpMailJobParams [mailType=" + mailType + ", mailFileName=" + mailFileName + "]";
	}
}
