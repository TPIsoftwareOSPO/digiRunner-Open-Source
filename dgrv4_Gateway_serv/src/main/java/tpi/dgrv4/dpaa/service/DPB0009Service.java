package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB0009Req;
import tpi.dgrv4.dpaa.vo.DPB0009Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0009Service {

	private TsmpDpAppCategoryDao tsmpDpAppCategoryDao;

	@Autowired
	public DPB0009Service(TsmpDpAppCategoryDao tsmpDpAppCategoryDao) {
		super();
		this.tsmpDpAppCategoryDao = tsmpDpAppCategoryDao;
	}

	public DPB0009Resp queryAppCateById(TsmpAuthorization authorization, DPB0009Req req) {
		String userName = authorization.getUserName();
		if (userName == null || userName.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_TSMP_USER.throwing();
		}

		Long appCateId = req.getAppCateId();
		if (appCateId == null) {
			throw TsmpDpAaRtnCode.NO_APP_CATE_DATA_BY_CODE.throwing();
		}

		TsmpDpAppCategory category = getCategory(appCateId);
		if (category == null) {
			throw TsmpDpAaRtnCode.NO_APP_CATE_DATA_BY_CODE.throwing();
		}

		DPB0009Resp dpb0009Resp = new DPB0009Resp();
		dpb0009Resp.setAppCateId(category.getAppCateId());
		dpb0009Resp.setAppCateName(category.getAppCateName());
		dpb0009Resp.setDataSort(category.getDataSort());
		
		Date date = category.getCreateDateTime();
		Optional<String> opt = DateTimeUtil.dateTimeToString(date, null);
		if (opt.isPresent()) {
			dpb0009Resp.setCreateDateTime(opt.get());
		}

		date = category.getUpdateDateTime();
		opt = DateTimeUtil.dateTimeToString(date, null);
		if (opt.isPresent()) {
			dpb0009Resp.setUpdateDateTime(opt.get());
		}
		return dpb0009Resp;
	}

	private TsmpDpAppCategory getCategory(Long appCateId) {
		Optional<TsmpDpAppCategory> opt = getTsmpDpAppCategoryDao().findById(appCateId);
		return opt.orElse(null);
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpAppCategoryDao;
	}

}
