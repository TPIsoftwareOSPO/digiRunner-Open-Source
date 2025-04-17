package tpi.dgrv4.dpaa.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.DPB0078Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * FileService: 檔案處理
 * 
 * @author Kim
 */
@RestController
public class DPB0078Controller {

	private DPB0078Service service;

	@Autowired
	public DPB0078Controller(DPB0078Service service) {
		super();
		this.service = service;
	}

	/**
	 * 下載檔案:<br/>
	 * 依據指定的路徑下載檔案
	 * 
	 * @param jsonStr
	 * @return
	 */

	@GetMapping(value = "/dgrv4/11/DPB0078")
	public ResponseEntity<byte[]> downloadFile(@RequestHeader HttpHeaders headers, @RequestParam String filePath) {

		TsmpBaseReq<String> req = new TsmpBaseReq<String>();
		try {

			if (headers.containsKey("Authorization")) {
				TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
				req = parseHeaderAndBody(headers);
				ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			}

			return service.downloadFile(filePath);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

	private TsmpBaseReq<String> parseHeaderAndBody(HttpHeaders headers) {
		TsmpBaseReq<String> req = new TsmpBaseReq<String>();
		ReqHeader reqHeader = new ReqHeader();
		Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
		for (Map.Entry<String, List<String>> entry : entries) {
			String headerName = entry.getKey();
			List<String> headerValues = entry.getValue();
			if ("cid".equalsIgnoreCase(headerName)) {
				reqHeader.setcID(headerValues.get(0));
			}
			if ("locale".equalsIgnoreCase(headerName)) {
				reqHeader.setLocale(headerValues.get(0));
			}
			if ("txdate".equalsIgnoreCase(headerName)) {
				reqHeader.setTxDate(headerValues.get(0));
			}
			if ("txid".equalsIgnoreCase(headerName)) {
				reqHeader.setTxID(headerValues.get(0));
			}
			if ("txsn".equalsIgnoreCase(headerName)) {
				reqHeader.setTxSN(headerValues.get(0));
			}

		}
		req.setReqHeader(reqHeader);
		req.setBody(new String());
		return req;
	}
}
