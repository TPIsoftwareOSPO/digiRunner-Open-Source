package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.codec.utils.Base64Util.base64Decode2;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0028Req;
import tpi.dgrv4.dpaa.vo.DPB0028Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqAnswer;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqQuestion;
import tpi.dgrv4.entity.repository.TsmpDpFaqAnswerDao;
import tpi.dgrv4.entity.repository.TsmpDpFaqQuestionDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0028Service {

    private TPILogger logger = TPILogger.tl;

	private TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0028Service(TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao, TsmpDpFileDao tsmpDpFileDao,
			TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFaqAnswerDao = tsmpDpFaqAnswerDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpFaqQuestionDao = tsmpDpFaqQuestionDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0028Resp updateFaqById(TsmpAuthorization authorization, DPB0028Req req) {
		String userName = authorization.getUserName();
		checkParams(userName, req);

		try {
			TsmpDpFaqQuestion newQ = updateQ(userName, req);

			TsmpDpFaqAnswer newA = null;
			if (newQ != null) {
				newA = updateA(userName, req);
			}

			if (newA != null) {
				updateFaqAttachment(userName, req);
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_FAQ.throwing();
		}

		return new DPB0028Resp();
	}

	private void checkParams(String userName, DPB0028Req req) {
		if (
			noInput(userName) ||
			noInput(req.getQuestionId()) ||
			noInput(req.getQuestionName()) ||
			noInput(req.getDataStatus())
		) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_FAQ_REQUIRED.throwing();
		}
		if (!isValidDataStatus(req.getDataStatus())){
			throw TsmpDpAaRtnCode.FAIL_UPDATE_FAQ.throwing();
		}
	}

	private boolean isValidDataStatus(String dataStatus) {
		String text = TsmpDpDataStatus.text(dataStatus);
		return !(text.equals(dataStatus));
	}

	private TsmpDpFaqQuestion updateQ(String userName, DPB0028Req req) {
		Long questionId = req.getQuestionId();
		Optional<TsmpDpFaqQuestion> opt = getTsmpDpFaqQuestionDao().findById(questionId);
		if (opt.isPresent()) {
			TsmpDpFaqQuestion q = opt.get();
			q.setQuestionName(req.getQuestionName());
			q.setDataSort(req.getDataSort());
			q.setDataStatus(req.getDataStatus());
			q.setUpdateDateTime(DateTimeUtil.now());
			q.setUpdateUser(userName);
			return getTsmpDpFaqQuestionDao().save(q);
		}
		return null;
	}

	private TsmpDpFaqAnswer updateA(String userName, DPB0028Req req) {
		Long answerId = req.getAnswerId();
		Optional<TsmpDpFaqAnswer> opt = getTsmpDpFaqAnswerDao().findById(answerId);
		if (opt.isPresent()) {
			TsmpDpFaqAnswer a = opt.get();
			a.setAnswerName(req.getAnswerName());
			a.setUpdateDateTime(DateTimeUtil.now());
			a.setUpdateUser(userName);
			return getTsmpDpFaqAnswerDao().save(a);
		}
		return null;
	}

	private void updateFaqAttachment(String userName, DPB0028Req req) {
		Long refId = req.getAnswerId();

		String newFileName = req.getFileName();
		String newFileContent = req.getFileContent();

		// 是否有新檔案
		boolean isNewFileUploading = hasInput(newFileName) && hasInput(newFileContent);
		// 是否保留舊檔案
		boolean isOldFilePreserved = hasInput(req.getOrgFileId()) || hasInput(req.getOrgFileName());

		try {
			boolean hasOldFileBeenRemoved = false;

			// 移除舊檔案
			if (!isOldFilePreserved) {
				hasOldFileBeenRemoved = removeOldFaqAttachment(refId);
			}

			// 有新檔案
			if (isNewFileUploading) {
				// 卻要保留舊檔案 -> 刪除舊檔案
				if (!hasOldFileBeenRemoved && isOldFilePreserved) {
					hasOldFileBeenRemoved = removeOldFaqAttachment(refId);
				}

				// 上傳新檔案
				byte[] content = base64Decode2(newFileContent);
				if (content != null) {
					TsmpDpFile newDpFile = getFileHelper().upload(userName,TsmpDpFileType.FAQ_ATTACHMENT, refId
							, newFileName, content, "N");
					/*String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.FAQ_ATTACHMENT, refId);
					Path uploadFile = getFileHelper().upload(tsmpDpFilePath, newFileName, content);
					if (uploadFile != null) {
						TsmpDpFile newDpFile = new TsmpDpFile();
						newDpFile.setFileName(uploadFile.getFileName().toString());
						newDpFile.setFilePath(tsmpDpFilePath);
						newDpFile.setRefFileCateCode(TsmpDpFileType.FAQ_ATTACHMENT.value());
						newDpFile.setRefId(refId);
						newDpFile.setCreateUser(userName);
						getTsmpDpFileDao().save(newDpFile);
					}*/
				}
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private boolean removeOldFaqAttachment(Long refId) {
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.FAQ_ATTACHMENT.value(), refId);
		if (fileList != null && !fileList.isEmpty()) {
			for(TsmpDpFile oldFile : fileList) {
				try {
					if("Y".equals(oldFile.getIsBlob()) && oldFile.getBlobData() != null) {
						getTsmpDpFileDao().deleteById(oldFile.getFileId());
					}else {
						getFileHelper().remove01(oldFile.getFilePath(), oldFile.getFileName(), (filename) -> {
							getTsmpDpFileDao().deleteById(oldFile.getFileId());
						});
					}
					
				} catch (Exception e) {
					this.logger.error(StackTraceUtil.logStackTrace(e));
				}
			}
		}
		return true;
	}

	private boolean hasInput(Object input) {
		return !noInput(input);
	}

	private boolean noInput(Object input) {
		return (input == null || input.toString().isEmpty());
	}

	protected TsmpDpFaqQuestionDao getTsmpDpFaqQuestionDao() {
		return this.tsmpDpFaqQuestionDao;
	}

	protected TsmpDpFaqAnswerDao getTsmpDpFaqAnswerDao() {
		return this.tsmpDpFaqAnswerDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

}
