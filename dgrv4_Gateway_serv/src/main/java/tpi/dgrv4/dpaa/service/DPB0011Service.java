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
import tpi.dgrv4.dpaa.vo.DPB0011Req;
import tpi.dgrv4.dpaa.vo.DPB0011Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpApp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.entity.repository.TsmpDpAppDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0011Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpAppCategoryDao tsmpDpAppCategoryDao;
	private TsmpDpAppDao tsmpDpAppDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0011Service(TsmpDpAppCategoryDao tsmpDpAppCategoryDao, TsmpDpAppDao tsmpDpAppDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpAppCategoryDao = tsmpDpAppCategoryDao;
		this.tsmpDpAppDao = tsmpDpAppDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0011Resp deleteAppCateById(TsmpAuthorization authorization, DPB0011Req req) {
		String userName = authorization.getUserName();
		if (userName == null || userName.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_TSMP_USER.throwing();
		}

		Long appCateId = req.getAppCateId();
		if (appCateId == null) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP_CATE.throwing();
		}

		boolean isExists = getTsmpDpAppCategoryDao().existsById(appCateId);
		if (!isExists) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP_CATE.throwing();
		}

		// 刪除應用實例分類也要連同底下的應用實例及其相關檔案一起刪除
		// 但不刪除應用實例所使用的API(TsmpDpApiApp)
		// 且仍需檢查應用實例是否啟用
		deleteApp(appCateId);

		try {
			getTsmpDpAppCategoryDao().deleteById(appCateId);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP_CATE.throwing();
		}

		// 檢查
		Optional<TsmpDpAppCategory> opt = getTsmpDpAppCategoryDao().findById(appCateId);
		if (opt.isPresent()) {
			throw TsmpDpAaRtnCode.FAIL_DELETE_APP_CATE.throwing();
		}

		return new DPB0011Resp();
	}

	private void deleteApp(Long appCateId) {
		List<TsmpDpApp> apps = getTsmpDpAppDao().findByRefAppCateId(appCateId);
		if (apps == null || apps.isEmpty()) {
			return;
		}

		boolean isExists = true;
		for(TsmpDpApp app : apps) {
			// 狀態為啟用不可刪除
			if (TsmpDpDataStatus.ON.value().equals(app.getDataStatus())) {
				throw TsmpDpAaRtnCode.ERROR_DELETE_ACTIVE_DATA.throwing();
			}

			getTsmpDpAppDao().delete(app);
			isExists = getTsmpDpAppDao().existsById(app.getAppId());
			if (isExists) {
				throw TsmpDpAaRtnCode.FAIL_DELETE_APP.throwing();
			}

			// 刪檔案失敗時不拋錯, 避免因rollback造成實體檔案與tsmp_dp_file紀錄不一致
			try {
				// 跟 TsmpDpApp 相關的檔案
				deleteFile(TsmpDpFileType.APP_IMAGE, app.getAppId());
				deleteFile(TsmpDpFileType.APP_ATTACHMENT, app.getAppId());
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}
	}

	private boolean deleteFile(TsmpDpFileType fileType, Long refId) throws Exception {
		
		boolean isDeleted = false;
		
		String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(fileType, refId);
		//檔案(舊)
		isDeleted = getFileHelper().remove01(tsmpDpFilePath, null, (filename) -> {
			List<TsmpDpFile> fileList = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefIdAndFileName( //
						fileType.value(), refId, filename);

			if (fileList != null && !fileList.isEmpty()) {
				fileList.forEach((file) -> {
					getTsmpDpFileDao().delete(file);
				});
			}
		});
		
		//DB(新)
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(fileType.value(), refId);
		if (fileList != null && !fileList.isEmpty()) {
			fileList.forEach((file) -> {
				getTsmpDpFileDao().delete(file);
			});
			isDeleted = true;
		}

		return isDeleted;
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpAppCategoryDao;
	}

	protected TsmpDpAppDao getTsmpDpAppDao() {
		return this.tsmpDpAppDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
