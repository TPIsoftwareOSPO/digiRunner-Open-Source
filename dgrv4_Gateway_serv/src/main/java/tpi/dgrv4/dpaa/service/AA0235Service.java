package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0235API;
import tpi.dgrv4.dpaa.vo.AA0235Module;
import tpi.dgrv4.dpaa.vo.AA0235Req;
import tpi.dgrv4.dpaa.vo.AA0235Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0235Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpApiDao tsmpApiDao;

	@Autowired
	public AA0235Service(TsmpGroupDao tsmpGroupDao, TsmpGroupApiDao tsmpGroupApiDao, TsmpApiDao tsmpApiDao) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpApiDao = tsmpApiDao;
	}

	public AA0235Resp queryModuleListByGroupId(TsmpAuthorization authorization, AA0235Req req) {
		AA0235Resp resp = new AA0235Resp();
		try {

			Optional<TsmpGroup> opt_tsmpGroup = getTsmpGroupDao().findById(req.getGroupId());
			if (opt_tsmpGroup.isPresent()==false) {
				throw TsmpDpAaRtnCode._1360.throwing();
			}
			
			//以Group ID查詢出，不重複的ModuleName
			List<String> uniqueModule = getTsmpGroupApiDao().findUniqueModuleByGroupIdOrderByModuleName(opt_tsmpGroup.get().getGroupId());
			
			List<AA0235Module> aa0235ModuleList = new ArrayList<AA0235Module>();
			
			if (uniqueModule!=null && uniqueModule.size()>0) {
				
				//以Group ID與Module Name查詢出Api Key
				for (String moduleName : uniqueModule) {
					AA0235Module aa0235Module = new AA0235Module();
					aa0235Module.setModuleName(moduleName);	
					List<AA0235API> aa0235APIList = new ArrayList<AA0235API>();
					aa0235Module.setApiList(aa0235APIList);
					aa0235ModuleList.add(aa0235Module);
					
					List<TsmpGroupApi>  groupApiList = getTsmpGroupApiDao().findByGroupIdAndModuleName(opt_tsmpGroup.get().getGroupId(), moduleName);
					if (groupApiList!=null && groupApiList.isEmpty() == false) {
						for (TsmpGroupApi tsmpGroupApi : groupApiList) {
							AA0235API aa0235API = new AA0235API();	  
							aa0235API.setApiKey(tsmpGroupApi.getApiKey());
							aa0235Module.getApiList().add(aa0235API);
						}
					}
				}
	
				//以Module Name與Api Key查詢出Api Name
				for (AA0235Module aa0235Module : aa0235ModuleList) {
					for (AA0235API aa0235API : aa0235Module.getApiList()) {
						TsmpApi tsmpApi = getTsmpApiDao().findByModuleNameAndApiKey(aa0235Module.getModuleName(), aa0235API.getApiKey());
						if (tsmpApi!=null) {
							aa0235API.setApiName(tsmpApi.getApiName());		
						}
					}
				}
			}
			resp.setModuleList(aa0235ModuleList);
			
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

}
