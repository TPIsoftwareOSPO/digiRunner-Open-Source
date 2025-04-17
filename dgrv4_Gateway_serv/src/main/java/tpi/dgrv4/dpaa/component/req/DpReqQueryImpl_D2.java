package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2d;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 處理"API上下架"簽核單查詢
 * @author Kim
 *
 */
@Service
public class DpReqQueryImpl_D2 extends DpReqQueryAbstract<DpReqQueryResp_D2> //
	implements DpReqQueryIfs<DpReqQueryResp_D2> {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	private TsmpApiExtDao tsmpApiExtDao;

	@Autowired
	public DpReqQueryImpl_D2(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, TsmpClientDao tsmpClientDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory, TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao,
			TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao, TsmpApiDao tsmpApiDao,
			TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpDpFileDao tsmpDpFileDao2,
			TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy, TsmpApiExtDao tsmpApiExtDao) {
		super(tsmpDpReqOrdermDao, tsmpDpReqOrdersDao, tsmpDpItemsCacheProxy, tsmpOrganizationDao, tsmpUserDao,
				tsmpClientDao, tsmpDpFileDao, dpReqServiceFactory);
		this.tsmpDpReqOrderd2Dao = tsmpDpReqOrderd2Dao;
		this.tsmpDpReqOrderd2dDao = tsmpDpReqOrderd2dDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		tsmpDpFileDao = tsmpDpFileDao2;
		this.tsmpDpMailTpltCacheProxy = tsmpDpMailTpltCacheProxy;
		this.tsmpApiExtDao = tsmpApiExtDao;
	}

	@Override
	public List<DpReqQueryResp_D2> doQueryDetail(Long reqOrdermId, String locale) {
		List<TsmpDpReqOrderd2> d2List = getTsmpDpReqOrderd2Dao().findByRefReqOrdermId(reqOrdermId);
		if (d2List == null || d2List.isEmpty()) {
			return Collections.emptyList();
		}

		List<DpReqQueryResp_D2> d2RespList = new ArrayList<>();
		DpReqQueryResp_D2 d2Resp = null;
		Long d2Id = null;
		String apiUid = null;
		TsmpApi api = null;
		String apiName = null;
		String moduleName = null;
		String apiDesc = null;
		String orgName = null;
		String publicFlag = null;
		String publicFlagName = null;
		String apiKey = null;
		String orgId = null;
		TsmpApiExt apiExt = null;
		Long apiExtId = null;
		String dpStatus = null;
		List<DpReqQueryResp_D2d> d2dRespList = null;
		for (TsmpDpReqOrderd2 d2 : d2List) {
			d2Id = d2.getReqOrderd2Id();
			apiUid = d2.getApiUid();
			api = getApi(apiUid);
			apiName = new String();
			moduleName = new String();
			apiDesc = new String();
			orgName = new String();
			apiKey = new String();
			orgId = new String();
			apiExt = null;
			apiExtId = null;
			dpStatus = new String();
			if (api != null) {
				apiName = api.getApiName();
				moduleName = api.getModuleName();
				apiDesc = nvl(api.getApiDesc());
				orgName = getOrgName(api.getOrgId());
				apiKey = api.getApiKey();
				orgId = nvl(api.getOrgId());
				apiExt = getApiExt(apiKey, moduleName);
				if (apiExt != null) {
					apiExtId = apiExt.getApiExtId();
					dpStatus = apiExt.getDpStatus();
				}
			}
			d2dRespList = getD2dRespList(d2Id);
			publicFlag = nvl(d2.getPublicFlag());
			publicFlagName = getPublicFlagName(publicFlag, locale);

			d2Resp = new DpReqQueryResp_D2();
			d2Resp.setReqOrderd2Id(d2Id);
			d2Resp.setApiUid(apiUid);
			d2Resp.setApiName(apiName);
			d2Resp.setPublicFlag(publicFlag);
			d2Resp.setPublicFlagName(publicFlagName);
			d2Resp.setD2dRespList(d2dRespList);
			setD2FileInfo(d2Resp, d2Id);
			d2Resp.setModuleName(moduleName);
			d2Resp.setApiDesc(apiDesc);
			d2Resp.setOrgName(orgName);
			d2Resp.setApiKey(apiKey);
			d2Resp.setOrgId(orgId);
			d2Resp.setApiExtId(apiExtId);
			d2Resp.setDpStatus(dpStatus);
			d2RespList.add(d2Resp);
		}
		return d2RespList;
	}

	@Override
	protected TsmpMailEvent getTsmpMailEvent(String userId, String recipients, TsmpAuthorization auth,
			DpReqQueryResp<DpReqQueryResp_D2> resp) {
		if (StringUtils.isEmpty(recipients)) {
			this.logger.debug(String.format("USER %s has empty emails!", userId));
			return null;
		}

		String subject = getTemplate("subject.revi-wait");
		String body = getTemplate("body.revi-wait");
		if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(body)) {
			this.logger.debug(String.format("Cannot find email templates: %s, %s", "subject.revi-wait", "body.revi-wait"));
			return null;
		}
		
		Map<String, String> subjectParams = getSubjectParams(resp);
		if (subjectParams == null || subjectParams.isEmpty()) {
			this.logger.debug(String.format("USER %s has empty subject params!", userId));
			return null;
		}

		Map<String, String> bodyParams = getBodyParams(resp);
		if (bodyParams == null || bodyParams.isEmpty()) {
			this.logger.debug(String.format("USER %s has empty body params!", userId));
			return null;
		}

		final String title = MailHelper.buildContent(subject, subjectParams);
		final String content = MailHelper.buildContent(body, bodyParams);
		this.logger.debug("Email title = " + title);
		this.logger.debug("Email content = " + content);
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode("body.revi-wait")
		.build();
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList == null || apiList.isEmpty()) {
			return null;
		}
		return apiList.get(0);
	}

	private TsmpApiExt getApiExt(String apiKey, String moduleName) {
		TsmpApiExtId id = new TsmpApiExtId(apiKey, moduleName);
		Optional<TsmpApiExt> opt = getTsmpApiExtDao().findById(id);
		return opt.orElse(null);
	}

	private List<DpReqQueryResp_D2d> getD2dRespList(Long d2Id) {
		List<TsmpDpReqOrderd2d> d2dList = getTsmpDpReqOrderd2dDao().findByReqOrderd2Id(d2Id);
		if (d2dList == null || d2dList.isEmpty()) {
			return Collections.emptyList();
		}

		List<DpReqQueryResp_D2d> d2dRespList = new ArrayList<>();
		DpReqQueryResp_D2d d2dResp = null;
		Long d2dId = null;
		Long refThemeId = null;
		String apiThemeName = null;
		for (TsmpDpReqOrderd2d d2d : d2dList) {
			d2dId = d2d.getReqOrderd2dId();
			refThemeId = d2d.getRefThemeId();
			apiThemeName = getApiThemeName(refThemeId);

			d2dResp = new DpReqQueryResp_D2d();
			d2dResp.setReqOrderd2dId(d2dId);
			d2dResp.setRefThemeId(refThemeId);
			d2dResp.setApiThemeName(apiThemeName);
			d2dRespList.add(d2dResp);
		}
		return d2dRespList;
	}

	private String getApiThemeName(Long apiThemeId) {
		Optional<TsmpDpThemeCategory> opt_t = getTsmpDpThemeCategoryDao().findById(apiThemeId);
		if (opt_t.isPresent()) {
			return opt_t.get().getApiThemeName();
		}
		return new String();
	}

	private void setD2FileInfo(DpReqQueryResp_D2 resp, Long d2Id) {
		resp.setFilePath(new String());
		resp.setFileName(new String());
		
		List<TsmpDpFile> files = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.D2_ATTACHMENT.value(), d2Id);
		if (files == null || files.isEmpty()) {
			return;
		}

		String fileName = null;
		String filePath = null;
		for (TsmpDpFile f : files) {
			fileName = f.getFileName();
			filePath = f.getFilePath() + fileName;

			resp.setFilePath(filePath);
			resp.setFileName(fileName);
		}
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> list = getTsmpDpMailTpltCacheProxy().findByCode(code);
		if (list != null && !list.isEmpty()) {
			return list.get(0).getTemplateTxt();
		}
		return null;
	}

	private Map<String, String> getSubjectParams(DpReqQueryResp<DpReqQueryResp_D2> resp) {
		String reqOrderNo = null;
		String reviewType = null;
		if(resp != null) {
			reqOrderNo = resp.getReqOrderNo();
			reviewType = resp.getReqTypeName();
		}
		Map<String, String> emailParams = new HashMap<>();
		emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		emailParams.put("reqOrderNo", reqOrderNo);
		emailParams.put("reviewType", reviewType);
		return emailParams;
	}

	private Map<String, String> getBodyParams(DpReqQueryResp<DpReqQueryResp_D2> resp) {
		String reviewType = null;
		String reqOrderNo = null;
		String createDateTime = null;
		String applyUserName = null;
		String orgName = null;
		String subTitle = null;
		String chkStatusName = null;
		String chkPointName = null;
		List<DpReqQueryResp_D2> apiOnOffList = null;
		String publicFlagName = null;
		
		if(resp != null) {
			reviewType = resp.getReqTypeName();
			reqOrderNo = resp.getReqOrderNo();
			createDateTime = DateTimeUtil.dateTimeToString(resp.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(new String());
			applyUserName = resp.getApplierName();
			orgName = resp.getOrgName();
			subTitle = resp.getReqSubtypeName();
			chkStatusName = resp.getCurrentReviewStatusName();
			chkPointName = resp.getCurrentLayerName();
			apiOnOffList = resp.getDetailList();
			publicFlagName = groupPublicFlagName(apiOnOffList);
		}
		
		String dataList = getDataList(apiOnOffList);

		Map<String, String> emailParams = new HashMap<>();
		emailParams.put("reviewType", reviewType);
		emailParams.put("reqOrderNo", reqOrderNo);
		emailParams.put("createDateTime", createDateTime);
		emailParams.put("applyUserName", applyUserName);
		emailParams.put("orgName", orgName);
		emailParams.put("subTitle", subTitle);
		emailParams.put("chkStatusName", chkStatusName);
		emailParams.put("chkPointName", chkPointName);
		emailParams.put("publicFlagName", publicFlagName);
		emailParams.put("data-list", dataList);
		return emailParams;
	}

	private String groupPublicFlagName(List<DpReqQueryResp_D2> apiOnOffList) {
		if (apiOnOffList != null) {
			Set<String> publicFlagNames = new HashSet<>();
			for (DpReqQueryResp_D2 d2 : apiOnOffList) {
				publicFlagNames.add(d2.getPublicFlagName());
			}
			return String.join(", ", publicFlagNames);
		}
		return new String();
	}

	private String getDataList(List<DpReqQueryResp_D2> apiOnOffList) {
		if(apiOnOffList == null || apiOnOffList.isEmpty()) {
			return null;
		}
		StringBuffer dataList = new StringBuffer();

		String rowTemplate = getTemplate("body.revi-wait.list");
		Map<String, String> rowParams = null;
		int index = 1;
		for(DpReqQueryResp_D2 apiOnOff : apiOnOffList) {
			// Initialize
			rowParams = new HashMap<>();
			rowParams.put("index", "");
			rowParams.put("apiName", "");
			rowParams.put("themeName", "");
			rowParams.put("docFileName", "");
			fillRowParams(rowParams, apiOnOff, index);
			dataList.append(MailHelper.buildContent(rowTemplate, rowParams));
			index++;
		}

		return dataList.toString();
	}

	private void fillRowParams(Map<String, String> params, DpReqQueryResp_D2 apiOnOff, int index) {
		String themeStr = "";
		List<DpReqQueryResp_D2d> respD2dList = apiOnOff.getD2dRespList();
		if (respD2dList != null && !respD2dList.isEmpty()) {
			for(DpReqQueryResp_D2d respD2d : respD2dList) {
				if(!StringUtils.isEmpty(themeStr)) {
					themeStr += ",";
				}
				themeStr += respD2d.getApiThemeName();
			}
		}

		String fileNameStr = "";
		if (!StringUtils.isEmpty(apiOnOff.getFileName())) {
			fileNameStr = apiOnOff.getFileName();
		}
		
		params.put("index", index + "");
		params.put("apiName", apiOnOff.getApiName());
		params.put("themeName", themeStr);
		params.put("docFileName", fileNameStr);
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

	protected TsmpDpReqOrderd2Dao getTsmpDpReqOrderd2Dao() {
		return this.tsmpDpReqOrderd2Dao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpReqOrderd2dDao getTsmpDpReqOrderd2dDao() {
		return this.tsmpDpReqOrderd2dDao;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpDpMailTpltCacheProxy getTsmpDpMailTpltCacheProxy() {
		return this.tsmpDpMailTpltCacheProxy;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

}
