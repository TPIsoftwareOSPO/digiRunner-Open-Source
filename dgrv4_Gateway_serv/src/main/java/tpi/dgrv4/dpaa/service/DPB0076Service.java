package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0076Req;
import tpi.dgrv4.dpaa.vo.DPB0076Resp;
import tpi.dgrv4.dpaa.vo.DPB0076RespItem;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0076Service {
	
	private TPILogger logger = TPILogger.tl;

	private Integer pageSize;
	
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;
	
	@Autowired
	public DPB0076Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			TsmpOrganizationDao tsmpOrganizationDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0076Resp queryThemeLov(TsmpAuthorization authorization, DPB0076Req req) {
		DPB0076Resp resp = new DPB0076Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			Long lastId = req.getThemeId();
			String orgId = authorization.getOrgId();
			if(StringUtils.isEmpty(orgId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			List<TsmpDpThemeCategory> themeList = getTsmpDpThemeCategoryDao().query_dpb0076Service(orgDescList, words, lastId, getPageSize());

			if(themeList == null || themeList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<DPB0076RespItem> dataList = new ArrayList<>();
			resp.setDataList(dataList);
			
			resp = getResp(themeList, resp);
 
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private DPB0076Resp getResp(List<TsmpDpThemeCategory> themeList, DPB0076Resp resp) {
		List<DPB0076RespItem> dataList = resp.getDataList();
		
		for (TsmpDpThemeCategory c  : themeList) {			
			DPB0076RespItem item = new DPB0076RespItem();
			item.setThemeId(c.getApiThemeId());
			item.setThemeName(c.getApiThemeName());
			item.setOrgId(c.getOrgId());
			
			dataList.add(item);
		}
		
		return resp;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0076");
		return this.pageSize;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
}
