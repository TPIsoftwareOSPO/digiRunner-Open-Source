package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpSecurityLevelCacheProxy;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0223Auth;
import tpi.dgrv4.dpaa.vo.AA0223Req;
import tpi.dgrv4.dpaa.vo.AA0223Resp;
import tpi.dgrv4.dpaa.vo.ConvertTimeUnitRst;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpVgroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0223Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpSecurityLevelCacheProxy tsmpSecurityLevelCacheProxy;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpDpItemsDao tsmpDpItemsDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public AA0223Service(TsmpSecurityLevelCacheProxy tsmpSecurityLevelCacheProxy, TsmpVgroupDao tsmpVgroupDao,
			TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao, TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao,
			TsmpDpItemsDao tsmpDpItemsDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpSecurityLevelCacheProxy = tsmpSecurityLevelCacheProxy;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpVgroupAuthoritiesMapDao = tsmpVgroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpDpItemsDao = tsmpDpItemsDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public AA0223Resp queryVGroupByPk(AA0223Req req, ReqHeader reqHeader) {
		AA0223Resp resp = new AA0223Resp();
		try {
			
			TsmpVgroup tv = checkParams(req);
			
			//parser
			setAA0223Resp(resp, tv , reqHeader.getLocale());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private TsmpVgroup checkParams(AA0223Req req) throws Exception {
		String vgroupId = req.getVgroupId();
		String vgroupName = req.getVgroupName();
		
		if(StringUtils.isEmpty(vgroupId) || StringUtils.isEmpty(vgroupName) ) {
			throw TsmpDpAaRtnCode._1296.throwing();		// 1296:缺少必填參數
		}
		
		TsmpVgroup tv = getTsmpVgroupDao().findFirstByVgroupIdAndVgroupName(vgroupId, vgroupName);
		if(tv == null) {
			throw TsmpDpAaRtnCode._1298.throwing();		// 1298:查無資料
		}
		return tv;
		
	}
	
	private void setAA0223Resp(AA0223Resp resp, TsmpVgroup tv, String locale) throws Exception {
		Integer time = tv.getAllowDays();
		if(time == null ) {
			time = 0;
		}
		ConvertTimeUnitRst reult = ServiceUtil.convertTimeUnit(time, locale);
		resp.setApproximateTimeUnit(reult.getApproximateTimeUnit());
		resp.setAllowDays(reult.getTime());
		resp.setTimeUnit(reult.getTimeUnit());
		resp.setTimeUnitName(reult.getTimeUnitName());
		
		resp.setVgroupId(tv.getVgroupId());
		resp.setVgroupName(tv.getVgroupName());
		resp.setVgroupAlias(tv.getVgroupAlias());
		resp.setAllowTimes(getAllowTimes(tv.getAllowTimes()));
		resp.setVgroupAuthorities(getGorupAuthorities(tv.getVgroupId()));
		resp.setSecurityLevelId(nvl(tv.getSecurityLevelId()));
		resp.setSecurityLevelName(getSecurityLevel(nvl(tv.getSecurityLevelId())));
		resp.setVgroupDesc(nvl(tv.getVgroupDesc()));
		resp.setCreateDate(parseDatetime(tv.getCreateTime()));
		resp.setCreateUser(nvl(tv.getCreateUser()));
		resp.setUpdateDate(parseDatetime(tv.getUpdateTime()));
		resp.setUpdateUser(nvl(tv.getUpdateUser()));
		
	}
	
	/**
	 * TSMP_VGROUP.allow_times
	 * 資料為null時, 後端預設帶0
	 * 
	 * @param allowTimes
	 * @return
	 * @throws Exception
	 */
	private Integer getAllowTimes(Integer allowTimes) throws Exception {
		if(StringUtils.isEmpty(allowTimes)) {
			allowTimes = 0;
			
		}
		return allowTimes;
		
	}
	
	/**
	 * 由SecurityLevelId 找出SecurityLevelName
	 * 				
	 * @param req
	 * @throws Exception
	 */
	private String getSecurityLevel(String securityLevelId) throws Exception {
		String lvName = "";
		if(!StringUtils.isEmpty(securityLevelId)) {
			TsmpSecurityLevel securityLV = getSecurityLVById(securityLevelId);
			if(securityLV != null) {
				lvName = nvl(securityLV.getSecurityLevelName());
			}
		}
		return lvName;
		
	}
	
	private String parseDatetime(Date date) {
		// createTime
		String createDateStr = "";
		Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(date, null);
		if(createDateOpt.isPresent()) {
			createDateStr = createDateOpt.get();
		}
		return createDateStr;
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
	 * 用 TSMP_VGROUP.vgroup_id 關聯
	 * TSMP_VGROUP_AUTHORITIES_MAP.vgroup_id
	 * 再用 TSMP_VGROUP_AUTHORITIES_MAP.vgroup_authoritie_id 關聯
	 * TSMP_GROUP_AUTHORITIES.group_authoritie_id
	 */
	private List<AA0223Auth> getGorupAuthorities(String vgroupId) {
		List<AA0223Auth> list = new ArrayList<>();
		List<TsmpVgroupAuthoritiesMap> vgroupAuthoritiesMaps =  getTsmpVgroupAuthoritiesMapDao().findByVgroupId(vgroupId);
		
		if(vgroupAuthoritiesMaps != null) {
			vgroupAuthoritiesMaps.forEach((m) ->{
				String authoritiesId = m.getVgroupAuthoritieId();
				AA0223Auth aa0223Auth = getAuthoritiesyId(authoritiesId);
				if(aa0223Auth != null) {
					list.add(aa0223Auth);
				}
					
			});
			
		}
		return list;
		
	}
	
	/**
	 * 由 authoritiesId ,在TSMP_GROUP_AUTHORITIES 取得 ,將資料放入AA0223Auth
	 * 
	 * @param authoritiesId
	 * @return
	 */
	private AA0223Auth getAuthoritiesyId(String authoritiesId) {
		AA0223Auth aa0223Auth = null;
			Optional<TsmpGroupAuthorities> opt = getTsmpGroupAuthoritiesDao().findById(authoritiesId);
			if(opt.isPresent()) {
				TsmpGroupAuthorities ga = opt.get();
				aa0223Auth = new AA0223Auth();
				aa0223Auth.setGroupAuthoritieId(ga.getGroupAuthoritieId());
				aa0223Auth.setGroupAuthoritieName(ga.getGroupAuthoritieName());
			}
		
		return aa0223Auth;
	}
	
	protected TsmpSecurityLevelCacheProxy getSecurityLevelCacheProxy() {
		return tsmpSecurityLevelCacheProxy;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}
	
	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}
	
	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
	

}
