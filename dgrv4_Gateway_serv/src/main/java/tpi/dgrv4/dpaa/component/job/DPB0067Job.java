package tpi.dgrv4.dpaa.component.job;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import jakarta.transaction.Transactional;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.req.DpReqServiceFactory;
import tpi.dgrv4.dpaa.component.req.DpReqServiceIfs;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

/***
 * 
 * 刪除過期的草稿
 *
 */
@SuppressWarnings("serial")
@Transactional
public class DPB0067Job extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private DpReqServiceFactory dpReqServiceFactory;

	private String locale;

	public DPB0067Job(String locale, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			DpReqServiceFactory dpReqServiceFactory) {
		super();
		this.locale = locale;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.dpReqServiceFactory = dpReqServiceFactory;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			
			//HOUSEKEEPING 短期 期限
			TsmpDpItemsId id = new TsmpDpItemsId();
			id.setItemNo("HOUSEKEEPING");
			id.setLocale(locale);
			id.setSubitemNo("short");

			TsmpDpItems tsmpDpItems = getTsmpDpItemsCacheProxy().findById(id);

			// 參考TSMP_DP_ITEMS中 的短期天數，若無則預設 30 天
			int day = 30;
			if (tsmpDpItems != null && tsmpDpItems.getParam1() != null ) {
				String n_day = tsmpDpItems.getParam1();
				day = Integer.parseInt(n_day);
			}

			GregorianCalendar gc = new GregorianCalendar();
			gc.add(GregorianCalendar.DATE, -day);
			Date queryStartDate = gc.getTime();

			List<TsmpDpReqOrderm> mList = getTsmpDpReqOrdermDao().query_dpb0067_expired(queryStartDate);
			if (mList != null && mList.isEmpty() == false) {

				for (TsmpDpReqOrderm tsmpDpReqOrderm : mList) {

					try {

						DpReqServiceIfs dpReqService = getDpReqServiceFactory()
								.getDpReqService(tsmpDpReqOrderm.getReqOrdermId(), () -> {
									return TsmpDpAaRtnCode._1217.throwing();
								});

						//刪除草稿申請單相關資料
						dpReqService.deleteDraft(tsmpDpReqOrderm.getReqOrdermId());
						logger.debug(String.format("草稿申請單號: %d 刪除成功", tsmpDpReqOrderm.getReqOrdermId()));

					} catch (TsmpDpAaException e) {
						if (!TsmpDpAaRtnCode._1287.getCode().equals(e.getError().getCode())) {
							logger.debug(String.format("草稿申請單號: %d 刪除失敗: %s", tsmpDpReqOrderm.getReqOrdermId(), e.getMessage()));							
						}
					}

				}
			}
		} catch (Exception e) {
			this.logger.debug("DPB0067Job error: " + StackTraceUtil.logStackTrace(e));
		}

	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getClass().getSimpleName(), this.getId()));
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return tsmpDpReqOrdermDao;
	}

	protected DpReqServiceFactory getDpReqServiceFactory() {
		return this.dpReqServiceFactory;
	}
}
