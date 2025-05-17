package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * [ZH] GTW IdP 流程中, 當不需要轉到前端 User 同意畫面時使用, 例如 GTW CUS IdP, <br>
 * 因為前端 Angular 打包後, 無法在 IOS 16.3 以前的版本上操作, <br>
 * 此為 IOS 正規表示式的 bug  <br>
 * [EN] In the GTW IdP process, it is used when there is no need to go to the front-end User consent screen, such as GTW CUS IdP, <br>
 * Because the front-end Angular is packaged, it cannot be operated on versions before IOS 16.3, <br>
 * This is a bug in IOS regular expressions <br>
 *  
 * @author Mini
 */
@RestController
public class GtwIdPSowMsgController {
	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@GetMapping(value = "/dgrv4/gtwidp/msg")
	public ResponseEntity<?> showMsg(@RequestHeader HttpHeaders headers, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		
		TPILogger.tl.info("\n--【" + req.getRequestURL().toString() + "】--");
		
		String msg = req.getParameter("msg");
		msg = new String(Base64Util.base64URLDecode(msg));
		return new ResponseEntity<Object>(msg, HttpStatus.OK);
	}
}
