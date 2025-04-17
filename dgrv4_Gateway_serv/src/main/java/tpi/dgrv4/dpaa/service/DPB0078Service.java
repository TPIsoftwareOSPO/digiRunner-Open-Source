package tpi.dgrv4.dpaa.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0078Service {

	private TPILogger logger = TPILogger.tl;

	private FileHelper fileHelper;

	@Autowired
	public DPB0078Service(FileHelper fileHelper) {
		super();
		this.fileHelper = fileHelper;
	}

	private byte[] getFile(String tsmpDpFilePath, String filename) {
		try {
			//DB(新)
			String dbFilePath = tsmpDpFilePath.replaceAll("\\\\", "/") +"/";
			byte[] dpb0078_content = getFileHelper().downloadByPathAndName(dbFilePath, filename);
			
			if(dpb0078_content == null) {
				dbFilePath = tsmpDpFilePath.replaceAll("/", "\\\\") +"\\";
				dpb0078_content = getFileHelper().downloadByPathAndName(dbFilePath, filename);
			}
			
			//DB找不到改成找檔案的
			if(dpb0078_content == null) {
				//檔案(舊)
				return getFileHelper().download01(tsmpDpFilePath, filename);
			}else {
				return dpb0078_content;
			}
			
		} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		return null;
	}
	
	// inputFilePath = 路徑 + 檔名, ex: APP/10/申請說明.docx
	public ResponseEntity<byte[]> downloadFile(String inputFilePath) {
		if (inputFilePath == null || inputFilePath.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_FILE.throwing();
		}

		Path pathWithFilename = null;
		String tsmpDpFilePath = null;
		String filename = null;
		try {
			pathWithFilename = Paths.get(inputFilePath);
			tsmpDpFilePath = pathWithFilename.getParent().toString();
			filename = pathWithFilename.getFileName().toString();
		} catch (Exception e) {
			this.logger.error("Resolve path error: " + inputFilePath);
			throw TsmpDpAaRtnCode.NO_FILE.throwing();
		}

		byte[] content = getFile(tsmpDpFilePath, filename);
		if (content == null || content.length == 0) {
			this.logger.error("Unable to find resource: " + inputFilePath);
			throw TsmpDpAaRtnCode.NO_FILE.throwing();
		}

		String contentType = getContentType(pathWithFilename);
		String contentDisposition = getContentDisposition(filename);

		return ResponseEntity.ok().contentLength(content.length).header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(content);
	}

	/**
	 * Content-Disposition: attachment;
	 * filename="$encoded_fname";
	 * filename*=utf-8''$encoded_fname
	 * @param filename
	 * @return
	 */
	private String getContentDisposition(String filename) {
		int dpb0078_idx = filename.lastIndexOf(".");
		String encoded_fname = filename.substring(0, dpb0078_idx);
		try {
			encoded_fname = URLEncoder.encode(encoded_fname, StandardCharsets.UTF_8.displayName());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		encoded_fname += filename.substring(dpb0078_idx);

		String rtn = "attachment; ";
		rtn += "filename=\"" + encoded_fname + "\"; ";
		rtn += "filename*=utf-8''" + encoded_fname;

		return rtn;
	}
	
	private String getContentType(Path fullFilePath) {
		try {
			return Files.probeContentType(fullFilePath);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		return null;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
