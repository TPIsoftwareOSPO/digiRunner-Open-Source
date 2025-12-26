package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0087Req {

	/** PK s	支持多筆合併下載為 zip */
	private List<Long> ids;

	/** 憑證類型	使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2 */
	private String encodeCertType;

	public DPB0087Req() {
	}

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
