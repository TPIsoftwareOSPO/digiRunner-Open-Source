package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.dpaa.vo.DPB0029Req;
import tpi.dgrv4.dpaa.vo.DPB0029Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqAnswer;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqQuestion;
import tpi.dgrv4.entity.repository.TsmpDpFaqAnswerDao;
import tpi.dgrv4.entity.repository.TsmpDpFaqQuestionDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0029Service {

	private TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao;
	private TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao;
	private TsmpDpFileDao tsmpDpFileDao;

	@Autowired
	public DPB0029Service(TsmpDpFaqQuestionDao tsmpDpFaqQuestionDao, TsmpDpFaqAnswerDao tsmpDpFaqAnswerDao,
			TsmpDpFileDao tsmpDpFileDao) {
		super();
		this.tsmpDpFaqQuestionDao = tsmpDpFaqQuestionDao;
		this.tsmpDpFaqAnswerDao = tsmpDpFaqAnswerDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	public DPB0029Resp queryFaqById(TsmpAuthorization authorization, DPB0029Req req) {
		TsmpDpFaqQuestion q = getQ(req.getQuestionId());
		if (q == null) {
			throw TsmpDpAaRtnCode.NO_FAQ_ID.throwing();
		}

		TsmpDpFaqAnswer a = getA(q);
		TsmpDpFile attachment = getAttachment(a);
		DPB0029Resp resp = new DPB0029Resp();
		resp.setQuestionId(q.getQuestionId());
		resp.setQuestionName(q.getQuestionName());
		if (a != null) {
			resp.setAnswerId(a.getAnswerId());
			resp.setAnswerName(a.getAnswerName());
		}
		resp.setDataSort(q.getDataSort());
		if (attachment != null) {
			resp.setOrgFileId(attachment.getFileId());
			resp.setOrgFileName(attachment.getFileName());
		}
		resp.setDataStatus(TsmpDpDataStatus.text(q.getDataStatus()));
		return resp;
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

	private TsmpDpFile getAttachment(TsmpDpFaqAnswer a) {
		if (a != null) {
			List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
					TsmpDpFileType.FAQ_ATTACHMENT.value(), a.getAnswerId());
			if (fileList != null && !fileList.isEmpty()) {
				return fileList.get(0);
			}
		}
		return null;
	}

	private TsmpDpFaqAnswer getA(TsmpDpFaqQuestion q) {
		List<TsmpDpFaqAnswer> aList = getTsmpDpFaqAnswerDao().findByRefQuestionId(q.getQuestionId());
		if (aList != null && !aList.isEmpty()) {
			return aList.get(0);
		}
		return null;
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

}
