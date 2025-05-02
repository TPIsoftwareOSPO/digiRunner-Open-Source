package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0089Req {
	
	/** List<clientCertId>	PK s	支持多筆合併String 內容於 txt 中 */
	private List<Long> ids;
	
	/** 憑證類型	使用BcryptParam,ITEM_NO='CERT_TYPE',JWE 使用 TSMP_CLIENT_CERT,TLS 使用 TSMP_CLIENT_CERT2 */
	private String encodeCertType;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public String getEncodeCertType() {
		return encodeCertType;
	}

	public void setEncodeCertType(String encodeCertType) {
		this.encodeCertType = encodeCertType;
	}
	
}
