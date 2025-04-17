package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB0031Req;
import tpi.dgrv4.dpaa.vo.DPB0031Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAbout;
import tpi.dgrv4.entity.repository.TsmpDpAboutDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0031Service {

	private TsmpDpAboutDao tsmpDpAboutDao;

	@Autowired
	public DPB0031Service(TsmpDpAboutDao tsmpDpAboutDao) {
		super();
		this.tsmpDpAboutDao = tsmpDpAboutDao;
	}

	public DPB0031Resp saveAbout(TsmpAuthorization authorization, DPB0031Req req) {
		String userName = authorization.getUserName();
		checkParams(userName, req);

		try {
			saveAbout(userName, req.getSeqId(), req.getAboutSubject(), req.getAboutDesc());
		} catch (Exception e) {
			throw TsmpDpAaRtnCode.FAIL_SAVE_ABOUT.throwing();
		}
		
		return new DPB0031Resp();
	}

	private void checkParams(String userName, DPB0031Req req) {
		if (noInput(userName) ||
			noInput(req.getAboutSubject()) ||
			noInput(req.getAboutDesc())
		) {
			throw TsmpDpAaRtnCode.FAIL_SAVE_ABOUT_REQUIRED.throwing();
		}
	}

	private void saveAbout(String userName, Long seqId, String aboutSubject, String aboutDesc) throws Exception {
		// 2020/07/15, Mini修改, Seq Id 已無作用
		
		List<TsmpDpAbout> aboutList = getTsmpDpAboutDao().findAll();
		TsmpDpAbout about = null;
		if(aboutList != null && !aboutList.isEmpty()) {
			about = aboutList.get(0);//永遠只會有一筆
		}
		
		if (about == null) {
			about = new TsmpDpAbout();
			about.setCreateDateTime(DateTimeUtil.now());
			about.setCreateUser(userName);
		} else {
			about.setUpdateDateTime(DateTimeUtil.now());
			about.setUpdateUser(userName);
		}
		about.setAboutSubject(aboutSubject);
		about.setAboutDesc(aboutDesc);
		getTsmpDpAboutDao().save(about);
	}

	private boolean noInput(Object input) {
		return (input == null || input.toString().isEmpty());
	}

	protected TsmpDpAboutDao getTsmpDpAboutDao() {
		return this.tsmpDpAboutDao;
	}

}
