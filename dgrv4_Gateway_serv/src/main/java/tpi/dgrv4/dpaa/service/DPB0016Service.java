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
import tpi.dgrv4.dpaa.vo.DPB0016Req;
import tpi.dgrv4.dpaa.vo.DPB0016Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpApp;
import tpi.dgrv4.entity.repository.TsmpDpApiAppDao;
import tpi.dgrv4.entity.repository.TsmpDpAppDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0016Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpAppDao tsmpDpAppDao;
	private TsmpDpApiAppDao tsmpDpApiAppDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0016Service(TsmpDpAppDao tsmpDpAppDao, TsmpDpApiAppDao tsmpDpApiAppDao, TsmpDpFileDao tsmpDpFileDao,
			FileHelper fileHelper) {
		super();
		this.tsmpDpAppDao = tsmpDpAppDao;
		this.tsmpDpApiAppDao = tsmpDpApiAppDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0016Resp deleteAppById(TsmpAuthorization authorization, DPB0016Req req) {
		TsmpDpApp app = getApp(req.getAppId());
		if (app == null) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP.throwing();
		}

		// 狀態為啟用不可刪除
		if (TsmpDpDataStatus.ON.value().equals(app.getDataStatus())) {
			throw TsmpDpAaRtnCode.ERROR_DELETE_ACTIVE_DATA.throwing();
		}

		boolean isApiAppsDeleted = deleteApiApps(app.getAppId());
		if (!isApiAppsDeleted) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP.throwing();
		}
		
		boolean isAppDeleted = deleteApp(app);
		if (!isAppDeleted) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP.throwing();
		}

		// 刪檔案失敗時不拋錯, 避免因rollback造成實體檔案與tsmp_dp_file紀錄不一致
		try {
			deleteFiles(app.getAppId());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}

		return new DPB0016Resp();
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

	private boolean deleteApiApps(Long refAppId) {
		final long orgCount = getTsmpDpApiAppDao().countByRefAppId(refAppId);
		final long delCount = getTsmpDpApiAppDao().deleteByRefAppId(refAppId);
		final long aftCount = getTsmpDpApiAppDao().countByRefAppId(refAppId);
		return (orgCount == delCount && aftCount <= orgCount);
	}

	private boolean deleteApp(TsmpDpApp app) {
		final Long appId = app.getAppId();
		getTsmpDpAppDao().delete(app);
		boolean isExists = getTsmpDpAppDao().existsById(appId);
		return !isExists;
	}

	private boolean deleteFiles(Long refId) throws Exception {
		// 跟 TsmpDpApp 相關的檔案
		String tsmpDpFilePath_img = FileHelper.getTsmpDpFilePath(TsmpDpFileType.APP_IMAGE, refId);
		String tsmpDpFilePath_attachment = FileHelper.getTsmpDpFilePath(TsmpDpFileType.APP_ATTACHMENT, refId);

		boolean isImgsDeleted = false;
		
		//檔案(舊)
		isImgsDeleted = getFileHelper().remove01(tsmpDpFilePath_img, null, (filename) -> {
			List<TsmpDpFile> fileList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefIdAndFileName( //
					TsmpDpFileType.APP_IMAGE.value(), refId, filename);

			if (fileList != null && !fileList.isEmpty()) {
				fileList.forEach((file) -> {
					getTsmpDpFileDao().delete(file);
				});
			}
		});
		
		//DB(新)
		List<TsmpDpFile> fileDpList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefId( //
					TsmpDpFileType.APP_IMAGE.value(), refId);

		if (fileDpList != null && !fileDpList.isEmpty()) {
			fileDpList.forEach((file) -> {
				getTsmpDpFileDao().delete(file);
			});
			isImgsDeleted = true;
		}

		boolean isAttachmentDeleted = false;
		
		//檔案(舊)
		isAttachmentDeleted = getFileHelper().remove01(tsmpDpFilePath_attachment, null, (filename) -> {
			List<TsmpDpFile> fileList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefIdAndFileName( //
					TsmpDpFileType.APP_ATTACHMENT.value(), refId, filename);

			if (fileList != null && !fileList.isEmpty()) {
				fileList.forEach((file) -> {
					getTsmpDpFileDao().delete(file);
				});
			}
		});;
		
		//DB(新)
		List<TsmpDpFile> fileList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefId( //
					TsmpDpFileType.APP_ATTACHMENT.value(), refId);

		if (fileList != null && !fileList.isEmpty()) {
			fileList.forEach((file) -> {
				getTsmpDpFileDao().delete(file);
			});
			isAttachmentDeleted = true;
		}

		return (isImgsDeleted && isAttachmentDeleted);
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
