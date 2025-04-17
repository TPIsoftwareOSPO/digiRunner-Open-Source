package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.dpaa.component.job.SaveEventJob;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class TsmpEventsService {
	
	/**
	 * 事件類型
	 */
	public enum EventType {
		METHOD_INPUT,	// 接收參數
		METHOD_CHECK,	// 參數檢查結果
		METHOD_START,	// 開始執行工作
		INFO,			// 顯示訊息
		METHOD_OUTPUT,	// 產生回傳值
		METHOD_END,		// 結束執行工作
		ERROR			// 未知的錯誤
		;
	}

	private JobHelper jobHelper;
	private ApplicationContext ctx;

	@Autowired
	public TsmpEventsService(JobHelper jobHelper, ApplicationContext ctx) {
		super();
		this.jobHelper = jobHelper;
		this.ctx = ctx;
	}

	public Job saveEvent(EventType eventType, String eventNameId, String infoMsg, String traceId) {
		return saveEvent(eventType, eventNameId, infoMsg, traceId, null);
	}

	public Job saveEvent(EventType eventType, String eventNameId, String infoMsg, String traceId, String createUser) {
		String eventTypeId = eventType == null ? "" : eventType.name();
		traceId = (traceId.length() <= 20) ? traceId : traceId.substring(0, 20);
		TsmpEvents tsmpEvents = createEvent(eventTypeId, eventNameId, infoMsg, traceId, createUser);
		SaveEventJob job = getSaveEventJob(tsmpEvents);
		getJobHelper().add(job);
		return job;
	}

	private TsmpEvents createEvent(String eventTypeId, String eventNameId, String infoMsg, String traceId, //
			String createUser) {
		TsmpEvents tsmpEvents = new TsmpEvents();
		tsmpEvents.setEventTypeId(eventTypeId);
		tsmpEvents.setEventNameId(eventNameId);
		tsmpEvents.setModuleName(TsmpDpModule.DP.getName());
		tsmpEvents.setTraceId(traceId);
		tsmpEvents.setThreadName(Thread.currentThread().getName());
		tsmpEvents.setInfoMsg(infoMsg);
		if (!StringUtils.isEmpty(createUser)) {
			tsmpEvents.setCreateUser(createUser);
		}
		String node = this.getNode();
		tsmpEvents.setNodeAlias(node);
		tsmpEvents.setNodeId(node);
		try {
			tsmpEvents.setNodeId(node.split(" / ")[0]);
		} catch (Exception e) {}
		return tsmpEvents;
	}
	
	protected String getNode() {
		return TPILogger.lc.param.get(TPILogger.nodeInfo);
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected SaveEventJob getSaveEventJob(TsmpEvents tsmpEvents) {
		return (SaveEventJob) getCtx().getBean("saveEventJob", tsmpEvents);
	}

}