package tpi.dgrv4.dpaa.service.smartOnFhirProxyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0341Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDto;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.RelayConfig;
import tpi.dgrv4.gateway.service.SmartProxyBackendTokenService;
import tpi.dgrv4.gateway.service.SmartProxyBackendTokenService.TokenProbeResult;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * DPB0341：測試後端代取（outbound relay）能否拿到 token。
 *
 * <p>input 沿用 {@link SmartOnFhirProxyDto}（類似 DPB0311），但只讀取 out_* 欄位組成 RelayConfig，
 * 直接呼叫 {@link SmartProxyBackendTokenService#probe}（不寫 DB、不走快取），回傳成功與否與摘要。</p>
 */
@Service
public class SmartOnFhirProxyRelayTestService {

	private TPILogger logger = TPILogger.tl;

	private final TsmpTAEASKHelper taeask;
	private final SmartProxyBackendTokenService backendTokenService;

	@Autowired
	public SmartOnFhirProxyRelayTestService(TsmpTAEASKHelper taeask,
			SmartProxyBackendTokenService backendTokenService) {
		this.taeask = taeask;
		this.backendTokenService = backendTokenService;
	}

	public DPB0341Resp testRelay(TsmpAuthorization auth, SmartOnFhirProxyDto dto) {
		DPB0341Resp resp = new DPB0341Resp();

		// 必填檢查（與建立時的代取規則一致）
		if (dto == null) {
			resp.setSuccess(false);
			resp.setMessage("request body is required");
			return resp;
		}
		if (!StringUtils.hasText(dto.getSofProxyOutClientId())) {
			resp.setSuccess(false);
			resp.setMessage("sofProxyOutClientId is required");
			return resp;
		}
		if (!StringUtils.hasText(dto.getSofProxyOutClientSecret())) {
			resp.setSuccess(false);
			resp.setMessage("sofProxyOutClientSecret is required");
			return resp;
		}
		if (!StringUtils.hasText(dto.getSofProxyOutWellKnown())
				&& !StringUtils.hasText(dto.getSofProxyOutTokenEndpoint())) {
			resp.setSuccess(false);
			resp.setMessage("sofProxyOutWellKnown or sofProxyOutTokenEndpoint is required");
			return resp;
		}

		// 以傳入的明文 secret 組 RelayConfig（AES 加密，probe 內會解回；保持與正式路徑一致的 contract）
		RelayConfig cfg = RelayConfig.builder()
				.enabled(true)
				.authMethod(dto.getSofProxyOutAuthMethod())
				.clientId(dto.getSofProxyOutClientId())
				.clientSecretEnc(taeask.encrypt(dto.getSofProxyOutClientSecret()))
				.wellKnownUrl(dto.getSofProxyOutWellKnown())
				.tokenEndpoint(dto.getSofProxyOutTokenEndpoint())
				.scope(dto.getSofProxyOutScope())
				.build();

		try {
			TokenProbeResult r = backendTokenService.probe(cfg);
			resp.setSuccess(true);
			resp.setMessage("OK");
			resp.setTokenEndpoint(r.tokenEndpoint());
			resp.setTokenObtained(true);
			resp.setToken(r.token());
			resp.setExpiresInSec(r.expiresInSec());
		} catch (Exception e) {
			this.logger.error("DPB0341 relay test failed: " + e.getMessage());
			resp.setSuccess(false);
			resp.setMessage(e.getMessage());
		}
		return resp;
	}
}
