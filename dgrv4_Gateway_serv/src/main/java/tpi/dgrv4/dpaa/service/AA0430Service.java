package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0430Req;
import tpi.dgrv4.dpaa.vo.AA0430ReqItem;
import tpi.dgrv4.dpaa.vo.AA0430Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0430Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public AA0430Service(TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	@Transactional
	public AA0430Resp batchNoOauthModify(TsmpAuthorization authorization, AA0430Req req) {
		AA0430Resp resp = new AA0430Resp();

		try {
			checkParm(req);
			List<AA0430ReqItem> apiList = req.getApiList();
			List<TsmpApiId> apiIds = new ArrayList<>();

			Boolean noOauth = req.getNoOauth();
			
			apiList.forEach(a -> {
				TsmpApiId id = new TsmpApiId(a.getApiKey(), a.getModuleName());
				apiIds.add(id);
			});
			List<TsmpApiReg> updataData = new ArrayList<>();
			List<TsmpApi> tsmpapiList = getTsmpApiDao().findAllById(apiIds);
			tsmpapiList.forEach(a -> {
				String apikey = a.getApiKey();
				String moduleName = a.getModuleName();
				TsmpApiReg r = getTsmpApiRegDao().findByModuleNameAndApiKey(moduleName, apikey);
				if (r == null) {

					throw TsmpDpAaRtnCode._1481.throwing(apikey, moduleName);
				}
				r.setApiKey(apikey);
				r.setModuleName(moduleName);
				r.setNoOauth(noOauth ? "1" : "0");
				r.setUpdateTime(DateTimeUtil.now());
				r.setUpdateUser(authorization.getUserName());
				updataData.add(r);
				a.setUpdateTime(DateTimeUtil.now());
				a.setUpdateUser(authorization.getUserName());
				
			});
			getTsmpApiRegDao().saveAll(updataData);
			getTsmpApiDao().saveAll(tsmpapiList);
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

	private void checkParm(AA0430Req req) {
		if (CollectionUtils.isEmpty(req.getApiList())) {
			throw TsmpDpAaRtnCode._1350.throwing("apiList");
		}
		Boolean noOauth = req.getNoOauth();
		if (noOauth == null) {
			throw TsmpDpAaRtnCode._1350.throwing("noOauth");
		}
	
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}
}
