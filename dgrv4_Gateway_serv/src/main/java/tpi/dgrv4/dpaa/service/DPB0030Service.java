package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0030Req;
import tpi.dgrv4.dpaa.vo.DPB0030Resp;
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
public class DPB0030Service {

    private TPILogger logger = TPILogger.tl;

	private TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao;
	private TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0030Service(TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao, TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFaqQuestionDao = tsmpDpFaqQuestionDao;
		this.tsmpDpFaqAnswerDao = tsmpDpFaqAnswerDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0030Resp deleteFaqById(TsmpAuthorization authorization, DPB0030Req req) {
		try {
			Long questionId = req.getQuestionId();
			TsmpDpFaqQuestion q = getQ(questionId);
			if (q == null) {
				throw new Exception("Question ID(" + req.getQuestionId() + ") does not exist!");
			}
			questionId = q.getQuestionId();

			// 狀態為啟用不可刪除
			if (TsmpDpDataStatus.ON.value().equals(q.getDataStatus())) {
				throw TsmpDpAaRtnCode.ERROR_DELETE_ACTIVE_DATA.throwing();
			}

			// 先刪關聯表
			List<TsmpDpFaqAnswer> aList = getTsmpDpFaqAnswerDao().findByRefQuestionId(questionId);
			if (aList != null && !aList.isEmpty()) {
				aList.forEach((a) -> {
					getTsmpDpFaqAnswerDao().deleteById(a.getAnswerId());
					deleteFaqAttachment(a.getAnswerId());
				});
			}

			// 再刪主表
			getTsmpDpFaqQuestionDao().deleteById(questionId);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_DELETE_FAQ.throwing();
		}

		return new DPB0030Resp();
	}

	private TsmpDpFaqQuestion getQ(Long questionId) {
		if (questionId != null) {
			Optional<TsmpDpFaqQuestion> opt = getTsmpDpFaqQuestionDao().findById(questionId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private void deleteFaqAttachment(Long refId) {
		try {
			//檔案(舊)
			// 一個問答只會有一個附件
			getFileHelper().remove01(TsmpDpFileType.FAQ_ATTACHMENT, refId, null, (filename) -> {
				List<TsmpDpFile> fileList = getTsmpDpFileDao() //
						.findByRefFileCateCodeAndRefIdAndFileName( //
								TsmpDpFileType.FAQ_ATTACHMENT.value(), refId, filename);
				if (fileList != null && !fileList.isEmpty()) {
					fileList.forEach((file) -> {
						getTsmpDpFileDao().delete(file);
					});
				}
			});
			
			//DB(新)
			List<TsmpDpFile> fileList = getTsmpDpFileDao() //
					.findByRefFileCateCodeAndRefId( //
							TsmpDpFileType.FAQ_ATTACHMENT.value(), refId);
			if (fileList != null && !fileList.isEmpty()) {
				fileList.forEach((file) -> {
					getTsmpDpFileDao().delete(file);
				});
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	protected TsmpDpFaqQuestionDao getTsmpDpFaqQuestionDao() {
		return this.tsmpDpFaqQuestionDao;
	}

	protected TsmpDpFaqAnswerDao getTsmpDpFaqAnswerDao() {
		return this.tsmpDpFaqAnswerDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
