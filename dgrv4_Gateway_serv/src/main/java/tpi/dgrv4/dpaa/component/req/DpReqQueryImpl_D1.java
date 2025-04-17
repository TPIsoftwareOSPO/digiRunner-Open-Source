package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd1;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd1Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 處理"用戶申請API"簽核單查詢
 * @author Kim
 *
 */
@Service
public class DpReqQueryImpl_D1 extends DpReqQueryAbstract<DpReqQueryResp_D1> implements DpReqQueryIfs<DpReqQueryResp_D1> {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao;
	private MailHelper mailHelper;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpApiExtDao tsmpApiExtDao;

	@Autowired
	public DpReqQueryImpl_D1(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, TsmpClientDao tsmpClientDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory, TsmpClientDao tsmpClientDao2, TsmpApiDao tsmpApiDao,
			TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao, MailHelper mailHelper, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpApiExtDao tsmpApiExtDao) {
		super(tsmpDpReqOrdermDao, tsmpDpReqOrdersDao, tsmpDpItemsCacheProxy, tsmpOrganizationDao, tsmpUserDao,
				tsmpClientDao, tsmpDpFileDao, dpReqServiceFactory);
		tsmpClientDao = tsmpClientDao2;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpReqOrderd1Dao = tsmpDpReqOrderd1Dao;
		this.mailHelper = mailHelper;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
	}

	@Override
	protected List<DpReqQueryResp_D1> doQueryDetail(Long reqOrdermId, String locale) {
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(reqOrdermId);
		if (d1List == null || d1List.isEmpty()) {
			return Collections.emptyList();
		}
		
		// 找出每個clientId各申請哪些apiUid
		Map<String, List<DpReqQueryResp_D1d>> clientApiMapping = new HashMap<>();
		String clientId = null;
		String apiUid = null;
		DpReqQueryResp_D1d d1d = null;
		List<DpReqQueryResp_D1d> apiList = null;
		for(TsmpDpReqOrderd1 d1 : d1List) {
			clientId = d1.getClientId();
			apiUid = d1.getApiUid();
			d1d = getD1d(d1.getReqOrderd1Id(), apiUid, locale);
			if (d1d == null) {
				continue;
			}
			apiList = clientApiMapping.get(clientId) == null ? new ArrayList<>() : clientApiMapping.get(clientId);
			apiList.add(d1d);
			clientApiMapping.put(clientId, apiList);
		}
		
		List<DpReqQueryResp_D1> d1RespList = new ArrayList<>();
		DpReqQueryResp_D1 d1Resp = null;
		TsmpClient client = null;
		for(Map.Entry<String, List<DpReqQueryResp_D1d>> entry : clientApiMapping.entrySet()) {
			d1Resp = new DpReqQueryResp_D1();
			d1Resp.setClientId(entry.getKey());
			client = getClient(entry.getKey());
			if (client != null) {
				d1Resp.setClientName(client.getClientName());
				d1Resp.setClientAlias(nvl(client.getClientAlias()));
			}
			d1Resp.setApiList(entry.getValue());
			d1RespList.add(d1Resp);
		}
		return d1RespList;
	}

	@Override
	protected TsmpMailEvent getTsmpMailEvent(String userId, String recipients, TsmpAuthorization auth,
			DpReqQueryResp<DpReqQueryResp_D1> resp) {
		if (resp == null) {
			this.logger.debug("Query response is empty!");
			return null;
		}
		if (StringUtils.isEmpty(recipients)) {
			this.logger.debug(String.format("USER %s has empty emails!", userId));
			return null;
		}
		
		Map<String, Object> d1_subjectParams = new HashMap<>();
		d1_subjectParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		d1_subjectParams.put("reqOrderNo", resp.getReqOrderNo());
		d1_subjectParams.put("reviewType", resp.getReqTypeName());
		String subject = getMailHelper().buildNestedContent("subject.revi-wait.D1", d1_subjectParams);
		
		List<Map<String, Object>> clientApiMappings = getClientApiMappings(resp.getDetailList());
		Map<String, Object> bodyParams = new HashMap<>();
		bodyParams.put("reviewType", resp.getReqTypeName());
		bodyParams.put("reqOrderNo", resp.getReqOrderNo());
		bodyParams.put("createDateTime", DateTimeUtil.dateTimeToString(resp.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(new String()));
		bodyParams.put("applyUserName", resp.getApplierName());
		bodyParams.put("orgName", resp.getOrgName());
		bodyParams.put("chkStatusName", resp.getCurrentReviewStatusName());
		bodyParams.put("chkPointName", resp.getCurrentLayerName());
		bodyParams.put("clientApiMappings", clientApiMappings);
		
		String content = getMailHelper().buildNestedContent("body.revi-wait.D1", bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode("body.revi-wait.D1")
		.build();
	}

	private DpReqQueryResp_D1d getD1d(Long reqOrderd1Id, String apiUid, String locale) {
		if (StringUtils.isEmpty(apiUid)) {
			return null;
		}
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList == null || apiList.isEmpty()) {
			return null;
		}
		TsmpApi api = apiList.get(0);
		String publicFlagName = getPublicFlagName(api.getPublicFlag(), locale);
		Map<Long, String> themes = getApiThemes(api.getApiUid());

		TsmpApiExt apiExt = getApiExt(api.getApiKey(), api.getModuleName());
		Long apiExtId = null;
		List<TsmpDpFile> apiAttachments = new ArrayList<>();
		String dpStatus = new String();
		if (apiExt != null) {
			apiExtId = apiExt.getApiExtId();
			apiAttachments = getApiAttachments(apiExtId);
			dpStatus = apiExt.getDpStatus();
		}

		DpReqQueryResp_D1d d1d = new DpReqQueryResp_D1d();
		d1d.setReqOrderd1Id(reqOrderd1Id);
		d1d.setApiUid(apiUid);
		d1d.setApiName(api.getApiName());
		d1d.setModuleName(api.getModuleName());
		d1d.setOrgName( getOrgName(api.getOrgId()) );
		d1d.setApiDesc(nvl(api.getApiDesc()));
		d1d.setThemes(themes);
		d1d.setApiAttachments(apiAttachments);
		d1d.setApiKey(api.getApiKey());
		d1d.setOrgId(api.getOrgId());
		d1d.setApiExtId(apiExtId);
		d1d.setDpStatus(dpStatus);
		d1d.setPublicFlag(nvl(api.getPublicFlag()));
		d1d.setPublicFlagName(publicFlagName);
		return d1d;
	}

	private String getPublicFlagName(String publicFlag, String locale) {
		if (StringUtils.isEmpty(publicFlag)) {
			publicFlag = TsmpDpPublicFlag.EMPTY.value();
		}

		TsmpDpItemsId id = new TsmpDpItemsId("API_AUTHORITY", publicFlag, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo != null) {
			return vo.getSubitemName();
		}
		return new String();
	}

	private Map<Long, String> getApiThemes(String apiUid) {
		List<TsmpDpApiTheme> apiThemes = getTsmpDpApiThemeDao().findAllByRefApiUid(apiUid);
		if (apiThemes == null || apiThemes.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Long, String> map = new HashMap<>();
		
		Long refApiThemeId = null;
		String apiThemeName = null;
		for (TsmpDpApiTheme apiTheme : apiThemes) {
			refApiThemeId = apiTheme.getRefApiThemeId();
			apiThemeName = getApiThemeName(refApiThemeId);
			map.put(refApiThemeId, apiThemeName);
		}

		return map;
	}

	private String getApiThemeName(Long apiThemeId) {
		Optional<TsmpDpThemeCategory> opt_t = getTsmpDpThemeCategoryDao().findById(apiThemeId);
		if (opt_t.isPresent()) {
			return opt_t.get().getApiThemeName();
		}
		return new String();
	}

	private List<TsmpDpFile> getApiAttachments(Long apiExtId) {
		if (apiExtId == null) {
			return Collections.emptyList();
		}
		return getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.API_ATTACHMENT.value(), apiExtId);
	}

	private TsmpApiExt getApiExt(String apiKey, String moduleName) {
		TsmpApiExtId id = new TsmpApiExtId(apiKey, moduleName);
		Optional<TsmpApiExt> opt = getTsmpApiExtDao().findById(id);
		return opt.orElse(null);
	}

	private List<Map<String, Object>> getClientApiMappings(List<DpReqQueryResp_D1> detailList){
		List<Map<String, Object>> clientApiMappings = new ArrayList<>();
		
		Map<String, Object> clientApiMapping = null;
		List<DpReqQueryResp_D1d> d1dList = null;
		List<String> apiNames = null;
		for (DpReqQueryResp_D1 detail : detailList) {
			clientApiMapping = new HashMap<>();
			clientApiMapping.put("clientId", detail.getClientId());
			clientApiMapping.put("clientName", detail.getClientName());
			clientApiMapping.put("clientAlias", detail.getClientAlias());
			apiNames = new ArrayList<>();

			d1dList = detail.getApiList();
			if (d1dList != null && !d1dList.isEmpty()) {
				for (DpReqQueryResp_D1d d1d : d1dList) {
					apiNames.add(d1d.getApiName());
				}
			}
			
			clientApiMapping.put("apiNames", apiNames);
			clientApiMappings.add(clientApiMapping);
		}

		return clientApiMappings;
	}

	private TsmpClient getClient(String ClinetId) {
		return getTsmpClientDao().findById(ClinetId).orElse(null);
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpReqOrderd1Dao getTsmpDpReqOrderd1Dao() {
		return this.tsmpDpReqOrderd1Dao;
	}

	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

}
