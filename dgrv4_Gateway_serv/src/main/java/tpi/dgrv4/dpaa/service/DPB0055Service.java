package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.dpaa.vo.DPB0055Req;
import tpi.dgrv4.dpaa.vo.DPB0055Resp;
import tpi.dgrv4.dpaa.vo.DPB0055Themes;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0055Service {

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0055Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
		this.serviceConfig = serviceConfig;
	}

	public DPB0055Resp queryThemeLikeList_1(TsmpAuthorization authorization, DPB0055Req req) {
		// 只能看到自己組織向下的主題
		String orgId = authorization.getOrgId();
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		TsmpDpThemeCategory lastRecord = getLastRecordFromPrevPage(req);
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		List<TsmpDpThemeCategory> themeList = getTsmpDpThemeCategoryDao() //
				.query_dpb0055Service(orgDescList, words, lastRecord, pageSize);
		if (themeList == null || themeList.isEmpty()) {
			throw TsmpDpAaRtnCode._1208.throwing();
		}

		DPB0055Resp resp = new DPB0055Resp();
		List<DPB0055Themes> dpb0055ThemeList = getDpb0055ThemeList(themeList);
		resp.setDataList(dpb0055ThemeList);
		return resp;
	}

	private TsmpDpThemeCategory getLastRecordFromPrevPage(DPB0055Req req) {
		Long themeId = req.getThemeId();
		if (themeId != null) {
			Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(themeId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0055Themes> getDpb0055ThemeList(List<TsmpDpThemeCategory> themeList){
		List<DPB0055Themes> dpb0055ThemeList = new ArrayList<>();

		DPB0055Themes dpb0055Themes;
		String dataStatus = null;
		String dataStatusName = null;
		String orgId = null;
		String orgName = null;
		TsmpDpFile file = null;
		Long fileId = null;
		String fileName = null;
		String filePath = null;
		for(TsmpDpThemeCategory theme : themeList) {
			dpb0055Themes = new DPB0055Themes();
			
			dataStatus = theme.getDataStatus();
			dataStatusName = TsmpDpDataStatus.text(dataStatus);
			orgId = theme.getOrgId();
			orgName = getOrgName(orgId);
			file = getDpFile(theme.getApiThemeId());
			fileId = null;
			fileName = new String();
			filePath = new String();
			
			dpb0055Themes.setThemeId(theme.getApiThemeId());
			dpb0055Themes.setThemeName(theme.getApiThemeName());
			dpb0055Themes.setDataStatus(dataStatus);
			dpb0055Themes.setDataStatusName(dataStatusName);
			dpb0055Themes.setDataSort(theme.getDataSort());
			dpb0055Themes.setOrgId(orgId);
			dpb0055Themes.setOrgName(orgName);
			if (file != null) {
				fileId = file.getFileId();
				fileName = file.getFileName();
				filePath = file.getFilePath() + fileName;
				dpb0055Themes.setFileId(fileId);
				dpb0055Themes.setFileName(fileName);
				dpb0055Themes.setFilePath(filePath);
			}
			dpb0055ThemeList.add(dpb0055Themes);
		}
		
		return dpb0055ThemeList;
	}

	private String getOrgName(String orgId) {
		if (StringUtils.isEmpty(orgId)) {
			return new String();
		}
		
		Optional<TsmpOrganization> dpb0055_opt = getTsmpOrganizationDao().findById(orgId);
		if (dpb0055_opt.isPresent()) {
			return dpb0055_opt.get().getOrgName();
		}

		return new String();
	}

	private TsmpDpFile getDpFile(Long refId) {
		List<TsmpDpFile> dpb0055_iconList = getTsmpDpFileDao()//
				.findByRefFileCateCodeAndRefId(TsmpDpFileType.API_TH.value(), refId);

		if (dpb0055_iconList != null && !dpb0055_iconList.isEmpty()) {
			return dpb0055_iconList.get(0);
		}

		return null;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0055");
		return this.pageSize;
	}

}
