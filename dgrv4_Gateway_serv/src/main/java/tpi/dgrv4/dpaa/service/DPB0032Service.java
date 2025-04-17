package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0032Req;
import tpi.dgrv4.dpaa.vo.DPB0032Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAbout;
import tpi.dgrv4.entity.repository.TsmpDpAboutDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0032Service {

	private TsmpDpAboutDao tsmpDpAboutDao;

	@Autowired
	public DPB0032Service(TsmpDpAboutDao tsmpDpAboutDao) {
		super();
		this.tsmpDpAboutDao = tsmpDpAboutDao;
	}

	public DPB0032Resp queryAbout_0(TsmpAuthorization authorization, DPB0032Req req) {
		TsmpDpAbout about = getAbout();
		DPB0032Resp resp = new DPB0032Resp();
		if (about != null) {
			resp.setSeqId(about.getSeqId());
			resp.setAboutSubject(about.getAboutSubject());
			resp.setAboutDesc(about.getAboutDesc());
		}
		return resp;
	}

	private TsmpDpAbout getAbout() {
		List<TsmpDpAbout> aboutList = getTsmpDpAboutDao().findAll();
		if (aboutList == null || aboutList.isEmpty()) {
			throw TsmpDpAaRtnCode._1117.throwing();
		}
		// 最多應該只能有一筆資料
		if (aboutList.size() != 1) {
			throw TsmpDpAaRtnCode.FAIL_QUERY_ABOUT_MULTI_DATA.throwing();
		}
		return aboutList.get(0);
	}

	protected TsmpDpAboutDao getTsmpDpAboutDao() {
		return this.tsmpDpAboutDao;
	}

}
