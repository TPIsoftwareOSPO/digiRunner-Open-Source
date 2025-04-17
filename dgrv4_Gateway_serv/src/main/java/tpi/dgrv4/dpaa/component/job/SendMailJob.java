package tpi.dgrv4.dpaa.component.job;

import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.List;

public class SendMailJob extends Job {
    private PrepareMailService prepareMailService;
    
    private final TsmpAuthorization auth;

    private final List<TsmpMailEvent> mailEvents;
    private final String sendTime;
    private final String identif;

	public SendMailJob(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime, String identif,
			PrepareMailService prepareMailService) {
        this.auth = auth;
        this.mailEvents = mailEvents;
        this.sendTime = sendTime;
        this.identif = identif;
        this.prepareMailService = prepareMailService;
    }

    @Override
    public void run(JobHelperImpl jobHelper, JobManager jobManager) {
        if (mailEvents != null && !mailEvents.isEmpty()) {
            try {
                TPILogger.tl.debug("--- Begin SendMailJob ---");

                //準備好資料,以寫入排程

                getPrepareMailService().createMailSchedule(mailEvents, identif
                        , TsmpDpMailType.SAME.text(), sendTime);

            } catch (Exception e) {
                TPILogger.tl.debug("" + e);
            } finally {
                TPILogger.tl.debug("--- Finish SendMailJob ---");
            }
        }
    }

    protected PrepareMailService getPrepareMailService() {
        return prepareMailService;
    }

}
