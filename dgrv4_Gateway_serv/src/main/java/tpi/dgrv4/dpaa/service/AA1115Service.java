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
import tpi.dgrv4.dpaa.vo.AA1115GroupAuthorities;
import tpi.dgrv4.dpaa.vo.AA1115Req;
import tpi.dgrv4.dpaa.vo.AA1115Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1115Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private BcryptParamHelper bcryptParamHelper;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;

	@Autowired
	public AA1115Service(TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao, BcryptParamHelper bcryptParamHelper,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.serviceConfig = serviceConfig;
	}

	public AA1115Resp queryScopeAuthorities(TsmpAuthorization authorization, AA1115Req req) {
		AA1115Resp resp = new AA1115Resp();

		try {
			
			String lastGroupAuthoritieId = req.getLastGroupAuthoritieId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			List<String> selectedGroupAuthoritieIdList = req.getSelectedGroupAuthoritieIdList();
			
			// 條件查詢 + 分頁 + 關鍵字 
			List<TsmpGroupAuthorities> data = getTsmpGroupAuthoritiesDao().queryByIdAndLevelAndKeyword(lastGroupAuthoritieId, words, selectedGroupAuthoritieIdList, getPageSize());
			
			if (data == null || data.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			// 將Entity資料轉換成VO
			List<AA1115GroupAuthorities> groupAuthoritiesList = convertAA1115GroupAuthorities(data);
			
			resp.setGroupAuthoritiesList(groupAuthoritiesList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<AA1115GroupAuthorities> convertAA1115GroupAuthorities(List<TsmpGroupAuthorities> data){
		
		List<AA1115GroupAuthorities> groupAuthoritiesList = new ArrayList<AA1115GroupAuthorities>();
		
		for (TsmpGroupAuthorities groupAuthorities : data) {
			AA1115GroupAuthorities aa1115GroupAuthorities = new AA1115GroupAuthorities();
			
			aa1115GroupAuthorities.setGroupAuthoritiesId(groupAuthorities.getGroupAuthoritieId());
			aa1115GroupAuthorities.setGroupAuthoritiesName(groupAuthorities.getGroupAuthoritieName());
			aa1115GroupAuthorities.setGroupAuthoritiesLevel(groupAuthorities.getGroupAuthoritieLevel());
			aa1115GroupAuthorities.setGroupAuthoritiesDesc(groupAuthorities.getGroupAuthoritieDesc());
			
			groupAuthoritiesList.add(aa1115GroupAuthorities);
		}

		return groupAuthoritiesList;
	}
	
	public BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa1115");
		return this.pageSize;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	
}
