package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0007Req;
import tpi.dgrv4.dpaa.vo.DPB0007Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0007Service {

	private TsmpDpAppCategoryDao tsmpDpAppCategoryDao;

	@Autowired
	public DPB0007Service(TsmpDpAppCategoryDao tsmpDpAppCategoryDao) {
		super();
		this.tsmpDpAppCategoryDao = tsmpDpAppCategoryDao;
	}

	public DPB0007Resp addAppCate(TsmpAuthorization authorization, DPB0007Req req) {
		String userName = authorization.getUserName();
		String orgId = authorization.getOrgId();
		if (userName == null || userName.isEmpty() ||
			orgId == null || orgId.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_TSMP_USER.throwing();
		}

		String appCateName = req.getAppCateName();
		if (appCateName == null || appCateName.isEmpty()) {
			throw TsmpDpAaRtnCode.EMPTY_CATE_NAME.throwing();
		}

		TsmpDpAppCategory entity = new TsmpDpAppCategory();
		entity.setAppCateName(appCateName);
		entity.setCreateUser(userName);
		entity.setOrgId(orgId);
		entity = getTsmpDpAppCategoryDao().save(entity);

		return new DPB0007Resp();
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpAppCategoryDao;
	}

}
