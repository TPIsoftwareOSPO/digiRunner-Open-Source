package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.dpaa.service.DPB0079Service;

/**
 * FileService: 檔案處理
 * 
 * @author Kim
 */
@RestController
public class DPB0079Controller {

	private DPB0079Service service;

	@Autowired
	public DPB0079Controller(DPB0079Service service) {
		super();
		this.service = service;
	}

	/**
	 * 取出ImgBinaryData:<br/>
	 * 依據指定的路徑下載檔案
	 * @param jsonStr
	 * @return
	 */

	@GetMapping(value = "/dgrv4/11/DPB0079")
	public ResponseEntity<byte[]> getImage(@RequestParam String filePath) {
		return service.getImage(filePath);
	}

}
