package tpi.dgrv4.dpaa.component.job;


import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 由 {@link FileHelper#uploadTemp} 方法發動
 * 刪除過期的暫存檔案
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class DeleteExpiredTempFileTsmpDpFileJob extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private FileHelper fileHelper;
	private TsmpSettingService tsmpSettingService;
	
	private Long expMs;
	private final boolean switchPatternFileName;
	
	public DeleteExpiredTempFileTsmpDpFileJob(boolean switchPatternFileName, FileHelper fileHelper, TsmpSettingService tsmpSettingService) {
		super();
		this.switchPatternFileName = switchPatternFileName;
		this.fileHelper = fileHelper;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		// 預設1小時
		if (getExpMs() == null) {
			expMs = 3600000L;
			this.logger.debug(String.format("Set default temp file TSMP_DP_FILE expire time to %d ms.", expMs));
		}

		try {
			final int delCntDb = getFileHelper().deleteExpiredTempByTsmpDpFile(getExpMs(), switchPatternFileName);
			this.logger.debug(String.format("%d TSMP_DP_FILE has been deleted.", delCntDb));
		} catch (Exception e) {
			this.logger.error(String.format("Delete expired temp file TSMP_DP_FILE error! %s", StackTraceUtil.logStackTrace(e)));
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getGroupId(), this.getId()));
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
	protected Long getExpMs() {
		this.expMs = getTsmpSettingService().getVal_FILE_TEMP_EXP_TIME();
		return this.expMs;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

}
