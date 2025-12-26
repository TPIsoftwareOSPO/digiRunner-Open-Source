package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0417Req;
import tpi.dgrv4.dpaa.vo.AA0417Resp;
import tpi.dgrv4.entity.repository.TsmpNodeDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0417Service {

	private TsmpNodeDao tsmpNodeDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	private TPILogger logger = TPILogger.tl; 

	@Autowired
	public AA0417Service(TsmpNodeDao tsmpNodeDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpNodeDao = tsmpNodeDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0417Resp queryGreenTsmpNodeList(TsmpAuthorization authorization, AA0417Req req) {
		AA0417Resp resp = new AA0417Resp();
		try {
			// chk param
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String lastNode = req.getNode();
			List<String> excludeNode = req.getExcludeNode();
			
			LocalDateTime now = getNow();
			Date queryStartDate = minusMinutes(now, 1);//取得 now - 1分鐘
			
			List<String> nodeList = getTsmpNodeDao().queryGreenTsmpNode(lastNode, words, 
					queryStartDate, excludeNode, getPageSize());
			
			if (nodeList == null || nodeList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			resp.setNodeList(nodeList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	/**
	 * 取得現在時間減一分鐘
	 * 
	 * @param dt
	 * @param days
	 * @return
	 */
	private Date minusMinutes(LocalDateTime ldt, int minutes) {
		ldt = ldt.minusMinutes(minutes);
		return Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}
	
	protected LocalDateTime getNow() {
		LocalDateTime now = LocalDateTime.now();
		return now;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0417");
		return this.pageSize;
	}
	
	protected TsmpNodeDao getTsmpNodeDao() {
		return this.tsmpNodeDao;
	}
}