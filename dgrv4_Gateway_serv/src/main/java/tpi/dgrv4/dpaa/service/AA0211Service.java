package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0211APIKey;
import tpi.dgrv4.dpaa.vo.AA0211Req;
import tpi.dgrv4.dpaa.vo.AA0211Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
	
@Service
public class AA0211Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private SeqStoreService seqStoreService;
	private BcryptParamHelper bcryptParamHelper;
	private DgrAuditLogService dgrAuditLogService;

	@Autowired
	public AA0211Service(TsmpGroupDao tsmpGroupDao, TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao,
			TsmpApiDao tsmpApiDao, TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao, TsmpGroupApiDao tsmpGroupApiDao,
			SeqStoreService seqStoreService, BcryptParamHelper bcryptParamHelper,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.seqStoreService = seqStoreService;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0211Resp addGroup(TsmpAuthorization authorization, AA0211Req req, ReqHeader reqHeader, InnerInvokeParam iip) {

		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_GROUP.value());

		
		AA0211Resp resp = new AA0211Resp();
		try {

			String userName = authorization.getUserName();
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			//檢查資料否存在於資料表
			validate(req);
			
			//新增TSMP_GROUP(主要資料表)
			TsmpGroup tsmpGroup = addGroup(req, userName, locale, iip);
			
			//新增TSMP_GROUP_AUTHORITIES_MAP 	
			addTsmpGroupAuthoritiesMap(req, tsmpGroup, iip);
			
			//新增TSMP_GROUP_API
			addTsmpGroupApi(req, tsmpGroup, iip);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	private void addTsmpGroupApi(AA0211Req req, TsmpGroup tsmpGroup, InnerInvokeParam iip) {
		if (req.getApiKeyList() != null && req.getApiKeyList().size() > 0) {
			for (AA0211APIKey aa0211APIKey : req.getApiKeyList()) {
				TsmpGroupApi tsmpGroupApi =new TsmpGroupApi();
				tsmpGroupApi.setGroupId(tsmpGroup.getGroupId());
				tsmpGroupApi.setApiKey(aa0211APIKey.getApiKey());
				tsmpGroupApi.setModuleName(aa0211APIKey.getModuleName());
				tsmpGroupApi.setModuleVer("0");
				tsmpGroupApi.setCreateTime(DateTimeUtil.now());
				getTsmpGroupApiDao().saveAndFlush(tsmpGroupApi);
				
				//寫入 Audit Log D
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						TsmpGroupApi.class.getSimpleName(), TableAct.C.value(), null, tsmpGroupApi);
				
			}
		}
	}

	private void addTsmpGroupAuthoritiesMap(AA0211Req req, TsmpGroup tsmpGroup, InnerInvokeParam iip) {
		for (String groupAuthoritieId : req.getGroupAuthorities()) {
			TsmpGroupAuthoritiesMap map = new TsmpGroupAuthoritiesMap();
			map.setGroupId(tsmpGroup.getGroupId());
			map.setGroupAuthoritieId(groupAuthoritieId);
			getTsmpGroupAuthoritiesMapDao().saveAndFlush(map);	
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpGroupAuthoritiesMap.class.getSimpleName(), TableAct.C.value(), null, map);
			
		}
	}

	private TsmpGroup addGroup(AA0211Req req, String userName, String locale, InnerInvokeParam iip) {
		TsmpGroup tsmpGroup = new TsmpGroup();	
		final Long seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK);
		if (seq != null) {
			tsmpGroup.setGroupId(seq.toString());
		}
		tsmpGroup.setGroupName(req.getGroupName());
		tsmpGroup.setCreateTime(DateTimeUtil.now());
		tsmpGroup.setUpdateTime(DateTimeUtil.now());
		tsmpGroup.setCreateUser(userName);
		tsmpGroup.setUpdateUser(userName);
		
		tsmpGroup.setGroupAlias(req.getGroupAlias());
		tsmpGroup.setGroupDesc(req.getGroupDesc());
		tsmpGroup.setGroupAccess("[]");
		tsmpGroup.setSecurityLevelId(req.getSecurityLevel());
		
		//允許使用時間(單位) - 解密
		String timeUnit = this.getValueByBcryptParamHelper(req.getAllowAccessUseTimesTimeUnit(), "TIME_UNIT", locale);
		int allowAccessUseTimes = this.convertSecond(req.getAllowAccessDays(), timeUnit);
		tsmpGroup.setAllowDays(allowAccessUseTimes);
		
		tsmpGroup.setAllowTimes(req.getAllowAccessUseTimes());
		tsmpGroup.setVgroupFlag("0");
		
		getTsmpGroupDao().saveAndFlush(tsmpGroup);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpGroup.class.getSimpleName(), TableAct.C.value(), null, tsmpGroup);
		
		
		return tsmpGroup;
	}

	/***
	 * 
	 * 檢查資料否存在於資料表
	 * 
	 * @param req
	 */
	private void validate(AA0211Req req) {
		//檢查授權核身種類是正確。
		if (req.getGroupAuthorities()!=null) {
			for (String groupAuthoritieId : req.getGroupAuthorities()) {
				Optional<TsmpGroupAuthorities> opt_tsmpGroupAuthorities = getTsmpGroupAuthoritiesDao().findById(groupAuthoritieId);
				if (opt_tsmpGroupAuthorities.isPresent() == false) {
					throw TsmpDpAaRtnCode._1397.throwing(groupAuthoritieId);
				}
			}
		}

		//檢查群組代碼(groupName)是否重複
		List<TsmpGroup> groupNameList = getTsmpGroupDao().findByGroupName(req.getGroupName());
		if (groupNameList != null && groupNameList.size() > 0) {
			throw TsmpDpAaRtnCode._1399.throwing();
		}

		//檢查群組名稱(groupAlias)是否重複
		if (!StringUtils.isEmpty(req.getGroupAlias())) {
			List<TsmpGroup> groupAliasList = getTsmpGroupDao().findByGroupAlias(req.getGroupAlias());
			if (groupAliasList != null && groupAliasList.size() > 0) {
				throw TsmpDpAaRtnCode._1398.throwing();
			}
		}
		
		//檢查apiKey清單內的資料是否有存在於TSMP_API資料表。
		if (req.getApiKeyList() != null && req.getApiKeyList().size() > 0) {
			for (AA0211APIKey aa0211APIKey : req.getApiKeyList()) {
				TsmpApiId id = new 	TsmpApiId(aa0211APIKey.getApiKey(), aa0211APIKey.getModuleName());
				Optional<TsmpApi> opt_tsmpApi = getTsmpApiDao().findById(id);
				if (opt_tsmpApi.isPresent()==false) {
					throw TsmpDpAaRtnCode._1400.throwing(aa0211APIKey.getApiKey());
				}
			}
		}
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}
	
	private String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.SUBITEM_NO, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	private int convertSecond(int value, String timeUnit) {
		if("d".equals(timeUnit)) {
			return value * 60 * 60 * 24;
		}else if("H".equals(timeUnit)) {
			return value * 60 * 60;
		}else if("m".equals(timeUnit)) {
			return value * 60;
		}else {
			return value;
		}
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

}
