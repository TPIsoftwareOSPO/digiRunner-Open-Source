package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0154ItemReq;
import tpi.dgrv4.dpaa.vo.DPB0154Req;
import tpi.dgrv4.dpaa.vo.DPB0154Resp;
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
public class DPB0154Service {
	
	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	public DPB0154Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao,
			BcryptParamHelper bcryptParamHelper, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0154Resp createWebsite(TsmpAuthorization auth, DPB0154Req req , ReqHeader header) {
		
		DPB0154Resp resp = new DPB0154Resp();
		
		try {

			checkParams(req);
			
			String websiteName = req.getWebsiteName();
			
			// 檢查 website 名稱是否重複
			List<DgrWebsite> list = getDgrWebsiteDao().findByWebsiteName(websiteName);
			if (!list.isEmpty()) {
				this.logger.error("The website name [" + websiteName + "] already exists.");
				throw TsmpDpAaRtnCode._1353.throwing("{{websiteName}}",websiteName);
			}
			
			// 開始新增資料
			createWebsite(req,header);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}
	
	@Transactional
	private void createWebsite(DPB0154Req req, ReqHeader header) {

		String websiteName = req.getWebsiteName();
		String websiteStatus = req.getWebsiteStatus();
		String local = header.getLocale();
		websiteStatus = getValueByBcryptParamHelper(websiteStatus, "ENABLE_FLAG", local);
		// websiteStatus = getItemsParam("ENABLE_FLAG",websiteStatus,local);
		List<DPB0154ItemReq> webSiteList = req.getWebSiteList();
		String remark = req.getRemark();

		// 新增 dgrWebsite
		DgrWebsite dgrWebsite = new DgrWebsite();
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
		
		
		// 新增 dgrWebsiteDetail
		Long websiteId = dgrWebsite.getDgrWebsiteId();
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
	
	private void checkParams(DPB0154Req req) {

		// 空值檢查
		if (req == null) {
			this.logger.error("DPB0154Req is Null !");
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		// 檢查資料是否符合規範
		for (DPB0154ItemReq itemReq : req.getWebSiteList()) {
			boolean isUrlValidOk = itemReq.isUrlValid();
			if (!isUrlValidOk) {
				this.logger.error("DPB0154Req - Please enter URL format !");
				throw TsmpDpAaRtnCode._1405.throwing();
			}
		}

		// 檢查 Probability 總和是否為 100
		List<DPB0154ItemReq> list = req.getWebSiteList();
		int total = 0;
		for (DPB0154ItemReq dpb0154ItemReq : list) {
			int number = dpb0154ItemReq.getProbability();
			total = total + number;
		}
		if (total != 100) {
			this.logger.error("DPB0154Req - Probability total is not equal to 100, which does not meet the restriction.");
			throw TsmpDpAaRtnCode._1528.throwing(total+"");
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
