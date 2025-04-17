package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9918Req;
import tpi.dgrv4.dpaa.vo.DPB9918Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9918Service {

	private TsmpDpFileDao tsmpDpFileDao;
	private BcryptParamHelper bcryptParamHelper;
	private FileHelper fileHelper;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB9918Service(TsmpDpFileDao tsmpDpFileDao, BcryptParamHelper bcryptParamHelper, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB9918Resp updateTsmpDpFile(TsmpAuthorization tsmpAuthorization, DPB9918Req req, ReqHeader reqHeader) {

		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String updateUser = tsmpAuthorization.getUserName();
			Date updateDateTime = DateTimeUtil.now();

			// 若缺少必填參數則拋出 1296 錯誤。
			Long fileId = req.getFileId();
			Long version = req.getVersion();
			if (fileId == null || version == null) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			// refFileCateCode解密
			String encodeRefFileCateCode = req.getRefFileCateCode();
			String refFileCateCode = decodeRefFileCateCode(encodeRefFileCateCode, locale);

			// 確定資料是否存在
			TsmpDpFile tsmpDpFile = checkInformationState(fileId);

			// 確認更新後是否為唯一一筆
			String fileName = req.getFileName();
			Long refId = req.getRefId();
			checkInformationOnlyOne(fileId, fileName, refFileCateCode, refId);

			// 是否需要更新或刪除
			String reqIsBlob = req.getIsBlob();
			String tmpFileName = req.getTmpFileName();
			tsmpDpFile = checkIsBlobstate(reqIsBlob, tmpFileName, tsmpDpFile);

			// 更新
			updateTsmpDpFile(tsmpDpFile, fileName, refFileCateCode, refId, reqIsBlob, updateUser, updateDateTime,
					version);

			// 刪除暫存擋
			if (StringUtils.hasLength(tmpFileName)) {
				getTsmpDpFileDao().deleteByfileName(tmpFileName);
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		DPB9918Resp resp = new DPB9918Resp();
		return resp;
	}

	private TsmpDpFile checkIsBlobstate(String reqIsBlob, String tmpFileName, TsmpDpFile tsmpDpFile) {
		// 更新&新增檔案
		if (StringUtils.hasLength(tmpFileName)) {

			byte[] newBlobData = null;
			newBlobData = checkInformationExist(tmpFileName, newBlobData);
			tsmpDpFile.setBlobData(newBlobData);

			// 刪除檔案
		} else if (reqIsBlob.equals("N")) {
			tsmpDpFile.setBlobData(null);
			// 不更新檔案
		} else {
			return tsmpDpFile;
		}

		return tsmpDpFile;
	}

	private byte[] checkInformationExist(String tmpFileName, byte[] newBlobData) {
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByFileName(tmpFileName);
		if (CollectionUtils.isEmpty(fileList)) {
			logger.error("check db tmpFile is not Exist");
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		byte[] arrBlobData = fileList.get(0).getBlobData();

		return arrBlobData;
	}

	private void checkInformationOnlyOne(Long fileId, String fileName, String refFileCateCode, Long refId) {
		Long count = getTsmpDpFileDao().countByFileNameAndRefIdAndRefFileCateCodeAndFileIdNot(fileName, refId,
				refFileCateCode, fileId);
		if (count > 0) {
			throw TsmpDpAaRtnCode._1353.throwing(false, "fileName", fileName);
		}
	}

	private TsmpDpFile checkInformationState(Long fileId) {
		Optional<TsmpDpFile> tsmpDpFile = getTsmpDpFileDao().findById(fileId);
		if (!tsmpDpFile.isPresent()) {
			logger.error("check DB fileId Information is null");
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return tsmpDpFile.get();
	}

	private void updateTsmpDpFile(TsmpDpFile tsmpDpFile, String fileName, String refFileCateCode, Long refId,
			String reqIsBlob, String updateUser, Date updateDateTime, Long version) throws SQLException {
		TsmpDpFile newTsmpDpFile = new TsmpDpFile();
		byte[] bytes = getFileHelper().download(tsmpDpFile);

		newTsmpDpFile.setFileId(tsmpDpFile.getFileId());
		newTsmpDpFile.setFileName(fileName);
		newTsmpDpFile.setFilePath(Paths.get(refFileCateCode, String.valueOf(refId)).toString() + File.separator);
		newTsmpDpFile.setRefFileCateCode(refFileCateCode);
		newTsmpDpFile.setRefId(refId);
		newTsmpDpFile.setIsBlob(reqIsBlob);
		newTsmpDpFile.setIsTmpfile(tsmpDpFile.getIsTmpfile());
		newTsmpDpFile.setBlobData(bytes);
		newTsmpDpFile.setCreateDateTime(tsmpDpFile.getCreateDateTime());
		newTsmpDpFile.setCreateUser(tsmpDpFile.getCreateUser());
		newTsmpDpFile.setUpdateDateTime(updateDateTime);
		newTsmpDpFile.setUpdateUser(updateUser);
		newTsmpDpFile.setVersion(version);

		try {

			getTsmpDpFileDao().save(newTsmpDpFile);

		} catch (ObjectOptimisticLockingFailureException e) {
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				throw e;
			}
		}
	}

	protected String decodeRefFileCateCode(String refFileCateCode, String locale) {
		if (StringUtils.hasLength(refFileCateCode)) {
			try {
				refFileCateCode = getBcryptParamHelper().decode(refFileCateCode, "FILE_CATE_CODE", locale);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		return refFileCateCode;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
