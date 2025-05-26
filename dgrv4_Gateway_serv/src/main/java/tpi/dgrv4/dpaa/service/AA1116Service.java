package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1116Item;
import tpi.dgrv4.dpaa.vo.AA1116Req;
import tpi.dgrv4.dpaa.vo.AA1116Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1116Service {

	private TsmpSecurityLevelDao tsmpSecurityLevelDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public AA1116Service(TsmpSecurityLevelDao tsmpSecurityLevelDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
		this.serviceConfig = serviceConfig;
	}

	public AA1116Resp querySecurityLevelList(TsmpAuthorization auth, AA1116Req req, ReqHeader reqHeader) {
		AA1116Resp resp = new AA1116Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			TsmpSecurityLevel lastRecord = getLastRecordFromPrevPage(req.getSecurityLevelId());
			
			List<TsmpSecurityLevel> securityLevelList = getTsmpSecurityLevelDao().querySecurityLevelList(lastRecord, words, getPageSize());
			if(CollectionUtils.isEmpty(securityLevelList)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<AA1116Item> dataList = getItemList(securityLevelList);
			resp.setDataList(dataList);
					
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<AA1116Item> getItemList(List<TsmpSecurityLevel> securityLevelList) throws Exception {
		List<AA1116Item> dataList = new ArrayList<AA1116Item>();
		for (TsmpSecurityLevel securityLevel : securityLevelList) {
			AA1116Item vo = getItem(securityLevel);
			dataList.add(vo);
		}
		return dataList;
	}

	private AA1116Item getItem(TsmpSecurityLevel securityLevel) throws Exception {
		AA1116Item data = new AA1116Item();
		
		data.setSecurityLevelId(securityLevel.getSecurityLevelId());
		data.setSecurityLevelName(securityLevel.getSecurityLevelName());
		data.setSecurityLevelDesc(ServiceUtil.nvl(securityLevel.getSecurityLevelDesc()));

		return data;
	}
	
	private TsmpSecurityLevel getLastRecordFromPrevPage(String id) {
		if (id != null) {
			Optional<TsmpSecurityLevel> opt = getTsmpSecurityLevelDao().findById(id);
			return opt.orElseThrow(() -> {return TsmpDpAaRtnCode._1297.throwing();});
		}
		return null;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa1116");
		return this.pageSize;
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}
}
