package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.nodeTask.CleanAllCacheNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.CleanCacheByKeyNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.CleanCacheByTableNameNotifier;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class NoticeClearCacheEventsJob extends Job {
	
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private CleanAllCacheNotifier cleanAllCacheNotifier;
	private CleanCacheByKeyNotifier cleanCacheByKeyNotifier;
	private CleanCacheByTableNameNotifier cleanCacheByTableNameNotifier;

	private TPILogger logger = TPILogger.tl;

	private final Integer action;
	private final String cacheName;
	private final List<String> tableNames;
	
	public NoticeClearCacheEventsJob(Integer action, String cacheName, List<String> tableNames,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, CleanAllCacheNotifier cleanAllCacheNotifier,
			CleanCacheByKeyNotifier cleanCacheByKeyNotifier,
			CleanCacheByTableNameNotifier cleanCacheByTableNameNotifier) {
		this.action = action;
		this.cacheName = cacheName;
		this.tableNames = tableNames;

		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.cleanAllCacheNotifier = cleanAllCacheNotifier;
		this.cleanCacheByKeyNotifier = cleanCacheByKeyNotifier;
		this.cleanCacheByTableNameNotifier = cleanCacheByTableNameNotifier;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin NoticeClearCacheEventsJob ---");
			//chk param
			if (StringUtils.isEmpty(action)){
				this.logger.debug("缺少必填參數 action");
				throw TsmpDpAaRtnCode._1296.throwing();
			}
 
			String taskId = "";
			if(action == 0) {//全部清除
				taskId = getCleanAllCacheNotifier().noticePublicEvent();
				 
			}else if(action == 1) {//依分類清除
				if (StringUtils.isEmpty(cacheName)){
					this.logger.debug("缺少必填參數 cacheName");
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				getItemsById("DB_CACHE_NAME", cacheName, true);
				
				taskId = getCleanCacheByKeyNotifier().noticePublicEvent(cacheName);
			}else if(action == 2) {//依資料表名稱清除
				if (StringUtils.isEmpty(tableNames)){
					this.logger.debug("缺少必填參數 tableNames");
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				
				taskId = getCleanCacheByTableNameNotifier().noticePublicEvent(tableNames);
			}else {
				this.logger.debug("action 參數錯誤: " + action);
				throw TsmpDpAaRtnCode._1290.throwing();
			}
			this.logger.debug("任務編號 TaskId: " + taskId);
			
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
		} finally {
			this.logger.debug("--- Finish NoticeClearCacheEventsJob ---");
		}
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
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected CleanAllCacheNotifier getCleanAllCacheNotifier() {
		return this.cleanAllCacheNotifier;
	}
	
	protected CleanCacheByKeyNotifier getCleanCacheByKeyNotifier() {
		return this.cleanCacheByKeyNotifier;
	}
	
	protected CleanCacheByTableNameNotifier getCleanCacheByTableNameNotifier() {
		return this.cleanCacheByTableNameNotifier;
	}
 
}
