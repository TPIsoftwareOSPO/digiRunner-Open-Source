package tpi.dgrv4.dpaa.service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LoggerLevelConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.controller.AA0325Controller;
import tpi.dgrv4.dpaa.vo.AA0325Log;
import tpi.dgrv4.dpaa.vo.AA0325Req;
import tpi.dgrv4.dpaa.vo.AA0325Resp;
import tpi.dgrv4.dpaa.vo.AA0325SysLog;
import tpi.dgrv4.gateway.TCP.Packet.ComposerInfoPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.ComposerInfoData;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class AA0325Service {

	private ExecutorService executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("AA0325Service"));;

	private ObjectMapper objectMapper;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public AA0325Service(ObjectMapper objectMapper, TsmpSettingService tsmpSettingService) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
	}

	public AA0325Resp compSync(AA0325Req req, HttpServletRequest httpReq) throws JsonProcessingException {

		AA0325Resp resp = new AA0325Resp();
		resp.setTs(AA0325Controller.ts);
		
		//暫存liveComposer,防止keeper server有問題或沒開,造成驗capiKey時失敗
		CApiKeyService.tempComposerLiveMap.put(req.getComposerID(), System.currentTimeMillis());

		ComposerInfoData composerInfoData = new ComposerInfoData();
		composerInfoData.setRemoteIP(httpReq.getRemoteAddr());
		composerInfoData.setRemotePort(httpReq.getRemotePort());
		composerInfoData.setComposerID(req.getComposerID());
		composerInfoData.setStartupTime(TimeZoneUtil.long2UTCstring(req.getStartupTime()));
		composerInfoData.setKeeperAPI(req.getKeeperAPI());
		composerInfoData.setWebLocalIP(req.getWebLocalIP());
		composerInfoData.setWebServerPort(req.getWebServerPort());
		composerInfoData.setVersion(req.getVersion());
		composerInfoData.setTs(req.getTs());
		composerInfoData.setCpuUsage(req.getCpuUsage());
		composerInfoData.setMemoryUsage(req.getMemoryUsage());

		composerInfoData.setUpTime(
				DateTimeUtil.secondsToDaysHoursMinutesSeconds(composerInfoData.getTs() - req.getStartupTime()));

		composerInfoData.setTsToString(
				DateTimeUtil.dateTimeToString(new Date(composerInfoData.getTs()), DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));

		// 定期跟Server回報主機的Composer資訊。
		ComposerInfoPacket composerInfoPacket = new ComposerInfoPacket();
		composerInfoPacket.composerInfoData = composerInfoData;
		if (TPILogger.lc != null) {
			TPILogger.lc.send(composerInfoPacket);
		} else {
			//因為不一定要啟動keeper server且這又是每秒call一次,所以不印Log了
		}

		// 是否禁止紀錄ES的LOG
		boolean esDisable = getTsmpSettingService().getVal_ES_LOG_DISABLE();
		if (esDisable) {
			// TPILogger.tl.debug("ES_LOG_DISABLE is true");
		} else {
			// 有Composer http request資料才寫到ES
			if (req.getHttpRequestLog()!=null && !req.getHttpRequestLog().isEmpty()) {
				writeToES(req);
			}
		}
		
		// 輸出 Composer 兩道log到online conolse
		List<AA0325Log> logData = req.getHttpRequestLog();
		if (logData != null) {
			for (AA0325Log log : logData) {
				TPILogger.tl.debug(log.getConsoleLog());
			}
		}
		
		// 輸出 sys Log 到online conolse
		List<AA0325SysLog> syslogData = req.getSysLog();
		if (syslogData != null) {
			for (AA0325SysLog log : syslogData) {
				
				// 以loggerLevel判斷要輸出到ERROR、WARN、INFO、DEBUG、TRACE
				int loggerLevel = log.getLoggerLevel().intValue();
				
				if (LoggerLevelConstant.ERROR.value().intValue()==loggerLevel) {
					TPILogger.tl.error(log.getConsoleLog());	
				}
				
				if (LoggerLevelConstant.WARN.value().intValue()==loggerLevel) {
					TPILogger.tl.warn(log.getConsoleLog());	
				}
				
				if (LoggerLevelConstant.INFO.value().intValue()==loggerLevel) {
					TPILogger.tl.info(log.getConsoleLog());	
				}
				
				if (LoggerLevelConstant.DEBUG.value().intValue()==loggerLevel) {
					TPILogger.tl.debugDelay2sec(log.getConsoleLog());
				}
				
				if (LoggerLevelConstant.TRACE.value().intValue()==loggerLevel) {
					TPILogger.tl.trace(log.getConsoleLog());
				}
				
			}
		}
		
		return resp;

	}

	private void writeToES(AA0325Req req) throws JsonProcessingException {

		// 檢查ES_ID_PWD參數內容值個數和URL個數有沒有吻合
		String[] arrEsUrl = getTsmpSettingService().getVal_ES_URL().split(",");
		String[] arrIdPwd = getTsmpSettingService().getVal_ES_ID_PWD().split(",");
		if (arrIdPwd.length != arrEsUrl.length) {
			TPILogger.tl.warn("ES connection info and id pwd is not mapping");
			return;
		}

		// 測試連線
		int index = 0;
		boolean isConnection = false;
		for (; index < arrEsUrl.length; index++) {
			isConnection = this.checkConnection(arrEsUrl[index]);
			if (isConnection) {
				break;
			}
		}
		if (!isConnection) {
			TPILogger.tl.error("es connection is fail");
			return;
		}
		Date nowDate = new Date();

		// index是否存在,不存在就建立
		String indexName = "tsmp_comp_log_" + DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日_4).orElse(String.valueOf(TsmpDpAaRtnCode._1295));

		List<AA0325Log> logData = req.getHttpRequestLog();

		for (AA0325Log log : logData) {
			String strJson = getObjectMapper().writeValueAsString(log.getEsData());
			String esReqUrl = arrEsUrl[index] + indexName + "/_doc";

			// call es add data
			Map<String, String> header = new HashMap<>();
			header.put("Accept", "application/json");
			header.put("Content-Type", "application/json");
			header.put("Authorization", "Basic " + arrIdPwd[index]);

			// asynchronous call
			this.execute(() -> {
				try {

					HttpRespData resp = HttpUtil.httpReqByRawData(esReqUrl, HttpMethod.POST.toString(), strJson, header,
							false);
					if (resp.statusCode == 200) {
						TPILogger.tl.debug("write Composer http API Log to ES = " + strJson);						
					}
				} catch (Exception e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			});
		}

	}

	public boolean checkConnection(String strUrl) {
		try (Socket socket = new Socket()) {
			int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
			URL url = new URL(strUrl);
			socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), timeout);
			return true;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return false;
		}
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	private void execute(Runnable command) {
		this.executor.execute(command);
	}

}
