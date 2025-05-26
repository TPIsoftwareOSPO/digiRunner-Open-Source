package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0053Req;
import tpi.dgrv4.dpaa.vo.DPB0053Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0053Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0053Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpDpFileDao tsmpDpFileDao,
			FileHelper fileHelper) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0053Resp createTheme(TsmpAuthorization authorization, DPB0053Req req) {
		final String themeName = req.getThemeName();
		final String tempFilename = req.getFileName();
		final String userName = authorization.getUserName();
		final String orgId = authorization.getOrgId();
		if (StringUtils.isEmpty(themeName) ||
			StringUtils.isEmpty(tempFilename) ||
			StringUtils.isEmpty(userName) ||
			StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}
		
		// 2020/06/01; Kim; 檢查主題名稱是否已經存在
		checkThemeName(themeName);
		
		// 確定暫存檔是否存在
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByFileName(tempFilename);
		if(fileList == null || fileList.size() != 1) {
			throw TsmpDpAaRtnCode._1204.throwing();
		}
		/*String tsmpDpFilePath = getFileHelper().getUploadTemp();
		boolean isTempFileExists = getFileHelper().exists(tsmpDpFilePath, tempFilename);
		if (!isTempFileExists) {
			throw TsmpDpAaRtnCode._1204.throwing();
		}*/ 

		// 儲存主題分類
		final Long themeId = saveTheme(themeName, orgId, userName);

		// 儲存檔案
		final Long fileId = saveFile(themeId, tempFilename, userName);

		DPB0053Resp resp = new DPB0053Resp();
		resp.setFileId(fileId);
		resp.setThemeId(themeId);
		return resp;
	}

	protected void checkThemeName(String themeName) {
		// John: 不同dataStatus跟orgId也不能有相同的themeName
		long cnt = getTsmpDpThemeCategoryDao().countByApiThemeName(themeName);
		if (cnt > 0) {
			this.logger.error("已有相同名稱的主題");
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}
	}

	private Long saveTheme(String themeName, String orgId, String userName) {
		TsmpDpThemeCategory themeCategory = new TsmpDpThemeCategory();
		try {
			themeCategory.setApiThemeName(themeName);
			themeCategory.setOrgId(orgId);
			themeCategory.setCreateDateTime(DateTimeUtil.now());
			themeCategory.setCreateUser(userName);
			themeCategory = getTsmpDpThemeCategoryDao().save(themeCategory);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}

		final Long apiThemeId = themeCategory.getApiThemeId();
		if (apiThemeId == null) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}

		return apiThemeId;
	}

	private Long saveFile(Long refId, String tempFilename, String userName) {

		// 紀錄資料庫
		TsmpDpFile iconFile = null;
		try {
			iconFile = getFileHelper().moveTemp(userName, TsmpDpFileType.API_TH, refId, tempFilename, true, false);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}
		
		if(iconFile == null) {
			throw TsmpDpAaRtnCode._1204.throwing();
		}

		final Long fileId = iconFile.getFileId();
		if (fileId == null) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_THEME_CATE.throwing();
		}

		return fileId;
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

}
