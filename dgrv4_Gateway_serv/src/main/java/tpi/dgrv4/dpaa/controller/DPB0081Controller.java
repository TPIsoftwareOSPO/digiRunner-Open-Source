package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.dpaa.service.DPB0081Service;

/**
 * FileService: 檔案處理
 * 20191224 基於安全理由暫時不啟用
 * @author Kim
 
@RestController
*/
public class DPB0081Controller {

	private DPB0081Service dpf0081Service;

	@Autowired
	public DPB0081Controller(DPB0081Service dpf0081Service) {
		super();
		this.dpf0081Service = dpf0081Service;
	}

	/**
	 * 刪除檔案:<br/>
	 * 依照指定的file id, 刪除DB record and HDD File
	 * @param jsonStr
	 * @return
	 
	
	@PostMapping(value = "/dgrv4/11/DPB0081", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0081Resp> deleteFile(@RequestBody TsmpBaseReq<DPB0081Req> req) {
		
		DPB0081Resp resp = null;
		try {
			resp = dpf0081Service.deleteFile(req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
	*/

}
