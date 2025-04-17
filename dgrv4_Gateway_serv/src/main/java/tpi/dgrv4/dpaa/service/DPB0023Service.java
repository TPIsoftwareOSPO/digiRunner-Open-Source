package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0023Req;
import tpi.dgrv4.dpaa.vo.DPB0023Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0023Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0023Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0023Resp deleteThemeById(TsmpAuthorization authorization, DPB0023Req req) {
		TsmpDpThemeCategory category = getCategory(req.getApiThemeId());
		if (category == null) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_THEME_CATE.throwing();
		}

		// 狀態為啟用不可刪除
		if (TsmpDpDataStatus.ON.value().equals(category.getDataStatus())) {
			throw TsmpDpAaRtnCode.ERROR_DELETE_ACTIVE_DATA.throwing();
		}

		// 先刪關聯表
		boolean isApiThemesDeleted = deleteApiThemes(category.getApiThemeId());
		if (!isApiThemesDeleted) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_THEME_CATE.throwing();
		}

		// 再刪主表
		boolean isThemeCategoryDeleted = deleteThemeCategory(category);
		if (!isThemeCategoryDeleted) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_THEME_CATE.throwing();
		}

		// 刪檔案失敗時不拋錯, 避免因rollback造成實體檔案與tsmp_dp_file紀錄不一致
		try {
			deleteThemeIcon(category.getApiThemeId());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}

		return new DPB0023Resp();
	}

	private TsmpDpThemeCategory getCategory(Long apiThemeId) {
		if (apiThemeId != null) {
			Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(apiThemeId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private boolean deleteApiThemes(Long refApiThemeId) {
		final long orgCount = getTsmpDpApiThemeDao().countByRefApiThemeId(refApiThemeId);
		final long delCount = getTsmpDpApiThemeDao().deleteByRefApiThemeId(refApiThemeId);
		final long aftCount = getTsmpDpApiThemeDao().countByRefApiThemeId(refApiThemeId);
		return (orgCount == delCount && aftCount <= orgCount);
	}

	private boolean deleteThemeCategory(TsmpDpThemeCategory themeCategory) {
		final Long apiThemeId = themeCategory.getApiThemeId();
		getTsmpDpThemeCategoryDao().delete(themeCategory);
		boolean isExists = getTsmpDpThemeCategoryDao().existsById(apiThemeId);
		return !isExists;
	}

	private boolean deleteThemeIcon(Long refId) throws Exception {
		// 跟 TsmpDpThemeCategory 相關的檔案
		String tsmpDpFilePath_icon = FileHelper.getTsmpDpFilePath(TsmpDpFileType.API_TH, refId);

		//檔案(舊)
		boolean isIconDeleted = false;
		isIconDeleted = getFileHelper().remove01(tsmpDpFilePath_icon, null, (filename) -> {
			List<TsmpDpFile> fileList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefIdAndFileName( //
					TsmpDpFileType.API_TH.value(), refId, filename);

			if (fileList != null && !fileList.isEmpty()) {
				fileList.forEach((file) -> {
					getTsmpDpFileDao().delete(file);
				});
			}
		});
		
		//DB(新)
		List<TsmpDpFile> fileList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefId( //
					TsmpDpFileType.API_TH.value(), refId);

			if (fileList != null && !fileList.isEmpty()) {
				fileList.forEach((file) -> {
					getTsmpDpFileDao().delete(file);
				});
			}

		return isIconDeleted;
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

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
