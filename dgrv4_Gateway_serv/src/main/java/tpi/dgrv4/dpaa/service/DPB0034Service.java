package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0034Req;
import tpi.dgrv4.dpaa.vo.DPB0034Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpSiteMap;
import tpi.dgrv4.entity.repository.TsmpDpSiteMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0034Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpSiteMapDao tsmpDpSiteMapDao;

	@Autowired
	public DPB0034Service(TsmpDpSiteMapDao tsmpDpSiteMapDao) {
		super();
		this.tsmpDpSiteMapDao = tsmpDpSiteMapDao;
	}

	public DPB0034Resp updateNodeById(TsmpAuthorization authorization, DPB0034Req req) {
		String userName = authorization.getUserName();
		checkParams(userName, req);

		try {
			Optional<TsmpDpSiteMap> opt = getTsmpDpSiteMapDao().findById(req.getSiteId());
			if (!opt.isPresent()) {
				throw new Exception("Site ID(" + req.getSiteId() + ") does not exist!");
			}

			TsmpDpSiteMap node = opt.get();
			node.setSiteDesc(req.getSiteDesc());
			node.setSiteUrl(req.getSiteUrl());
			node.setUpdateDateTime(DateTimeUtil.now());
			node.setUpdateUser(userName);
			node = getTsmpDpSiteMapDao().save(node);

			return new DPB0034Resp();
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_NODE.throwing();
		}
	}

	private void checkParams(String userName, DPB0034Req req) {
		if (
			noInput(userName) ||
			noInput(req.getSiteId()) ||
			noInput(req.getSiteDesc()) ||
			noInput(req.getSiteUrl())
		) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_NODE_REQUIRED.throwing();
		}
	}

	private boolean noInput(Object input) {
		return (input == null || input.toString().isEmpty());
	}

	protected TsmpDpSiteMapDao getTsmpDpSiteMapDao() {
		return this.tsmpDpSiteMapDao;
	}

}
