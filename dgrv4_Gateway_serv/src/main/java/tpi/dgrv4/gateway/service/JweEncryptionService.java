package tpi.dgrv4.gateway.service;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.JweEncryptionReq;
import tpi.dgrv4.gateway.vo.JweEncryptionResp;

/**
 * 將傳入的資料,經過 JWE 加密後回傳 <br>
 * 回傳的 JWE 加密值, 其中包含增加的到期時間
 * 
 * @author Mini
 */

@Service
public class JweEncryptionService {
	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;

	private static ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	public JweEncryptionService(TsmpCoreTokenEntityHelper tsmpCoreTokenHelper) {
		super();
		this.tsmpCoreTokenHelper = tsmpCoreTokenHelper;
	}

	public JweEncryptionResp jweEncryption(HttpServletRequest httpReq, HttpServletResponse httResp,
			JweEncryptionReq req) {
		try {
			Map<String, String> reqDataMap = req.getDataMap();
			return jweEncryption(reqDataMap);

		} catch (Exception e) {
			String ciphertext = "";
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			JweEncryptionResp resp = new JweEncryptionResp();
			resp.setText(ciphertext);
			return resp;
		}
	}

	/**
	 * 做 JWE 加密, 其中包含增加的 exp (到期時間)
	 */
	public JweEncryptionResp jweEncryption(Map<String, String> reqDataMap) throws Exception {
		JweEncryptionResp resp = new JweEncryptionResp();
		String ciphertext = "";// 密文

		// 若沒有傳入的資料, 回傳 ""
		if (CollectionUtils.isEmpty(reqDataMap)) {
			resp.setText(ciphertext);
			return resp;
		}

		// 到期時間: 現在時間 + 15 秒(15000亳秒)
		long exp = System.currentTimeMillis() + (15 * 1000);

		// 增加到期時間 exp
		Map<String, Object> targetMap = new HashMap<>();
		for (Map.Entry<String, String> entry : reqDataMap.entrySet()) {
			targetMap.put(entry.getKey(), entry.getValue());
		}

		targetMap.put("exp", exp);

		// 將傳入的 body 資料轉為 JSON
		String payload = objectMapper.writeValueAsString(targetMap);

		// 取得 Public Key, 將 JSON 值做 JWE 加密
		PublicKey publicKey = getTsmpCoreTokenHelper().getKeyPair().getPublic();
		ciphertext = JWEcodec.jweEncryption(publicKey, payload); // 較換成 JWE 格式
		resp.setText(ciphertext);
		return resp;
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenHelper() {
		return tsmpCoreTokenHelper;
	}
}
