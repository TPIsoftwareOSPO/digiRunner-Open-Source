package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.TCP.Packet.RequireTPILogInfoPacket;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.controller.OnlineConsole2Controller;
import tpi.dgrv4.gateway.keeper.TPILogInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.CurrentLogReq;
import tpi.dgrv4.gateway.vo.OnlineConsole;

@Service
public class OnlineConsole2Service {
	
	private TsmpSettingDao tsmpSettingDao;
	private TsmpSettingCacheProxy tsmpSettingCacheProxy;

	@Autowired
	public OnlineConsole2Service(TsmpSettingDao tsmpSettingDao, TsmpSettingCacheProxy tsmpSettingCacheProxy) {
		super();
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
	}

	public List<OnlineConsole> outputlog(CurrentLogReq req) {

		List<OnlineConsole> data = new ArrayList<OnlineConsole>();

		Long currentTime = req.getCurrentTime();
		if (currentTime == -1) {
			currentTime = System.currentTimeMillis() - 60000;
		}
		RequireTPILogInfoPacket requireTPILogInfoPacket = new RequireTPILogInfoPacket();
		requireTPILogInfoPacket.currentTime = currentTime;
		
		if (TPILogger.lc == null) {
			TPILogger.tl.error("<Keeper Server> Lost Connection" );
			return data;
		}
		
		TPILogger.lc.send(requireTPILogInfoPacket);

		LinkedBlockingQueue<TPILogInfo> logPool = null;

		for (int i = 0; i < 20; i++) {
			logPool = (LinkedBlockingQueue<TPILogInfo>) TPILogger.lc.paramObj.remove("logPool");

			if (logPool != null) {
				break;
			}
			mysleep();
		}

		if(logPool!=null) {
			synchronized (OnlineConsole2Controller.lockObj) {
	
				Map<Long, ArrayList<TPILogInfo>> logs = new LinkedHashMap<Long, ArrayList<TPILogInfo>>();
	
				for (TPILogInfo logInfo : logPool) {
					long mstime = logInfo.getMstime();
					
					if (logs.containsKey(mstime)) {
						logs.get(mstime).add(logInfo);
					} else {
						ArrayList<TPILogInfo> logMsg = new ArrayList<TPILogInfo>();
						logMsg.add(logInfo);
						logs.put(mstime, logMsg);
					}
				}
	
				for (Long keyTime : logs.keySet()) {
					ArrayList<TPILogInfo> values = logs.get(keyTime);
	
					for (TPILogInfo v : values) {
						OnlineConsole o = new OnlineConsole();
						o.setTime(keyTime);
						o.setFormatDate(v.getFormatDate());
						o.setUserName(v.userName);
						o.setLogLevel(v.getLevel());
						o.setLogMsg(v.getLogMsg().toString());
						o.setLine(v.getLine());
						data.add(o);
					}
				}
	
				data.sort((data1, data2) -> data1.getTime().compareTo(data2.getTime()));
	
			}
		}

		return data;

	}

	private void mysleep() {
		try {
			Thread.sleep(100);// 因為要等待更新。
		} catch (InterruptedException e) {
			// Restore interrupted state...
		    Thread.currentThread().interrupt();
		}
	}
	
	public void changeLoggerLevel(String loggerLevel) {		
		Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.LOGGER_LEVEL);
		if (opt_tsmpSetting.isPresent()) {
			TsmpSetting tsmpSetting = opt_tsmpSetting.get();
			tsmpSetting.setValue(loggerLevel);
			getTsmpSettingDao().saveAndFlush(tsmpSetting);
		}
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}
	
	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return tsmpSettingCacheProxy;
	}
	
	public String currentLoggerLevel(){
		Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.LOGGER_LEVEL);
		if (opt_tsmpSetting.isPresent()) {
			TsmpSetting tsmpSetting = opt_tsmpSetting.get();
			return tsmpSetting.getValue();
		}
		return "";
	}
	
	
}
