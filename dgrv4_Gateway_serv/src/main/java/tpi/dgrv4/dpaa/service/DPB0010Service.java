package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0010Req;
import tpi.dgrv4.dpaa.vo.DPB0010Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0010Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpAppCategoryDao tsmpDpAppCategoryDao;

	@Autowired
	public DPB0010Service(TsmpDpAppCategoryDao tsmpDpAppCategoryDao) {
		super();
		this.tsmpDpAppCategoryDao = tsmpDpAppCategoryDao;
	}

	public DPB0010Resp updateAppCateById(TsmpAuthorization authorization, DPB0010Req req) {
		String userName = authorization.getUserName();
		if (userName == null || userName.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_TSMP_USER.throwing();
		}

		Long appCateId = req.getAppCateId();
		String appCateName = req.getAppCateName();
		if (appCateId == null || appCateName == null || appCateName.isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_APP_CATE.throwing();
		}

		TsmpDpAppCategory cate = getCate(appCateId);
		if (cate == null) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_APP_CATE.throwing();
		}

		try {
			cate.setAppCateName(appCateName);
			cate.setUpdateDateTime(DateTimeUtil.now());
			cate.setUpdateUser(userName);
			cate = getTsmpDpAppCategoryDao().save(cate);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_APP_CATE.throwing();
		}

		return new DPB0010Resp();
	}

	private TsmpDpAppCategory getCate(Long appCateId) {
		Optional<TsmpDpAppCategory> opt = getTsmpDpAppCategoryDao().findById(appCateId);
		return opt.orElse(null);
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpAppCategoryDao;
	}

}
