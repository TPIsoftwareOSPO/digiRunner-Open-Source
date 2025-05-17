package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherIfs;
import tpi.dgrv4.dpaa.vo.AA0704Req;
import tpi.dgrv4.dpaa.vo.AA0704Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class AA0704Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpAlertDao tsmpAlertDao;
	private TsmpRoleAlertDao tsmpRoleAlertDao;
	private TsmpRoleDao tsmpRoleDao;
	private BcryptParamHelper bcryptParamHelper;
    private DpaaAlertDispatcherIfs dpaaAlertDispatcher;
	
    @Autowired
	public AA0704Service(TsmpAlertDao tsmpAlertDao, TsmpRoleAlertDao tsmpRoleAlertDao, TsmpRoleDao tsmpRoleDao,
			BcryptParamHelper bcryptParamHelper, DpaaAlertDispatcherIfs dpaaAlertDispatcher) {
		super();
		this.tsmpAlertDao = tsmpAlertDao;
		this.tsmpRoleAlertDao = tsmpRoleAlertDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dpaaAlertDispatcher = dpaaAlertDispatcher;
	}

	@Transactional
	public AA0704Resp updateAlertSetting(TsmpAuthorization authorization, AA0704Req req, ReqHeader reqHeader) {
		AA0704Resp resp = new AA0704Resp();

		try {
			checkParam(req);
			
			//更新TsmpAlert
			TsmpAlert tsmpAlert = updateTsmpAlert(req, authorization, reqHeader.getLocale());
			//刪除與新增TsmpRoleAlert
			updateTsmpRoleAlert(req);
			
			// 有更新才需刷新
            if (tsmpAlert != null) {
                getDpaaAlertDispatcher().updateAlert(tsmpAlert);
            }

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1286:更新失敗
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return resp;
	}
	
	private void updateTsmpRoleAlert(AA0704Req req) {
		Long alertId = Long.valueOf(req.getAlertId());
		List<String> roleIDList = req.getRoleIDList();
		
		//刪除
		getTsmpRoleAlertDao().deleteByAlertId(alertId);
		
		//新增
		roleIDList.forEach(roleId -> {
			TsmpRoleAlert tsmpRoleAlertVo = new TsmpRoleAlert();
			tsmpRoleAlertVo.setAlertId(alertId);
			tsmpRoleAlertVo.setRoleId(roleId);
			getTsmpRoleAlertDao().save(tsmpRoleAlertVo);
		});
	}
	
	private TsmpAlert updateTsmpAlert(AA0704Req req, TsmpAuthorization authorization, String locale) {
		Long alertId = Long.valueOf(req.getAlertId());
		String alertDesc = req.getAlertDesc();
		String decodeAlertEnabled = getValueByBcryptParamHelper(req.getAlertEnabled(), "ENABLE_FLAG", locale);
		Boolean alertEnabled = "0".equals(decodeAlertEnabled) ? false : true;
		Integer alertInterval = req.getAlertInterval();
		String alertMsg = req.getAlertMsg();
		String alertSys = req.getAlertSys();
		String alertType = req.getAlertType();
		Boolean cFlag = "true".equalsIgnoreCase(req.getcFlag())? true : false;
		Integer duration = req.getDuration();
		String exDays = req.getExDays();
		String exTime = req.getExTime();
		String exType = req.getExType();
		Boolean imFlag = "true".equalsIgnoreCase(req.getImFlag())? true : false;
		String imId = req.getImId();
		String imType = req.getImType();
		
		String searchMapString = req.getSearchMapString();
		Integer threshold = req.getThreshold();
		
		TsmpAlert tsmpAlertVo = getTsmpAlertDao().findById(alertId).orElse(null);
		assert tsmpAlertVo != null;
		String strOriTsmpAlert = tsmpAlertVo.toString();
		
		tsmpAlertVo.setAlertDesc(alertDesc);
		tsmpAlertVo.setAlertEnabled(alertEnabled);
		tsmpAlertVo.setAlertInterval(alertInterval);
		tsmpAlertVo.setAlertMsg(alertMsg);
		tsmpAlertVo.setAlertSys(alertSys);
		tsmpAlertVo.setAlertType(alertType);
		tsmpAlertVo.setcFlag(cFlag);
		tsmpAlertVo.setDuration(duration);
		tsmpAlertVo.setEsSearchPayload(searchMapString);
		tsmpAlertVo.setExDays(exDays);
		tsmpAlertVo.setExTime(exTime);
		tsmpAlertVo.setExType(exType);
		tsmpAlertVo.setImFlag(imFlag);
		tsmpAlertVo.setImId(imId);
		tsmpAlertVo.setImType(imType);
		tsmpAlertVo.setThreshold(threshold);
		
		String strNewTsmpAlert = tsmpAlertVo.toString();

		//前後資料不一樣才更新
		if(!strOriTsmpAlert.equals(strNewTsmpAlert)) {
			tsmpAlertVo.setUpdateTime(DateTimeUtil.now());
			tsmpAlertVo.setUpdateUser(authorization.getUserName());
			tsmpAlertVo = getTsmpAlertDao().save(tsmpAlertVo);
			return tsmpAlertVo;
		}
		
		return null;
	}
	
	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.SUBITEM_NO, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}

	private void checkParam(AA0704Req req) throws IOException {
		Long alertId = Long.valueOf(req.getAlertId());
		List<String> roleIDList = req.getRoleIDList();
		String exType = req.getExType();
		String exDays = req.getExDays();
		String aa0704w_exDays = req.getExDays();//解決sonarQube的Duplicated
		String aa0704m_exDays = req.getExDays();//解決sonarQube的Duplicated
		String exTime = req.getExTime();
		String aa0704w_exTime = req.getExTime();//解決sonarQube的Duplicated
		String aa0704m_exTime = req.getExTime();//解決sonarQube的Duplicated
		String alertType = req.getAlertType();
		String searchMapString = req.getSearchMapString();
		
		if("N".equals(exType)) {
			//a. AA0704Req.exType=N，若AA0701Req.exDays有值，則throw RTN Code 1447。
			if(!StringUtils.isEmpty(exDays)) {
				//例外日期不可填寫
				throw TsmpDpAaRtnCode._1447.throwing();
			}
			
		}else if("D".equals(exType)) {
			//b1若AA0704Req.exDays有值，則throw RTN Code 1447。
			if(!StringUtils.isEmpty(exDays)) {
				//例外日期不可填寫
				throw TsmpDpAaRtnCode._1447.throwing();
			}
			//b2.檢查AA0704Req.exTime必填，若沒有值則throw RTN Code 1449
			if(StringUtils.isEmpty(exTime)) {
				//開始時間與結束時間為必填
				throw TsmpDpAaRtnCode._1449.throwing();
			}
			
			//b3.檢查AA0704Req.exTime是否介於0000-2359之間，若沒有則throw RTN Code 1448。
			String[] arrExTime = exTime.split("-");
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HHmm");
			try {
				for(String strExTime : arrExTime) {
					LocalTime.parse(strExTime, dtf);
				}
			}catch(Exception e) {
				//開始時間與結束時間格式不正確
				throw TsmpDpAaRtnCode._1448.throwing();
			}
		}else if("W".equals(exType)) {
			// c1.檢查AA0704Req.exDays必填，若沒有值則throw RTN Code 1450
			if(StringUtils.isEmpty(aa0704w_exDays)) {
				//例外日期為必填
				throw TsmpDpAaRtnCode._1450.throwing();
			}
			// c2.檢查AA0704Req.exTime必填，若沒有值則throw RTN Code 1449
			if(StringUtils.isEmpty(aa0704w_exTime)) {
				//開始時間與結束時間為必填
				throw TsmpDpAaRtnCode._1449.throwing();
			}
			
			//c3.檢查AA0704Req.exDays格式為pattern("[數字|數字_數字]*")，需要再檢查數字在1~7之間，若沒有則throw RTN Code 1451。
			if("_".equals(aa0704w_exDays.substring(aa0704w_exDays.length()-1))) {
				//例外日期格式不正確
				throw TsmpDpAaRtnCode._1451.throwing();
			}
			String[] arrExDays = aa0704w_exDays.split("_");
			for(String strExDays : arrExDays) {
				try {
					int intExDays = Integer.parseInt(strExDays);
					if(!(intExDays > 0 && intExDays < 8)) {
						//例外日期格式不正確
						throw TsmpDpAaRtnCode._1451.throwing();
					}
				}catch(Exception e) {
					//例外日期格式不正確
					throw TsmpDpAaRtnCode._1451.throwing();
				}
			}
			
			//c4.檢查AA0704Req.exTime是否介於0000-2359之間，若沒有則throw RTN Code 1448。
			String[] arrExTime = aa0704w_exTime.split("-");
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HHmm");
			try {
				for(String strExTime : arrExTime) {
					LocalTime.parse(strExTime, dtf);
				}
			}catch(Exception e) {
				//開始時間與結束時間格式不正確
				throw TsmpDpAaRtnCode._1448.throwing();
			}
		}else if("M".equals(exType)) {
			// c1.檢查AA0704Req.exDays必填，若沒有值則throw RTN Code 1450
			if(StringUtils.isEmpty(aa0704m_exDays)) {
				//例外日期為必填
				throw TsmpDpAaRtnCode._1450.throwing();
			}
			// c2.檢查AA0704Req.exTime必填，若沒有值則throw RTN Code 1449
			if(StringUtils.isEmpty(aa0704m_exTime)) {
				//開始時間與結束時間為必填
				throw TsmpDpAaRtnCode._1449.throwing();
			}
			
			//c3.檢查AA0704Req.exDays格式為pattern("[數字|數字_數字]*")，需要再檢查數字在1~31之間，若沒有則throw RTN Code 1451。
			if("_".equals(aa0704m_exDays.substring(aa0704m_exDays.length()-1))) {
				//例外日期格式不正確
				throw TsmpDpAaRtnCode._1451.throwing();
			}
			String[] arrExDays = aa0704m_exDays.split("_");
			for(String strExDays : arrExDays) {
				try {
					int intExDays = Integer.parseInt(strExDays);
					if(!(intExDays > 0 && intExDays < 32)) {
						//例外日期格式不正確
						throw TsmpDpAaRtnCode._1451.throwing();
					}
				}catch(Exception e) {
					//例外日期格式不正確
					throw TsmpDpAaRtnCode._1451.throwing();
				}
			}
			
			//c4.檢查AA0704Req.exTime是否介於0000-2359之間，若沒有則throw RTN Code 1448。
			String[] arrExTime = aa0704m_exTime.split("-");
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HHmm");
			try {
				for(String strExTime : arrExTime) {
					LocalTime.parse(strExTime, dtf);
				}
			}catch(Exception e) {
				//開始時間與結束時間格式不正確
				throw TsmpDpAaRtnCode._1448.throwing();
			}
		}
		
		//4.1若AA0701Req.alertType為keyword，AA0701Req.searchMapString的JSON兩個欄位中，若有一個欄位有填寫，則另一個欄位也要填寫，throw RTN Code 1468。
		if("keyword".equals(alertType)) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String,String> map = mapper.readValue(searchMapString, new TypeReference<Map<String, String>>() {});
			map.forEach((k, v)->{
				if((StringUtils.isEmpty(k) && !StringUtils.isEmpty(v)) || (!StringUtils.isEmpty(k) && StringUtils.isEmpty(v))) {
					//Log欄位與回應值為必填欄位
					throw TsmpDpAaRtnCode._1468.throwing();
				}
			});
		}else if("responseTime".equals(alertType)) {
			//4.2若AA0701Req.alertType為responseTime，AA0701Req.searchMapString的JSON兩個欄位中的txid與moduleName，若有一個欄位有填寫，則另一個欄位也要填寫，throw RTN Code 1469。
			ObjectMapper mapper = new ObjectMapper();
		    JsonNode root = mapper.readTree(searchMapString);
			JsonNode dpNode = root.findValue("txid");
			String txid = dpNode.asText();
			dpNode = root.findValue("moduleName");
			String moduleName = dpNode.asText();
			if((StringUtils.isEmpty(txid) && !StringUtils.isEmpty(moduleName)) 
					|| (!StringUtils.isEmpty(txid) && StringUtils.isEmpty(moduleName))) {
				//模組名稱與API ID為必填欄位
				throw TsmpDpAaRtnCode._1469.throwing();
			}
		}
		
		//1.檢查AA0701Req.alertName是否在TSMP_ALERT資料表有存在，若有查詢到資料，則throw 1428。
		TsmpAlert tsmpAlertVo = getTsmpAlertDao().findById(alertId).orElse(null);
		if(tsmpAlertVo == null) {
			//1429:警告設定不存在
			throw TsmpDpAaRtnCode._1429.throwing();
		}
		
		//2.檢查AA0701Req.roleIDList的roldId是否有存在TSMP_ROLE資料表，若沒有查到資料，則throw 1230
		roleIDList.forEach(roleId ->{
			TsmpRole tsmpRoleVo = getTsmpRoleDao().findById(roleId).orElse(null);
			if(tsmpRoleVo == null) {
				//1230:角色不存在
				throw TsmpDpAaRtnCode._1230.throwing();
			}
			
		});
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpRoleAlertDao getTsmpRoleAlertDao() {
		return tsmpRoleAlertDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}
	
	protected TsmpAlertDao getTsmpAlertDao() {
		return tsmpAlertDao;
	}

	protected DpaaAlertDispatcherIfs getDpaaAlertDispatcher() {
		return dpaaAlertDispatcher;
	}

}
