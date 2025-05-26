package tpi.dgrv4.dpaa.service;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0401Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0401Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpSettingService tsmpSettingService;
	private FileHelper fileHelper;
	
	@Autowired
	public AA0401Service(TsmpApiModuleDao tsmpApiModuleDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpSettingService tsmpSettingService, FileHelper fileHelper) {
		super();
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpSettingService = tsmpSettingService;
		this.fileHelper = fileHelper;
	}

	public AA0401Resp uploadModuleFile(TsmpAuthorization tsmpAuthorization, String uploadFileName, byte[] fileContent) {
		String userName = tsmpAuthorization.getUserName();
		String orgId = tsmpAuthorization.getOrgId();
		String fileUploadPath = checkParams(userName, orgId, uploadFileName, fileContent);
		
		try {
			uploadFileName = doUploadModuleFile(tsmpAuthorization, fileUploadPath, uploadFileName, fileContent);
			
			if (StringUtils.isEmpty(uploadFileName)) {
				throw new Exception("Failed to upload, cannot get upload target path.");
			}
			// 在相同路徑下產生 *.user 檔
			doUploadUserFile(fileUploadPath, uploadFileName, userName, orgId);
			
			// 重新命名上傳的模組檔案，檔名再加上後綴 ".ready" 字串，如：xxxx-fileName-v9.9.9.war.ready。
			doRenameModuleFile(fileUploadPath, uploadFileName);
			
			return new AA0401Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1203.throwing();
		}
	}

	protected String checkParams(String userName, String orgId, String uploadFileName, byte[] fileContent) {
		if (StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		if (fileContent == null || fileContent.length <= 0) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		// 擷取出上傳的原始檔名 (getOriginalFilename)，若無原始檔名，或原始檔名不包含 "-v" 字串，則 throw 1291，並以 log.debug 紀錄。
		String lowerFileName = uploadFileName.toLowerCase();
		if (StringUtils.isEmpty(uploadFileName) || lowerFileName.contains("-v") == false) {
			logger.debug("Filename must contains '-v': " + uploadFileName);
			throw TsmpDpAaRtnCode._1291.throwing();
		}
		
		boolean isV2 = false;
		// 擷取原始檔名的副檔名，若非 "jar" 或 "war" (忽略大小寫)，則 throw 1443。
		if (lowerFileName.endsWith("jar")) {
			isV2 = true;
		} else if (lowerFileName.endsWith("war")) {
			isV2 = false;
		} else {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
		
		// 若無法依照模組的架構取得對應的模組檔案上傳路徑，則 throw 1297。
		String fileUploadPath = null;
		if (isV2) {
			fileUploadPath = getV2FileUploadPath();
		} else {
			fileUploadPath = getV3FileUploadPath();
		}
		if (StringUtils.isEmpty(fileUploadPath)) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		// 將原始檔名以 "-v" 拆分成 [0]=檔名、[1]=版號，並查詢 TSMP_API_MODULE 中，是否有相同檔名 (module_name)，
		// 但 org_id 卻與 AA0401Req.orgId 相異的資料 (不同組織不能存在相同名稱的模組)，若有則 throw 1442。
		// (其他組織單位已存在相同名稱的模組：[org_id])
		String moduleName = uploadFileName.split("-v")[0];
		List<TsmpApiModule> mList = getTsmpApiModuleDao().queryByModuleName(moduleName);
		for (TsmpApiModule m : mList) {
			if (orgId.equals(m.getOrgId()) == false) {
				throw TsmpDpAaRtnCode._1442.throwing(m.getOrgId());
			}
		}
		
		return fileUploadPath;
	}

	protected String doUploadModuleFile(TsmpAuthorization auth, String fileUploadPath, String uploadFileName, byte[] fileContent) //
			throws Exception {
		// 產生出一組序號字串
		String seq = generateSeq();
		
		// 將序號字串與原始檔名以 "-" 串接，如：{{序號}}-{{原始檔名}}，做為上傳檔案的暫存檔名。
		String temporaryFileName = String.format("%s-%s", seq, uploadFileName);
		
		// 將 AA0401Req.file 內容寫到指定的路徑下，並命名為上步驟的暫存檔名，並以 log.debug 紀錄。
		File temporaryFile = null;
		Path path = Paths.get(fileUploadPath);
		if (!Files.exists(path)) {
			this.logger.debug("Creating directories: " + path);
			Files.createDirectories(path);
		}
		
		//checkmarx, Relative Path Traversal, 已通過中風險
		temporaryFileName = temporaryFileName.replaceAll("\\.\\.", "").replaceAll("/", "").replaceAll("\\\\", "");
		
		temporaryFile = new File(fileUploadPath, temporaryFileName);
		try (FileOutputStream fos = new FileOutputStream(temporaryFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);){
			
			this.logger.debug("Uploading file to... " + temporaryFile.getAbsolutePath());
			
			bos.write(fileContent);
			
			this.logger.debug("Upload successfully!");
			return temporaryFileName;
		}
	}

	protected void doUploadUserFile(String fileUploadPath, String uploadFileName, String userName, String orgId) //
			throws Exception {
		String userFileName = String.format("%s.user", uploadFileName);
		File userFile = new File(fileUploadPath, userFileName);
		
		try (FileWriter fw = new FileWriter(userFile);
			BufferedWriter bw = new BufferedWriter(fw)){
			
			this.logger.debug("Uploading file to... " + userFile.getAbsolutePath());
			bw.write(String.format("%s,%s", userName, orgId));
			this.logger.debug("Upload successfully!");
		}
	}

	protected void doRenameModuleFile(String fileUploadPath, String uploadFileName) throws Exception {
		File moduleFile = new File(fileUploadPath, uploadFileName);
		if (moduleFile.exists()) {
			String newFileName = String.format("%s.ready", uploadFileName);
			
			this.logger.debug(String.format("Renaming filename from %s to %s", uploadFileName, newFileName));
			File newModuleFile = new File(fileUploadPath, newFileName);
			boolean isRename = moduleFile.renameTo(newModuleFile);
			if(isRename) {
				this.logger.debug("Rename successfully!");
			}else {
				this.logger.debug("Rename fail!");
			}
		}
	}

	protected String generateSeq() {
		return Long.toString(Instant.now().toEpochMilli(), Character.MAX_RADIX);
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return tsmpApiModuleDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return tsmpOrganizationDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected String getV2FileUploadPath() {
		return getTsmpSettingService().getVal_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH();
	}

	protected String getV3FileUploadPath() {
		return getTsmpSettingService().getVal_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH();
	}

}