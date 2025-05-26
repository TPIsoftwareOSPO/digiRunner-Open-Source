package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0083Req;
import tpi.dgrv4.dpaa.vo.DPB0083Resp;
import tpi.dgrv4.dpaa.vo.DPB0083RespItem;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0083Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;

	@Autowired
	public DPB0083Service(TsmpClientDao tsmpClientDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0083Resp queryClientListByLike(TsmpAuthorization tsmpAuthorization, DPB0083Req req) {
		DPB0083Resp resp = new DPB0083Resp();

		try {
			String lastId = req.getClientId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			List<TsmpClient> clientList = getTsmpClientDao().queryLike(lastId, words, getPageSize());
			
			if (clientList == null || clientList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<DPB0083RespItem> dataList = getRespItemList(clientList);
			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<DPB0083RespItem> getRespItemList(List<TsmpClient> clientList) {
		List<DPB0083RespItem> dataList = new ArrayList<DPB0083RespItem>();
		for (TsmpClient client : clientList) {
			DPB0083RespItem data = getRespItem(client);
			dataList.add(data);
		}
		return dataList;
	}
	
	private DPB0083RespItem getRespItem(TsmpClient c) {
		DPB0083RespItem data = new DPB0083RespItem();
		data.setClientId(c.getClientId());
		data.setClientName(c.getClientName());
		data.setClientAlias(c.getClientAlias());
		data.setEmails(c.getEmails());
		data.setClientStatus(c.getClientStatus());
		
		return data;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0083");
		return this.pageSize;
	}
}
