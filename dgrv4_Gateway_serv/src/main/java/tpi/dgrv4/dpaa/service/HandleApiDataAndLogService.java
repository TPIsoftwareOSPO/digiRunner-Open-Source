package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class HandleApiDataAndLogService {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpReqLogDao tsmpReqLogDao;
	private TsmpResLogDao tsmpResLogDao;
	private TsmpApiDao tsmpApiDao;

	@Autowired
	public HandleApiDataAndLogService(TsmpReqLogDao tsmpReqLogDao, TsmpResLogDao tsmpResLogDao, TsmpApiDao tsmpApiDao) {
		super();
		this.tsmpReqLogDao = tsmpReqLogDao;
		this.tsmpResLogDao = tsmpResLogDao;
		this.tsmpApiDao = tsmpApiDao;
	}

	@Transactional
	public void insertTsmpApiAndDeleteLog(List<TsmpReqLog> reqList, List<TsmpResLog> res) {

		Map<String, List<TsmpReqLog>> groupedMap = new HashMap<>();
		reqList.forEach(x -> {

			String key = x.getModuleName() + " " + x.getTxid();
			groupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(x);
		});

		List<TsmpApi> apis = new ArrayList<>();
		groupedMap.forEach((k, vList) -> {
			long success = 0l;
			long fail = 0l;
			long total = 0l;
			long elapse = 0l;
			String moduleName = k.split(" ")[0];
			String apiKey = k.split(" ")[1];
			TsmpApi api = geTsmpApiDao().findByModuleNameAndApiKey(moduleName, apiKey);
			if (api != null) {

				for (TsmpReqLog tsmpReqLog : vList) {
					for (TsmpResLog tsmpResLog : res) {

						if (tsmpResLog.getId().equals(tsmpReqLog.getId())) {

							if ("Y".equals(tsmpResLog.getExeStatus())) {
								success++;
								elapse += tsmpResLog.getElapse();

							} else {
								fail++;
							}
							total++;
						}
					}
				}
				api.setTotal(api.getTotal() + total);
				api.setSuccess(api.getSuccess() + success);
				api.setFail(api.getFail() + fail);
				api.setElapse(api.getElapse() + elapse);
				apis.add(api);

			}
		});
		geTsmpApiDao().saveAll(apis);
		getTsmpResLogDao().deleteAll(res);

		getTsmpReqLogDao().deleteAll(reqList);

		this.logger.debug("TsmpReqLog 已刪除 " + reqList.size() + " 筆紀錄");
	}

	protected TsmpApiDao geTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpReqLogDao getTsmpReqLogDao() {
		return tsmpReqLogDao;
	}

	protected TsmpResLogDao getTsmpResLogDao() {
		return tsmpResLogDao;
	}
}
