package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0015Req;
import tpi.dgrv4.dpaa.vo.DPB0015Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiApp;
import tpi.dgrv4.entity.entity.sql.TsmpDpApp;
import tpi.dgrv4.entity.repository.TsmpDpApiAppDao;
import tpi.dgrv4.entity.repository.TsmpDpAppDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0015Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpAppDao tsmpDpAppDao;
	private FileHelper fileHelper;
	private TsmpDpApiAppDao tsmpDpApiAppDao;
	private TsmpDpFileDao tsmpDpFileDao;

	@Autowired
	public DPB0015Service(TsmpDpAppDao tsmpDpAppDao, FileHelper fileHelper, TsmpDpApiAppDao tsmpDpApiAppDao,
			TsmpDpFileDao tsmpDpFileDao) {
		super();
		this.tsmpDpAppDao = tsmpDpAppDao;
		this.fileHelper = fileHelper;
		this.tsmpDpApiAppDao = tsmpDpApiAppDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	@Transactional
	public DPB0015Resp updateAppById(TsmpAuthorization authorization, DPB0015Req req) {
		String userName = authorization.getUserName();
		String appName = req.getName();
		String intro = req.getIntro();
		Long refAppCateId = req.getRefAppCateId();
		if (userName == null || userName.isEmpty() ||
			appName == null || appName.isEmpty() ||
			intro == null || intro.isEmpty() ||
			refAppCateId == null) {
			throw TsmpDpAaRtnCode.FAIL_UPDATE_APP.throwing();
		}

		TsmpDpApp app = getApp(req.getAppId());
		if (app == null) {
			throw TsmpDpAaRtnCode.NO_APP_DATA.throwing();
		}

		try {
			app.setName(appName);
			app.setRefAppCateId(refAppCateId);
			app.setIntro(intro);
			app.setAuthor(req.getAuthor());
			app.setDataStatus(getDataStatus(req.getDataStatus()));
			app.setUpdateDateTime(DateTimeUtil.now());
			app.setUpdateUser(userName);
			app = getTsmpDpAppDao().saveAndFlush(app);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_APP.throwing();
		}

		try {
			updateApiApps(app.getAppId(), userName, req.getUseApis(), req.getOrgUseApis());
			updateIcon(app.getAppId(), userName, req.getIcon(), req.getOrgIcon());
			updateIntroFiles(app.getAppId(), userName, req.getIntroFiles(), req.getOrgIntroFiles());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_UPDATE_APP.throwing();
		}

		return new DPB0015Resp();
	}

	private TsmpDpApp getApp(Long appId) {
		if (appId != null) {
			Optional<TsmpDpApp> opt = getTsmpDpAppDao().findById(appId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private String getDataStatus(String dataStatus) {
		if (dataStatus == null || dataStatus.isEmpty()) {
			return "1";
		}
		return dataStatus;
	}

	private void updateApiApps(Long refAppId, String userName //
			, List<String> newApiUids, List<String> oldApiUids) {

		/* req.getUseApis 只會放新增的資料; req.getOrgUseApis 則是放修改後的舊資料
		if (oldApiUids != null && !oldApiUids.isEmpty()) {
			TsmpDpApiAppId id = null;
			for(String refApiUid : oldApiUids) {
				id = null;
				if (newApiUids != null && newApiUids.contains(refApiUid)) {
					newApiUids.remove(refApiUid);
				} else {
					id = new TsmpDpApiAppId(refAppId, refApiUid);
					if (getTsmpDpApiAppDao().existsById(id)) {
						getTsmpDpApiAppDao().deleteById(id);
					}
				}
			}
		}
		*/

		if (oldApiUids == null || oldApiUids.isEmpty()) {
			getTsmpDpApiAppDao().deleteByRefAppId(refAppId);
		} else {
			List<TsmpDpApiApp> oldList = getTsmpDpApiAppDao().findAllByRefAppId(refAppId);
			if (oldList != null && !oldList.isEmpty()) {
				for(TsmpDpApiApp old : oldList) {
					if (!oldApiUids.contains(old.getRefApiUid())) {
						getTsmpDpApiAppDao().delete(old);
					}
				}
			}
		}

		if (newApiUids != null && !newApiUids.isEmpty()) {
			TsmpDpApiApp apiApp = null;
			for(String refApiUid : newApiUids) {
				apiApp = new TsmpDpApiApp();
				apiApp.setRefAppId(refAppId);
				apiApp.setRefApiUid(refApiUid);
				apiApp.setCreateDateTime(DateTimeUtil.now());
				apiApp.setCreateUser(userName);
				apiApp = getTsmpDpApiAppDao().save(apiApp);
			}
		}
	}

	private void updateIcon(Long appId, String userName //
			, Map<String, String> newIcon, String oldIconFilename) throws Exception {
		String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.APP_IMAGE, appId);
		
		if (newIcon != null && !newIcon.isEmpty()) {
			// 若有上傳新圖示才需刪掉舊圖示
			if (oldIconFilename != null && !oldIconFilename.isEmpty()) {
				//檔案(舊)
				getFileHelper().remove01(tsmpDpFilePath, oldIconFilename //
						, (filename) -> {
					List<TsmpDpFile> oldIconFileList = getTsmpDpFileDao() //
							.findByRefFileCateCodeAndRefIdAndFileName( //
									TsmpDpFileType.APP_IMAGE.value(), appId, filename);
					if (oldIconFileList != null && !oldIconFileList.isEmpty()) {
						oldIconFileList.forEach((file) -> {
							getTsmpDpFileDao().delete(file);
						});
					}
				});
				
				//DB(新)
				List<TsmpDpFile> oldIconFileList = getTsmpDpFileDao() //
						.findByRefFileCateCodeAndRefId( //
								TsmpDpFileType.APP_IMAGE.value(), appId);
				if (oldIconFileList != null && !oldIconFileList.isEmpty()) {
					oldIconFileList.forEach((file) -> {
						getTsmpDpFileDao().delete(file);
					});
				}
			}

			TsmpDpFile iconFile = null;
			String filename, base64EncodedContent;
			for(Map.Entry<String, String> entry : newIcon.entrySet()) {
				filename = entry.getKey();
				base64EncodedContent = entry.getValue();

				if (base64EncodedContent != null && !base64EncodedContent.isEmpty()) {
					// image base64
					if (base64EncodedContent.indexOf(",") != -1) {
						base64EncodedContent = base64EncodedContent.split(",")[1];
					}
					byte[] content = ServiceUtil.base64Decode(base64EncodedContent);
					iconFile = getFileHelper().upload(userName,TsmpDpFileType.APP_IMAGE, appId
							, filename, content, "N");
					/*Path uploadFilePath = getFileHelper().upload(tsmpDpFilePath, filename, content);
					if (uploadFilePath != null) {
						iconFile = new TsmpDpFile();
						iconFile.setFileName(uploadFilePath.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
						iconFile.setFilePath(tsmpDpFilePath);
						iconFile.setRefFileCateCode(TsmpDpFileType.APP_IMAGE.value());
						iconFile.setRefId(appId);
						iconFile.setCreateUser(userName);
						iconFile = getTsmpDpFileDao().save(iconFile);
					}*/
				}
			}
		}
	}

	private void updateIntroFiles(Long appId, String userName //
			, List<Map<String, String>> newIntroFiles, List<String> oldIntroFileNames) throws Exception {
		String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.APP_ATTACHMENT, appId);

		/* req.getIntroFiles 只會放新增的資料; req.getOrgIntroFiles 則是放修改後的舊資料
		if (oldIntroFileNames != null && !oldIntroFileNames.isEmpty()) {
			for(String oldIntroFileName : oldIntroFileNames) {
				getFileHelper().remove(tsmpDpFilePath, oldIntroFileName //
					, (filename) -> {
					List<TsmpDpFile> oldIntroFileList = getTsmpDpFileDao() //
							.findByRefFileCateCodeAndRefIdAndFileName( //
									TsmpDpFileType.APP_ATTACHMENT.value(), appId, filename);
					if (oldIntroFileList != null && !oldIntroFileList.isEmpty()) {
						oldIntroFileList.forEach((file) -> {
							getTsmpDpFileDao().delete(file);
						});
					}
				});
			}
		}
		*/

		if (oldIntroFileNames == null || oldIntroFileNames.isEmpty()) {
			//檔案(舊)
			getFileHelper().remove01(tsmpDpFilePath, null, (filename) -> {
				getTsmpDpFileDao().deleteByRefFileCateCodeAndRefIdAndFileName( //
						TsmpDpFileType.APP_ATTACHMENT.value(), appId, filename);
			});
			//DB(新)
			getTsmpDpFileDao().deleteByRefFileCateCodeAndRefId( //
					TsmpDpFileType.APP_ATTACHMENT.value(), appId);
		} else {
			List<TsmpDpFile> oldList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
					TsmpDpFileType.APP_ATTACHMENT.value(), appId);
			if (oldList != null && !oldList.isEmpty()) {
				for(TsmpDpFile old : oldList) {
					if (!oldIntroFileNames.contains(old.getFileName())) {
						if("Y".equals(old.getIsBlob()) && old.getBlobData() != null) {
							getTsmpDpFileDao().delete(old);
						}else {
							getFileHelper().remove01(old.getFilePath(), old.getFileName(), (filename) -> {
								getTsmpDpFileDao().delete(old);
						});
						}
						
					}
				}
			}
		}

		if (newIntroFiles != null && !newIntroFiles.isEmpty()) {
			TsmpDpFile introFile;
			String filename, base64EncodedContent;
			for(Map<String, String> fileEntry : newIntroFiles) {
				for(Map.Entry<String, String> entry : fileEntry.entrySet()) {
					filename = entry.getKey();
					base64EncodedContent = entry.getValue();

					if (base64EncodedContent != null && !base64EncodedContent.isEmpty()) {
						byte[] content = ServiceUtil.base64Decode(base64EncodedContent);
						introFile = getFileHelper().upload(userName,TsmpDpFileType.APP_ATTACHMENT, appId
								, filename, content, "N");
						/*Path uploadFilePath = getFileHelper().upload(tsmpDpFilePath, filename, content);
						if (uploadFilePath != null) {
							introFile = new TsmpDpFile();
							introFile.setFileName(uploadFilePath.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
							introFile.setFilePath(tsmpDpFilePath);
							introFile.setRefFileCateCode(TsmpDpFileType.APP_ATTACHMENT.value());
							introFile.setRefId(appId);
							introFile.setCreateUser(userName);
							introFile = getTsmpDpFileDao().save(introFile);
						}*/
					}
				}
			}
		}
	}

	protected TsmpDpAppDao getTsmpDpAppDao() {
		return this.tsmpDpAppDao;
	}

	protected TsmpDpApiAppDao getTsmpDpApiAppDao() {
		return this.tsmpDpApiAppDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
