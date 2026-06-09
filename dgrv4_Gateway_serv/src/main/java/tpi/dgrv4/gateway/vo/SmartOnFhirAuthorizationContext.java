package tpi.dgrv4.gateway.vo;

/**
 * @author Mini
 */
public class SmartOnFhirAuthorizationContext {
	private SmartOnFhirTokenClaims claims;

	public SmartOnFhirAuthorizationContext(SmartOnFhirTokenClaims claims) {
		this.claims = claims;
	}

	public SmartOnFhirTokenClaims getClaims() {
		return claims;
	}

	public String getFhirUser() {
		return claims.getFhirUser();
	}

	public String getPatient() {
		return claims.getPatient();
	}

	public String getEncounter() {
		return claims.getEncounter();
	}

	public String getClientId() {
		return claims.getClientId();
	}

	public String getSimError() {
		return claims.getSimError();
	}
}