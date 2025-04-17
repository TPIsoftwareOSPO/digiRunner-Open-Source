package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.base64Decode;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0022Req;
import tpi.dgrv4.dpaa.vo.DPB0022Resp;
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
public class DPB0022Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0022Service(TsmpDpApiThemeDao tsmpDpApiThemeDao, TsmpDpFileDao tsmpDpFileDao,
			TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, FileHelper fileHelper) {
		super();
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0022Resp updateThemeById(TsmpAuthorization authorization, DPB0022Req req) {
		String userName = authorization.getUserName();
		if (userName == null || userName.isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_THEME_CATE.throwing();
		}

		TsmpDpThemeCategory category = getCategory(req.getApiThemeId());
		if (category == null) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_THEME_CATE.throwing();
		}

		final Long versionBefore = category.getVersion();
		try {
			Long versionAfter = updateThemeCategory(userName, category, req);
			if (versionBefore.equals(versionAfter)) {
				throw TsmpDpAaRtnCode.FAIL_UPDATE_THEME_CATE.throwing();
			}

			updateApiThemeList(userName, category.getApiThemeId() //
					, req.getUseApis(), req.getOrgApiList());
			updateThemeIcon(userName, category.getApiThemeId() //
					, req.getFileName(), req.getIconFileContent(), req.getOrgIcon());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_THEME_CATE.throwing();
		}

		return new DPB0022Resp();
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

	private Long updateThemeCategory(String userName //
			, TsmpDpThemeCategory category, DPB0022Req req) {
		String apiThemeName = req.getApiThemeName();
		if (apiThemeName == null || apiThemeName.isEmpty()) {
			return category.getVersion();
		}

		String dataStatus = req.getDataStatus();
		Integer dataSort = getDataSort(req.getDataSort());
		
		category.setApiThemeName(apiThemeName);
		category.setDataStatus(dataStatus);
		category.setDataSort(dataSort);
		category.setUpdateDateTime(DateTimeUtil.now());
		category.setUpdateUser(userName);
		category = getTsmpDpThemeCategoryDao().saveAndFlush(category);
		return category.getVersion();
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

	private void updateApiThemeList(String userName, Long refApiThemeId //
			, List<String> newApiUids, List<String> oldApiUids) {
		
		/* req.getUseApis 只會放新增的資料; req.getOrgApiList 則是放修改後的舊資料
		if (oldApiUids != null && !oldApiUids.isEmpty()) {
			TsmpDpApiThemeId id = null;
			for(String refApiUid : oldApiUids) {
				id = null;
				if (newApiUids != null && newApiUids.contains(refApiUid)) {
					newApiUids.remove(refApiUid);
				} else {
					id = new TsmpDpApiThemeId(refApiThemeId, refApiUid);
					if (getTsmpDpApiThemeDao().existsById(id)) {
						getTsmpDpApiThemeDao().deleteById(id);
					}
				}
			}
		}
		*/
		
		if (oldApiUids == null || oldApiUids.isEmpty()) {
			getTsmpDpApiThemeDao().deleteByRefApiThemeId(refApiThemeId);
		} else {
			List<TsmpDpApiTheme> oldList = getTsmpDpApiThemeDao().findAllByRefApiThemeId(refApiThemeId);
			if (oldList != null && !oldList.isEmpty()) {
				for(TsmpDpApiTheme old : oldList) {
					if (!oldApiUids.contains(old.getRefApiUid())) {
						getTsmpDpApiThemeDao().delete(old);
					}
				}
			}
		}

		if (newApiUids != null && !newApiUids.isEmpty()) {
			TsmpDpApiTheme apiTheme = null;
			for(String refApiUid : newApiUids) {
				apiTheme = new TsmpDpApiTheme();
				apiTheme.setRefApiThemeId(refApiThemeId);
				apiTheme.setRefApiUid(refApiUid);
				apiTheme.setCreateDateTime(DateTimeUtil.now());
				apiTheme.setCreateUser(userName);
				apiTheme = getTsmpDpApiThemeDao().save(apiTheme);
			}
		}
	}

	private void updateThemeIcon(String userName, Long apiThemeId //
			, String newFilename, String base64EncodedContent, String oldFilename) {
		String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.API_TH, apiThemeId);

		if (newFilename != null && !newFilename.isEmpty() &&
			base64EncodedContent != null && !base64EncodedContent.isEmpty()) {

			// 有上傳新的圖示才須刪除舊磁碟及資料庫檔案
			if (oldFilename != null && !oldFilename.isEmpty()) {
				try {
					//檔案(舊)
					getFileHeler().remove01(tsmpDpFilePath, oldFilename //
							, (filename) -> {
						List<TsmpDpFile> oldIconFileList = getTsmpDpFileDao() //
								.findByRefFileCateCodeAndRefIdAndFileName( //
										TsmpDpFileType.API_TH.value(), apiThemeId, filename);
						if (oldIconFileList != null && !oldIconFileList.isEmpty()) {
							oldIconFileList.forEach((file) -> {
								getTsmpDpFileDao().delete(file);
							});
						}
					});
					
					//DB(新)
					List<TsmpDpFile> oldIconFileList = getTsmpDpFileDao() //
							.findByRefFileCateCodeAndRefId( //
									TsmpDpFileType.API_TH.value(), apiThemeId);
					if (oldIconFileList != null && !oldIconFileList.isEmpty()) {
						oldIconFileList.forEach((file) -> {
							getTsmpDpFileDao().delete(file);
						});
					}
				} catch (Exception e) {
					this.logger.error(String.format("Removing file error: %s, %s", oldFilename, StackTraceUtil.logStackTrace(e)));
				}
			}

			// 寫入新的磁碟及資料庫檔案
			// image base64
			if (base64EncodedContent.indexOf(",") != -1) {
				base64EncodedContent = base64EncodedContent.split(",")[1];
			}
			byte[] content = base64Decode(base64EncodedContent);
			if (content != null && content.length > 0) {
				try {
					TsmpDpFile iconFile = getFileHeler().upload(userName,TsmpDpFileType.API_TH, apiThemeId
							, newFilename, content, "N");
					/*Path uploadFilePath = getFileHeler().upload(tsmpDpFilePath, newFilename, content);
					if (uploadFilePath != null) {
						TsmpDpFile iconFile = new TsmpDpFile();
						iconFile.setFileName(uploadFilePath.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
						iconFile.setFilePath(tsmpDpFilePath);
						iconFile.setRefFileCateCode(TsmpDpFileType.API_TH.value());
						iconFile.setRefId(apiThemeId);
						iconFile.setCreateUser(userName);
						iconFile = getTsmpDpFileDao().save(iconFile);
					}*/
				} catch (Exception e) {
					this.logger.error(String.format("Uploading file error: %s, %s", newFilename, StackTraceUtil.logStackTrace(e)));
				}
			}
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
