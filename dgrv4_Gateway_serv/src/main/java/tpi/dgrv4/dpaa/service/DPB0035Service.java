package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0035Req;
import tpi.dgrv4.dpaa.vo.DPB0035Resp;
import tpi.dgrv4.entity.repository.TsmpDpSiteMapDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0035Service {
	
	private TsmpDpSiteMapDao tsmpDpSiteMapDao;

	@Autowired
	public DPB0035Service(TsmpDpSiteMapDao tsmpDpSiteMapDao) {
		super();
		this.tsmpDpSiteMapDao = tsmpDpSiteMapDao;
	}

	public DPB0035Resp deleteNodeById(TsmpAuthorization authorization, DPB0035Req req) {
		Long siteId = req.getSiteId();
		if (siteId == null) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_NODE_REQUIRED.throwing();
		}

		boolean isExists = getTsmpDpSiteMapDao().existsById(siteId);
		if (!isExists) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_NODE.throwing();
		}

		try {
			getTsmpDpSiteMapDao().deleteById(siteId);
		} catch (Exception e) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_NODE.throwing();
		}

		return new DPB0035Resp();
	}

	protected TsmpDpSiteMapDao getTsmpDpSiteMapDao() {
		return this.tsmpDpSiteMapDao;
	}

}
