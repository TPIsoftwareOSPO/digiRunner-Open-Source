package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.UrlCodecHelper;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0042Req;
import tpi.dgrv4.dpaa.vo.DPB0042Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0042Service {
	
	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpNewsDao tsmpDpNewsDao;

	@Autowired
	public DPB0042Service(TsmpDpNewsDao tsmpDpNewsDao) {
		super();
		this.tsmpDpNewsDao = tsmpDpNewsDao;
	}

	public DPB0042Resp createNews_v3_4(TsmpAuthorization tsmpAuthorization, DPB0042Req req) {
		DPB0042Resp resp = new DPB0042Resp();
		
		try {
			String newTitle = req.getNewTitle();
			String newContent = req.getNewContent();
			
			String postDateTime = req.getPostDateTime();
			String typeItemNo = req.getTypeItemNo();
			String orgId = tsmpAuthorization.getOrgId();
			
			if (!StringUtils.hasLength(newTitle) 
					|| !StringUtils.hasLength(newContent) 
					|| !StringUtils.hasLength(postDateTime) 
					|| !StringUtils.hasLength(typeItemNo)
					|| !StringUtils.hasLength(orgId)) {
				throw TsmpDpAaRtnCode.FAIL_CREATE_NEWS_REQUIRED.throwing();
			}
			
			// javascript 傳 URL encode text 需要還原
			newContent = UrlCodecHelper.getDecode(newContent);
			req.setNewContent(newContent);
//			this.logger.debug("=== [ DPB0042內容 ] Decode ===");		
//			this.logger.debug("{}",req.getNewContent());
			
			TsmpDpNews tsmpDpNews = new TsmpDpNews();
			try {
				BeanUtils.copyProperties(req, tsmpDpNews);
			} catch (BeansException e) {
				throw TsmpDpAaRtnCode.FAIL_CREATE_NEWS.throwing();
			}
			
			if (req.getPostDateTime() != null) {
				Optional<Date> opt_date = DateTimeUtil.stringToDateTime(req.getPostDateTime(), DateTimeFormatEnum.西元年月日_2);
				if (opt_date.isPresent()) {
					tsmpDpNews.setPostDateTime(opt_date.get());
				} else {
					throw TsmpDpAaRtnCode.FAIL_CREATE_NEWS.throwing();
				}
			}
			
			tsmpDpNews.setRefTypeSubitemNo(req.getTypeItemNo());
			tsmpDpNews.setOrgId(orgId);
			tsmpDpNews.setCreateUser(tsmpAuthorization.getUserName());
			tsmpDpNews.setCreateDateTime(DateTimeUtil.now());
			tsmpDpNews = getTsmpDpNewsDao().saveAndFlush(tsmpDpNews);

			resp.setNewsId(tsmpDpNews.getNewsId());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		return resp;
	}

	protected TsmpDpNewsDao getTsmpDpNewsDao() {
		return this.tsmpDpNewsDao;
	}
}
