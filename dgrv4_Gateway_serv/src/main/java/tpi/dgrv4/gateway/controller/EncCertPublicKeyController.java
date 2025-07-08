package tpi.dgrv4.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * [ZH] 取得 API body 做 JWS 驗章 & JWE 加密, 所使用的 dgR 金鑰對的公鑰憑證內容 (也是核發 Token & ENC
 * 加密的公鑰) <br>
 * [EN] Get the public key certificate content of the dgR key pair used for JWS
 * stamping & JWE encryption of the API body (also the public key for issuing
 * Token & ENC encryption) <br>
 * 
 * @author Mini <br>
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@RestController
public class EncCertPublicKeyController {

	private final TsmpCoreTokenEntityHelper tsmpCoreTokenEntityHelper;

	@GetMapping(value = "/dgrv4/ssotoken/enccert", produces = "text/plain")
	public ResponseEntity<String> getEncCert(HttpServletRequest httpReq, HttpServletResponse httpResp) {
		// [ZH] 取得 dgR 的公鑰憑證內容
		// [EN] Get the public key certificate content of dgR
		String pemPub = getTsmpCoreTokenEntityHelper().getPublicKeyPem();
		TPILogger.tl.debug(pemPub);
		return new ResponseEntity<>(pemPub, HttpStatus.OK);
	}
}
