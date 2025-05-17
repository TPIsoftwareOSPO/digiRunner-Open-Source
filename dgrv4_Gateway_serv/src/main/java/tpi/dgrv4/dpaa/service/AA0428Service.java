package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpOrganizationCacheProxy;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0301Item;
import tpi.dgrv4.dpaa.vo.AA0428Req;
import tpi.dgrv4.dpaa.vo.AA0428Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.vo.AA0301SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0428Service {
	private TPILogger logger = TPILogger.tl;
	private Integer pageSize;

	private TsmpApiDao tsmpApiDao;
	private ServiceConfig serviceConfig;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private AA0301Service aa0301Service;

	@Autowired
	public AA0428Service(TsmpApiDao tsmpApiDao, ServiceConfig serviceConfig, BcryptParamHelper bcryptParamHelper,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, AA0301Service aa0301Service) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.serviceConfig = serviceConfig;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpOrganizationCacheProxy = tsmpOrganizationCacheProxy;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.aa0301Service = aa0301Service;
	}

	public AA0428Resp queryAPIListByLabel(TsmpAuthorization authorization, AA0428Req req, ReqHeader reqHeader) {
		AA0428Resp resp = new AA0428Resp();
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			AA0301SearchCriteria cri = checkAndSetParm(authorization, req);
			List<TsmpApi> tspmApi = getTsmpApiDao().query_AA0428Service(cri);

			if (tspmApi.size() == 0) {
				// 1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<AA0301Item> itemList = getAa0301Service().setAA0301Data(tspmApi, locale);
			
			Map<String, String> sortBy = new HashMap<>();
			sortBy.put("apiKey", "asc");
			resp.setSortBy(sortBy);

			resp.setDataList(itemList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private AA0301SearchCriteria checkAndSetParm(TsmpAuthorization authorization, AA0428Req req) {
		String orgId = authorization.getOrgId();
		if (!StringUtils.hasLength(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing(); // 1273:組織單位ID:必填參數
		}

		String paging = nvl(req.getPaging());
		if (StringUtils.hasLength(paging)) {
			if (!"Y".equals(paging) && !"N".equals(paging)) {
				throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
			}
		} else {
			paging = "N";
		}
		List<String> labeList = req.getLabelList();
		if (CollectionUtils.isEmpty(labeList)) {
			throw TsmpDpAaRtnCode._2009.throwing("1");
		}

		String apiKey = nvl(req.getApiKey());
		String moduleName = nvl(req.getModuleName());
		TsmpApi lastTsmpApi = null;

		TsmpApiId id = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> optApi = getTsmpApiDao().findById(id);
		if (optApi.isPresent()) {
			lastTsmpApi = optApi.get();
		}
		AA0301SearchCriteria cri = new AA0301SearchCriteria();
		cri.setLastTsmpApi(lastTsmpApi);
		// 取得組織原則
		List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(),
				Integer.MAX_VALUE); // 組織與子組織的orgId
		cri.setOrgList(orgList);
		cri.setPaging(paging);
		cri.setPageSize(getPageSize());
		String reqKey = "apiKey";
		String reqValue = "asc";
		cri.setSortColumn(nvl(reqKey));
		cri.setSort(nvl(reqValue));
		cri.setLabeList(labeList);
		return cri;

	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0301");
		return pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpOrganizationCacheProxy getTsmpOrganizationCacheProxy() {
		return this.tsmpOrganizationCacheProxy;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected AA0301Service getAa0301Service() {
		return aa0301Service;
	}
}
