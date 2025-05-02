package tpi.dgrv4.dpaa.component.nodeTask;

import java.util.Date;

import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 此類別原是在 v3 底層的 SDK,
 * 因 v4 沒有底層, 所以做一個空殼來減少程式的異動
 * @author Kim
 *
 * @param <EventType>
 */
public abstract class NodeTaskNotifiers<EventType> {

	private TPILogger logger = TPILogger.tl;

	public class NodeTaskType {
		public final static String Public = "Public";
		public final static String Single = "Single";
	}

	protected abstract String taskName();

	protected abstract Class<EventType> eventType();

	public String noticePublicEvent() {
		return noticePublicEvent(null, null);
	}

	public abstract String noticePublicEvent(EventType event);

	public String noticePublicEvent(EventType event, Date executeTime) {
		doNothing();
		return null;
	}

	public String noticeSingleEvent() {
		return noticeSingleEvent(null, null);
	}

	public abstract String noticeSingleEvent(EventType event);

	public String noticeSingleEvent(EventType event, Date executeTime) {
		doNothing();
		return null;
	}

	public void doNothing() {
		this.logger.trace("v4 DO NOTHING...");
	}

	protected abstract String notice(EventType event, String taskType, Date executeTime);
	
}
