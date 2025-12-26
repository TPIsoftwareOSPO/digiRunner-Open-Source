package tpi.dgrv4.gateway.vo;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DgrIdPType;

public class AcCusIdPLoginUrl {

	private String acIdpInfoCusName;

	private String cusLoginUrl;

	public AcCusIdPLoginUrl(String acIdpInfoCusName, String cusLoginUrl) {

		if (StringUtils.hasText(acIdpInfoCusName)) {
			this.acIdpInfoCusName = acIdpInfoCusName;
		} else {
			this.acIdpInfoCusName = DgrIdPType.CUS;
		}
		this.cusLoginUrl = cusLoginUrl;
	}

	public String getAcIdpInfoCusName() {
		return acIdpInfoCusName;
	}

	public void setAcIdpInfoCusName(String acIdpInfoCusName) {
		this.acIdpInfoCusName = acIdpInfoCusName;
	}

	public String getCusLoginUrl() {
		return cusLoginUrl;
	}

	public void setCusLoginUrl(String cusLoginUrl) {
		this.cusLoginUrl = cusLoginUrl;
	}

}
