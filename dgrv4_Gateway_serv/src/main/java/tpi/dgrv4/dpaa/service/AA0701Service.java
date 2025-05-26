package tpi.dgrv4.dpaa.service;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherIfs;
import tpi.dgrv4.dpaa.vo.AA0701Req;
import tpi.dgrv4.dpaa.vo.AA0701Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class AA0701Service {

	private TPILogger logger = TPILogger.tl;

	private final TsmpAlertDao tsmpAlertDao;
	private final TsmpRoleAlertDao tsmpRoleAlertDao;
	private final TsmpRoleDao tsmpRoleDao;
	private final SeqStoreService seqStoreService;
	private final BcryptParamHelper bcryptParamHelper;
    private final DpaaAlertDispatcherIfs dpaaAlertDispatcher;

	private void checkParam(AA0701Req req) throws JsonParseException, JsonMappingException, IOException {
		String aa0701_alertName = req.getAlertName();
		List<String> aa0701_roleIDList = req.getRoleIDList();
		String aa0701_exType = req.getExType();
		String aa0701_exDays = req.getExDays();
		String aa0701_exTime = req.getExTime();
		String aa0701w_exTime = req.getExTime();//解決sonarQube的Duplicated
		String aa0701m_exTime = req.getExTime();//解決sonarQube的Duplicated
		String aa0701_alertType = req.getAlertType();
		String aa0701_searchMapString = req.getSearchMapString();
		
		if("N".equals(aa0701_exType)) {
			//a. AA0701Req.exType=N，若AA0701Req.exDays有值，則throw RTN Code 1447。
			if(!StringUtils.isEmpty(aa0701_exDays)) {
				//例外日期不可填寫
				throw TsmpDpAaRtnCode._1447.throwing();
			}
			
		}else if("D".equals(aa0701_exType)) {
			//b1若AA0701Req.exDays有值，則throw RTN Code 1447。
			if(!StringUtils.isEmpty(aa0701_exDays)) {
				//例外日期不可填寫
				throw TsmpDpAaRtnCode._1447.throwing();
			}
			//b2.檢查AA0701Req.exTime必填，若沒有值則throw RTN Code 1449
			if(StringUtils.isEmpty(aa0701_exTime)) {
				//開始時間與結束時間為必填
				throw TsmpDpAaRtnCode._1449.throwing();
			}
			
			//b3.檢查AA0701Req.exTime是否介於0000-2359之間，若沒有則throw RTN Code 1448。
			String[] arrExTime = aa0701_exTime.split("-");
			DateTimeFormatter aa0701_dtf = DateTimeFormatter.ofPattern("HHmm");
			try {
				for(String strExTime : arrExTime) {
					LocalTime.parse(strExTime, aa0701_dtf);
				}
			}catch(Exception e) {
				//開始時間與結束時間格式不正確
				throw TsmpDpAaRtnCode._1448.throwing();
			}
		}else if("W".equals(aa0701_exType)) {
			// c1.檢查AA0701Req.exDays必填，若沒有值則throw RTN Code 1450
			if(StringUtils.isEmpty(aa0701_exDays)) {
				//例外日期為必填
				throw TsmpDpAaRtnCode._1450.throwing();
			}
			// c2.檢查AA0701Req.exTime必填，若沒有值則throw RTN Code 1449
			if(StringUtils.isEmpty(aa0701w_exTime)) {
				//開始時間與結束時間為必填
				throw TsmpDpAaRtnCode._1449.throwing();
			}
			
			//c3.檢查AA0701Req.exDays格式為pattern("[數字|數字_數字]*")，需要再檢查數字在1~7之間，若沒有則throw RTN Code 1451。
			if("_".equals(aa0701_exDays.substring(aa0701_exDays.length()-1))) {
				//例外日期格式不正確
				throw TsmpDpAaRtnCode._1451.throwing();
			}
			String[] arrExDays = aa0701_exDays.split("_");
			for(String strExDays : arrExDays) {
				try {
					int aa0701_intExDays = Integer.parseInt(strExDays);
					if(!(aa0701_intExDays > 0 && aa0701_intExDays < 8)) {
						//例外日期格式不正確
						throw TsmpDpAaRtnCode._1451.throwing();
					}
				}catch(Exception e) {
					//例外日期格式不正確
					throw TsmpDpAaRtnCode._1451.throwing();
				}
			}
			
			//c4.檢查AA0701Req.exTime是否介於0000-2359之間，若沒有則throw RTN Code 1448。
			String[] arrExTime = aa0701w_exTime.split("-");
			DateTimeFormatter aa0701_dtf = DateTimeFormatter.ofPattern("HHmm");
			try {
				for(String aa0701_strExTime : arrExTime) {
					LocalTime.parse(aa0701_strExTime, aa0701_dtf);
				}
			}catch(Exception e) {
				//開始時間與結束時間格式不正確
				throw TsmpDpAaRtnCode._1448.throwing();
			}
		}else if("M".equals(aa0701_exType)) {
			// c1.檢查AA0701Req.exDays必填，若沒有值則throw RTN Code 1450
			if(StringUtils.isEmpty(aa0701_exDays)) {
				//例外日期為必填
				throw TsmpDpAaRtnCode._1450.throwing();
			}
			// c2.檢查AA0701Req.exTime必填，若沒有值則throw RTN Code 1449
			if(StringUtils.isEmpty(aa0701m_exTime)) {
				//開始時間與結束時間為必填
				throw TsmpDpAaRtnCode._1449.throwing();
			}
			
			//c3.檢查AA0701Req.exDays格式為pattern("[數字|數字_數字]*")，需要再檢查數字在1~31之間，若沒有則throw RTN Code 1451。
			if("_".equals(aa0701_exDays.substring(aa0701_exDays.length()-1))) {
				//例外日期格式不正確
				throw TsmpDpAaRtnCode._1451.throwing();
			}
			String[] arrExDays = aa0701_exDays.split("_");
			for(String aa0701_strExDays : arrExDays) {
				try {
					int aa0701_intExDays = Integer.parseInt(aa0701_strExDays);
					if(!(aa0701_intExDays > 0 && aa0701_intExDays < 32)) {
						//例外日期格式不正確
						throw TsmpDpAaRtnCode._1451.throwing();
					}
				}catch(Exception e) {
					//例外日期格式不正確
					throw TsmpDpAaRtnCode._1451.throwing();
				}
			}
			
			//c4.檢查AA0701Req.exTime是否介於0000-2359之間，若沒有則throw RTN Code 1448。
			String[] arrExTime = aa0701m_exTime.split("-");
			DateTimeFormatter aa0701_dtf = DateTimeFormatter.ofPattern("HHmm");
			try {
				for(String strExTime : arrExTime) {
					LocalTime.parse(strExTime, aa0701_dtf);
				}
			}catch(Exception e) {
				//開始時間與結束時間格式不正確
				throw TsmpDpAaRtnCode._1448.throwing();
			}
		}
		
		//4.1若AA0701Req.alertType為keyword，AA0701Req.searchMapString的JSON兩個欄位中，若有一個欄位有填寫，則另一個欄位也要填寫，throw RTN Code 1468。
		if("keyword".equals(aa0701_alertType)) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String,String> aa0701_map = mapper.readValue(aa0701_searchMapString, new TypeReference<Map<String, String>>() {});
			aa0701_map.forEach((k, v)->{
				if((StringUtils.isEmpty(k) && !StringUtils.isEmpty(v)) || (!StringUtils.isEmpty(k) && StringUtils.isEmpty(v))) {
					//Log欄位與回應值為必填欄位
					throw TsmpDpAaRtnCode._1468.throwing();
				}
			});
		}else if("responseTime".equals(aa0701_alertType)) {
			//4.2若AA0701Req.alertType為responseTime，AA0701Req.searchMapString的JSON兩個欄位中的txid與moduleName，若有一個欄位有填寫，則另一個欄位也要填寫，throw RTN Code 1469。
			ObjectMapper mapper = new ObjectMapper();
			JsonNode aa0701_root = mapper.readTree(aa0701_searchMapString);
			JsonNode dpNode = aa0701_root.findValue("txid");
			String aa0701_txid = dpNode.asText();
			dpNode = aa0701_root.findValue("moduleName");
			String moduleName = dpNode.asText();
			if((StringUtils.isEmpty(aa0701_txid) && !StringUtils.isEmpty(moduleName)) 
					|| (!StringUtils.isEmpty(aa0701_txid) && StringUtils.isEmpty(moduleName))) {
				//模組名稱與API ID為必填欄位
				throw TsmpDpAaRtnCode._1469.throwing();
			}
		}
		
		//1.檢查AA0701Req.alertName是否在TSMP_ALERT資料表有存在，若有查詢到資料，則throw 1428。
		TsmpAlert tsmpAlertVo = getTsmpAlertDao().findFirstByAlertName(aa0701_alertName);
		if(tsmpAlertVo != null) {
			//1428:告警名稱已存在
			throw TsmpDpAaRtnCode._1428.throwing();
		}
		
		//2.檢查AA0701Req.roleIDList的roldId是否有存在TSMP_ROLE資料表，若沒有查到資料，則throw 1230
		aa0701_roleIDList.forEach(roleId ->{
			TsmpRole tsmpRoleVo = getTsmpRoleDao().findById(roleId).orElse(null);
			if(tsmpRoleVo == null) {
				//1230:角色不存在
				throw TsmpDpAaRtnCode._1230.throwing();
			}
			
		});
		
	}
	
	@Transactional
	public AA0701Resp addAlarmSettings(TsmpAuthorization authorization, AA0701Req req, ReqHeader reqHeader) {
		AA0701Resp resp = new AA0701Resp();

		try {
			checkParam(req);
			
			String alertDesc = req.getAlertDesc();
			String decodeAlertEnabled = getValueByBcryptParamHelper(req.getAlertEnabled(), "ENABLE_FLAG", reqHeader.getLocale());
			Boolean alertEnabled = "0".equals(decodeAlertEnabled) ? false : true;
			Integer alertInterval = req.getAlertInterval();
			String alertMsg = req.getAlertMsg();
			String alertName = req.getAlertName();
			String aa0701_alertSys = req.getAlertSys();
			String aa0701_alertType = req.getAlertType();
			Boolean aa0701_cFlag = "true".equalsIgnoreCase(req.getcFlag())? true : false;
			Integer aa0701_duration = req.getDuration();
			String aa0701_exDays = req.getExDays();
			String aa0701_exTime = req.getExTime();
			String aa0701_exType = req.getExType();
			Boolean aa0701_imFlag = "true".equalsIgnoreCase(req.getImFlag())? true : false;
			String aa0701_imId = req.getImId();
			String aa0701_imType = req.getImType();
			List<String> roleIDList = req.getRoleIDList();
			String searchMapString = req.getSearchMapString();
			Integer threshold = req.getThreshold();
			Long alertId = this.getId(TsmpSequenceName.SEQ_TSMP_ALERT_PK);
			
			//TSMP_ALERT資料新增
			TsmpAlert tsmpAlertVo = new TsmpAlert();
			tsmpAlertVo.setAlertId(alertId);
			tsmpAlertVo.setAlertDesc(alertDesc);
			tsmpAlertVo.setAlertEnabled(alertEnabled);
			tsmpAlertVo.setAlertInterval(alertInterval);
			tsmpAlertVo.setAlertMsg(alertMsg);
			tsmpAlertVo.setAlertName(alertName);
			tsmpAlertVo.setAlertSys(aa0701_alertSys);
			tsmpAlertVo.setAlertType(aa0701_alertType);
			tsmpAlertVo.setcFlag(aa0701_cFlag);
			tsmpAlertVo.setCreateTime(DateTimeUtil.now());
			tsmpAlertVo.setCreateUser(authorization.getUserName());
			tsmpAlertVo.setDuration(aa0701_duration);
			tsmpAlertVo.setEsSearchPayload(searchMapString);
			tsmpAlertVo.setExDays(aa0701_exDays);
			tsmpAlertVo.setExTime(aa0701_exTime);
			tsmpAlertVo.setExType(aa0701_exType);
			tsmpAlertVo.setImFlag(aa0701_imFlag);
			tsmpAlertVo.setImId(aa0701_imId);
			tsmpAlertVo.setImType(aa0701_imType);
			tsmpAlertVo.setThreshold(threshold);
			
			tsmpAlertVo = getTsmpAlertDao().save(tsmpAlertVo);
			
			//TSMP_ROLE_ALERT資料新增
			roleIDList.forEach(roleId -> {
				TsmpRoleAlert tsmpRoleAlertVo = new TsmpRoleAlert();
				tsmpRoleAlertVo.setAlertId(alertId);
				tsmpRoleAlertVo.setRoleId(roleId);
				getTsmpRoleAlertDao().save(tsmpRoleAlertVo);
			});
			
			resp.setAlertID(alertId.toString());

			getDpaaAlertDispatcher().joinAlert(tsmpAlertVo);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1288:新增失敗
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
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

	private Long getId(TsmpSequenceName seqName) throws Exception {

		Long id = getSeqStoreService().nextTsmpSequence(seqName);

		if (id == null) {
			logger.debug("Get " + seqName + " error");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return id;
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

	protected SeqStoreService getSeqStoreService() {
		return seqStoreService;
	}

	protected DpaaAlertDispatcherIfs getDpaaAlertDispatcher() {
		return dpaaAlertDispatcher;
	}

}
