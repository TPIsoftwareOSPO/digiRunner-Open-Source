package tpi.dgrv4.dpaa.component.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.SeqStore;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 刪除過期的申請單序號
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class DPB0065Job extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private SeqStoreDao seqStoreDao;
	
	private String locale;

	public DPB0065Job(String locale, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, SeqStoreDao seqStoreDao) {
		this.locale = locale;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.seqStoreDao = seqStoreDao;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		// 找出所有申請單子類別參數
		List<String> params = getReqParams(TsmpDpReqReviewType.ITEM_NO);
		if (params == null || params.isEmpty()) {
			return;
		}

		int delCnt = 0;
		for(String param : params) {
			delCnt += doDeleteExpiredSequence(param);
		}
		this.logger.debug(String.format("已刪除 %d 筆序號檔", delCnt));
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getGroupId(), this.getId()));
	}

	public List<String> getReqParams(String itemNo) {
		List<TsmpDpItems> items = getTsmpDpItemsCacheProxy().findByItemNoAndLocale(itemNo, locale);
		if (items != null && !items.isEmpty()) {
			List<String> params = new ArrayList<>();

			String subitemNo = null;
			List<String> subParams = null;
			String param1 = null;
			for(TsmpDpItems item : items) {
				subitemNo = item.getSubitemNo();
				subParams = getReqParams(subitemNo);
				params.addAll(subParams);
				param1 = item.getParam1();
				if (!StringUtils.isEmpty(param1)) {
					params.add(param1);
				}
			}

			return params;
		}
		return Collections.emptyList();
	}

	/**
	 * 依簽核類型的參數, 刪除除了今天以外的序號
	 * @param param
	 * @return
	 */
	private int doDeleteExpiredSequence(String param) {
		return doDeleteExpiredSequence(param, new Date());
	}

	public int doDeleteExpiredSequence(String param, Date date) {
		String today_yyyyMMdd = DateTimeUtil.dateTimeToString(date, DateTimeFormatEnum.西元年月日_3).orElse(null);
		
		List<SeqStore> seqList = getSeqStoreDao().queryExpiredSequence(param, today_yyyyMMdd);
		if (seqList == null || seqList.isEmpty()) {
			return 0;
		}
		
		int delCnt = 0;
		for(SeqStore seq : seqList) {
			getSeqStoreDao().delete(seq);
			delCnt++;
		}
		
		return delCnt;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected SeqStoreDao getSeqStoreDao() {
		return this.seqStoreDao;
	}

}
