package tpi.dgrv4.dpaa.component.job;

import org.springframework.beans.factory.annotation.Autowired;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 總共刪除以下Tables:<br/>
 * tsmpDpNews<br/>
 * 
 * @author Mini
 */

@SuppressWarnings("serial")
public class DPB0046Job extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpNewsDao tsmpDpNewsDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	@Autowired
	public DPB0046Job(TsmpDpNewsDao tsmpDpNewsDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpNewsDao = tsmpDpNewsDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin DPB0046Job ---");
			
			Integer expDay = getExpDay();//過期公告消息天數
			// 找出狀態為0=停用, 且更新時間為 ${dpb0046.job.exp-day} 天前的過期公告消息資料
			List<TsmpDpNews> expiredNewsList = getExpiredExpList(expDay);
			if (expiredNewsList == null || expiredNewsList.isEmpty()) {
				this.logger.debug("No expired news.");
				return;
			}
//			this.logger.debug("Expired news: {}", expiredNewsList.size());
			
			
			Long newsId;
			for(TsmpDpNews news : expiredNewsList) {
				newsId = news.getNewsId();
				
				if (this.getTsmpDpNewsDao().existsById(newsId)) {
					this.getTsmpDpNewsDao().deleteById(newsId);
					this.logger.debug("delete過期的公告: " + newsId + " - " + news.getNewTitle());
				}
			}
		}  catch (Exception e) {
			logger.debug("" + e);
		} finally {
			this.logger.debug("--- Finish DPB0046Job ---");
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getClass().getSimpleName(), this.getId()));
	}
	
	private Integer getExpDay() {
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("HOUSEKEEPING", "gov_short", LocaleType.EN_US);
		String expDay = null;
		try {
			expDay = vo.getParam1();
			return Integer.valueOf(expDay);
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid setting: exp-day = %s, set to default 30.", expDay));
		}
		return Integer.valueOf(30);
	}

	private List<TsmpDpNews> getExpiredExpList(Integer expDay){
		LocalDate ld = LocalDate.now().minusDays(expDay);
		Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Optional<String> opt = DateTimeUtil.dateTimeToString(expDate, DateTimeFormatEnum.西元年月日時分秒_2);
		if (opt.isPresent()) {
			this.logger.debug("expDate = " + opt.get());
		}
		
		return this.getTsmpDpNewsDao().query_dpb0046Job(expDate);
	}
	
	protected TsmpDpNewsDao getTsmpDpNewsDao() {
		return this.tsmpDpNewsDao;
	} 
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
}
