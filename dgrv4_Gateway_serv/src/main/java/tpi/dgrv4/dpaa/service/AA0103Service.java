package tpi.dgrv4.dpaa.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.constant.ReportType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0103List;
import tpi.dgrv4.dpaa.vo.AA0103Req;
import tpi.dgrv4.dpaa.vo.AA0103Resp;
import tpi.dgrv4.dpaa.vo.AA0506Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrlId;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0103Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;
	private ServiceConfig serviceConfig;
	private TsmpReportUrlDao tsmpReportUrlDao;
	private TsmpDpItemsDao tsmpDpItemsDao;
	private BcryptParamHelper bcryptParamHelper;
	
	@Autowired
	public AA0103Service(TsmpFuncDao tsmpFuncDao, TsmpRoleDao tsmpRoleDao, TsmpRoleFuncDao tsmpRoleFuncDao,
			ServiceConfig serviceConfig, TsmpReportUrlDao tsmpReportUrlDao, TsmpDpItemsDao tsmpDpItemsDao,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
		this.serviceConfig = serviceConfig;
		this.tsmpReportUrlDao = tsmpReportUrlDao;
		this.tsmpDpItemsDao = tsmpDpItemsDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	private Integer pageSize;

	public AA0103Resp queryTFuncList(AA0103Req req, ReqHeader reqHeader) {
		AA0103Resp resp =  new AA0103Resp();

		try {
			
			String funcCode = req.getFuncCode();
			String locale = reqHeader.getLocale();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");

			if (!StringUtils.hasLength(req.getFuncType())) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			String funcType = getFuncTypeByBcryptParamHelper(req.getFuncType(), locale);
			List<TsmpFunc> tsmpFuncList =  getTsmpFuncDao().query_aa0103Service(funcCode, locale, words, getPageSize(), funcType);
			//我不知道為什麼之前要寫兩次，但是我覺得他應該可能也許是多寫的，所以我先把它註解起來 by zoe
//			getTsmpFuncDao().query_aa0103Service(funcCode, locale, words, getPageSize());

			// 1298:查無資料
			if(tsmpFuncList == null || tsmpFuncList.size() == 0)
				throw TsmpDpAaRtnCode._1298.throwing();
			
			Map<String,TsmpFunc> masterFuncMap = getTsmpFuncDao().findMasterFuncList(locale).stream().collect(Collectors.toMap(TsmpFunc::getFuncCode, TsmpFunc -> TsmpFunc));
				
			List<AA0103List> funcInfoList = getAA1003Resp(tsmpFuncList, masterFuncMap, locale);
			resp.setFuncInfoList(funcInfoList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<AA0103List> getAA1003Resp( List<TsmpFunc> tsmpFuncList , Map<String,TsmpFunc> masterFuncMap, String locale){
		List<AA0103List> list = new ArrayList<>();
		
		Map<String, String> localeItemsMap = getTsmpDpItemsDao().findByItemNoAndLocaleOrderBySortByAsc("RTN_CODE_LOCALE", locale).
				stream().collect(Collectors.toMap(TsmpDpItems::getSubitemNo, TsmpDpItems::getSubitemName));
		
		tsmpFuncList.forEach((tsmpFunc) ->{
			AA0103List aa0103List = new AA0103List();
			aa0103List.setFuncCode(ServiceUtil.nvl(tsmpFunc.getFuncCode()));
			aa0103List.setFuncDesc(ServiceUtil.nvl(tsmpFunc.getFuncDesc()));
			aa0103List.setFuncName(ServiceUtil.nvl(tsmpFunc.getFuncName()));
			aa0103List.setFuncNameEn(ServiceUtil.nvl(tsmpFunc.getFuncNameEn()));
			aa0103List.setLocale(ServiceUtil.nvl(tsmpFunc.getLocale()));
			aa0103List.setFuncType(ServiceUtil.nvl(tsmpFunc.getFuncType()));
			String createDateStr = "";
			Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(tsmpFunc.getUpdateTime(), null);
			if(createDateOpt.isPresent())
				createDateStr = createDateOpt.get();
			
			aa0103List.setUpdateTime(createDateStr);
			aa0103List.setUpdateUser(ServiceUtil.nvl(tsmpFunc.getUpdateUser()));
			//其他時間日期已棄用，只撈 "T" 的
			TsmpReportUrl tru = getTsmpReportUrlDao().findById(new TsmpReportUrlId(tsmpFunc.getFuncCode(), "T"))
					.orElse(null);
			if (tru != null && StringUtils.hasLength(tru.getReportUrl())) {
				aa0103List.setReportUrl(tru.getReportUrl());
				try {
					URI uri = new URI(tru.getReportUrl());
					if(uri.getHost() == null) {
						aa0103List.setReportType(ReportType.SYSTEM_REPORT);
					}else if(tru.getReportUrl().indexOf("dgrv4/cus") > -1){
						aa0103List.setReportType(ReportType.EMBEDDED_LINKS);
					}else {
						aa0103List.setReportType(ReportType.OUTBOUND_LINKS);
					}
				} catch (URISyntaxException e) {
//					e.printStackTrace();
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			}
			
			if(tsmpFunc.getFuncCode().length() > 4) {
				TsmpFunc masterFunc = masterFuncMap.get(tsmpFunc.getFuncCode().substring(0, 4));
				if(null != masterFunc) {
					aa0103List.setMasterFuncName(masterFunc.getFuncName());
				}else {
					aa0103List.setMasterFuncName(tsmpFunc.getFuncCode().substring(0, 4));
				}				
			}
			aa0103List.setLocaleName(localeItemsMap.get(tsmpFunc.getLocale()));
		
			list.add(aa0103List);
			
		});
		
		return list;
	}
	
	/**
	 * 後台-tsmpaa( v3.8) API
	 * 
	 * 使用BcryptParam, 
	 * ITEM_NO='FUNC_TYPE' , DB儲存值對應代碼如下:
	 * DB值 (SUBITEM_NO) = 0; 
	 * 0=原功能, 1=嵌入功能
	 * @param encodeFuncType
	 * @return
	 */
	protected String getFuncTypeByBcryptParamHelper(String encodeFuncType, String locale) {
		String funcType = null;
		try {
			funcType = getBcryptParamHelper().decode(encodeFuncType, "FUNC_TYPE", BcryptFieldValueEnum.SUBITEM_NO, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return funcType;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}
	
	protected TsmpFuncDao getTsmpFuncDao() {
		return this.tsmpFuncDao;
	}
	
	protected TsmpRoleFuncDao getTsmpRoleFuncDao() {
		return this.tsmpRoleFuncDao;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0103");
		return this.pageSize;
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}
	
	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
}
