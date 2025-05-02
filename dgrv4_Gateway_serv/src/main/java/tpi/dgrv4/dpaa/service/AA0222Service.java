package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpGroupAuthoritiesCacheProxy;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpSecurityLevelCacheProxy;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0222Item;
import tpi.dgrv4.dpaa.vo.AA0222Req;
import tpi.dgrv4.dpaa.vo.AA0222Resp;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpVgroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.*;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

@Service
public class AA0222Service {

	private Integer pageSize;
	
	private TsmpSecurityLevelCacheProxy securityLevelCacheProxy;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy;
	private ServiceConfig serviceConfig;

	@Autowired
	public AA0222Service(TsmpSecurityLevelCacheProxy securityLevelCacheProxy, TsmpVgroupDao tsmpVgroupDao,
			TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao,
			TsmpGroupAuthoritiesCacheProxy tsmpGroupAuthoritiesCacheProxy, ServiceConfig serviceConfig) {
		super();
		this.securityLevelCacheProxy = securityLevelCacheProxy;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpVgroupAuthoritiesMapDao = tsmpVgroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesCacheProxy = tsmpGroupAuthoritiesCacheProxy;
		this.serviceConfig = serviceConfig;
	}

	public AA0222Resp queryVGroupList_1(AA0222Req req) {
		AA0222Resp resp = new AA0222Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");	// 虛擬群組代碼,虛擬群組名稱
			String securityLevelId = req.getSecurityLevelId();
			List<String> vgroupAuthoritiesIds = req.getVgroupAuthoritiesIds();
			String vgroupId = req.getVgroupId();

			checkParams(req);
			
			TsmpVgroup lastTsmpVgroup = null;
			if(!StringUtils.isEmpty(vgroupId)) {
				Optional<TsmpVgroup> tsmpVgroup = getTsmpVgroupDao().findById(vgroupId);
				if (tsmpVgroup.isPresent()) {
					lastTsmpVgroup = tsmpVgroup.get();
				}
			}
			List<TsmpVgroup> vgroupList = getTsmpVgroupDao().query_aa0222Service(lastTsmpVgroup, securityLevelId, 
										vgroupAuthoritiesIds, words, getPageSize());
			
			// 1298:查無資料
			if(vgroupList == null || vgroupList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//parser
			List<AA0222Item> dataList = getAA0222Item(vgroupList);
			resp.setDataList(dataList);
			

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private void checkParams(AA0222Req req) throws Exception {
		// 1364:安全等級不存在
		checkSecurityLevel(req);
		
		// 1397:授權核身種類:[{{0}}]不存在
		checkAuthoritiesIds(req);
		
		
	}
	
	/**
	 * 檢查傳入的 securityLevelId 是否存在 TSMP_SECURITY_LEVEL 中, 否則 throw 1364:安全等級不存在。
	 * 				
	 * @param req
	 * @throws Exception
	 */
	private void checkSecurityLevel(AA0222Req req) throws Exception {
		
		// 1364:安全等級不存在
		String securityLevelId = req.getSecurityLevelId();
		if(!StringUtils.isEmpty(securityLevelId)) {
			TsmpSecurityLevel securityLV = getSecurityLVById(securityLevelId);
			
			if(securityLV == null) {
				throw TsmpDpAaRtnCode._1364.throwing();
				
			}
			
		}
	}
	
	
	/**
	 * 檢查傳入的每個 vgroupAuthoritiesIds 是否都存在 TSMP_GROUP_AUTHORITIES 中, 否則 throw 1397:授權核身種類:[{{0}}]不存在。				
	 *
	 * @param req
	 * @throws Exception
	 */
	private void checkAuthoritiesIds(AA0222Req req) {
		// 1397:授權核身種類:[{{0}}]不存在
		List<String> authoritiesIdList = req.getVgroupAuthoritiesIds();
		
		if(authoritiesIdList != null) {
			authoritiesIdList.forEach((authoritiesId)->{
				TsmpGroupAuthorities tsmpGroupAuthorities =	getAuthoritiesyId(authoritiesId);
				if(tsmpGroupAuthorities == null) {
					throw TsmpDpAaRtnCode._1397.throwing(authoritiesId);
				}
				
			});
			
		}
		
	}
	
	/**
	 * 由 securityLevelId ,使用快取取得SecurityLV
	 * 
	 * @param securityLevelId
	 * @return
	 */
	private TsmpSecurityLevel getSecurityLVById(String securityLevelId) {
		TsmpSecurityLevel i = getSecurityLevelCacheProxy().findById(securityLevelId).orElse(null);
		return i;
	}
	
	/**
	 * 由 authoritiesId ,使用快取取得TsmpGroupAuthorities
	 * 
	 * @param authoritiesId
	 * @return
	 */
	private TsmpGroupAuthorities getAuthoritiesyId(String authoritiesId) {
		TsmpGroupAuthorities i = getTsmpGroupAuthoritiesCacheProxy().findById(authoritiesId).orElse(null);
		return i;
	}
	
	private List<AA0222Item> getAA0222Item(List<TsmpVgroup> vgroupList) {
		List<AA0222Item> dataList = new ArrayList<>();
		for (TsmpVgroup group : vgroupList) {
			// desc
			boolean isDescTruncated = false;
			String oriVgroupDesc = ServiceUtil.nvl(group.getVgroupDesc());
			String vgroupDesc = oriVgroupDesc;
			if (oriVgroupDesc.length() > 30) {
				vgroupDesc = oriVgroupDesc.substring(0, 30) + "...";
				isDescTruncated = true;
			}
			
			// gorupAuthorities
			String oriVgroupAuthorities = getGorupAuthorities(group.getVgroupId());
			String vgroupAuthorities = oriVgroupAuthorities;
			boolean isTruncated = false;
			if (oriVgroupAuthorities.length() > 30) {
				vgroupAuthorities = oriVgroupAuthorities.substring(0, 30) + "...";
				isTruncated = true;
			}
			
			// securityLevelName
			String securityLVName = "";
			String lvId = nvl(group.getSecurityLevelId());
			if (!lvId.isBlank()) {
				TsmpSecurityLevel securityLV = getSecurityLVById(lvId);
				if (securityLV != null) {
					securityLVName = securityLV.getSecurityLevelName();
				} else {
					securityLVName = "Unknown";
					//logger.warn("Security level not found for ID: " + lvId);
					TPILogger.tl.warn("Security level not found for ID: " + lvId);
				}
			}
			
			// createTime
			String createDateStr = "";
			Date createDate = group.getCreateTime();
			Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(createDate, null);
			if(createDateOpt.isPresent()) {
				createDateStr = createDateOpt.get();
			}
			
			AA0222Item item = new AA0222Item();
			item.setVgroupId(group.getVgroupId());
			item.setVgroupName(group.getVgroupName());
			item.setVgroupAlias(ServiceUtil.nvl(group.getVgroupAlias()));
			
			item.setIsDescTruncated(isDescTruncated);
			item.setVgroupDesc(vgroupDesc);
			item.setOriVgroupDesc(oriVgroupDesc);
			
			item.setIsTruncated(isTruncated);
			item.setVgroupAuthorities(vgroupAuthorities);
			item.setOriVgroupAuthorities(oriVgroupAuthorities);
			
			item.setSecurityLevelName(securityLVName);
			item.setCreateTime(createDateStr);
			
			
			dataList.add(item);
		}
		return dataList;
	}
	
	private String getGorupAuthorities(String vgroupId) {
		StringBuffer sb = new StringBuffer();
		String gorupAuthorities = "";
		List<TsmpVgroupAuthoritiesMap> vgroupAuthoritiesMaps =  getTsmpVgroupAuthoritiesMapDao().findByVgroupId(vgroupId);
		
		if(vgroupAuthoritiesMaps != null) {
			vgroupAuthoritiesMaps.forEach((m) ->{
				String authoritiesId = m.getVgroupAuthoritieId();
				if(!StringUtils.isEmpty(authoritiesId)) {
					TsmpGroupAuthorities tsmpGroupAuthorities = getAuthoritiesyId(authoritiesId);
					if(tsmpGroupAuthorities != null) {
						sb.append(tsmpGroupAuthorities.getGroupAuthoritieName() + ",");
					}
				}
			});
			
			if(!sb.toString().isEmpty()) {
				gorupAuthorities = sb.toString().substring(0, sb.toString().length()-1);
			}
		}
		return gorupAuthorities;
		
	}
	
	protected TsmpSecurityLevelCacheProxy getSecurityLevelCacheProxy() {
		return this.securityLevelCacheProxy;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}
	
	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpGroupAuthoritiesCacheProxy getTsmpGroupAuthoritiesCacheProxy() {
		return this.tsmpGroupAuthoritiesCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}	

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0222");
		return this.pageSize;
	}
	
}
