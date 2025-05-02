package tpi.dgrv4.entity.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_token_history")
public class TsmpTokenHistory implements Serializable {

	@Id
	@Column(name = "seq_no")
	private Long seqNo;

	@Column(name = "user_nid")
	private String userNid;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "token_jti")
	private String tokenJti;

	@Column(name = "scope")
	private String scope;

	@Column(name = "expired_at")
	private Date expiredAt;

	@Column(name = "create_at")
	private Date createAt;

	@Column(name = "revoked_at")
	private Date revokedAt;

	@Column(name = "revoked_status")
	private String revokedStatus;

	@Column(name = "retoken_jti")
	private String retokenJti;

	@Column(name = "reexpired_at")
	private Date reexpiredAt;

	@Column(name = "stime")
	private Date stime;

	@Column(name = "rft_revoked_at")
	private Date rftRevokedAt;

	@Column(name = "rft_revoked_status")
	private String rftRevokedStatus;

	@Column(name = "token_quota")
	private Long tokenQuota;
	
	@Column(name = "token_used")
	private Long tokenUsed;
	
	@Column(name = "rft_quota")
	private Long rftQuota;
	
	@Column(name = "rft_used")
	private Long rftUsed;
	
	@Column(name = "idp_type")
	private String idpType;
	
	@Column(name = "id_token_jwtstr")
	private String idTokenJwtstr;
	
	@Column(name = "refresh_token_jwtstr")
	private String refreshTokenJwtstr;
	
	@Column(name = "api_resp")
	private String apiResp;

	@Override
	public String toString() {
		return "TsmpTokenHistory [seqNo=" + seqNo + ", userNid=" + userNid + ", userName=" + userName + ", clientId="
				+ clientId + ", tokenJti=" + tokenJti + ", scope=" + scope + ", expiredAt=" + expiredAt + ", createAt="
				+ createAt + ", revokedAt=" + revokedAt + ", revokedStatus=" + revokedStatus + ", retokenJti="
				+ retokenJti + ", reexpiredAt=" + reexpiredAt + ", stime=" + stime + ", rftRevokedAt=" + rftRevokedAt
				+ ", rftRevokedStatus=" + rftRevokedStatus + ", tokenQuota=" + tokenQuota + ", tokenUsed=" + tokenUsed
				+ ", rftQuota=" + rftQuota + ", rftUsed=" + rftUsed + ", idpType=" + idpType + ", idTokenJwtstr="
				+ idTokenJwtstr + ", refreshTokenJwtstr=" + refreshTokenJwtstr + ", apiResp=" + apiResp + "]\n";
	}

	public Long getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(Long seqNo) {
		this.seqNo = seqNo;
	}

	public String getUserNid() {
		return userNid;
	}

	public void setUserNid(String userNid) {
		this.userNid = userNid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTokenJti() {
		return tokenJti;
	}

	public void setTokenJti(String tokenJti) {
		this.tokenJti = tokenJti;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Date getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(Date expiredAt) {
		this.expiredAt = expiredAt;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public Date getRevokedAt() {
		return revokedAt;
	}

	public void setRevokedAt(Date revokedAt) {
		this.revokedAt = revokedAt;
	}

	public String getRevokedStatus() {
		return revokedStatus;
	}

	public void setRevokedStatus(String revokedStatus) {
		this.revokedStatus = revokedStatus;
	}

	public String getRetokenJti() {
		return retokenJti;
	}

	public void setRetokenJti(String retokenJti) {
		this.retokenJti = retokenJti;
	}

	public Date getReexpiredAt() {
		return reexpiredAt;
	}

	public void setReexpiredAt(Date reexpiredAt) {
		this.reexpiredAt = reexpiredAt;
	}

	public Date getStime() {
		return stime;
	}

	public void setStime(Date stime) {
		this.stime = stime;
	}

	public Date getRftRevokedAt() {
		return rftRevokedAt;
	}

	public void setRftRevokedAt(Date rftRevokedAt) {
		this.rftRevokedAt = rftRevokedAt;
	}

	public String getRftRevokedStatus() {
		return rftRevokedStatus;
	}

	public void setRftRevokedStatus(String rftRevokedStatus) {
		this.rftRevokedStatus = rftRevokedStatus;
	}

	public Long getTokenQuota() {
		return tokenQuota;
	}

	public void setTokenQuota(Long tokenQuota) {
		this.tokenQuota = tokenQuota;
	}

	public Long getTokenUsed() {
		return tokenUsed;
	}

	public void setTokenUsed(Long tokenUsed) {
		this.tokenUsed = tokenUsed;
	}

	public Long getRftQuota() {
		return rftQuota;
	}

	public void setRftQuota(Long rftQuota) {
		this.rftQuota = rftQuota;
	}

	public Long getRftUsed() {
		return rftUsed;
	}

	public void setRftUsed(Long rftUsed) {
		this.rftUsed = rftUsed;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getIdTokenJwtstr() {
		return idTokenJwtstr;
	}

	public void setIdTokenJwtstr(String idTokenJwtstr) {
		this.idTokenJwtstr = idTokenJwtstr;
	}

	public String getRefreshTokenJwtstr() {
		return refreshTokenJwtstr;
	}

	public void setRefreshTokenJwtstr(String refreshTokenJwtstr) {
		this.refreshTokenJwtstr = refreshTokenJwtstr;
	}

	public String getApiResp() {
		return apiResp;
	}

	public void setApiResp(String apiResp) {
		this.apiResp = apiResp;
	}
}