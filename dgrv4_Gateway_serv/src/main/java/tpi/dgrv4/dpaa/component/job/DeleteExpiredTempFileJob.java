package tpi.dgrv4.dpaa.component.job;

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
public class DeleteExpiredTempFileJob extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private FileHelper fileHelper;
	private TsmpSettingService tsmpSettingService;
	
	private Long expMs;

	public DeleteExpiredTempFileJob(FileHelper fileHelper, TsmpSettingService tsmpSettingService) {
		super();
		this.fileHelper = fileHelper;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		// 預設1小時
		if (getExpMs() == null) {
			expMs = 3600000L;
			this.logger.debug(String.format("Set default temp file expire time to %d ms.", expMs));
		}

		try {
			final int delCnt = getFileHelper().deleteExpiredTempFile(getExpMs());
			this.logger.debug(String.format("%d file(s) has been deleted.", delCnt));
			
		} catch (Exception e) {
			this.logger.error(String.format("Delete expired temp file error! %s", StackTraceUtil.logStackTrace(e)));
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getGroupId(), this.getId()));
	}

	protected Long getExpMs() {
		this.expMs = getTsmpSettingService().getVal_FILE_TEMP_EXP_TIME();
		return this.expMs;
	}
	
	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
}
