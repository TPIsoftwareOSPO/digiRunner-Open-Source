package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.UrlCodecHelper;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0043Req;
import tpi.dgrv4.dpaa.vo.DPB0043Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0043Service {
	
	private TsmpDpNewsDao tsmpDpNewsDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private BcryptParamHelper helper;
	
	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	public DPB0043Service(TsmpDpNewsDao tsmpDpNewsDao, TsmpOrganizationDao tsmpOrganizationDao,
			BcryptParamHelper helper) {
		super();
		this.tsmpDpNewsDao = tsmpDpNewsDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.helper = helper;
	}

	public DPB0043Resp updateNews(TsmpAuthorization authorization, DPB0043Req req, ReqHeader reqHeader) {
		DPB0043Resp resp = new DPB0043Resp();
		
		try {
			String userName = authorization.getUserName();
			String orgId = authorization.getOrgId();		
			Long newsId = req.getNewsId();
			Long lv = req.getLv();
			String statusEncode = req.getStatusEncode();
			String newTitle = req.getNewTitle();
			String newContent = req.getNewContent();
			String postDateTime = req.getPostDateTime();
			String typeItemNo = req.getTypeItemNo();
			
			//chk param
			if(newsId == null) {
				throw TsmpDpAaRtnCode._1196.throwing();
			}
			if(lv == null) {
				throw TsmpDpAaRtnCode._1196.throwing();
			}
			if(!StringUtils.hasLength(postDateTime)) {
				throw TsmpDpAaRtnCode._1196.throwing();
			}

			String status = null;
			if(StringUtils.hasLength(statusEncode)) {
				status = getDecodeStatus(statusEncode, reqHeader.getLocale());//解碼
			}
			resp = updateNews(newsId, lv, status, newTitle, newContent, postDateTime, typeItemNo, orgId, userName);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	public DPB0043Resp updateNews(Long newsId, Long lv, String status, String newTitle, String newContent, 
			String postDateTime, String typeItemNo, String orgId, String userName) throws Exception {
		
		// 確認公告是否存在
		// 找出包含此 orgId 及其向下的所有組織
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		
		// 只能看到自己組織向下的公告
		List<TsmpDpNews> newsList = getTsmpDpNewsDao().queryId(orgDescList, newsId);
		TsmpDpNews orig = null;
		if(newsList == null || newsList.isEmpty()) {
			throw TsmpDpAaRtnCode._1219.throwing();
		}
		
		// 深層拷貝
		orig = newsList.get(0);
		TsmpDpNews news = ServiceUtil.deepCopy(orig, TsmpDpNews.class);
		
		// 更新公告
		if(StringUtils.hasLength(status)) {//啟用狀態code BcryptParam(ENABLE_FLAG) = 1, 這裡有值時表示只要變動 status, 其它不更新
			news.setStatus(status);
			
		}else {
			
			// javascript 傳 URL encode text 需要還原
			newContent = UrlCodecHelper.getDecode(newContent); 
			news.setNewTitle(newTitle);
			news.setNewContent(newContent);		
			if (StringUtils.hasLength(postDateTime)) {
				Optional<Date> opt_date = DateTimeUtil.stringToDateTime(postDateTime, DateTimeFormatEnum.西元年月日_2);
				if (opt_date.isPresent()) {
					news.setPostDateTime(opt_date.get());
				} else {
					throw TsmpDpAaRtnCode._1295.throwing();
				}
			}
			news.setRefTypeSubitemNo(typeItemNo);
			news.setOrgId(orgId);
		}
		news.setUpdateDateTime(DateTimeUtil.now());
		news.setUpdateUser(userName);
		news.setVersion(lv);		
		
		try {
			news = getTsmpDpNewsDao().saveAndFlush(news);
		} catch (ObjectOptimisticLockingFailureException e) {
			throw TsmpDpAaRtnCode._1197.throwing();
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
		}
		
		DPB0043Resp resp = new DPB0043Resp();
		resp.setNewsId(news.getNewsId());
		resp.setLv(news.getVersion());
		return resp;
	}
	
	public String getDecodeStatus(String statusEncode, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(statusEncode, "ENABLE_FLAG", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}
	
	protected TsmpDpNewsDao getTsmpDpNewsDao() {
		return this.tsmpDpNewsDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
}
