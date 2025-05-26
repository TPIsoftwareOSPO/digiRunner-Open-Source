package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0012Req;
import tpi.dgrv4.dpaa.vo.DPB0012Resp;
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
public class DPB0012Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpAppDao tsmpDpAppDao;
	private TsmpDpApiAppDao tsmpDpApiAppDao;
	private FileHelper fileHelper;
	private TsmpDpFileDao tsmpDpFileDao;

	@Autowired
	public DPB0012Service(TsmpDpAppDao tsmpDpAppDao, TsmpDpApiAppDao tsmpDpApiAppDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao) {
		super();
		this.tsmpDpAppDao = tsmpDpAppDao;
		this.tsmpDpApiAppDao = tsmpDpApiAppDao;
		this.fileHelper = fileHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	@Transactional
	public DPB0012Resp addApp(TsmpAuthorization authorization, DPB0012Req req) {
		String appName = req.getName();
		Long refAppCateId = req.getRefAppCateId();
		String appIntro = req.getIntro();
		if (appName == null || appName.isEmpty() ||
			refAppCateId == null ||
			appIntro == null || appIntro.isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_CREATE_APP_REQUIRED.throwing();
		}

		try {
			TsmpDpApp app = saveApp(authorization, req);
			if (app.getAppId() != null) {
				saveIcon(app, req.getIcon());
				saveIntroFiles(app, req.getIntroFiles());
				saveTsmpDpApiApp(app, req.getUseApis());
			}

			return new DPB0012Resp();
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_CREATE_APP.throwing();
		}
	}

	private TsmpDpApp saveApp(TsmpAuthorization authorization, DPB0012Req req) {
		TsmpDpApp app = new TsmpDpApp();
		app.setRefAppCateId(req.getRefAppCateId());
		app.setName(req.getName());
		app.setIntro(req.getIntro());
		app.setAuthor(req.getAuthor());
		String dataStatus = getDataStatus(req.getDataStatus());
		app.setDataStatus(dataStatus);
		app.setOrgId(authorization.getOrgId());
		app.setCreateDateTime(DateTimeUtil.now());
		app.setCreateUser(authorization.getUserName());
		app = getTsmpDpAppDao().save(app);
		return app;
	}

	private String getDataStatus(String dataStatus) {
		if (dataStatus == null || dataStatus.isEmpty()) {
			return "1";
		}
		return dataStatus;
	}

	private TsmpDpFile saveIcon(TsmpDpApp app, Map<String, String> icon) throws Exception {
		TsmpDpFile iconFile = null;

		if (icon == null || icon.size() != 1) {
			throw new Exception("Wrong icon size!");
		}

		// 只會有一筆icon
		//String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.APP_IMAGE, app.getAppId());
		String filename, base64EncodedContent;
		for(Map.Entry<String, String> entry : icon.entrySet()) {
			filename = entry.getKey();
			base64EncodedContent = entry.getValue();

			if (base64EncodedContent != null && !base64EncodedContent.isEmpty()) {
				// image base64
				if (base64EncodedContent.indexOf(",") != -1) {
					base64EncodedContent = base64EncodedContent.split(",")[1];
				}
				byte[] content = ServiceUtil.base64Decode(base64EncodedContent);
				iconFile = getFileHelper().upload(app.getCreateUser(),TsmpDpFileType.APP_IMAGE, app.getAppId()
						, filename, content, "N");
				/*Path uploadFilePath = getFileHelper().upload(tsmpDpFilePath, filename, content);
				if (uploadFilePath != null) {
					iconFile = new TsmpDpFile();
					iconFile.setFileName(uploadFilePath.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
					iconFile.setFilePath(tsmpDpFilePath);
					iconFile.setRefFileCateCode(TsmpDpFileType.APP_IMAGE.value());
					iconFile.setRefId(app.getAppId());
					iconFile.setCreateUser(app.getCreateUser());
					iconFile = getTsmpDpFileDao().save(iconFile);
				}*/
			}
		}
		
		return iconFile;
	}

	private List<TsmpDpFile> saveIntroFiles(TsmpDpApp app, List<Map<String, String>> introFiles) throws Exception {
		List<TsmpDpFile> fileList = null;

		if (introFiles == null || introFiles.isEmpty()) {
			return fileList;
		}

		fileList = new ArrayList<>();

		TsmpDpFile introFile;
		//String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.APP_ATTACHMENT, app.getAppId());
		String filename, base64EncodedContent;
		for(Map<String, String> fileEntry : introFiles) {
			for(Map.Entry<String, String> entry : fileEntry.entrySet()) {
				filename = entry.getKey();
				base64EncodedContent = entry.getValue();

				if (base64EncodedContent != null && !base64EncodedContent.isEmpty()) {
					byte[] content = ServiceUtil.base64Decode(base64EncodedContent);
					introFile = getFileHelper().upload(app.getCreateUser(),TsmpDpFileType.APP_ATTACHMENT, app.getAppId()
							, filename, content, "N");
					if(introFile != null) {
						fileList.add(introFile);
					}
					/*Path uploadFilePath = getFileHelper().upload(tsmpDpFilePath, filename, content);
					if (uploadFilePath != null) {
						introFile = new TsmpDpFile();
						introFile.setFileName(uploadFilePath.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
						introFile.setFilePath(tsmpDpFilePath);
						introFile.setRefFileCateCode(TsmpDpFileType.APP_ATTACHMENT.value());
						introFile.setRefId(app.getAppId());
						introFile.setCreateUser(app.getCreateUser());
						introFile = getTsmpDpFileDao().save(introFile);
						fileList.add(introFile);
					}*/
				}
			}
		}
		
		return fileList;
	}

	private List<TsmpDpApiApp> saveTsmpDpApiApp(TsmpDpApp app, List<String> apiUids) {
		List<TsmpDpApiApp> apiApps = null;

		if (apiUids == null || apiUids.isEmpty()) {
			return apiApps;
		}
		
		apiApps = new ArrayList<>();
		TsmpDpApiApp apiApp;
		for(String apiUid : apiUids) {
			apiApp = new TsmpDpApiApp();
			apiApp.setRefAppId(app.getAppId());
			apiApp.setRefApiUid(apiUid);
			apiApp.setCreateDateTime(DateTimeUtil.now());
			apiApp.setCreateUser(app.getCreateUser());
			apiApp = getTsmpDpApiAppDao().save(apiApp);
			apiApps.add(apiApp);
		}

		return apiApps;
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
