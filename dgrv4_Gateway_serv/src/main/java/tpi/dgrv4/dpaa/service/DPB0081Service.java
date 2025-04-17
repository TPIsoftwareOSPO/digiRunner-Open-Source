package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0081Req;
import tpi.dgrv4.dpaa.vo.DPB0081Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0081Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0081Service(TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@SuppressWarnings("unused")
	public DPB0081Resp deleteFile(DPB0081Req req) {
		Long fileId = req.getFileId();
		if (fileId == null) {
			throw TsmpDpAaRtnCode.NO_FILE_ID.throwing();
		}

		Optional<TsmpDpFile> opt = getTsmpDpFileDao().findById(fileId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode.NO_FILE_ID.throwing();
		}

		TsmpDpFile tsmpDpFile = opt.get();
		String filePath = tsmpDpFile.getFilePath();
		String filename = tsmpDpFile.getFileName();
		try {
			if("Y".equals(tsmpDpFile.getIsBlob()) && tsmpDpFile.getBlobData() != null) {
				deleteFromDb(fileId);
			}else {
				boolean isExistsInHdd = getFileHelper().exists(filePath, filename);

				boolean isDeleteHddSuccess = false;
				if (isExistsInHdd) {
					isDeleteHddSuccess = deleteFromHardDiskDrive(filePath, filename);
				}

				boolean isDeleteDbSuccess = false;
				if (!isExistsInHdd || (isExistsInHdd && isDeleteHddSuccess)) {
					isDeleteDbSuccess = deleteFromDb(fileId);
				}
			}
			
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		
		return new DPB0081Resp();
	}

	private boolean deleteFromHardDiskDrive(String tsmpDpFilePath, String filename) {
		try {
			getFileHelper().remove01(tsmpDpFilePath, filename, null);
			return !getFileHelper().exists(tsmpDpFilePath, filename);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		return false;
	}

	private boolean deleteFromDb(Long fileId) {
		getTsmpDpFileDao().deleteById(fileId);
		boolean isExist = getTsmpDpFileDao().existsById(fileId);
		return !isExist;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
