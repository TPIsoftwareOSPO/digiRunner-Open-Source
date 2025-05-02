package tpi.dgrv4.gateway.component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class JwtJweAccessTokenConverter {
//
//	/* [static] field */
//
//	/* [static] */
//
//	/* [static] method */
//
//	/* [instance] field */
//

//
//	private JWEAlgorithm alg = JWEAlgorithm.RSA_OAEP_256;
//
//	private EncryptionMethod enc = EncryptionMethod.A128CBC_HS256;
//
//	private RSAPublicKey rsaPublicKey;
//
//	private RSAPrivateKey rsaPrivateKey;
//
//	// private JsonParser objectMapper = JsonParserFactory.create();
//
//	/* [instance] constructor */
//	public JwtJweAccessTokenConverter() {
//		// Generate an RSA key pair
//		try {
//			KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
//			rsaGen.initialize(2048);
//			KeyPair rsaKeyPair = rsaGen.generateKeyPair();
//			rsaPublicKey = (RSAPublicKey) rsaKeyPair.getPublic();
//			rsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();
//		} catch (NoSuchAlgorithmException e) {
//			this.logger.error(StackTraceUtil.logStackTrace(e));
//		}
//	}
//
//	public JwtJweAccessTokenConverter(KeyPair JweRSAKeyPair) {
//		rsaPublicKey = (RSAPublicKey) JweRSAKeyPair.getPublic();
//		rsaPrivateKey = (RSAPrivateKey) JweRSAKeyPair.getPrivate();
//	}
//
//	/* [instance] method */
//	@Override
//	protected String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
//		String jwt = super.encode(accessToken, authentication);
//
//		try {
//			// jwt is already signed at this point (by JwtAccessTokenConverter)
//			SignedJWT parsed = SignedJWT.parse(jwt);
//
//			// Create JWE object with signed JWT as payload
//			JWEObject jwe = new JWEObject(new JWEHeader(alg, enc), new Payload(parsed));
//
//			// Encrypt the JWE with the RSA public key + specified AES CEK
//			jwe.encrypt(new RSAEncrypter(rsaPublicKey));
//			String jweString = jwe.serialize();
//			return jweString;
//		} catch (Exception e) {
//			logger.error("JWE Token encode error:\n" + StackTraceUtil.logStackTrace(e));
//		}
//
//		return jwt;
//	}
//
//	@Override
//	protected Map<String, Object> decode(String token) {
//		try {
//			// Decrypt the JWE with the RSA private key
//			JWEObject jwe = JWEObject.parse(token);
//			jwe.decrypt(new RSADecrypter(rsaPrivateKey));
//
//			// content of the encrypted token is a signed JWT (signed by
//			// JwtAccessTokenConverter)
//			SignedJWT signedJWT = jwe.getPayload().toSignedJWT();
//
//			// pass on the serialized, signed JWT to JwtAccessTokenConverter
//			return super.decode(signedJWT.serialize());
//
//		} catch (ParseException e) {
//			logger.info("Token might be JWS:\n" + StackTraceUtil.logStackTrace(e));
//		} catch (JOSEException e) {
//			logger.info("Token might be JWS:\n" + StackTraceUtil.logStackTrace(e));
//		}
//		return super.decode(token);
//	}
//
//	/** DefaultAccessTokenConverter used to set Authentication details to Null. */
//	@Override
//	public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
//		OAuth2Authentication authentication = super.extractAuthentication(claims);
//		authentication.setDetails(claims);
//		return authentication;
//	}
//
//	@Override
//	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
//		DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);
//		Map<String, Object> info = new LinkedHashMap<String, Object>(accessToken.getAdditionalInformation());
//		String tokenId = result.getValue();
//		if (!info.containsKey(TOKEN_ID)) {
//			info.put(TOKEN_ID, tokenId);
//		} else {
//			tokenId = (String) info.get(TOKEN_ID);
//		}
//		result.setAdditionalInformation(info);
//		result.setValue(encode(result, authentication));
//		OAuth2RefreshToken refreshToken = result.getRefreshToken();
//		if (refreshToken != null) {
//			DefaultOAuth2AccessToken encodedRefreshToken = new DefaultOAuth2AccessToken(accessToken);
//			encodedRefreshToken.setValue(refreshToken.getValue());
//			// Refresh tokens do not expire unless explicitly of the right type
//			encodedRefreshToken.setExpiration(null);
//			try {
//				Map<String, Object> claims = decode(refreshToken.getValue());
//				if (claims.containsKey(TOKEN_ID)) {
//					encodedRefreshToken.setValue(claims.get(TOKEN_ID).toString());
//				}
//			} catch (InvalidTokenException e) {
//			}
//			Map<String, Object> refreshTokenInfo = new LinkedHashMap<String, Object>(
//					accessToken.getAdditionalInformation());
//			refreshTokenInfo.put(TOKEN_ID, encodedRefreshToken.getValue());
//			refreshTokenInfo.put(ACCESS_TOKEN_ID, tokenId);
//			encodedRefreshToken.setAdditionalInformation(refreshTokenInfo);
//			DefaultOAuth2RefreshToken token = new DefaultOAuth2RefreshToken(
//					encode(encodedRefreshToken, authentication));
//			if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
//				Date expiration = ((ExpiringOAuth2RefreshToken) refreshToken).getExpiration();
//				encodedRefreshToken.setExpiration(expiration);
//				token = new DefaultExpiringOAuth2RefreshToken(encode(encodedRefreshToken, authentication), expiration);
//			}
//			result.setRefreshToken(token);
//		}
//		return result;
//	}
//
//	/* [instance] getter/setter */
//
//	public String getUserName(String token) {
//		return getStringValueFromToken(token, "user_name");
//	}
//
//	public String getClientId(String token) {
//		return getStringValueFromToken(token, "client_id");
//	}
//
//	public String getOrgId(String token) {
//		return getStringValueFromToken(token, "org_id");
//	}
//
//	public String getStringValueFromToken(String token, String key) {
//		Map<String, Object> map = getTokenAsMap(token);
//		return (String) map.get(key);
//	}
//
//	public Map<String, Object> getTokenAsMap(String token) {
//		return decode(token);
//	}
//
}