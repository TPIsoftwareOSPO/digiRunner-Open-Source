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
import tpi.dgrv4.dpaa.vo.DPB0095Item;
import tpi.dgrv4.dpaa.vo.DPB0095Req;
import tpi.dgrv4.dpaa.vo.DPB0095Resp;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;						
						
@Service						
public class DPB0095Service {						
						
	private TsmpClientDao tsmpClientDao;					
	private ServiceConfig serviceConfig;					
						
	private Integer pageSize;					
				
	private TPILogger logger = TPILogger.tl;		
						
	@Autowired					
	public DPB0095Service(TsmpClientDao tsmpClientDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0095Resp queryClientListByRegStatusLike(TsmpAuthorization auth, DPB0095Req req) {	
		DPB0095Resp resp = new DPB0095Resp();

		try {				
			// chk param	
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String lastId = req.getClientId();	
			String regStatus = req.getRegStatus();	
			
			List<TsmpClient> clientList = getTsmpClientDao().queryByRegStatusAndLike(lastId, words, regStatus, getPageSize());
			
			if (clientList == null || clientList.isEmpty()) {			
				throw TsmpDpAaRtnCode._1298.throwing();		
			}			
			
			List<DPB0095Item> dataList = getRespItemList(clientList);
			
			resp.setDataList(dataList);
			
		} catch (TsmpDpAaException e) {				
			throw e;			
		} catch (Exception e) {				
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();			
		}				
		return resp;				
	}		
	
	private List<DPB0095Item> getRespItemList(List<TsmpClient> clientList){
		List<DPB0095Item> dataList = new ArrayList<DPB0095Item>();
		for (TsmpClient client : clientList) {
			DPB0095Item data = new DPB0095Item();
			data.setClientId(client.getClientId());	
			data.setClientName(client.getClientName());	
			data.setClientAlias(client.getClientAlias());	
			data.setEmails(client.getEmails());	
			data.setClientStatus(client.getClientStatus());	
			
			dataList.add(data);
		}
		return dataList;
	}
						
	protected ServiceConfig getServiceConfig() {					
		return this.serviceConfig;				
	}					
						
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0095");				
		return this.pageSize;				
	}					
						
	protected TsmpClientDao getTsmpClientDao() {					
		return tsmpClientDao;				
	}					
}						
						