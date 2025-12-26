package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0218Req  extends ReqValidator{
	
	/**用戶端帳號*/
	private String clientID;
	
	/**Access token expire*/
	private Long accessTokenValidity;
	
	/**OAuth Grant Type*/
	private Set<String> authorizedGrantType;
	
	/**Refresh token expire*/
	private Long raccessTokenValidity;
	
	/**Redirect URL*/
	private String webServerRedirectUri;
	
	private String accessTokenValidityTimeUnit;
	
	private String raccessTokenValidityTimeUnit;
	
	private Integer accessTokenQuota;
	
	private Integer refreshTokenQuota;
	
	/** Extends Redirect URL 1 */
	private String webServerRedirectUri1;
	
	/** Extends Redirect URL 2 */
	private String webServerRedirectUri2;
	
	/** Extends Redirect URL 3 */
	private String webServerRedirectUri3;
	
	/** Extends Redirect URL 4 */
	private String webServerRedirectUri4;
	
	/** Extends Redirect URL 5 */
	private String webServerRedirectUri5;

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	
	public String getClientID() {
		return clientID;
	}

	public Set<String> getAuthorizedGrantType() {
		return authorizedGrantType;
	}

	public void setAuthorizedGrantType(Set<String> authorizedGrantType) {
		this.authorizedGrantType = authorizedGrantType;
	}

	public Long getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Long accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public Long getRaccessTokenValidity() {
		return raccessTokenValidity;
	}

	public void setRaccessTokenValidity(Long raccessTokenValidity) {
		this.raccessTokenValidity = raccessTokenValidity;
	}

	public String getWebServerRedirectUri() {
		return webServerRedirectUri;
	}

	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}

	public String getAccessTokenValidityTimeUnit() {
		return accessTokenValidityTimeUnit;
	}

	public void setAccessTokenValidityTimeUnit(String accessTokenValidityTimeUnit) {
		this.accessTokenValidityTimeUnit = accessTokenValidityTimeUnit;
	}

	public String getRaccessTokenValidityTimeUnit() {
		return raccessTokenValidityTimeUnit;
	}

	public void setRaccessTokenValidityTimeUnit(String raccessTokenValidityTimeUnit) {
		this.raccessTokenValidityTimeUnit = raccessTokenValidityTimeUnit;
	}
	
	public Integer getAccessTokenQuota() {
		return accessTokenQuota;
	}

	public void setAccessTokenQuota(Integer accessTokenQuota) {
		this.accessTokenQuota = accessTokenQuota;
	}

	public Integer getRefreshTokenQuota() {
		return refreshTokenQuota;
	}

	public void setRefreshTokenQuota(Integer refreshTokenQuota) {
		this.refreshTokenQuota = refreshTokenQuota;
	}
 
	public String getWebServerRedirectUri1() {
		return webServerRedirectUri1;
	}

	public void setWebServerRedirectUri1(String webServerRedirectUri1) {
		this.webServerRedirectUri1 = webServerRedirectUri1;
	}

	public String getWebServerRedirectUri2() {
		return webServerRedirectUri2;
	}

	public void setWebServerRedirectUri2(String webServerRedirectUri2) {
		this.webServerRedirectUri2 = webServerRedirectUri2;
	}

	public String getWebServerRedirectUri3() {
		return webServerRedirectUri3;
	}

	public void setWebServerRedirectUri3(String webServerRedirectUri3) {
		this.webServerRedirectUri3 = webServerRedirectUri3;
	}

	public String getWebServerRedirectUri4() {
		return webServerRedirectUri4;
	}

	public void setWebServerRedirectUri4(String webServerRedirectUri4) {
		this.webServerRedirectUri4 = webServerRedirectUri4;
	}

	public String getWebServerRedirectUri5() {
		return webServerRedirectUri5;
	}

	public void setWebServerRedirectUri5(String webServerRedirectUri5) {
		this.webServerRedirectUri5 = webServerRedirectUri5;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("clientID")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("accessTokenValidity")
				.isRequired()
				.min(0)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("raccessTokenValidity")
				.isRequired()
				.min(0)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildCollection(locale)
				.field("authorizedGrantType")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("webServerRedirectUri")
				.maxLength(255)
//				.pattern(RegexpConstant.URI, TsmpDpAaRtnCode._1405.getCode(), null) // 2023/4/7,先註解,以寫入 localhost 的 URL
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("accessTokenValidityTimeUnit")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("raccessTokenValidityTimeUnit")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("accessTokenQuota")
				.isRequired()
				.min(0)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("refreshTokenQuota")
				.isRequired()
				.min(0)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("webServerRedirectUri1")
				.maxLength(255)
				.build(),				
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("webServerRedirectUri2")
				.maxLength(255)
				.build(),				
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("webServerRedirectUri3")
				.maxLength(255)
				.build(),				
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("webServerRedirectUri4")
				.maxLength(255)
				.build(),				
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("webServerRedirectUri5")
				.maxLength(255)
				.build(),				
		});
		
		
	}
	
}
