package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0040Req;
import tpi.dgrv4.dpaa.vo.DPB0040Resp;
import tpi.dgrv4.dpaa.vo.DPB0040TsmpModule;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpDpDeniedModule;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpDpDeniedModuleDao;
import tpi.dgrv4.gateway.component.ServiceConfig;

@Service
public class DPB0040Service {

	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpDeniedModuleDao tsmpDpDeniedModuleDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0040Service(TsmpApiModuleDao tsmpApiModuleDao, TsmpDpDeniedModuleDao tsmpDpDeniedModuleDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpDeniedModuleDao = tsmpDpDeniedModuleDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0040Resp queryModuleLikeList(String clientId, DPB0040Req req) {
				
		//查找 Table 
		String keyword = req.getKeyword();
		String[] words = getKeywords(keyword, " ");
		String moduleName = req.getModuleName();
		String moduleVersion = req.getModuleVersion();
		List<TsmpApiModule> tsmpApiModuleList = getTsmpApiModuleDao().queryApiDocsListLike(words //
				, moduleName, moduleVersion, getPageSize());
		if (tsmpApiModuleList == null || tsmpApiModuleList.size()==0) {
			throw TsmpDpAaRtnCode.NO_MODULE_DATA.throwing();
		}
		
		//逐筆計算[public or private]
		List<DPB0040TsmpModule> moduleList = new ArrayList<DPB0040TsmpModule>();
		List<TsmpDpDeniedModule> tsmpDpDeniedModuleList = getTsmpDpDeniedModuleDao().findAll();
		List<String> deniedNames = new ArrayList<>();
		for (TsmpDpDeniedModule tsmpDpDeniedModule : tsmpDpDeniedModuleList) {
			deniedNames.add(tsmpDpDeniedModule.getRefModuleName());
		}
		for (TsmpApiModule tsmpApiModule : tsmpApiModuleList) {
			DPB0040TsmpModule module = new DPB0040TsmpModule();
			module.setModuleName(tsmpApiModule.getModuleName());
			module.setModuleVersion(tsmpApiModule.getModuleVersion());
			module.setId(tsmpApiModule.getId());
			boolean isContains = deniedNames.contains(tsmpApiModule.getModuleName());
			if (isContains) {
				module.setDeniedFlag("1");
			} else {
				module.setDeniedFlag("0");
			}
			moduleList.add(module);
		}

		DPB0040Resp resp = new DPB0040Resp();
		resp.setModuleList(moduleList);
		return resp;
	}
	
	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return tsmpApiModuleDao;
	}

	protected TsmpDpDeniedModuleDao getTsmpDpDeniedModuleDao() {
		return tsmpDpDeniedModuleDao;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = this.serviceConfig.getDefaultPageSize();
		return this.pageSize;
	}
	
}
