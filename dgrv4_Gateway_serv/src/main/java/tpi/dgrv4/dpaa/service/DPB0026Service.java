package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.codec.utils.Base64Util.base64Decode2;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0026Req;
import tpi.dgrv4.dpaa.vo.DPB0026Resp;
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
public class DPB0026Service {

    private TPILogger logger = TPILogger.tl;

	private TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao;
	private TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0026Service(TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao, TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFaqAnswerDao = tsmpDpFaqAnswerDao;
		this.tsmpDpFaqQuestionDao = tsmpDpFaqQuestionDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0026Resp addFaq(TsmpAuthorization authorization, DPB0026Req req) {
		String userName = authorization.getUserName();
		checkParams(userName, req);

		TsmpDpFaqQuestion q = createQ(userName, req.getQuestionName(), req.getDataSort(), req.getDataStatus());
		
		TsmpDpFaqAnswer a = null;
		if (q != null && q.getQuestionId() != null) {
			a = createA(userName, q.getQuestionId(), req.getAnswerName());
		}

		if (a != null && a.getAnswerId() != null) {
			uploadFaqAttachment(userName, a.getAnswerId(), req.getFileName(), req.getFileContent());
		}
		
		return new DPB0026Resp();
	}

	private void checkParams(String userName, DPB0026Req req) {
		if (
			noInput(userName) ||
			noInput(req.getQuestionName()) ||
			noInput(req.getDataStatus()) || !isValidDataStatus(req.getDataStatus()) ||
			noInput(req.getAnswerName()) ||
			noInput(req.getFileName()) ||
			noInput(req.getFileContent())
		) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_FAQ_REQUIRED.throwing();
		}
	}

	private boolean isValidDataStatus(String dataStatus) {
		String text = TsmpDpDataStatus.text(dataStatus);
		return !(text.equals(dataStatus));
	}

	private TsmpDpFaqQuestion createQ(String userName, String questionName, Integer dataSort, String dataStatus) {
		TsmpDpFaqQuestion q = new TsmpDpFaqQuestion();
		q.setQuestionName(questionName);
		q.setDataSort(dataSort);
		q.setDataStatus(dataStatus);
		q.setCreateDateTime(DateTimeUtil.now());
		q.setUpdateDateTime(DateTimeUtil.now());
		q.setCreateUser(userName);
		q = getTsmpDpFaqQuestionDao().save(q);
		return q;
	}

	private TsmpDpFaqAnswer createA(String userName, Long refQuestionId, String answerName) {
		TsmpDpFaqAnswer a = new TsmpDpFaqAnswer();
		a.setRefQuestionId(refQuestionId);
		a.setAnswerName(answerName);
		a.setCreateDateTime(DateTimeUtil.now());
		a.setUpdateDateTime(DateTimeUtil.now());
		a.setCreateUser(userName);
		a = getTsmpDpFaqAnswerDao().save(a);
		return a;
	}

	private void uploadFaqAttachment(String userName, Long answerId, String filename, String fileContent) {
		try {
			byte[] content = base64Decode2(fileContent);
			if (content != null) {
				TsmpDpFile dpFile = getFileHelper().upload(userName,TsmpDpFileType.FAQ_ATTACHMENT, answerId
						, filename, content, "N");
				/*String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.FAQ_ATTACHMENT, answerId);
				Path uploadFile = getFileHelper().upload(tsmpDpFilePath, filename, content);
				if (uploadFile != null) {
					TsmpDpFile dpFile = new TsmpDpFile();
					dpFile.setFileName(uploadFile.getFileName().toString());
					dpFile.setFilePath(tsmpDpFilePath);
					dpFile.setRefFileCateCode(TsmpDpFileType.FAQ_ATTACHMENT.value());
					dpFile.setRefId(answerId);
					dpFile.setCreateUser(userName);
					getTsmpDpFileDao().save(dpFile);
				}*/
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private boolean noInput(String input) {
		return (input == null || input.isEmpty());
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
