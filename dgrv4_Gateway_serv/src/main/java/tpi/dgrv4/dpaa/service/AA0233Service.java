package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0233Req;
import tpi.dgrv4.dpaa.vo.AA0233Resp;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0233Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0233Service(TsmpApiDao tsmpApiDao, TsmpOrganizationDao tsmpOrganizationDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0233Resp queryModuleByClinetOrgId(TsmpAuthorization authorization, AA0233Req req) {
		AA0233Resp resp = new AA0233Resp();
		try {
			
			String orgId = authorization.getOrgId();
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
			
			// 查無資料
			if (orgDescList == null || orgDescList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			String lastModuleNameRecord = req.getModuleName();
			String[] keyword = ServiceUtil.getKeywords(req.getKeyword(), " ");
			List<String> selectedModuleNameList = req.getSelectedModuleNameList();
			
			// 進行 分頁 + 關鍵字 查詢
			List<String> moduleNameList = getTsmpApiDao().queryModuleByClinetOrgId(lastModuleNameRecord, keyword, orgDescList, selectedModuleNameList, getPageSize());
			
			// 查無資料
			if (moduleNameList == null || moduleNameList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			resp.setModuleNameList(moduleNameList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0233");
		return this.pageSize;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	
}
