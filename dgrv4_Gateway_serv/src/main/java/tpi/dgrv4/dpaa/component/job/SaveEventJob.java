package tpi.dgrv4.dpaa.component.job;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.repository.TsmpEventsDao;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;

@SuppressWarnings("serial")
public class SaveEventJob extends Job {

	private TsmpEventsDao tsmpEventsDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private SeqStoreService seqStoreService;

	private TsmpEvents tsmpEvents;

	public SaveEventJob(TsmpEvents tsmpEvents, TsmpEventsDao tsmpEventsDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			SeqStoreService seqStoreService) {
		this.tsmpEvents = tsmpEvents;
		this.tsmpEventsDao = tsmpEventsDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.seqStoreService = seqStoreService;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		tsmpEvents.setCreateDateTime(DateTimeUtil.now());
		
		checkEventTypeId();
		
		checkEventNameId();
		
		try {
			// 取得 序號
			Long eventId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_EVENTS);
			tsmpEvents.setEventId(eventId);
			
			getTsmpEventsDao().save(tsmpEvents);
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				throw e;
			}
		}
	}

	private void checkEventTypeId() {
		String eventTypeId = tsmpEvents.getEventTypeId();
		getItemsById("EVENT_TYPE", eventTypeId, true);
	}

	private void checkEventNameId() {
		String eventNameId = tsmpEvents.getEventNameId();
		getItemsById("EVENT_NAME", eventNameId, true);
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists) {
		String defaultLocale = LocaleType.EN_US;
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, defaultLocale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
		}
		return i;
	}

	protected TsmpEventsDao getTsmpEventsDao() {
		return tsmpEventsDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected TsmpEvents getTsmpEvents() {
		return tsmpEvents;
	}

	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

}