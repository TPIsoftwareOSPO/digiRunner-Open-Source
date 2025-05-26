package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.vo.AA0304Req;
import tpi.dgrv4.dpaa.vo.AA0304Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0304Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpUserDao tsmpUserDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private BcryptParamHelper bcryptParamHelper;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private DaoGenericCacheService daoGenericCacheService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	@Autowired
	public AA0304Service(TsmpUserDao tsmpUserDao, TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao,
			BcryptParamHelper bcryptParamHelper, ApplicationContext ctx, JobHelper jobHelper,
			TsmpRegModuleDao tsmpRegModuleDao, DaoGenericCacheService daoGenericCacheService,
			DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.daoGenericCacheService = daoGenericCacheService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	@Transactional
	public AA0304Resp updateAPIInfo(TsmpAuthorization auth, AA0304Req req, ReqHeader reqHeader) {
		String userName = auth.getUserName();
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		String orgId = auth.getOrgId();
		
		TsmpApi tsmpApi = checkParams(userName, orgId, req, reqHeader.getLocale(), idPType, userNameForQuery);

		try {
			setTsmpApiFields(tsmpApi, userName, req);
			tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);
			clearAPICache();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		return new AA0304Resp();
	}

	protected TsmpApi checkParams(String userName, String orgId, AA0304Req req, String locale, String idPType,
			String userNameForQuery) {

		if (StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
		
		if (StringUtils.isEmpty(userNameForQuery)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
		
		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		checkUserExists(userNameForQuery, idPType);

		List<String> userOrgIdList = checkOrgExists(orgId);

		String jweFlag = req.getJweFlag();
		String jweFlagResp = req.getJweFlagResp();
		try {
			jweFlag = getBcryptParamHelper().decode(jweFlag, "API_JWT_FLAG", locale);
			jweFlagResp = getBcryptParamHelper().decode(jweFlagResp, "API_JWT_FLAG", locale);
			req.setJweFlag(jweFlag);
			req.setJweFlagResp(jweFlagResp);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		TsmpApi tsmpApi = checkApiExists(apiKey, moduleName);

		// API組織原則
		checkApiOrg(tsmpApi, userOrgIdList);
		
		String apiSrc = tsmpApi.getApiSrc();
		if (!(TsmpApiSrc.JAVA_MODULE.value().equals(apiSrc) || TsmpApiSrc.NET_MODULE.value().equals(apiSrc))) {
			throw TsmpDpAaRtnCode._1467.throwing();
		}
		
		return tsmpApi;
	}

	protected void checkUserExists(String userNameForQuery, String idPType) {
		if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				//Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery + ", idp_type: " + idPType);
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
		} else {//以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			//Table 查不到 user
			TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
			if (tsmpUser == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
		}
	}

	protected List<String> checkOrgExists(String userOrgId) {
		List<String> aa0304_userOrgIdList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(userOrgId, null);
		if (CollectionUtils.isEmpty(aa0304_userOrgIdList)) {
			throw TsmpDpAaRtnCode._1222.throwing();
		}
		return aa0304_userOrgIdList;
	}

	protected TsmpApi checkApiExists(String apiKey, String moduleName) {
		Optional<TsmpApi> aa0304_opt = getTsmpApiDao().findById(new TsmpApiId(apiKey, moduleName));
		if (!aa0304_opt.isPresent()) {
			this.logger.debug(String.format("API doesn't exist: %s-%s", moduleName, apiKey));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		return aa0304_opt.get();
	}

	protected void checkApiOrg(TsmpApi tsmpApi, List<String> userOrgIdList) {
		String apiOrgId = tsmpApi.getOrgId();
		if (!StringUtils.isEmpty(apiOrgId) && !userOrgIdList.contains(apiOrgId)) {
			this.logger.debug(String.format("Violate organization principle: apiOrgId(%s) not in %s", apiOrgId, userOrgIdList.toString()));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
	}

	protected void setTsmpApiFields(TsmpApi tsmpApi, String userName, AA0304Req req) {
		
		tsmpApi.setApiName(req.getApiName());
		
		tsmpApi.setApiStatus(req.getApiStatus());
		tsmpApi.setJewFlag(req.getJweFlag());
		tsmpApi.setJewFlagResp(req.getJweFlagResp());
		String apiDesc = req.getApiDesc();
		if (StringUtils.isEmpty(apiDesc)) {
			apiDesc = null;
		}
		tsmpApi.setApiDesc(apiDesc);
		tsmpApi.setUpdateTime(DateTimeUtil.now());
		tsmpApi.setUpdateUser(userName);
	}

	protected void clearAPICache() {
		/* 20221130 改用 Keeper 通知其他節點
		NoticeClearCacheEventsJob job = getNoticeClearCacheEventsJob("TSMP_API", "TSMP_API_REG");
		getJobHelper().add(job);
		*/
		getDaoGenericCacheService().clearAndNotify();
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected NoticeClearCacheEventsJob getNoticeClearCacheEventsJob(String tableName1, String tableName2) {
		Integer action = Integer.valueOf(2);
		List<String> tableNameList = new ArrayList<>();
		tableNameList.add(tableName1);
		tableNameList.add(tableName2);
		return (NoticeClearCacheEventsJob) getCtx().getBean("noticeClearCacheEventsJob", action, null, tableNameList);
	}
	
	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

}