package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0153NameItem;
import tpi.dgrv4.dpaa.vo.DPB0153Req;
import tpi.dgrv4.dpaa.vo.DPB0153Resp;
import tpi.dgrv4.dpaa.vo.DPB0153Trunc;
import tpi.dgrv4.dpaa.vo.DPB0153WebsiteItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Service
public class DPB0153Service {

	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;
	private TsmpSettingService tsmpSettingService;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0153Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao,
			TsmpSettingService tsmpSettingService, BcryptParamHelper bcryptParamHelper,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ServiceConfig serviceConfig) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
		this.tsmpSettingService = tsmpSettingService;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
	}

	public DPB0153Resp queryWebsite(TsmpAuthorization auth, DPB0153Req req, ReqHeader header) {

		DPB0153Resp resp = new DPB0153Resp();

		try {
			// 分頁用，DGR_WEBSITE.DGR_WEBSITE_ID，是當頁資料的最後一筆 id，按下 [查看更多] 時才需要帶入。
			String id = req.getId();
			String keywords = req.getKeyword();
			String local = header.getLocale();
			String websiteStatus = req.getWebsiteStatus();
			// 當websiteStatus為-1，表示「全部」。不需進行解密。
			if (websiteStatus != null && !"-1".equals(websiteStatus)) {
				websiteStatus = getValueByBcryptParamHelper(websiteStatus, "ENABLE_FLAG", local);
			}

			// 填入名稱清單
			List<DgrWebsite> list = getDgrWebsiteDao().findByWebsiteStatus("Y");
			
			list.forEach(in -> {
				DPB0153NameItem item = new DPB0153NameItem();
				Long dgrWebsiteId = in.getDgrWebsiteId();
				String name = in.getWebsiteName();
				item.setId(dgrWebsiteId.toString());
				item.setName(name);
			});
			

			List<DPB0153WebsiteItem> respWebsiteList = new ArrayList<>();

			String[] words = ServiceUtil.getKeywords(keywords, " ");
			Long websiteId = null;
			if (StringUtils.hasText(id)) {
				websiteId = Long.parseLong(id);
			}
			List<DgrWebsite> websiteList = getDgrWebsiteDao().findByDgrWebsiteIdAndKeyword(websiteId, websiteStatus,
					words, getPageSize());

			if (!websiteList.isEmpty()) {

				websiteList.forEach(e -> {
					Long websiteAId = e.getDgrWebsiteId();
					String websiteAName = e.getWebsiteName();
					String websiteAStatus = e.getWebsiteStatus();
					String websiteARemark = e.getRemark();

					DPB0153WebsiteItem item = new DPB0153WebsiteItem();
					DPB0153Trunc trunc = null;

					if (StringUtils.hasText(websiteARemark)) {
						trunc = new DPB0153Trunc(websiteARemark);
					} else {
						trunc = new DPB0153Trunc();
					}

					item.setId(websiteAId.toString());
					item.setName(websiteAName);
					item.setStatus(websiteAStatus);
					item.setStatusName(getSubitemNameByParam2("ENABLE_FLAG", websiteAStatus, local));// 狀態名稱
					item.setRemark(trunc);

					respWebsiteList.add(item);

				});
			}else {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setWebsiteList(respWebsiteList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<Long> findElementsInBNotInA(List<DgrWebsite> websiteList, List<DgrWebsiteDetail> websiteDetailList) {

		Set<Long> theWebsiteLongId = new HashSet<>();

		websiteList.forEach(e -> {
			Long id = e.getDgrWebsiteId();
			theWebsiteLongId.add(id);
		});

		websiteDetailList.forEach(e -> {
			Long id = e.getDgrWebsiteId();
			theWebsiteLongId.add(id);
		});

		ArrayList<Long> sortedList = new ArrayList<>(theWebsiteLongId);
		Collections.sort(sortedList);
		return sortedList;
	}

	private String getItemsParam(String itemNo, String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);

		if (vo == null) {
			return null;
		}

		return vo.getParam2();
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
	
	private String getSubitemNameByParam2(String itemNo, String param2, String locale) {
		TsmpDpItems n = getTsmpDpItemsCacheProxy().findByItemNoAndParam2AndLocale(itemNo, param2, locale);
		if (n==null) {
			return null;
		}
		String subitemName = n.getSubitemName();
		return subitemName;
	}

	protected DgrWebsiteDao getDgrWebsiteDao() {
		return dgrWebsiteDao;
	}

	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return dgrWebsiteDetailDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("DPB0153");
		return this.pageSize;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}
