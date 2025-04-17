package tpi.dgrv4.dpaa.component.nodeTask;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.service.TsmpEventsService;
import tpi.dgrv4.entity.entity.jpql.TsmpNodeTask;
import tpi.dgrv4.entity.repository.TsmpNodeTaskDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 覆寫SDK發送任務通知的方法，先轉導到 {@link TsmpNodeTaskNotifier#noticeWithLog}<br>
 * 在發送前、後寫入事件紀錄，才真正執行發送的動作
 * @author Kim
 *
 * @param <EventType>
 */
public abstract class TsmpNodeTaskNotifier<EventType> extends NodeTaskNotifiers<EventType> {

	private static final String EVENT_NAME = "NODE_TASK_NOTIFIERS";

	private TPILogger logger = TPILogger.tl;

	private TsmpEventsService tsmpEventsService;
	private TsmpNodeTaskDao tsmpNodeTaskDao;

	@Autowired
	protected TsmpNodeTaskNotifier(TsmpEventsService tsmpEventsService, TsmpNodeTaskDao tsmpNodeTaskDao) {
		super();
		this.tsmpEventsService = tsmpEventsService;
		this.tsmpNodeTaskDao = tsmpNodeTaskDao;
	}

	@Override
	public String noticePublicEvent(EventType event) {
		return noticePublicEvent(event, null);
	}

	@Override
	public String noticePublicEvent(EventType event, Date executeTime) {
		return noticeWithLog(NodeTaskType.Public, event, executeTime, (e, et) -> {
			return super.noticePublicEvent(e, et);
		});
	}

	@Override
	public String noticeSingleEvent(EventType event) {
		return noticeSingleEvent(event, null);
	}

	@Override
	public String noticeSingleEvent(EventType event, Date executeTime) {
		return noticeWithLog(NodeTaskType.Single, event, executeTime, (e, et) -> {
			return super.noticeSingleEvent(e, et);
		});
	}

	@Override
	public String notice(EventType event, String taskType, Date executeTime) {
		this.logger.debug("暫不開放使用此方法");
		throw TsmpDpAaRtnCode._1297.throwing();
	}

	/**
	 * 使用 {@link NodeTaskNoticeManager#notifyEvent} 所回傳的 taskId<br>
	 * 在非 digiRunner 環境下執行，會是 TSMP_NODE_TASK.task_id<br>
	 * 在 digiRunner 環境下則會是 String.valueOf( TSMP_NODE_TASK.id )<br>
	 * 因此可將 notifyEvent 的回傳值傳入此方法 <br>
	 * 取得正確的 TSMP_NODE_TASK.id<br>
	 * @param idOrTaskId
	 * @return nullable
	 */
	public Long getTsmpNodeTaskId(String idOrTaskId) {
		Long tsmpNodeTaskId = null;
		
		// 是否為 TSMP_NODE_TASK.id
		try {
			tsmpNodeTaskId = Long.parseLong(idOrTaskId);
			Optional<TsmpNodeTask> opt = getTsmpNodeTaskDao().findById(tsmpNodeTaskId);
			if (opt.isPresent()) {
				return tsmpNodeTaskId;
			}
		} catch (Exception e) {}
		
		tsmpNodeTaskId = null;
		
		// 是否為 TSMP_NODE_TASK.task_id
		String taskSignature = taskName();
		if (!StringUtils.isEmpty(taskSignature)) {
			TsmpNodeTask tsmpNodeTask = getTsmpNodeTaskDao().findFirstByTaskSignatureAndTaskId( //
				taskSignature, idOrTaskId);
			if (tsmpNodeTask != null) {
				tsmpNodeTaskId = tsmpNodeTask.getId();
			}
		}

		return tsmpNodeTaskId;
	}

	private String noticeWithLog(String taskType, EventType event, Date executeTime, //
			BiFunction<EventType, Date, String> biFunc) {		
		String traceId = getEventTraceId();
		
		log_methodStart(traceId, String.format("%s: %s", taskType, taskName()));
		log_methodInput(traceId, String.valueOf(event));
		
		String taskId = new String();
		try {
			taskId = biFunc.apply(event, executeTime);
			log_methodOutput(traceId, taskId);
		} catch (Exception e) {
			log_error(traceId, e.toString());
		}
		
		log_methodEnd(traceId);
		return taskId;
	}

	private String getEventTraceId() {
		ZonedDateTime zdt = ZonedDateTime.now();
		return String.valueOf(zdt.toInstant().toEpochMilli());
	}

	private void log_methodInput(String traceId, String infoMsg) {
		log(TsmpEventsService.EventType.METHOD_INPUT, infoMsg, traceId);
	}

	private void log_methodStart(String traceId, String infoMsg) {
		log(TsmpEventsService.EventType.METHOD_START, infoMsg, traceId);
	}

	private void log_methodOutput(String traceId, String infoMsg) {
		log(TsmpEventsService.EventType.METHOD_OUTPUT, infoMsg, traceId);
	}

	private void log_methodEnd(String traceId) {
		String infoMsg = getClass().getCanonicalName();
		if (StringUtils.isEmpty(infoMsg)) {
			infoMsg = getClass().getName();
		}
		log(TsmpEventsService.EventType.METHOD_END, infoMsg, traceId);
	}

	private void log_error(String traceId, String infoMsg) {
		log(TsmpEventsService.EventType.ERROR, infoMsg, traceId);
	}

	private void log(TsmpEventsService.EventType eventType, String infoMsg, String traceId) {
		getTsmpEventsService().saveEvent(eventType, EVENT_NAME, infoMsg, traceId);
	}

	protected TsmpEventsService getTsmpEventsService() {
		return this.tsmpEventsService;
	}

	protected TsmpNodeTaskDao getTsmpNodeTaskDao() {
		return this.tsmpNodeTaskDao;
	}

}