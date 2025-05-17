package tpi.dgrv4.dpaa.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0102Req;
import tpi.dgrv4.dpaa.vo.AA0102Resp;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpFuncId;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrlId;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0102Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	private TsmpReportUrlDao tsmpReportUrlDao;

	@Autowired
	public AA0102Service(TsmpFuncDao tsmpFuncDao, TsmpReportUrlDao tsmpReportUrlDao) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpReportUrlDao = tsmpReportUrlDao;
	}

	public AA0102Resp updateTFunc(TsmpAuthorization auth, AA0102Req req) {

		AA0102Resp resp = new AA0102Resp();

		try {
			checkParams(req);

			updateTsmpFuncTable(auth, req);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing(); // 更新錯誤
		}
		return resp;
	}

	protected void checkParams(AA0102Req req) throws Exception {

		checkFuncCode(req);
		checkLocale(req);

		TsmpFunc tsmpFunc = getTsmpFunc(req);
		// 1298:查無資料
		if (tsmpFunc == null) {
			throw TsmpDpAaRtnCode._1298.throwing();

		}

		checkNewDesc(req);
		checkNewFuncName(req);
		checkNewReportUrl(req);
	}

	/**
	 * 1351:報表url:[{{0}}] 長度限制 [{{1}}] 字內，您輸入[{{2}}] 個字
	 * 
	 * @param req
	 */
	private void checkNewReportUrl(AA0102Req req) {
		String newReportUrl = req.getNewReportUrl();
		if (StringUtils.hasLength(newReportUrl) && newReportUrl.length() > 2000) {
			int length = newReportUrl.length();
			String msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1351.throwing("{{newReportUrl}}","2000", msg);
		}

	}

	private TsmpFunc getTsmpFunc(AA0102Req req) {
		String funcCode = ServiceUtil.nvl(req.getFuncCode());
		String local = ServiceUtil.nvl(req.getLocale());
		TsmpFunc tsmpFunc = null;
		TsmpFuncId funcId = new TsmpFuncId(funcCode, local);
		Optional<TsmpFunc> opt_tsmpFunc = getTsmpFuncDao().findById(funcId);

		if (opt_tsmpFunc.isPresent()) {
			tsmpFunc = opt_tsmpFunc.get();
		}

		return tsmpFunc;
	}

	/**
	 * 1314:功能代碼:必填參數
	 * 
	 * @param req
	 */
	private void checkFuncCode(AA0102Req req) {
		String funcCode = req.getFuncCode();

		// 1314:功能代碼:必填參數
		if (StringUtils.isEmpty(funcCode)) {
			throw TsmpDpAaRtnCode._1314.throwing();
		}

	}

	/**
	 * 1316:語系:必填參數
	 * 
	 * @param req
	 */
	private void checkLocale(AA0102Req req) {
		String locale = req.getLocale();

		// 1316:語系:必填參數
		if (StringUtils.isEmpty(locale)) {
			throw TsmpDpAaRtnCode._1316.throwing();
		}

	}

	/**
	 * 1318:功能描述:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param req
	 */
	private void checkNewDesc(AA0102Req req) {
		String newDesc = req.getNewDesc();

		if (newDesc.length() > 300) {
			int length = newDesc.length();
			String msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1318.throwing("300", msg);
		}
	}

	/**
	 * 1319:功能名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param req
	 */
	private void checkNewFuncName(AA0102Req req) {
		String newFuncName = req.getNewFuncName();

		if (newFuncName.length() > 50) {
			int length = newFuncName.length();
			String msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1319.throwing("50", msg);
		}
	}



	private void updateTsmpFuncTable(TsmpAuthorization auth, AA0102Req req) throws Exception {
		boolean flag = false;
		boolean isUrlChange = false;
		String desc = ServiceUtil.nvl(req.getDesc());
		String newDesc = ServiceUtil.nvl(req.getNewDesc());
		String funcName = ServiceUtil.nvl(req.getFuncName());
		String newFuncName = ServiceUtil.nvl(req.getNewFuncName());
		String reportUrl = ServiceUtil.nvl(req.getReportUrl());
		String newReportUrl = ServiceUtil.nvl(req.getNewReportUrl());
		TsmpFunc tsmpFunc = getTsmpFunc(req);
		// 1298:查無資料
		if (tsmpFunc == null) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		if(StringUtils.hasLength(reportUrl) && StringUtils.hasLength(newReportUrl)) {
			URI uri = new URI(reportUrl);
			URI uriNew = new URI(newReportUrl);
			String uriHost = uri.getHost();
			String uriNewHost = uriNew.getHost();
			
			//都要有值或都要無值
			if((StringUtils.hasLength(uriHost) && !StringUtils.hasLength(uriNewHost)) || (!StringUtils.hasLength(uriHost) && StringUtils.hasLength(uriNewHost))) {
				throw TsmpDpAaRtnCode._1352.throwing("Embedded URL");
			}
			if(req.getFuncCode()!=null && req.getFuncCode().length()>4 && 
					req.getIsKibana()!=null && req.getIsKibana() && uriNewHost!=null) {
				throw TsmpDpAaRtnCode._1352.throwing("Embedded URL");
			}
			if(req.getFuncCode()!=null && req.getFuncCode().length()>4 && 
					req.getIsKibana()!=null && !req.getIsKibana() && uriNewHost==null) {
				throw TsmpDpAaRtnCode._1352.throwing("Embedded URL");
			}
		}

		if (!desc.equals(newDesc)) {
			tsmpFunc.setFuncDesc(newDesc);
			flag = true;
		}

		if (!funcName.equals(newFuncName)) {
			tsmpFunc.setFuncName(newFuncName);
			flag = true;
		}



		TsmpReportUrl tru = getTsmpReportUrlDao().findById(new TsmpReportUrlId(req.getFuncCode(), "T")).orElse(null);
		if (tru != null && !reportUrl.equals(newReportUrl)) {
			tru.setReportUrl(newReportUrl);
			isUrlChange = true;
			getTsmpReportUrlDao().save(tru);
		}

		if (flag || isUrlChange) {
			tsmpFunc.setUpdateUser(auth.getUserName());
			tsmpFunc.setUpdateTime(DateTimeUtil.now());

			getTsmpFuncDao().save(tsmpFunc);
			

		}
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return this.tsmpFuncDao;
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}

}
