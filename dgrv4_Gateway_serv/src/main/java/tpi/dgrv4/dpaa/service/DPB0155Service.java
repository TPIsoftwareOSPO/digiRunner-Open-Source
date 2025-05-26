package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0155ItemReq;
import tpi.dgrv4.dpaa.vo.DPB0155Req;
import tpi.dgrv4.dpaa.vo.DPB0155Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0155Service {
	
	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	public DPB0155Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao,
			BcryptParamHelper bcryptParamHelper, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	@Transactional
	public DPB0155Resp updateWebsite(TsmpAuthorization auth, DPB0155Req req , ReqHeader header) {
		
		DPB0155Resp resp = new DPB0155Resp();
		
		try {

			checkParams(req);
			
			String websiteName = req.getWebsiteName();
			Long dgrWebsiteId = req.getDgrWebsiteId();
			
			// 檢查 website 名稱是否重複
			List<DgrWebsite> list = getDgrWebsiteDao().findByDgrWebsiteIdNotAndWebsiteName(dgrWebsiteId, websiteName);
			if (!list.isEmpty()) {
				this.logger.error("The website name [" + websiteName + "] already exists.");
				throw TsmpDpAaRtnCode._1353.throwing("{{websiteName}}",websiteName);
			}
			
			// 開始修改資料
			updateWebsiteAndWebsiteDetail(auth,req,header);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}
	
	private void updateWebsiteAndWebsiteDetail(TsmpAuthorization auth, DPB0155Req req, ReqHeader header) {

		Long websiteId = req.getDgrWebsiteId();
		String websiteName = req.getWebsiteName();
		String websiteStatus = req.getWebsiteStatus();
		String local = header.getLocale();
		websiteStatus = getValueByBcryptParamHelper(websiteStatus, "ENABLE_FLAG", local);
		//websiteStatus = getItemsParam("ENABLE_FLAG",websiteStatus,local);
		List<DPB0155ItemReq> webSiteList = req.getWebSiteList();
		String remark = req.getRemark();

		Optional<DgrWebsite> website = getDgrWebsiteDao().findById(websiteId);
		
		if (website.isEmpty()) {
			this.logger.error("Website ID is Not Found !");
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		// 更新 dgrWebsite
		DgrWebsite dgrWebsite = website.get();
		dgrWebsite.setWebsiteName(websiteName);
		dgrWebsite.setWebsiteStatus(websiteStatus);
		dgrWebsite.setRemark(remark);
		dgrWebsite.setAuth(req.getAuth());
		dgrWebsite.setSqlInjection(req.getSqlInjection());
		dgrWebsite.setTraffic(req.getTraffic());
		dgrWebsite.setXss(req.getXss());
		dgrWebsite.setXxe(req.getXxe());
		dgrWebsite.setTps(req.getTps());
		dgrWebsite.setIgnoreApi(req.getIgnoreApi());
		dgrWebsite.setShowLog(req.getShowLog());
		dgrWebsite = getDgrWebsiteDao().save(dgrWebsite);
		dgrWebsite.setUpdateDateTime(DateTimeUtil.now());
		dgrWebsite.setUpdateUser(auth.getUserName());
		
		// 刪除舊有的 WebsiteDetail
		getDgrWebsiteDetailDao().deleteByDgrWebsiteId(websiteId);
		// 檢查舊有的 WebsiteDetail 是否刪除
		List<DgrWebsiteDetail> oldWebsiteDetilList = getDgrWebsiteDetailDao().findByDgrWebsiteId(websiteId);
		if (!oldWebsiteDetilList.isEmpty()) {
			this.logger.error("The old WebsiteDetail was not deleted completely.");
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		// 更新 dgrWebsiteDetail
		webSiteList.forEach(vo -> {
			
			DgrWebsiteDetail dgrWebsiteDetail = new DgrWebsiteDetail();

			int probability = vo.getProbability();
			String url = vo.getUrl();
			
			dgrWebsiteDetail.setDgrWebsiteId(websiteId);
			dgrWebsiteDetail.setProbability(probability);
			dgrWebsiteDetail.setUrl(url);
			
			getDgrWebsiteDetailDao().save(dgrWebsiteDetail);
		});
	}
	
	private String getItemsParam(String itemNo, String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		
		if (vo == null) {
			return null;
		}
		
		return vo.getParam2();
	}
	
	private void checkParams(DPB0155Req req) {

		// 空值檢查
		if (req == null) {
			this.logger.error("DPB0155Req is Null !");
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		// 檢查資料是否符合規範
		if (!req.isWebsiteNameValid()) {
			throw TsmpDpAaRtnCode._1350.throwing("Website Name");
		}
		
		if (!req.isWebsiteStatusValid()) {
			throw TsmpDpAaRtnCode._1288.throwing("Website Status");
		}
		
		boolean isReqDateOk = req.checkdate();
		if (!isReqDateOk) {
			this.logger.error("DPB0155Req data, verification failed.");
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		// 檢查 Probability 總和是否為 100
		List<DPB0155ItemReq> list = req.getWebSiteList();
		int total = 0;
		for (DPB0155ItemReq dpb0155ItemReq : list) {
			int number = dpb0155ItemReq.getProbability();
			total = total + number;
		}
		if (total != 100) {
			this.logger.error("Probability total is not equal to 100, which does not meet the restriction.");
			throw TsmpDpAaRtnCode._1528.throwing(total + "");
		}	
	}
	
	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.PARAM2, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	protected DgrWebsiteDao getDgrWebsiteDao() {
		return dgrWebsiteDao;
	}
	
	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return dgrWebsiteDetailDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}
