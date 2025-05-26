package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0703Req;
import tpi.dgrv4.dpaa.vo.AA0703Resp;
import tpi.dgrv4.dpaa.vo.AA0703RoleInfo;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.ArrayList;
import java.util.List;

@Service
public class AA0703Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpAlertDao tsmpAlertDao;
	private TsmpRoleAlertDao tsmpRoleAlertDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public AA0703Service(TsmpAlertDao tsmpAlertDao, TsmpRoleAlertDao tsmpRoleAlertDao, TsmpRoleDao tsmpRoleDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpAlertDao = tsmpAlertDao;
		this.tsmpRoleAlertDao = tsmpRoleAlertDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public AA0703Resp queryAlertSettingDetail(TsmpAuthorization authorization, AA0703Req req, ReqHeader reqHeader) {
		AA0703Resp resp = new AA0703Resp();

		try {
			String alertId = req.getAlertId();
			String alertName = req.getAlertName();
			
			//檢查是否在TSMP_ALERT資料表有存在，若沒有查詢到資料，則throw 1429。
			TsmpAlert vo = getTsmpAlertDao().findFirstByAlertIdAndAlertName(Long.valueOf(alertId), alertName);
			if(vo == null) {
				//告警設定不存在
				throw TsmpDpAaRtnCode._1429.throwing();
			}
			
			//將查詢到的TSMP_ALERT資料放進AA0703Resp內
			resp.setAlertId(vo.getAlertId().toString());
			resp.setAlertDesc(vo.getAlertDesc());
			resp.setAlertEnabled(vo.getAlertEnabled() ? "1" : "0");
			resp.setAlertEnabledName(this.getSbNameByItemsSbNo("ENABLE_FLAG", vo.getAlertEnabled() ? "1" : "0", reqHeader.getLocale()));
			resp.setAlertInterval(vo.getAlertInterval());
			resp.setAlertMsg(vo.getAlertMsg());
			resp.setAlertName(vo.getAlertName());
			resp.setAlertSys(vo.getAlertSys());
			resp.setAlertType(vo.getAlertType());
			resp.setcFlag(vo.getcFlag().toString());
			resp.setDuration(vo.getDuration());
			resp.setExDays(vo.getExDays());
			resp.setExTime(vo.getExTime());
			resp.setExType(vo.getExType());
			resp.setImFlag(vo.getImFlag().toString());
			resp.setImId(vo.getImId());
			resp.setImType(vo.getImType());
			resp.setSearchMapString(vo.getEsSearchPayload());
			resp.setThreshold(vo.getThreshold());
			
			//2.查詢TSMP_ROLE為主要資料表，TSMP_ROLE_ALERT次要資料表。將查詢到的TSMP_ROLE資料放進AA0703Resp.roleInfoList
			List<TsmpRoleAlert> tsmpRoleAlertList = getTsmpRoleAlertDao().findByAlertId(Long.valueOf(alertId));
			List<AA0703RoleInfo> roleInfoList = new ArrayList<>();
			tsmpRoleAlertList.forEach(roleAlertVo -> {
				TsmpRole tsmpRoleVo = getTsmpRoleDao().findById(roleAlertVo.getRoleId()).orElse(null);
				if(tsmpRoleVo != null) {
					AA0703RoleInfo infoVo = new AA0703RoleInfo();
					infoVo.setRoleId(tsmpRoleVo.getRoleId());
					infoVo.setRoleName(tsmpRoleVo.getRoleName());
					roleInfoList.add(infoVo);
				}
			});
			resp.setRoleInfoList(roleInfoList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private String getSbNameByItemsSbNo(String itemNo, String subitemNo, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, locale);
		if (dpItem == null) {
		    return null;
		}
		subitemName = dpItem.getSubitemName();
		  
		return subitemName;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpAlertDao getTsmpAlertDao() {
		return tsmpAlertDao;
	}

	protected TsmpRoleAlertDao getTsmpRoleAlertDao() {
		return tsmpRoleAlertDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

}
