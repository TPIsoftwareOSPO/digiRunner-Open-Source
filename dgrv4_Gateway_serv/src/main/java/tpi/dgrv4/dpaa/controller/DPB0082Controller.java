
package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.DPB0082Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0082Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0082Controller {

	private DPB0082Service service;
	
	@Autowired
	public DPB0082Controller(DPB0082Service service) {
		super();
		this.service = service;
	}

	/** 
	 * for MultiPart X JSON整合設計-上傳檔案	
	 * 為了避免上傳檔案太大, 造成連線逾時, 故使用單一檔案上傳				
	 * 原先使用 multiPart的方式上版時發現 v3 不支持, 暫先使用 base64 方式傳送				
	 * 單純將檔案做Base64後上傳後, 取得上傳的 tmp file name 檔名	
	 * 可呼叫 FileHelper.uploadTemp 上傳到暫存資料夾並取得暫存檔名	
	 */
	@PostMapping(value = "/dgrv4/11/DPB0082", //
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0082Resp> uploadFile2(@RequestHeader HttpHeaders headers, @RequestParam("txSN") String txSN, @RequestParam("txDate") String txDate
			, @RequestParam("txID") String txID, @RequestParam("cID") String cID, @RequestParam("file") MultipartFile mtpFile) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0082Resp resp = null;
		ReqHeader reqHeader = new ReqHeader();
		try {
			reqHeader.setcID(cID);
			reqHeader.setTxDate(txDate);
			reqHeader.setTxID(txID);
			reqHeader.setTxSN(txSN);
			
			if(mtpFile == null || mtpFile.getSize() == 0) {
				//檔案不得為空檔
				throw TsmpDpAaRtnCode._1233.throwing();
			}
			
			resp = service.uploadFile2(tsmpHttpHeader.getAuthorization(), mtpFile.getOriginalFilename(), mtpFile.getBytes());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, reqHeader);
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(reqHeader, resp);
	}
}
