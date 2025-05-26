package tpi.dgrv4.dpaa.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0106Item;
import tpi.dgrv4.dpaa.vo.AA0106Req;
import tpi.dgrv4.dpaa.vo.AA0106Resp;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0106Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	private TsmpReportUrlDao tsmpReportUrlDao;

	private static final String funcCodePrefix = "AC09";
	private static final String cusFuncCodePrefix = "ZA";
	private static final Integer START = 11;
	private static final Integer END = 99;
	
	@Autowired
	public AA0106Service(TsmpFuncDao tsmpFuncDao, TsmpReportUrlDao tsmpReportUrlDao) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpReportUrlDao = tsmpReportUrlDao;
	}
	protected  Integer  start() { // 從AC0911開始
		return START;
	}
	protected  Integer  end() { // 到AC0999
		return END;
	}
	
	
	public AA0106Resp addReport(TsmpAuthorization auth, AA0106Req req) {
		AA0106Resp resp = new AA0106Resp();
		try {

			checkParams(req);
			
			String reportUrl = req.getEmbeddedUrl();
			List<AA0106Item> funcList = req.getFuncList();
			//取得可用的funcCode
			String funcCode = "";
			if(0 == req.getType()) {
				funcCode = getFuncCode(req.getType(), null); //客製主選單
			}else {
				URI uri = new URI(reportUrl);
				if(null == uri.getHost()) {
					funcCode = getFuncCode(req.getType(), null); //嵌入報表
				}else {
					funcCode = getFuncCode(req.getType(), req.getMasterFuncCode()); //客製子選單
				}
			}
			
			//檢查funcCode是否存在
			checkReportId(funcCode);
			//寫入 TsmpFunc
			insertTsmpFunc(funcCode, funcList, auth);
			//寫入 TsmpReportUrl
			if(1 == req.getType())
				insertTsmpReportUrl(funcCode, reportUrl);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing(); // 更新錯誤
		}

		return resp;

	}

	private void insertTsmpReportUrl(String funcCode, String reportUrl) {
		TsmpReportUrl tsmpReportUrl = new TsmpReportUrl();
		tsmpReportUrl.setReportId(funcCode);
		tsmpReportUrl.setTimeRange("T");
		tsmpReportUrl.setReportUrl(reportUrl);
		
		getTsmpReportUrlDao().save(tsmpReportUrl);		
	}

	private void insertTsmpFunc(String funcCode, List<AA0106Item> funcList, TsmpAuthorization auth) {
		List<TsmpFunc> tsmpFuncs = new ArrayList<TsmpFunc>();
		for (AA0106Item aa0106Item : funcList) {
			TsmpFunc tsmpFunc = new TsmpFunc();
			tsmpFunc.setFuncCode(funcCode);
			tsmpFunc.setFuncName(aa0106Item.getFuncName());
			tsmpFunc.setFuncDesc(aa0106Item.getFuncDesc());
			tsmpFunc.setLocale(aa0106Item.getLocale());
			tsmpFunc.setUpdateUser(auth.getUserName());
			tsmpFunc.setUpdateTime(DateTimeUtil.now());
			tsmpFuncs.add(tsmpFunc);
		}
		getTsmpFuncDao().saveAll(tsmpFuncs);
	}

	private void checkReportId(String funcCode) {
		boolean flag = getTsmpReportUrlDao().existsByReportId(funcCode);
		if (flag) {
			TPILogger.tl.debug(funcCode + " is in TsmpReportUrl table");
			throw TsmpDpAaRtnCode._1353.throwing("{{funcCode}}", funcCode);
		}
	}
	/**
	 * 若有prefix為客製子選單,無則為報表
	 * @param prefix
	 * @return
	 */
	private String getFuncCode(Integer type, String prefix) {
		String funcCode = "";
		try {			
			String queryPrefix="";
			if(type == 0) {
				queryPrefix = cusFuncCodePrefix;
			}else if(type == 1) {
				queryPrefix = (null == prefix)?funcCodePrefix:prefix;
			}
			
			List<TsmpFunc> tsmpFuncList = getTsmpFuncDao().findByFuncCodeStartsWith(queryPrefix);
			
			// 找出已使用的funcCode			
			if(null != tsmpFuncList) {
				List<String> reportIds = new ArrayList<String>();
				reportIds = tsmpFuncList.stream().map(TsmpFunc::getFuncCode).collect(Collectors.toList());

				List<String> list = new ArrayList<String>();
				int start = 0;
				if(type == 0) {
					start = 30;
				}else if(null!= prefix) {
					start = 1;
				}else {
					start = start();
				}				
				int end = end();
				for (int i = start ; i < end + 1; i++) {
					list.add(String.format("%s%02d", queryPrefix, i));
				}

				list.removeAll(reportIds);
				if (CollectionUtils.isEmpty(list)) {
					throw TsmpDpAaRtnCode._2027.throwing();
				}
				// 取一個未使用的funcCode
				funcCode = list.get(0);
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
		return funcCode;
	}

	private void checkParams(AA0106Req req) throws URISyntaxException {

		String embeddedUrl = req.getEmbeddedUrl();
		// 1350:[{{0}}] 為必填欄位
		if (!StringUtils.hasLength(embeddedUrl) && req.getType() == 1) {
			throw TsmpDpAaRtnCode._1350.throwing("Embedded URL");
		}
		// 1351:[{{0}}] 長度限制 [{{1}}] 字內，您輸入[{{2}}] 個字
		if (embeddedUrl!=null && embeddedUrl.length() > 2000) {
			int length = embeddedUrl.length();
			String msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1351.throwing("Embedded URL", "2000", msg);
		}
		
		//當Type = 1 且有Host時,MasterFuncCode必填
		if(1 == req.getType()) {
			URI uri;
			try {
				if(req.getIsKibana() == null) {		
					throw TsmpDpAaRtnCode._1350.throwing("{{Kibana}}");
				}
				
				uri = new URI(embeddedUrl);
				if(!req.getIsKibana() && !StringUtils.hasLength(req.getMasterFuncCode())) {		
					throw TsmpDpAaRtnCode._1350.throwing("{{masterFuncCode}}");
				}
				
				if(req.getIsKibana() && uri.getHost()!=null) {
					throw TsmpDpAaRtnCode._1352.throwing("Embedded URL");
				}
				if(!req.getIsKibana() && uri.getHost()==null) {
					throw TsmpDpAaRtnCode._1352.throwing("Embedded URL");
				}
			} catch (TsmpDpAaException e) {
				throw e;
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		
		String locale;
		String funcName;
		List<AA0106Item> funcList = req.getFuncList();
		Set<String> set = new HashSet<>();
		for (AA0106Item aa0106Item : funcList) {
			locale = aa0106Item.getLocale();
			funcName = aa0106Item.getFuncName();
			// 1316:語系:必填參數
			if (!StringUtils.hasLength(locale)) {
				throw TsmpDpAaRtnCode._1316.throwing();
			}
			// 1350:[{{0}}] 為必填欄位
			if (!StringUtils.hasLength(funcName)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{funcName}}");
			}
			// 1319:功能名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			if (funcName.length() > 50) {
				int length = funcName.length();
				String msg = String.valueOf(length);
				throw TsmpDpAaRtnCode._1319.throwing("50", msg);
			}
			// 1284:[{{0}}] 不得重複
			if (set.contains(locale)) {
				throw TsmpDpAaRtnCode._1284.throwing("{{locale}}");
			} else {
				set.add(locale);
			}
			// 1318:功能描述:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			String desc = aa0106Item.getFuncDesc();
			if (StringUtils.hasLength(desc) && desc.length() > 300) {
				int length = desc.length();
				String msg = String.valueOf(length);
				throw TsmpDpAaRtnCode._1318.throwing("300", msg);
			}
		}
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return this.tsmpFuncDao;
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}
}
