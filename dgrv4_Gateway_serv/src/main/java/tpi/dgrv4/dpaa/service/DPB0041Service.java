package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0041Req;
import tpi.dgrv4.dpaa.vo.DPB0041Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpDpDeniedModule;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpDpDeniedModuleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0041Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpDeniedModuleDao tsmpDpDeniedModuleDao;

	@Autowired
	public DPB0041Service(TsmpApiModuleDao tsmpApiModuleDao, TsmpDpDeniedModuleDao tsmpDpDeniedModuleDao) {
		super();
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpDeniedModuleDao = tsmpDpDeniedModuleDao;
	}

	@Transactional
	public DPB0041Resp saveDeniedModule(TsmpAuthorization authorization, DPB0041Req req) {
		try {
			String userName = authorization.getUserName();
			if (userName == null || userName.isEmpty()) {
				throw new Exception("Operation denied! Please login!");
			}

			final String input = req.getModuleNames();
			final List<String> newDeniedModuleNames = getNewDeniedModuleNames(input);
			boolean isExists = true;
			for(String newDeniedModuleName : newDeniedModuleNames) {
				isExists = getTsmpApiModuleDao().isExistsByModuleName(newDeniedModuleName);
				if (!isExists) {
					throw new Exception("Module name(" + newDeniedModuleName + ") does not exist!");
				}
			}

			getTsmpDpDeniedModuleDao().deleteAll();

			for(String newDeniedModuleName : newDeniedModuleNames) {
				insertNewDeniedModule(userName, newDeniedModuleName);
			}

			return new DPB0041Resp();
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_SAVE_DENIED_MODULE.throwing();
		}
	}

	private void insertNewDeniedModule(String userName, String refModuleName) {
		TsmpDpDeniedModule m = new TsmpDpDeniedModule();
		m.setRefModuleName(refModuleName);
		m.setCreateDateTime(DateTimeUtil.now());
		m.setCreateUser(userName);
		getTsmpDpDeniedModuleDao().save(m);
	}

	private List<String> getNewDeniedModuleNames(String input) {
		if (input != null && !input.isEmpty()) {
			List<String> newModuleNames = new ArrayList<>();
			String[] inputs = getKeywords(input, ",");
			for(String name : inputs) {
				name = name.trim();
				if (!name.isEmpty()) {
					newModuleNames.add(name);
				}
			}
			return newModuleNames;
		}

		return Collections.emptyList();
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpDpDeniedModuleDao getTsmpDpDeniedModuleDao() {
		return this.tsmpDpDeniedModuleDao;
	}

}
