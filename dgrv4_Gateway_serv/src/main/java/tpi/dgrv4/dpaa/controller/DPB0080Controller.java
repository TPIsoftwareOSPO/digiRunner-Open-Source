package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DPB0080Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0080Req;
import tpi.dgrv4.dpaa.vo.DPB0080Resp;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * FileService: 檔案處理
 * 
 * @author Kim
 */
@RestController
public class DPB0080Controller {

	private TPILogger logger = TPILogger.tl;

	private DPB0080Service dpb0080Service;
	private FileHelper fileHelper;

	/* tsmp-v3 暫不支持 MULTIPART_FORM_DATA 類型

	@PostMapping(value = "/dgrv4/11/DPB0080", //
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public TsmpBaseResp<DPB0080Resp> uploadFile(HttpServletRequest req, @RequestHeader HttpHeaders headers) {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		if (!resolver.isMultipart(req)) {
			throw new TsmpDpApiException("Unsupport content type: " + req.getContentType());
		}

		this.logger.debug("=== HttpServletRequest ===");
		this.logger.debug("ReqHeader = {}", req.getParameter("ReqHeader"));
		this.logger.debug("fileCateCode = {}", req.getParameter("fileCateCode"));		
		this.logger.debug("refId = {}", req.getParameter("refId"));
		this.logger.debug("attachFile = {}", req.getParameter("attachFile"));

		//不能使用 CommonsMultipartResolver.resolveMultipart(),
		//否則 MultipartHttpServletRequest.getFiles() 會取不到檔案
		//MultipartHttpServletRequest mReq = resolver.resolveMultipart(req);
		MultipartHttpServletRequest mReq = (MultipartHttpServletRequest) req;

		this.logger.debug("=== MultipartHttpServletRequest ===");
		this.logger.debug("ReqHeader = {}", mReq.getParameter("ReqHeader"));
		this.logger.debug("fileCateCode = {}", mReq.getParameter("fileCateCode"));		
		this.logger.debug("refId = {}", mReq.getParameter("refId"));
		this.logger.debug("attachFile = {}", mReq.getParameter("attachFile"));

		ReqHeader reqHeader = null;
		try {
			reqHeader = ControllerUtil.toReqHeader(mReq.getParameter("ReqHeader"));

			// 準備暫存上傳檔案的目錄
			Resource uploadTempDir = getUploadTempDir();
			resolver.setUploadTempDir(uploadTempDir);

			// 拆解出檔案與欄位
			TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
			String clientId = tsmpHttpHeader.getAuthorization().getClientId();

			DPB0080Req dpb0080Req = new DPB0080Req();
			dpb0080Req.setClientId(clientId);
			dpb0080Req.setFileCateCode(mReq.getParameter("fileCateCode"));
			dpb0080Req.setRefId(null);
			try {
				dpb0080Req.setRefId(Long.valueOf(mReq.getParameter("refId")));
			} catch (NumberFormatException nfe) {
				this.logger.error("", nfe);
			}
			dpb0080Req.setAttachFile(mReq.getFiles("attachFile"));

			dpb0080Service.uploadFile(dpb0080Req);
		} catch (Exception e) {
			this.logger.error("", e);
			throw new TsmpDpApiException(e, reqHeader);
		}

		return ControllerUtil.tsmpResponseBaseObj(reqHeader, new DPB0080Resp());
	}
	*/

	@Autowired
	public DPB0080Controller(DPB0080Service dpb0080Service, FileHelper fileHelper) {
		super();
		this.dpb0080Service = dpb0080Service;
		this.fileHelper = fileHelper;
	}

	@PostMapping(value = "/dgrv4/11/DPB0080", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public TsmpBaseResp<DPB0080Resp> uploadFile(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0080Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0080Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			String clientId = tsmpHttpHeader.getAuthorization().getClientId();
			req.getBody().setClientId(clientId);
			resp = dpb0080Service.uploadFile(req.getBody());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

	/* tsmp-v3 暫不支援 Multipart 格式，故暫不使用
	private Resource getUploadTempDir() {
		Path uploadTempPath = getFileHelper().getTsmpDpApiUploadTemp();
		if (uploadTempPath != null) {
			return new FileSystemResource(uploadTempPath);
		}
		this.logger.warn("Upload temp dir is not available! Use classpath resource dir...");
		return new ClassPathResource("");
	}
	*/

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
