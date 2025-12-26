package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpAlertVo;
@Service
public class TsmpAlertTableInitializer {

	private  List<TsmpAlertVo> tsmpAlertVoList = new LinkedList<TsmpAlertVo>();
	
	
	public List<TsmpAlertVo> insertTsmpAlert() {
		try {
			Long alertId;
			String alertName;
			String alertType;
			Boolean alertEnabled;
			Integer threshold;
			Integer duration;
			Integer alertInterval;
			Boolean cFlag;
			Boolean imFlag;
			String imType;
			String imId;
			String exType;
			String exDays;
			String exTime;
			String alertDesc;
			String alertSys;
			String alertMsg;

			
			createTsmpAlert((alertId = 1L), (alertName = "CPU High"), (alertType = "CPU"), (alertEnabled = false), (threshold = 90), (duration = 180), (alertInterval = 180), (cFlag = false), (imFlag = false), (imType = null), (imId = null), (exType = "N"), (exDays = null), (exTime = null), (alertDesc = "When TSMP CPU is high(> $THRESHOLD%) over $DURATION seconds, send alert every $INTERVAL seconds a time."), (alertSys = "TSMP"), (alertMsg = "When TSMP CPU is high(> $THRESHOLD%) over $DURATION seconds, send alert every $INTERVAL seconds a time."));
			createTsmpAlert((alertId = 2L), (alertName = "Heap High"), (alertType = "Heap"), (alertEnabled = false), (threshold = 90), (duration = 180), (alertInterval = 180), (cFlag = false), (imFlag = false), (imType = null), (imId = null), (exType = "N"), (exDays = null), (exTime = null), (alertDesc = "When TSMP Java heap size high(> $THRESHOLD%) over $DURATION seconds, send alert every $INTERVAL seconds a time."), (alertSys = "TSMP"), (alertMsg = "When TSMP Java heap size high(> $THRESHOLD%) over $DURATION seconds, send alert every $INTERVAL seconds a time."));
			createTsmpAlert((alertId = 3L), (alertName = "Disk High"), (alertType = "Disk"), (alertEnabled = false), (threshold = 90), (duration = 180), (alertInterval = 180), (cFlag = false), (imFlag = false), (imType = null), (imId = null), (exType = "N"), (exDays = null), (exTime = null), (alertDesc = "When TSMP Disk usage over  $THRESHOLD%, send alert every $INTERVAL seconds a time."), (alertSys = "TSMP"), (alertMsg = "When TSMP Disk usage over  $THRESHOLD%, send alert every $INTERVAL seconds a time."));
		
		
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return tsmpAlertVoList;
	}


	protected void createTsmpAlert(Long alertId, String alertName, String alertType, Boolean alertEnabled,
			Integer threshold, Integer duration, Integer alertInterval, Boolean cFlag, Boolean imFlag, String imType,
			String imId, String exType, String exDays, String exTime, String alertDesc, String alertSys,
			String alertMsg) {
		
		TsmpAlertVo tsmpAlertVo = new TsmpAlertVo();
		tsmpAlertVo.setAlertId(alertId);
		tsmpAlertVo.setAlertName(alertName);
		tsmpAlertVo.setAlertType(alertType);
		tsmpAlertVo.setAlertEnabled(alertEnabled);
		tsmpAlertVo.setThreshold(threshold);
		tsmpAlertVo.setDuration(duration);
		tsmpAlertVo.setAlertInterval(alertInterval);
		tsmpAlertVo.setcFlag(cFlag);
		tsmpAlertVo.setImFlag(imFlag);
		tsmpAlertVo.setImType(imType);
		tsmpAlertVo.setImId(imId);
		tsmpAlertVo.setExType(exType);
		tsmpAlertVo.setExDays(exDays);
		tsmpAlertVo.setExTime(exTime);
		tsmpAlertVo.setAlertDesc(alertDesc);
		tsmpAlertVo.setAlertSys(alertSys);
		tsmpAlertVo.setAlertMsg(alertMsg);		
		
		tsmpAlertVoList.add(tsmpAlertVo);

	}
    
}
