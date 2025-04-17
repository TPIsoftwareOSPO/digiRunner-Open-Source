package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB9916Req;
import tpi.dgrv4.dpaa.vo.DPB9916Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9916Service {

	private final String File_Format = "mail log txt json";
	
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB9916Service(TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}


	public DPB9916Resp queryTsmpDpFileDetail(TsmpAuthorization tsmpAuthorization, DPB9916Req req) {
		
		DPB9916Resp resp = new DPB9916Resp();
		try {
			Long fileId = req.getFileId();
			// 1.若缺少必填參數則拋出 1296 錯誤。
			if (fileId == null) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			Optional<TsmpDpFile> opt_tsmpDpFile = getTsmpDpFileDao().findById(fileId);

			// 2.若查不到資料則拋出 1298 錯誤。
			if (!opt_tsmpDpFile.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			TsmpDpFile tsmpDpFile = null;
			String createDateTimeString;
			
			tsmpDpFile = opt_tsmpDpFile.get();
			
			resp.setFileId(tsmpDpFile.getFileId());
			resp.setRefFileCateCode(tsmpDpFile.getRefFileCateCode());
			resp.setRefId(tsmpDpFile.getRefId());
			resp.setFileName(tsmpDpFile.getFileName());
			resp.setCreateUser(tsmpDpFile.getCreateUser());
			createDateTimeString = DateTimeUtil.dateTimeToString(tsmpDpFile.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
			resp.setCreateDateTime(createDateTimeString);
			resp.setFilePath(tsmpDpFile.getFilePath()+tsmpDpFile.getFileName());
			String isBlob=tsmpDpFile.getIsBlob();
			if (!StringUtils.hasLength(tsmpDpFile.getIsBlob())) {
				isBlob="N";
			}
			resp.setIsBlob(isBlob);
			resp.setVersion(tsmpDpFile.getVersion());

			// 3.判斷是否有更新
			if (StringUtils.hasLength(tsmpDpFile.getUpdateUser())) {
				String updateDateTimeString;
				resp.setUpdateUser(tsmpDpFile.getUpdateUser());
				updateDateTimeString = DateTimeUtil.dateTimeToString(tsmpDpFile.getUpdateDateTime(), DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
				resp.setUpdateDateTime(updateDateTimeString);
			}
			
			// 4.判斷是否有檔案
			byte[] blobData = tsmpDpFile.getBlobData();
			String fileName = tsmpDpFile.getFileName();
			String base64Blob;
			base64Blob = checkBlobData(isBlob, blobData, fileName, tsmpDpFile);
			resp.setBlobData(base64Blob);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	
	private String checkBlobData(String isBlob, byte[] blobData, String fileName, TsmpDpFile tsmpDpFile) {
		String base64Blob = null;
		try {
			if (isBlob.equals("Y") && blobData != null && blobData.length > 0) {
				// 5. 判斷檔案格式是否能預覽
				byte[] byteArray;
				boolean filePreviewResult;
				filePreviewResult = checkFileNameFormat(fileName);
				if (filePreviewResult) {
					byteArray = getFileHelper().download(tsmpDpFile);
					base64Blob = Base64Util.base64URLEncode(byteArray);
				}
			}
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
		return base64Blob;
	}

	/**
	 * 判斷檔案格式是否能預覽方法
	 * true:可預覽
	 * false:無法預覽
	 */
	public boolean checkFileNameFormat(String filename) {
		boolean previewStatus = false;
		try {
			int index = filename.lastIndexOf(".") + 1;
			String fileFormat = filename.substring(index);
			if (File_Format.indexOf(fileFormat) != -1) {
				previewStatus = true;
			}
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			return previewStatus;
		}
		return previewStatus;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
