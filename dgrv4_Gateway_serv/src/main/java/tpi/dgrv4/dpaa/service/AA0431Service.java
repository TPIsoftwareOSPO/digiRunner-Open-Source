package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0431Req;
import tpi.dgrv4.dpaa.vo.AA0431ReqItem;
import tpi.dgrv4.dpaa.vo.AA0431Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0431Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public AA0431Service(TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	@Transactional
	public AA0431Resp batchLabelReset(TsmpAuthorization authorization, AA0431Req req) {
		AA0431Resp resp = new AA0431Resp();
		try {
			checkParm(req);
			List<AA0431ReqItem> apiList = req.getApiList();
			List<String> labelList = req.getLabelList();
			List<TsmpApiId> ids = new ArrayList<>();
			apiList.forEach(a -> {
				TsmpApiId id = new TsmpApiId(a.getApiKey(), a.getModuleName());
				ids.add(id);
			});
			List<TsmpApi> apis = getTsmpApiDao().findAllById(ids);
			List<TsmpApi> updateData = new ArrayList<>();
			apis.forEach(a -> {
				String apikey = a.getApiKey();
				String moduleName = a.getModuleName();

				TsmpApiReg r = getTsmpApiRegDao().findByModuleNameAndApiKey(moduleName, apikey);
				if (r == null) {

					throw TsmpDpAaRtnCode._1481.throwing(apikey, moduleName);
				}

				String[] label = new String[5];
				if (labelList != null && labelList.size() > 0) {
					for (int i = 0; i < labelList.size(); i++) {
						label[i] = (labelList.get(i).toLowerCase());

					}

				}
				a.setApiKey(apikey);
				a.setModuleName(moduleName);
				a.setLabel1(label[0]);
				a.setLabel2(label[1]);
				a.setLabel3(label[2]);
				a.setLabel4(label[3]);
				a.setLabel5(label[4]);
				a.setUpdateTime(DateTimeUtil.now());
				a.setUpdateUser(authorization.getUserName());
				updateData.add(a);

			});

			getTsmpApiDao().saveAll(updateData);

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

	private void checkParm(AA0431Req req) {
		List<AA0431ReqItem> apiList = req.getApiList();
		List<String> labelList = req.getLabelList();
		if (apiList == null || apiList.size() == 0) {
			throw TsmpDpAaRtnCode._1350.throwing("apiList");
		}
		if (!CollectionUtils.isEmpty(labelList)) {
			if (labelList.size() > 5) {
				throw TsmpDpAaRtnCode._1407.throwing("{{labelList}}", "5", String.valueOf(labelList.size()));
			}
			labelList.forEach(l -> {
				if (l.length() > 20) {
					throw TsmpDpAaRtnCode._1351.throwing(l, "20", String.valueOf(l.length()));
				}
			});
		}
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}
}
