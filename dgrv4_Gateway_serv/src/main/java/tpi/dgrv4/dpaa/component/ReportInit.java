package tpi.dgrv4.dpaa.component;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@AllArgsConstructor
@Component
@Getter(AccessLevel.PROTECTED)
public class ReportInit {
	
    private TsmpDpApptJobDao tsmpDpApptJobDao;
    private TsmpDpApptRjobDao tsmpDpApptRjobDao;

	@PostConstruct
    public void init() {
		try {
			//因為在換版可能REPORT_BATCH還在執行中,會造成之後報表排程一直無法被執行
			checkReportJob();
			checkReportRjob();//上一個method會改掉週期排程處於執行中的狀態,此方法是預防排程先變成取消了
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

	}
	
	private void checkReportJob() {
		List<TsmpDpApptJob> jobList = getTsmpDpApptJobDao().findByRefItemNoAndStatus("REPORT_BATCH", TsmpDpApptJobStatus.RUNNING.value());
		if(!CollectionUtils.isEmpty(jobList)) {
			TPILogger.tl.info("REPORT_BATCH job running");
			jobList.forEach(jobVo->{
				jobVo.setStatus(TsmpDpApptJobStatus.CANCEL.value());
				jobVo.setUpdateDateTime(DateTimeUtil.now());
				jobVo.setUpdateUser("REPORT_INIT");
			});
			getTsmpDpApptJobDao().saveAll(jobList);
			TPILogger.tl.info("REPORT_BATCH job status change cancel id:" + jobList.stream().map( TsmpDpApptJob::getApptJobId )
            .collect( Collectors.toList() ));
			
			String periodUid = jobList.get(0).getPeriodUid();
			if(StringUtils.hasText(periodUid)) {
				TsmpDpApptRjob rjobVo = getTsmpDpApptRjobDao().findById(periodUid).orElse(null);
				if(rjobVo != null) {
					if("3".equals(rjobVo.getStatus())) {
						rjobVo.setStatus("1");
						rjobVo.setUpdateDateTime(DateTimeUtil.now());
						rjobVo.setUpdateUser("REPORT_INIT");
						getTsmpDpApptRjobDao().save(rjobVo);
						TPILogger.tl.info("Report Schedule job status change start");
					}
				}
			}
		}
	}
	
	private void checkReportRjob() {
		List<TsmpDpApptRjob> jobList = getTsmpDpApptRjobDao().findByRjobName("Report Schedule");
		if(!CollectionUtils.isEmpty(jobList)) {
			TPILogger.tl.info("REPORT_BATCH cycle job running");
			jobList.forEach(jobVo->{
				if("3".equals(jobVo.getStatus())) {
					jobVo.setStatus("1");
					jobVo.setUpdateDateTime(DateTimeUtil.now());
					jobVo.setUpdateUser("REPORT_INIT");
				}
			});
			getTsmpDpApptRjobDao().saveAll(jobList);
			TPILogger.tl.info("Report Schedule job status change start");
		}
	}
}
