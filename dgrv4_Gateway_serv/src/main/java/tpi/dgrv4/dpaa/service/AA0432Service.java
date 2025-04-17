package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0432Req;
import tpi.dgrv4.dpaa.vo.AA0432ReqItem;
import tpi.dgrv4.dpaa.vo.AA0432Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0432Service {
	private TPILogger logger = TPILogger.tl;


	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public AA0432Service(TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	public AA0432Resp batchFailHandlePolicy(TsmpAuthorization authorization, AA0432Req req) {
		AA0432Resp resp = new AA0432Resp();
		try {
			checkParm(req);
			List<TsmpApiReg> apiRegs = new ArrayList<>();
			List<TsmpApi> apis = new ArrayList<>();
			List<AA0432ReqItem> apiList = req.getApiList();
			for (AA0432ReqItem aa0432ReqItem : apiList) {
				String apiKey = aa0432ReqItem.getApiKey();
				String moduleName = aa0432ReqItem.getModuleName();
				TsmpApi api = getTsmpApiDao().findByModuleNameAndApiKey(moduleName, apiKey);
				if (api != null) {
					String type = AA0302Service.getType(api.getApiSrc(), apiKey, moduleName);
					if ("1".equals(type)) {
						TsmpApiReg apiReg = getTsmpApiRegDao().findByModuleNameAndApiKey(moduleName, apiKey);
						if (apiReg == null) {
							throw TsmpDpAaRtnCode._1481.throwing(apiKey, moduleName);
						}
						apiReg.setFailDiscoveryPolicy(req.getFailDiscoveryPolicy());
						apiReg.setFailHandlePolicy(req.getFailHandlePolicy());
						apiReg.setUpdateUser(authorization.getUserName());
						apiReg.setUpdateTime(DateTimeUtil.now());
						apiRegs.add(apiReg);
						
						api.setUpdateTime(DateTimeUtil.now());
						api.setUpdateUser(authorization.getUserName());
						apis.add(api);
					}
					
				}
			}

			getTsmpApiRegDao().saveAll(apiRegs);
			getTsmpApiDao().saveAll(apis);
			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.API.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;

	}

	private void checkParm(AA0432Req req) {
		if (CollectionUtils.isEmpty(req.getApiList())) {
			throw TsmpDpAaRtnCode._1350.throwing("apiList");
		}

		if (!StringUtils.hasLength(req.getFailDiscoveryPolicy())) {
			throw TsmpDpAaRtnCode._1350.throwing("failDiscoveryPolicy");
		}
		if (!StringUtils.hasLength(req.getFailHandlePolicy())) {
			throw TsmpDpAaRtnCode._1350.throwing("failHandlePolicy");
		}
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

}
