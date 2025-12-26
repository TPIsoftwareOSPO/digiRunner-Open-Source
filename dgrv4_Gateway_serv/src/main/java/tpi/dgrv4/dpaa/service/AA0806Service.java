package tpi.dgrv4.dpaa.service;


import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0806HostInfo;
import tpi.dgrv4.dpaa.vo.AA0806Req;
import tpi.dgrv4.dpaa.vo.AA0806Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0806Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRoleDao tsmpRoleDao;
	private AuthoritiesDao authoritiesDao;
	private TsmpRegHostDao tsmpRegHostDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;
	private BcryptParamHelper bcryptParamHelper;

	private Integer pageSize;
	
	@Autowired
	public AA0806Service(TsmpRoleDao tsmpRoleDao, AuthoritiesDao authoritiesDao, TsmpRegHostDao tsmpRegHostDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ServiceConfig serviceConfig,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRegHostDao = tsmpRegHostDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public AA0806Resp queryRegHostList_1(TsmpAuthorization authorization, AA0806Req req, ReqHeader reqHeader) {
		AA0806Resp resp = new AA0806Resp();
		try {

			String[] keyword = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			String status = getDecodeHostStatus(nvl(req.getHostStatus()), reqHeader.getLocale());					//主機狀態
			String enableHeartbeat = getDecodeEnableFlag(nvl(req.getEnableHeartbeat()), reqHeader.getLocale());	//啟用心跳
			String id = nvl(req.getLastReghostId());
			boolean isNeedCheck = isNeedCheck(authorization);
			String isParing = nvl(req.getPaging());
			List<TsmpRegHost> tsmpRegHostList = null;
			String userName = nvl(authorization.getUserName());

			// 進行 分頁 + 關鍵字 查詢
			if("true".equalsIgnoreCase(isParing)) {
				tsmpRegHostList = getTsmpRegHostDao().query_aa0806Service(id, enableHeartbeat, status, isNeedCheck, keyword, userName, getPageSize());
				
			}else if ("false".equalsIgnoreCase(isParing)){
				tsmpRegHostList = getTsmpRegHostDao().query_aa0806Service(id, enableHeartbeat, status, isNeedCheck, keyword, userName, Integer.MAX_VALUE);
				
			}
				
			if (tsmpRegHostList == null || tsmpRegHostList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			// 組資料
			List<AA0806HostInfo> aa0806HostInfoList = setAA0806Data(tsmpRegHostList, reqHeader.getLocale());
			resp.setHostInfoList(aa0806HostInfoList);
			
		} catch (TsmpDpAaException aa0806_e) {
			throw aa0806_e;
		} catch (Exception aa0806_e) {
			this.logger.error(StackTraceUtil.logStackTrace(aa0806_e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	/**
	 * TSMP_ROLE.ROLE_ID = AUTHORITIES.AUTHORITY AND
	 * TSMP_ROLE.ROLE_NAME IN ( 'ADMIN' OR 'API_ADMIN' ) AND
	 * AUTHORITIES.USERNAME = TsmpAuthorization.userName
	 * 
	 * @param authorization
	 * @return
	 */
	private boolean isNeedCheck(TsmpAuthorization authorization) {
		boolean aa0806_isNeedCheck = true;
		List<Authorities> aa0806_authoritiesList = getAuthoritiesDao().findByUsername(nvl(authorization.getUserName()));
			for (Authorities aa0806_authorities : aa0806_authoritiesList) {
				Optional<TsmpRole> aa0806_optRole = getTsmpRoleDao().findById(nvl(aa0806_authorities.getAuthority()));
				if(aa0806_optRole.isPresent()) {
					String aa0806_roleName = nvl(aa0806_optRole.get().getRoleName());
					if("ADMIN".equals(aa0806_roleName) || "API_ADMIN".equals(aa0806_roleName)) {
						return false ;
					}
			}
			
		}
		return aa0806_isNeedCheck;
	}
	
	private List<AA0806HostInfo> setAA0806Data(List<TsmpRegHost> tsmpRegHostList, String locale){
		List<AA0806HostInfo> aa0806HostInfoList = new ArrayList<>();
		
		tsmpRegHostList.forEach((regHost)->{
			AA0806HostInfo hostInfo = new AA0806HostInfo();
			hostInfo.setClientID(nvl(regHost.getClientid()));
			
			String enableName = "";
			String enable = nvl(regHost.getEnabled());
			if(!StringUtils.isEmpty(enable)) {
				enableName = nvl(getSbNameByItemsParam2(enable, locale));
			}
			hostInfo.setEnabled(enable);				//enableHeartbeat
			hostInfo.setEnabledName(enableName);		//enableHeartbeat
			
			hostInfo.setMemo(nvl(regHost.getMemo()));
			hostInfo.setRegHost(nvl(regHost.getReghost()));
			hostInfo.setRegHostID(nvl(regHost.getReghostId()));
			
			String regHostStatusName = "";
			String regHostStatus = nvl(regHost.getReghostStatus());
			if(!StringUtils.isEmpty(regHostStatus)) {
				regHostStatusName = nvl(getSbNameByItemsSbNo("HOST_STATUS", regHostStatus, locale));
			}
			hostInfo.setRegHostStatus(regHostStatus);
			hostInfo.setRegHostStatusName(regHostStatusName);
			
			//"TSMP_REG_HOST.HEARTBEAT 格式 2019-07-02 14:59:27"
			String heartbeatTime = "";
			
			Date heartbeatDate = regHost.getHeartbeat();
			Optional<String> heartbeatDateOpt = DateTimeUtil.dateTimeToString(heartbeatDate, null);
			
			if(heartbeatDateOpt.isPresent())
				heartbeatTime = heartbeatDateOpt.get();
			hostInfo.setHeartbeatTime(heartbeatTime);
			
			//1.綠燈：TSMP_REG_HOST.HEARTBEAT與server主機時間相差1分鐘之內，指定green
			//2.紅燈：TSMP_REG_HOST.HEARTBEAT與server主機時間相差1分鐘超過或TSMP_REG_HOST.HEARTBEAT為null，指定red
			if (regHost.getHeartbeat()!=null) {
				GregorianCalendar gcHeartbeat = new GregorianCalendar();
				gcHeartbeat.setTime(regHost.getHeartbeat());
				gcHeartbeat.add(GregorianCalendar.MINUTE, 1);			
				if (gcHeartbeat.getTime().after(getCurrentHostDate())) {
					hostInfo.setBulb("green");
				}else {
					hostInfo.setBulb("red");
				}
			}else {
				hostInfo.setBulb("red");
			}
				
			
			aa0806HostInfoList.add(hostInfo);
		});
		
		return aa0806HostInfoList;
	}	
	
	/**
	 * 使用BcryptParam, 
	 * ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:
	 * DB值 (PARAM2) = 中文說明; 
	 * 1=Y, 2=N
	 * @param encodeStatus
	 * @return
	 */
	protected String getDecodeEnableFlag(String encodeStatus, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(encodeStatus, "ENABLE_FLAG", BcryptFieldValueEnum.PARAM2, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}
	
	private String getSbNameByItemsParam2(String encodeStatus, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndParam2AndLocale("ENABLE_FLAG", encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		subitemName = dpItem.getSubitemName();
		
		return subitemName;
	}

	/**
	 * 使用BcryptParam, 
	 * ITEM_NO='HOST_STATUS' , DB儲存值對應代碼如下:
	 * DB值 (SUBITEM_NO) = 中文說明; 
	 * A=啟動, S=停止
	 * @param status
	 * @return
	 */
	private String getDecodeHostStatus(String status, String locale) {
		String decodeHostStatus = null;
		try {
			decodeHostStatus = getBcryptParamHelper().decode(status, "HOST_STATUS", locale);
		} catch (BcryptParamDecodeException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decodeHostStatus;
	}

	private String getSbNameByItemsSbNo(String item, String encodeStatus, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(item, encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		subitemName = dpItem.getSubitemName();
		
		return subitemName;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}
	
	protected TsmpRegHostDao getTsmpRegHostDao() {
		return tsmpRegHostDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0806");
		return this.pageSize;
	}
	
	protected Date getCurrentHostDate() {
		return DateTimeUtil.now();
	}
}
