package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.base64Decode;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0019Req;
import tpi.dgrv4.dpaa.vo.DPB0019Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0019Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0019Service(TsmpDpApiThemeDao tsmpDpApiThemeDao, TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0019Resp addTheme(TsmpAuthorization authorization, DPB0019Req req) {
		String userName = authorization.getUserName();
		String orgId = authorization.getOrgId();
		if (userName == null || userName.isEmpty() ||
			orgId == null || orgId.isEmpty() ||
			req.getApiThemeName() == null || req.getApiThemeName().isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}

		try {
			Long apiThemeId = saveThemeCategory(userName, orgId, req);
			saveApiThemeList(userName, apiThemeId, req.getUseApis());
			saveThemeIcon(userName, apiThemeId //
					, req.getFileName(), req.getIconFileContent());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}
		
		return new DPB0019Resp();
	}

	private Long saveThemeCategory(String userName, String orgId, DPB0019Req req) {
		String apiThemeName = req.getApiThemeName();
		String dataStatus = getDataStatus(req.getDataStatus());
		Integer dataSort = getDataSort(req.getDataSort());
		
		TsmpDpThemeCategory themeCategory = new TsmpDpThemeCategory();
		themeCategory.setApiThemeName(apiThemeName);
		themeCategory.setDataStatus(dataStatus);
		themeCategory.setDataSort(dataSort);
		themeCategory.setOrgId(orgId);
		themeCategory.setCreateDateTime(DateTimeUtil.now());
		themeCategory.setCreateUser(userName);
		themeCategory = getTsmpDpThemeCategoryDao().save(themeCategory);
		return themeCategory.getApiThemeId();
	}

	private String getDataStatus(String dataStatus) {
		if (dataStatus == null || dataStatus.isEmpty()) {
			return TsmpDpDataStatus.ON.value();
		}
		return dataStatus;
	}

	private Integer getDataSort(String dataSort) {
		if (dataSort == null || dataSort.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(dataSort);
		} catch (NumberFormatException ne) {
			this.logger.error(StackTraceUtil.logStackTrace(ne));
			return null;
		}
	}

	private void saveApiThemeList(String userName //
			, Long apiThemeId, List<String> apiUids) {
		if (apiThemeId == null ||
			apiUids == null || apiUids.isEmpty()) {
			return;
		}

		TsmpDpApiTheme tsmpDpApiTheme;
		for(String apiUid : apiUids) {
			tsmpDpApiTheme = new TsmpDpApiTheme();
			tsmpDpApiTheme.setRefApiThemeId(apiThemeId);
			tsmpDpApiTheme.setRefApiUid(apiUid);
			tsmpDpApiTheme.setCreateDateTime(DateTimeUtil.now());
			tsmpDpApiTheme.setCreateUser(userName);
			tsmpDpApiTheme = getTsmpDpApiThemeDao().save(tsmpDpApiTheme);
		}
	}

	private void saveThemeIcon(String userName, Long apiThemeId //
			, String filename, String base64EncodedContent) {
		if (apiThemeId == null ||
			filename == null || filename.isEmpty() ||
			base64EncodedContent == null || base64EncodedContent.isEmpty()) {
			return;
		}

		try {
			// image base64
			if (base64EncodedContent.indexOf(",") != -1) {
				base64EncodedContent = base64EncodedContent.split(",")[1];
			}
			byte[] content = base64Decode(base64EncodedContent);
			if (content != null && content.length > 0) {
				TsmpDpFile iconFile = getFileHeler().upload(userName,TsmpDpFileType.API_TH, apiThemeId
						, filename, content, "N");
				/*String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.API_TH, apiThemeId);
				Path uploadFilePath = getFileHeler().upload(tsmpDpFilePath, filename, content);
				if (uploadFilePath != null) {
					TsmpDpFile iconFile = new TsmpDpFile();
					iconFile.setFileName(uploadFilePath.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
					iconFile.setFilePath(tsmpDpFilePath);
					iconFile.setRefFileCateCode(TsmpDpFileType.API_TH.value());
					iconFile.setRefId(apiThemeId);
					iconFile.setCreateUser(userName);
					iconFile = getTsmpDpFileDao().save(iconFile);
				}*/
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHeler() {
		return this.fileHelper;
	}

}
