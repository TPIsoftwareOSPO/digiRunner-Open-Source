package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0080Req;
import tpi.dgrv4.dpaa.vo.DPB0080Resp;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0080Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpClientDao tsmpClientDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0080Service(TsmpDpFileDao tsmpDpFileDao, TsmpClientDao tsmpClientDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpClientDao = tsmpClientDao;
		this.fileHelper = fileHelper;
	}

	/* 因 tsmp-v3 暫不支援 Multipart 格式，故此方法暫不使用
	public void uploadFile(DPB0080Req req) {
		String fileCateCode = req.getFileCateCode();
		TsmpDpFileType fileType = getFileType(fileCateCode);
		if (fileType == null) {
			throw TsmpDpApiRtnCode.NO_FILE_CATE_CODE.throwing();
		}

		Long refId = req.getRefId();
		if (refId == null) {
			throw TsmpDpApiRtnCode.NO_CATE_ID_2.throwing();
		}

		List<MultipartFile> attachFiles = req.getAttachFile();
		if (attachFiles == null || attachFiles.isEmpty()) {
			throw TsmpDpApiRtnCode.NO_INCLUDING_FILE.throwing();
		}

		String clientId = req.getClientId();
		TsmpClient client = getTsmpClient(clientId);
		if (client == null) {
			throw TsmpDpApiRtnCode.NO_MEMBER_INFO.throwing();
		}

		for(MultipartFile attachFile : attachFiles) {
			doUpload(client, fileType, refId, attachFile);
		}
	}
	*/

	public DPB0080Resp uploadFile(DPB0080Req req) {
		String fileCateCode = req.getFileCateCode();
		TsmpDpFileType fileType = getFileType(fileCateCode);
		if (fileType == null) {
			throw TsmpDpAaRtnCode.NO_FILE_CATE_CODE.throwing();
		}

		Long refId = req.getRefId();
		if (refId == null) {
			throw TsmpDpAaRtnCode.NO_CATE_ID_2.throwing();
		}

		String fileName = req.getFileName();
		String attachFile = req.getAttachFile();	// Base64 encoded string
		byte[] fileContent = getFileContent(attachFile);
		if (fileName == null || fileName.isEmpty() || //
			fileContent == null || fileContent.length == 0) {
			throw TsmpDpAaRtnCode.NO_INCLUDING_FILE.throwing();
		}

		String clientId = req.getClientId();
		TsmpClient client = getTsmpClient(clientId);
		if (client == null) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}

		Long fileId = doUpload(client, fileType, refId, fileName, fileContent);
		DPB0080Resp resp = new DPB0080Resp();
		resp.setFileId(fileId);
		return resp;
	}

	private TsmpDpFileType getFileType(String fileCateCode) {
		for(TsmpDpFileType fileType : TsmpDpFileType.values()) {
			if (fileType.value().equals(fileCateCode)) {
				return fileType;
			}
		}
		return null;
	}

	private TsmpClient getTsmpClient(String clientId) {
		if (clientId != null && !clientId.isEmpty()) {
			Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private byte[] getFileContent(String attachFile) {
		if (attachFile == null || attachFile.isEmpty()) {
			return null;
		}
		// image base64
		if (attachFile.indexOf(",") != -1) {
			attachFile = attachFile.split(",")[1];
		}
		return ServiceUtil.base64Decode(attachFile);
	}

	/**
	 * 因 tsmp-v3 暫不支援 Multipart 格式，故此方法暫不使用
	 * 寫磁碟、資料庫
	 * @param clientId
	 * @param tsmpDpFilePath
	 * @param attachFile
	private void doUpload(TsmpClient client, TsmpDpFileType fileType, Long refId, MultipartFile attachFile) {
		try {
			String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(fileType, refId);
			String filename = attachFile.getOriginalFilename();
			// 寫磁碟
			Path uploadFile = getFileHelper().upload(tsmpDpFilePath, filename, attachFile.getBytes());
			// 成功才寫資料庫
			boolean isExists = getFileHelper().exists(tsmpDpFilePath, filename);
			if (uploadFile != null && isExists) {
				TsmpDpFile tsmpDpFile = new TsmpDpFile();
				tsmpDpFile.setFileName(filename);
				tsmpDpFile.setFilePath(tsmpDpFilePath);
				tsmpDpFile.setRefFileCateCode(fileType.value());
				tsmpDpFile.setRefId(refId);
				tsmpDpFile.setCreateDateTime(DateTimeUtil.now());
				tsmpDpFile.setCreateUser(client.getClientName());
				tsmpDpFile = getTsmpDpFileDao().save(tsmpDpFile);
			} else {
				this.logger.debug("Upload fail: [{}\\{}]!", tsmpDpFilePath, filename);
			}
		} catch (IOException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}
	*/

	private Long doUpload(TsmpClient client, TsmpDpFileType fileType, Long refId, String fileName, byte[] fileContent) {
		String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(fileType, refId);
		//boolean isExists = false;
		try {
			TsmpDpFile tsmpDpFile = getFileHelper().upload(client.getClientName(),fileType, refId
					, fileName, fileContent, "N");
			return tsmpDpFile.getFileId();
			// 寫磁碟
			/*Path uploadFile = getFileHelper().upload(tsmpDpFilePath, fileName, fileContent);
			// 成功才寫資料庫
			isExists = getFileHelper().exists(tsmpDpFilePath, fileName);
			if (uploadFile != null && isExists) {
				TsmpDpFile tsmpDpFile = new TsmpDpFile();
				tsmpDpFile.setFileName(uploadFile.getFileName().toString());
				tsmpDpFile.setFilePath(tsmpDpFilePath);
				tsmpDpFile.setRefFileCateCode(fileType.value());
				tsmpDpFile.setRefId(refId);
				tsmpDpFile.setCreateUser(client.getClientName());
				tsmpDpFile = getTsmpDpFileDao().save(tsmpDpFile);
				return tsmpDpFile.getFileId();
			} else {
				this.logger.debug("Upload fail: [{}" + File.separator + "{}]!", tsmpDpFilePath, fileName);
			}*/
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			/*if (isExists) {
				try {
					getFileHelper().remove01(tsmpDpFilePath, fileName, null);
				} catch (Exception ee) {
					this.logger.error("Hard drive disk file rollback error!", ee);
				}
			}*/
		}
		return null;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
