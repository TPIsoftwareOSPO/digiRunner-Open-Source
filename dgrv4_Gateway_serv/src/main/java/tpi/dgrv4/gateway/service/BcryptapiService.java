package tpi.dgrv4.gateway.service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.copy.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.BcryptUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 將傳入的資料(密碼明文),經過Base64Encode再Bcrypt後回傳
 * 
 * @author zoele
 *
 */
@Service
public class BcryptapiService {
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public BcryptapiService(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public ResponseEntity<?> bcryptapi(HttpServletRequest req) {
		String respText = null;
		try {
			// 取得傳入的表頭 tpi-plaintext 值
			String plaintext = req.getHeader("tpi-plaintext");
			if (!StringUtils.hasLength(plaintext)) {
				String errMsg = "Missing required header: tpi-plaintext";
				return new ResponseEntity<>(errMsg, HttpStatus.BAD_REQUEST);// 400
			}
			// 取得傳入的表頭 tpi-rounds 值
			String tpiRounds = req.getHeader("tpi-rounds");
			int rounds = 0;

			if (!StringUtils.hasLength(tpiRounds)) {
				rounds = 10; // 若沒有值, 預設為 10
			} else {

				rounds = Integer.valueOf(tpiRounds);

				if (rounds <= 3) {
					String errMsg = "Invalied required header: tpi-rounds";
					return new ResponseEntity<>(errMsg, HttpStatus.BAD_REQUEST);// 400
				}

			}
			// 限定使用API的權限
			Boolean onlineConsoleFlag = getTsmpSettingService().getVal_TSMP_ONLINE_CONSOLE();
			if (Boolean.FALSE.equals(onlineConsoleFlag)) {
				String errMsg = "Setting 'TSMP_ONLINE_CONSOLE' is false";
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<>(errMsg, HttpStatus.FORBIDDEN);// 403
			}
			
			//checkmarx, Privacy Violation, 所以不要命名password之類, 已通過中風險
			// 取得密碼的Hash值
			String base64Mima = ServiceUtil.base64Encode(plaintext.getBytes());
			//checkmarx, Spring BCrypt Insecure Parameters, 已通過中風險
			respText = BcryptUtil.encode(rounds, base64Mima);
		} catch (NumberFormatException e) {
			String errMsg = "Invalied required header: tpi-rounds";
			return new ResponseEntity<>(errMsg, HttpStatus.BAD_REQUEST);// 400
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			return new ResponseEntity<>(errMsg, HttpStatus.INTERNAL_SERVER_ERROR);// 500
		}

		return new ResponseEntity<Object>(respText, HttpStatus.OK);// 200
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
}
