package tpi.dgrv4.gateway.vo;

import java.util.Date;
import java.util.List;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * @author Mini
 */
public class SmartOnFhirTokenClaims {
	private String issuer;
	private List<String> audience;
	private Date expirationTime;
	private Date issuedAt;
	private String subject;
	private String clientId;
	private List<String> scopes;
	private String fhirUser;
	private String patient;
	private String encounter;
	private String simError;

	public static SmartOnFhirTokenClaims fromJWTClaimsSet(JWTClaimsSet claims) throws Exception {
		SmartOnFhirTokenClaims tokenClaims = new SmartOnFhirTokenClaims();

		tokenClaims.issuer = claims.getIssuer();
		tokenClaims.audience = claims.getAudience();
		tokenClaims.expirationTime = claims.getExpirationTime();
		tokenClaims.issuedAt = claims.getIssueTime();
		tokenClaims.subject = claims.getSubject();
		tokenClaims.clientId = claims.getStringClaim("client_id");
		tokenClaims.scopes = claims.getStringListClaim("scope");
		tokenClaims.fhirUser = claims.getStringClaim("fhirUser");
		tokenClaims.patient = claims.getStringClaim("patient");
		tokenClaims.encounter = claims.getStringClaim("encounter");
		tokenClaims.simError = claims.getStringClaim("sim_error");

		return tokenClaims;
	}

	// Getters
	public String getIssuer() {
		return issuer;
	}

	public List<String> getAudience() {
		return audience;
	}

	public Date getExpirationTime() {
		return expirationTime;
	}

	public Date getIssuedAt() {
		return issuedAt;
	}

	public String getSubject() {
		return subject;
	}

	public String getClientId() {
		return clientId;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public String getFhirUser() {
		return fhirUser;
	}

	public String getPatient() {
		return patient;
	}

	public String getEncounter() {
		return encounter;
	}

	public String getSimError() {
		return simError;
	}

	@Override
	public String toString() {
		return "SmartTokenClaims [issuer=" + issuer + ", audience=" + audience + ", expirationTime=" + expirationTime
				+ ", issuedAt=" + issuedAt + ", subject=" + subject + ", clientId=" + clientId + ", scopes=" + scopes
				+ ", fhirUser=" + fhirUser + ", patient=" + patient + ", encounter=" + encounter + "]";
	}
}