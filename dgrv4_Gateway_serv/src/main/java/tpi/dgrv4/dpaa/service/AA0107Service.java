package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0107Req;
import tpi.dgrv4.dpaa.vo.AA0107Resp;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0107Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	private TsmpReportUrlDao tsmpReportUrlDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;

	@Autowired
	public AA0107Service(TsmpFuncDao tsmpFuncDao, TsmpReportUrlDao tsmpReportUrlDao, TsmpRoleFuncDao tsmpRoleFuncDao) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpReportUrlDao = tsmpReportUrlDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
	}

	@Transactional
	public AA0107Resp deleteReport(TsmpAuthorization auth, AA0107Req req) {
		AA0107Resp resp = new AA0107Resp();
		try {
			String funcCode = req.getFuncCode();
			String locale = req.getLocale();
			checkParams(funcCode, locale);

			// 刪除 TsmpFunc
			List<TsmpFunc> tsmpFuncs = getTsmpFuncDao().findByFuncCode(funcCode);
			if (CollectionUtils.isEmpty(tsmpFuncs)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			if ("1".equals(tsmpFuncs.get(0).getFuncType())) {				
				if(funcCode.length() == 4 && funcCode.startsWith("ZA")) {
					tsmpFuncs = getTsmpFuncDao().findByFuncCodeStartsWith(funcCode);
					getTsmpFuncDao().deleteAll(tsmpFuncs);
					// 刪除 TsmpReportUrl
					List<TsmpReportUrl> tsmpReportUrl = getTsmpReportUrlDao().findByReportIdStartsWith(funcCode);
					getTsmpReportUrlDao().deleteAll(tsmpReportUrl);
					// 刪除 TsmpRoleFunc
					List<TsmpRoleFunc> rfList = getTsmpRoleFuncDao().findByFuncCodeStartsWith(funcCode);
					getTsmpRoleFuncDao().deleteAll(rfList);					
				}else {
					getTsmpFuncDao().deleteAll(tsmpFuncs);
					// 刪除 TsmpReportUrl
					List<TsmpReportUrl> tsmpReportUrl = getDeleteTsmpReportUrl(funcCode);
					getTsmpReportUrlDao().deleteAll(tsmpReportUrl);
					// 刪除 TsmpRoleFunc
					List<TsmpRoleFunc> rfList = getTsmpRoleFuncDao().findByFuncCode(funcCode);
					getTsmpRoleFuncDao().deleteAll(rfList);
				}
			} else {
				throw TsmpDpAaRtnCode._1219.throwing();
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing(); // 更新錯誤
		}
		return resp;

	}

	private List<TsmpReportUrl> getDeleteTsmpReportUrl(String funcCode) {
		List<TsmpReportUrl> tsmpReportUrls = getTsmpReportUrlDao().findByReportId(funcCode);

		if (CollectionUtils.isEmpty(tsmpReportUrls)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		TsmpReportUrl tsmpReportUrl = tsmpReportUrls.get(0);

		String reportUrl = tsmpReportUrl.getReportUrl();
		if (!StringUtils.hasLength(reportUrl)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return tsmpReportUrls;
	}

	private void checkParams(String funcCode, String locale) {
		if (!StringUtils.hasLength(funcCode)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasLength(locale)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return this.tsmpFuncDao;
	}

	protected TsmpRoleFuncDao getTsmpRoleFuncDao() {
		return this.tsmpRoleFuncDao;
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}
}
