package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.composer.ComposerService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class ComposerServiceJob extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private ComposerService composerService;

	private Integer act;

	private List<String> apiUUIDs;

	public ComposerServiceJob(Integer act, List<String> apiUUIDs, ComposerService composerService) {
		this.act = act;
		this.apiUUIDs = apiUUIDs;
		this.composerService = composerService;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		if (this.act == null || CollectionUtils.isEmpty(this.apiUUIDs)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if (1 == this.act) {
			for (String apiUUID : this.apiUUIDs) {
				try {
					getComposerService().confirmAllNodes(apiUUID);
				} catch (Exception e) {
					this.logger.debug("Confirm to nodes error: [" + apiUUID + "]\n" + StackTraceUtil.logStackTrace(e));
				}
			}
		} else if (2 == this.act) {
			for (String apiUUID : this.apiUUIDs) {
				try {
					getComposerService().deleteAndStopAllNodes(apiUUID);
				} catch (Exception e) {
					this.logger.debug("Delete/Stop to nodes error: [" + apiUUID + "]\n" + StackTraceUtil.logStackTrace(e));
				}
			}
		} else {
			this.logger.debug("Unknown act: " + this.act);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replace(DeferrableJob source) {
		ComposerServiceJob newJob = (ComposerServiceJob) source;
		this.act = newJob.getAct();
		this.apiUUIDs = (List<String>) ServiceUtil.deepCopy(newJob.getApiUUIDs(), List.class);
	}

	protected ComposerService getComposerService() {
		return this.composerService;
	}

	public Integer getAct() {
		return act;
	}

	public List<String> getApiUUIDs() {
		return apiUUIDs;
	}

}