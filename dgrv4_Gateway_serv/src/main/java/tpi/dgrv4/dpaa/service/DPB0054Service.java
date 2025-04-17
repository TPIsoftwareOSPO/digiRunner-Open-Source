package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0054Req;
import tpi.dgrv4.dpaa.vo.DPB0054Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0054Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0054Service(TsmpDpFileDao tsmpDpFileDao, TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			TsmpOrganizationDao tsmpOrganizationDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0054Resp updateTheme(TsmpAuthorization authorization, DPB0054Req req) {
		final String userName = authorization.getUserName();
		final String themeName_n = req.getThemeName();
		final Long themeId = req.getThemeId();
		final Long lv = req.getLv();
		String orgId = authorization.getOrgId();
		
		//chk param
		if(StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}		
		if(StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}	
		if(StringUtils.isEmpty(themeName_n)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}	
		if(StringUtils.isEmpty(lv)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}	
		if(StringUtils.isEmpty(themeId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}	
		
		// 檢查主題是否存在
		TsmpDpThemeCategory theme_o = getTheme(themeId);
		if (theme_o == null) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_THEME_CATE.throwing();
		}else {
			// 組織是否符合
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			String reqOrgId = theme_o.getOrgId();
			if (!StringUtils.isEmpty(reqOrgId)) {
				if (CollectionUtils.isEmpty(orgDescList) || !orgDescList.contains(reqOrgId)) {
					this.logger.debug(String.format("非所屬組織權限: %s not in %s", reqOrgId, orgDescList));
					throw TsmpDpAaRtnCode._1222.throwing();
				}
			}
		}

		// 更新主題(雙欄位)
		TsmpDpThemeCategory theme_n = null;
		try {
			theme_n = updateTheme(theme_o, themeName_n, lv, userName);
		} catch (ObjectOptimisticLockingFailureException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_THEME_CATE.throwing();
		}

		// 更新主題圖示
		final String tempFilename = req.getFileName();
		Long fileId = updateThemeIcon(themeId, tempFilename, userName);

		DPB0054Resp resp = new DPB0054Resp();
		resp.setThemeId(theme_n.getApiThemeId());
		resp.setLv(theme_n.getVersion());
		resp.setFileId(fileId);
		return resp;
	}

	private TsmpDpThemeCategory getTheme(Long apiThemeId) {
		Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(apiThemeId);
		return opt.orElse(null);
	}

	private TsmpDpThemeCategory updateTheme(TsmpDpThemeCategory theme_o, String themeName_n, Long lv, String userName) {
		// 深層複製一個新的 Bean, 才能檢查到 version
		TsmpDpThemeCategory theme_n = ServiceUtil.deepCopy(theme_o, TsmpDpThemeCategory.class);
		theme_n.setApiThemeName(themeName_n);
		theme_n.setUpdateDateTime(DateTimeUtil.now());
		theme_n.setUpdateUser(userName);
		theme_n.setVersion(lv);
		return getTsmpDpThemeCategoryDao().saveAndFlush(theme_n);
	}

	private Long updateThemeIcon(Long refId, String tempFilename, String userName) {
		// 表示不用更新
		if (StringUtils.isEmpty(tempFilename)) {
			return null;
		}

		List<TsmpDpFile> files = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.API_TH.value(), refId);

		// 若已存在則刪除再新增
		if (files != null && !files.isEmpty()) {
			files.forEach((file) -> {
				deleteFile(file);
			});
		}

		return saveFile(refId, tempFilename, userName);
	}

	private void deleteFile(TsmpDpFile dpFile) {
		try {
			if("Y".equals(dpFile.getIsBlob()) && dpFile.getBlobData() != null) {
				getTsmpDpFileDao().delete(dpFile);
			}else {
				getFileHelper().remove01(dpFile.getFilePath(), dpFile.getFileName() //
						, (filename) -> {
					getTsmpDpFileDao().delete(dpFile);
				});
			}
			
		} catch (Exception e) {
			this.logger.error(String.format("Removing file error: %s, %s", dpFile.getFileName(), 
					StackTraceUtil.logStackTrace(e)));
		}
	}

	// 搬移暫存檔到指定路徑
	private Long saveFile(Long refId, String tempFilename, String userName) {
		// 紀錄資料庫
		TsmpDpFile iconFile = null;
		try {
			iconFile = getFileHelper().moveTemp(userName, TsmpDpFileType.API_TH, refId, tempFilename, true, false);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1206.throwing();
		}

		if(iconFile == null) {
			throw TsmpDpAaRtnCode._1206.throwing();
		}

		final Long fileId = iconFile.getFileId();
		if (fileId == null) {
			throw TsmpDpAaRtnCode._1206.throwing();
		}

		return fileId;		
		
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
