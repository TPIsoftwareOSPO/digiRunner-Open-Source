package tpi.dgrv4.dpaa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.job.DPB0046Job;
import tpi.dgrv4.dpaa.vo.DPB0046Req;
import tpi.dgrv4.dpaa.vo.DPB0046Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0046Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpNewsDao tsmpDpNewsDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private ServiceConfig serviceConfig;
	
	@Autowired
	public DPB0046Service(TsmpDpNewsDao tsmpDpNewsDao, TsmpOrganizationDao tsmpOrganizationDao, JobHelper jobHelper,
			ApplicationContext ctx, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpNewsDao = tsmpDpNewsDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.serviceConfig = serviceConfig;
	}

	public DPB0046Resp deleteNews_v3_4(TsmpAuthorization tsmpAuthorization, DPB0046Req req) {
		Map<String, Object> map = deleteNews(tsmpAuthorization, req);
		return (DPB0046Resp) map.get("Resp");
	}
	
	@Transactional
	public Map<String, Object> deleteNews(TsmpAuthorization tsmpAuthorization, DPB0046Req req) {
		DPB0046Resp resp = new DPB0046Resp();
		DPB0046Job job = null;
		try {
			// 當按下刪除時, 以 [流式設計] 方式 刪除停用已久的News
			//if(hasDoJob()) {
				job = addDPB0046Job();
			//}
			
			String orgId = tsmpAuthorization.getOrgId();
			
			//chk param
			if(StringUtils.isEmpty(orgId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (req.getDelList() == null || req.getDelList().isEmpty()) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			List<Long> delList = req.getDelList();
			Map<Long, String> resultMap = new HashMap<>();//<id, "Ok">, <id, "Fail">....etc
			
			// 找出包含此 orgId 及其向下的所有組織
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			
			// 只能看到自己組織向下的公告
			for (Long newsId : delList) {
				Optional<TsmpDpNews> opt_news = getTsmpDpNewsDao().findById(newsId);
				if(opt_news.isPresent()) {
					TsmpDpNews tsmpDpNews = opt_news.get();
					String reqOrgId = tsmpDpNews.getOrgId();
					if (!StringUtils.isEmpty(reqOrgId)) {
						if (CollectionUtils.isEmpty(orgDescList) || !orgDescList.contains(reqOrgId)) {
							String orgDescsString = String.join(",", orgDescList);
							this.logger.debug(String.format("公告非所屬組織權限: %s not in %s",  reqOrgId, orgDescsString));
							throw TsmpDpAaRtnCode._1219.throwing();
						}
					}
				}
			}
			
			
			boolean isAllFail = true;//是否全部的公告消息都刪除失敗
			for (Long newsId : delList) {
				Optional<TsmpDpNews> opt_news = getTsmpDpNewsDao().findById(newsId);
				String result = null;
				if(opt_news.isPresent()) {
					TsmpDpNews tsmpDpNews = opt_news.get();
					tsmpDpNews.setStatus(TsmpDpDataStatus.OFF.value());//狀態=停用
					tsmpDpNews.setUpdateUser(getClass().getSimpleName());
					tsmpDpNews.setUpdateDateTime(DateTimeUtil.now());
					tsmpDpNews = getTsmpDpNewsDao().saveAndFlush(tsmpDpNews);
					if(tsmpDpNews != null) {
						isAllFail = false;
						result = "Ok";
					}else {
						result = "Fail";
					}		
				}else {
					result = "Fail";
				}
				resultMap.put(newsId, result);
			}
			
			if(isAllFail) {//全部的公告消息都刪除失敗
				throw TsmpDpAaRtnCode.FAIL_DELETE_NEWS.throwing();
			}
			resp.setResultMap(resultMap);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put("Resp", resp);
		map.put("Job", job);
		
		return map;
	}

	protected DPB0046Job addDPB0046Job() {
		DPB0046Job job = getDPB0046Job();
		getJobHelper().add(job);
		return job;
	}
	
	protected TsmpDpNewsDao getTsmpDpNewsDao() {
		return this.tsmpDpNewsDao;
	}
	
	protected DPB0046Job getDPB0046Job() {
		return (DPB0046Job) getCtx().getBean("dpb0046Job");
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	/*protected Integer getExpDay() {
		String expDay = getServiceConfig().get("dpb0046.job.exp-day");
		try {
			return Integer.valueOf(expDay);
		} catch (Exception e) {
			this.logger.warn("Invalid setting: exp-day = {}, set to default 30.", expDay);
		}
		return new Integer(30);
	}

	public boolean hasDoJob() {
		Integer expDay = getExpDay();//過期公告消息天數
		if(expDay == -1) {//不刪除停用的公告消息
			return false;
		}else {
			return true;
		}
	}*/
	
}
