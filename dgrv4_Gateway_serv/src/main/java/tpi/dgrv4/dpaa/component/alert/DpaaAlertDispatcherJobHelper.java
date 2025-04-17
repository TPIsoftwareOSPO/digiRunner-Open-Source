package tpi.dgrv4.dpaa.component.alert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.DpaaAlertDetectorJobCommand;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * Deal with relation file (TsmpDpFile) which connects between TsmpAlert and TsmpDpApptJob.
 * @author Kim
 *
 */
@Component
public class DpaaAlertDispatcherJobHelper {

	public static final String RELATION_FILE_NAME_TPLT = "{{alertId}}.relation";

	private final TPILogger logger = TPILogger.tl;

	private FileHelper fileHelper;
	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DpaaAlertDispatcherJobHelper(FileHelper fileHelper, TsmpDpApptJobDao tsmpDpApptJobDao,
			TsmpDpFileDao tsmpDpFileDao, TsmpSettingService tsmpSettingService) {
		super();
		this.fileHelper = fileHelper;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	public String buildRelationFileName(Long alertId) {
		String fileName = RELATION_FILE_NAME_TPLT;
		Map<String, String> params = new HashMap<>();
		params.put("alertId", String.valueOf(alertId));
		fileName = ServiceUtil.buildContent(fileName, params);
		return fileName;
	}

	public Optional<Long> parseAlertIdFromRelationFileName(String relationFileName) {
		Long alertId = null;
		if (StringUtils.hasLength(relationFileName) && relationFileName.indexOf(".") > -1) {
			try {
				alertId = Long.valueOf(relationFileName.split("\\.")[0]);
			} catch (Exception e) {
				this.logger.error("Invalid alertId in relation file name: " + relationFileName);
			}
		}
		return Optional.ofNullable(alertId);
	}
	
	// 預設關聯檔沒有檔案內容, 檔名範例: "10001.relation"
	public TsmpDpFile createRelationFile(String userName, Long alertId, Long apptJobId, byte[] content) {
		TsmpDpFileType fileType = TsmpDpFileType.TSMP_DP_APPT_JOB;
		Long refId = apptJobId;
		String filename = buildRelationFileName(alertId);
		String isTmpFile = "N";
		try {
			TsmpDpFile file = getFileHelper().upload(userName, fileType, refId, filename, content, isTmpFile);
			return file;
		} catch (SQLException e) {
			this.logger.error("Error occured when saving relation file." + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
	}

	/** 若關聯檔不存在, 要順便建立 */
	public DpaaAlertDetectorJobCommand readCommandFromRelationFileAndCreateIfNotExists(Long apptJobId, Long alertId) {
		String refFileCateCode = TsmpDpFileType.TSMP_DP_APPT_JOB.value();
		Long refId = apptJobId;
		String fileName = buildRelationFileName(alertId);
		List<TsmpDpFile> relationFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
			refFileCateCode, refId, fileName);
		TsmpDpFile relationFile = null;
		if (CollectionUtils.isEmpty(relationFiles)) {
			// 建立關聯檔
			relationFile = createRelationFile("SYSTEM", alertId, apptJobId, null);
			this.logger.debug(String.format("Relation file between TsmpApptJob(%d) and TsmpAlert(%d) is created: fileId=%d", //
				apptJobId, alertId, relationFile.getFileId()));
			return null;
		}
		
		relationFile = relationFiles.get(0);
		try {
			byte[] content = getFileHelper().download(relationFile);
			if (!(content == null || content.length == 0)) {
				return DpaaAlertDetectorJobCommand.resolve(new String(content));
			}
		} catch (Exception e) {
			this.logger.error(String.format("Unparsable command in relation file (apptJobId=%d)", apptJobId));
		}
		return null;
	}

	public Long clearRelationFile(Long apptJobId, Long alertId) {
		String relationFileName = buildRelationFileName(alertId);
		return updateRelationFileByApptJobIdAndFileName(apptJobId, relationFileName, null, false);
	}

	/** 回傳 apptJobId */
	public List<Long> updateRelationFileByAlertId(Long alertId, DpaaAlertDetectorJobCommand command, //
		boolean createFileIfNotExist) {
		String relationFileName = buildRelationFileName(alertId);
		List<TsmpDpApptJob> relatedApptJobs = findRelatedApptJob(relationFileName);
		if (CollectionUtils.isEmpty(relatedApptJobs)) {
			return null;
		}
		
		List<Long> updatedApptJobIds = relatedApptJobs.stream() //
		.map((relatedApptJob) -> {
			Long apptJobId = relatedApptJob.getApptJobId();
			updateRelationFileByApptJobIdAndFileName(apptJobId, relationFileName, //
				command, createFileIfNotExist);
			return apptJobId;
		}) //
		.filter(Objects::nonNull) //
		.collect(Collectors.toList());
		
		return updatedApptJobIds;
	}

	/** 回傳 alertId */
	public Long updateRelationFileByApptJobIdAndFileName(Long apptJobId, Long alertId, //
		DpaaAlertDetectorJobCommand command, boolean createFileIfNotExist) {
		String relationFileName = buildRelationFileName(alertId);
		return updateRelationFileByApptJobIdAndFileName(apptJobId, relationFileName, command, createFileIfNotExist);
	}

	/** 回傳 alertId */
	public Long updateRelationFileByApptJobIdAndFileName(Long apptJobId, String relationFileName, //
		DpaaAlertDetectorJobCommand command, boolean createFileIfNotExist) {
		// Check name of relation file
		Optional<Long> opt_alertId = parseAlertIdFromRelationFileName(relationFileName);
		if (!opt_alertId.isPresent()) {
			return null;
		}
		
		Long alertId = opt_alertId.get();
		
		List<TsmpDpFile> relationFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
			TsmpDpFileType.TSMP_DP_APPT_JOB.value(), apptJobId, relationFileName);
		if (CollectionUtils.isEmpty(relationFiles)) {
			if (!createFileIfNotExist) {
				this.logger.debug("Missing relation file of ApptJob " + apptJobId);
				return null;
			}
			createRelationFile("SYSTEM", alertId, apptJobId, command.toString().getBytes());
			return alertId;
		}

		// 狀態為 R 或 W 的排程都要更新 (正常是不會同時有兩個)
		for (TsmpDpFile relationFile : relationFiles) {
			try {
				if (command == null) {
					relationFile.setBlobData(null);
				} else {
					relationFile.setBlobData(command.toString().getBytes());
				}
				relationFile = getTsmpDpFileDao().save(relationFile);
			} catch (Exception e) {
				this.logger.error(String.format("Failed to set command into file id %d.\n%s", //
					relationFile.getFileId(), StackTraceUtil.logStackTrace(e)));
				alertId = null;	// 其中一筆更新失敗就當作沒更新成功
				break;
			}
		}
		return alertId;
	}

	public boolean isAlertDetectionEnabled() {
		int interval = getTsmpSettingService().getVal_TSMP_DPAA_RUNLOOP_INTERVAL();
		return (interval > 0);
	}
	
	public List<TsmpDpApptJob> findRelatedApptJob(Long alertId) {
		return findRelatedApptJob(null, alertId);
	}

	public List<TsmpDpApptJob> findRelatedApptJob(String fileName) {
		return findRelatedApptJob(null, fileName);
	}

	public List<TsmpDpApptJob> findRelatedApptJob(String refSubitemNo, Long alertId) {
		String fileName = buildRelationFileName(alertId);
		return findRelatedApptJob(refSubitemNo, fileName);
	}

	public List<TsmpDpApptJob> findRelatedApptJob(String refSubitemNo, String fileName) {
		List<String> statusList = Arrays.asList(new String[] { //
			TsmpDpApptJobStatus.WAIT.value(), TsmpDpApptJobStatus.RUNNING.value()
		});
		List<TsmpDpApptJob> relatedApptJobs = getTsmpDpApptJobDao().queryRunLoopJobByFileName( //
			refSubitemNo, fileName, statusList);
		return relatedApptJobs;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
}
