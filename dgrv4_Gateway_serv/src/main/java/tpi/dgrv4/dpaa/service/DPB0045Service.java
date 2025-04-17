package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.UrlCodecHelper;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFbFlag;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0045Req;
import tpi.dgrv4.dpaa.vo.DPB0045Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.jpql.TsmpDpNews;
import tpi.dgrv4.entity.repository.TsmpDpNewsDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0045Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpNewsDao tsmpDpNewsDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;
	private BcryptParamHelper helper;
 
	@Autowired
	public DPB0045Service(TsmpDpNewsDao tsmpDpNewsDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			TsmpOrganizationDao tsmpOrganizationDao, ServiceConfig serviceConfig, BcryptParamHelper helper) {
		super();
		this.tsmpDpNewsDao = tsmpDpNewsDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
		this.helper = helper;
	}

	public DPB0045Resp queryNewsById(TsmpAuthorization tsmpAuthorization, DPB0045Req req, ReqHeader reqHeader) {
		DPB0045Resp resp = new DPB0045Resp();
		
		try {
			Long newsId = req.getNewsId();
			String fbTypeEncode = req.getFbTypeEncode();//FRONT/BACK,使用BcryptParam設計
			String orgId = tsmpAuthorization.getOrgId();
			
			// .... chk
			if (StringUtils.isEmpty(newsId) ){
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (StringUtils.isEmpty(fbTypeEncode)){
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			// ...2, 此Helper 需要完整載入Spring Boot 
			String fbType = getDecodeFbType(fbTypeEncode, reqHeader.getLocale());
			
			// ...3, 本次真正邏輯, 以String及原生型態傳入
			resp = queryNewsById(newsId, orgId, fbType, reqHeader.getLocale());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	public String getDecodeFbType(String fbTypeEncode, String locale) {
		String fbType = null;
		try {
			fbType = getBcryptParamHelper().decode(fbTypeEncode, "FB_FLAG", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return fbType;
	}

	public DPB0045Resp queryNewsById(Long newsId, String orgId, String fbType, String locale) throws Exception {
		
		TsmpDpNews news = null;
		if(fbType.equals(TsmpDpFbFlag.FRONT.value())) {//前台不比對 orgId 權限,且只查狀態 enable
			news = getTsmpDpNewsDao().findByNewsIdAndStatus(newsId, "1");//狀態為啟用
			
			if (news == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
		}else if(fbType.equals(TsmpDpFbFlag.BACK.value())) {//後端從token取得資料,檢查是否有權限查詢,且不區分狀態

			Optional<TsmpDpNews> opt_news = getTsmpDpNewsDao().findById(newsId);
			if(!opt_news.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}else {
				news = opt_news.get();
			}
			// 找出包含此 orgId 及其向下的所有組織
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			String reqOrgId = news.getOrgId();
			
			// 只能看到自己組織向下的公告
			if (StringUtils.hasLength(reqOrgId)) {
				if (CollectionUtils.isEmpty(orgDescList) || !orgDescList.contains(reqOrgId)) {
					String orgDescsString = String.join(",", orgDescList);
					this.logger.debug(String.format("公告非所屬組織權限: %s not in %s",  reqOrgId, orgDescList));
					throw TsmpDpAaRtnCode._1219.throwing();
				}
			}
		}
		
		DPB0045Resp resp = new DPB0045Resp();
		resp = getResp(news, locale);
		return resp;
	}

	private DPB0045Resp getResp(TsmpDpNews tsmpDpNews, String locale) throws Exception {
		if (tsmpDpNews == null) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		DPB0045Resp resp = new DPB0045Resp();
		resp.setNewsId(tsmpDpNews.getNewsId());
		resp.setLv(tsmpDpNews.getVersion());
		resp.setNewTitle(tsmpDpNews.getNewTitle());
		
		// DB Save plain text 需要做 UrlEncode
		String newContent = tsmpDpNews.getNewContent();
		// DB 為明文時, Encode String 回傳
		newContent = UrlCodecHelper.getEncode(newContent); 
		resp.setNewContent(newContent);

		Optional<String> opt_date = DateTimeUtil.dateTimeToString(tsmpDpNews.getPostDateTime(),
				DateTimeFormatEnum.西元年月日_2);
		if (opt_date.isPresent()) {
			resp.setPostDateTime(opt_date.get());
		}

		String subitemNo = tsmpDpNews.getRefTypeSubitemNo();// 公告類型代碼
		String subitemName = getSubitemName(subitemNo, locale);// 公告類型中文
		resp.setTypeItemNo(subitemNo);
		resp.setTypeItemName(subitemName);
		String orgId = tsmpDpNews.getOrgId();// 組織單位ID
		String orgName = getOrgName(orgId);// 組織單位名稱
		resp.setOrgId(orgId);
		resp.setOrgName(orgName);

		return resp;
	}

	/**
	 * 取得 公告類型code 對映的 公告類型中文
	 * 
	 * @param subitemNo
	 * @return
	 */
	private String getSubitemName(String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId("NEWS_TYPE", subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo == null) {
			return null;
		}

		String subitemName = vo.getSubitemName();

		return subitemName;
	}

	/**
	 * 取得 組織單位ID 對映的 組織單位名稱
	 * 
	 * @param orgId
	 * @return
	 */
	private String getOrgName(String orgId) {
		Optional<TsmpOrganization> opt = getTsmpOrganizationDao().findById(orgId);
		if (!opt.isPresent()) {
			return null;
		}

		TsmpOrganization n = opt.get();
		String orgName = n.getOrgName();

		return orgName;
	}
	
	protected TsmpDpNewsDao getTsmpDpNewsDao() {
		return this.tsmpDpNewsDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
}
