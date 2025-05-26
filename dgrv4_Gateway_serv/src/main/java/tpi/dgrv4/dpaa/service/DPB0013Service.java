package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.dpaa.vo.DPB0013App;
import tpi.dgrv4.dpaa.vo.DPB0013Req;
import tpi.dgrv4.dpaa.vo.DPB0013Resp;
import tpi.dgrv4.entity.entity.sql.TsmpDpApp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.entity.repository.TsmpDpAppDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0013Service {

	private TsmpDpAppCategoryDao tsmpDpappCategoryDao;
	private TsmpDpAppDao tsmpDpAppDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0013Service(TsmpDpAppCategoryDao tsmpDpappCategoryDao, TsmpDpAppDao tsmpDpAppDao,
			TsmpOrganizationDao tsmpOrganizationDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpappCategoryDao = tsmpDpappCategoryDao;
		this.tsmpDpAppDao = tsmpDpAppDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0013Resp queryAppLikeList(TsmpAuthorization authorization, DPB0013Req req) {
		String orgId = authorization.getOrgId();
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		List<String> dataStatusList = getDataStatusList(req.getDataStatus());
		Long lastId = req.getAppId();
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		// 準備查詢參數
		List<TsmpDpApp> appList = getTsmpDpAppDao().queryLikeByDataStatus(orgDescList, dataStatusList //
				, words, lastId, pageSize);
		if (appList == null || appList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_APP_DATA.throwing();
		}

		DPB0013Resp resp = new DPB0013Resp();
		List<DPB0013App> dpb0013AppList = getDpb0013AppList(appList);
		resp.setAppList(dpb0013AppList);
		return resp;
	}

	private List<String> getDataStatusList(String dataStatus) {
		List<String> dataStatusList = new ArrayList<>();

		if (dataStatus == null || dataStatus.isEmpty()) {
			dataStatusList.add(TsmpDpDataStatus.ON.value());
			dataStatusList.add(TsmpDpDataStatus.OFF.value());
		} else {
			dataStatusList.add(dataStatus);
		}

		return dataStatusList;
	}

	private List<DPB0013App> getDpb0013AppList(List<TsmpDpApp> appList) {
		List<DPB0013App> dpb0013AppList = new ArrayList<>();

		String dataStatus, appCateName;
		DPB0013App dpb0013App;
		for(TsmpDpApp app : appList) {
			dpb0013App = new DPB0013App();
			dpb0013App.setAppId(app.getAppId());
			dpb0013App.setRefAppCateId(app.getRefAppCateId());
			dpb0013App.setName(app.getName());
			dataStatus = TsmpDpDataStatus.text(app.getDataStatus());
			dpb0013App.setDataStatus(dataStatus);
			appCateName = getAppCateName(app.getRefAppCateId());
			dpb0013App.setAppCateName(appCateName);
			dpb0013AppList.add(dpb0013App);
		}
		return dpb0013AppList;
	}

	private String getAppCateName(Long appCateId) {
		String appCateName = null;

		if (appCateId != null) {
			Optional<TsmpDpAppCategory> opt = getTsmpDpAppCategoryDao().findById(appCateId);
			if (opt.isPresent()) {
				TsmpDpAppCategory category = opt.get();
				appCateName = category.getAppCateName();
			}
		}

		return appCateName;
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpappCategoryDao;
	}

	protected TsmpDpAppDao getTsmpDpAppDao() {
		return this.tsmpDpAppDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0013");
		return this.pageSize;
	}

}
