package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.composer.ComposerURLService;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class ComposerSwaggerService {
	
	private ComposerURLService composerURLService;

	@Autowired
	public ComposerSwaggerService(ComposerURLService composerURLService) {
		super();
		this.composerURLService = composerURLService;
	}

	public ResponseEntity<?> forwardToGet(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes) {
		try {

			String uri = httpReq.getRequestURI();

			// 取代進入digiRunner的Path
			uri = uri.replaceFirst("/composer", "");

			// 取出Composer Server IP並組合swagger路徑。
			List<String> composerSwaggerAPIPath = getComposerURLService().resolvePath(uri);

			for (String path : composerSwaggerAPIPath) {

				Map<String, List<String>> headers = new HashMap<>();
				HttpRespData respObj = HttpUtil.httpReqByGetList(path, headers, true, false);
				
				// 連線失敗
				if (respObj.statusCode == -1) {
					// 設定編碼
					httpRes.addHeader("Content-Type", "application/json; charset=utf-8");
					ByteArrayInputStream bi = new ByteArrayInputStream(TsmpDpAaRtnCode._1530.getDefaultMessage().getBytes());
					IOUtils.copy(bi, httpRes.getOutputStream());
					throw TsmpDpAaRtnCode._1530.throwing();
				}
				
				respObj.fetchByte(); // because Enable inputStream

				TPILogger.tl.debug("Composer Swagger = " + respObj.respStr);
				
				// 將Composer Swagger回覆header取出來放在httpRes header
				respObj.respHeader.forEach((key, valList) -> valList.forEach((val) -> {
					if (key != null) {
						httpRes.addHeader(key, val);
					}
				}));
				//checkmarx, Missing HSTS Header
				httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
		        
				// 將內容輸出				
				ByteArrayInputStream bi = new ByteArrayInputStream(respObj.httpRespArray);
				IOUtils.copy(bi, httpRes.getOutputStream());

				// 只要沒有出錯就結束
				if (true) {
					break;
				}

			}
			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return null;
	}

	protected ComposerURLService getComposerURLService() {
		return composerURLService;
	}

}
