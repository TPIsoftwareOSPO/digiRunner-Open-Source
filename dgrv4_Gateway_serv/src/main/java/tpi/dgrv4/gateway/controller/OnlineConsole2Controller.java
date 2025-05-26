package tpi.dgrv4.gateway.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.TCP.Packet.ChangeLoggerLevelPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireUndertowMetricsInfosPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireUrlStatusInfosPacket;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OnlineConsole2Service;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.AllClientInfoData;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.ComposerInfoData;
import tpi.dgrv4.gateway.vo.CurrentLogReq;
import tpi.dgrv4.gateway.vo.OnlineConsole;
import tpi.dgrv4.tcp.utils.packets.UndertowMetricsPacket;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;

@RestController
@RequestMapping("/dgrv4")

public class OnlineConsole2Controller {

	public static Object lockObj = new Object();

	private TsmpSettingService tsmpSettingService;
	private OnlineConsole2Service onlineConsole2Service;
	private TPILogger logger;

	@Autowired
	public OnlineConsole2Controller(TsmpSettingService tsmpSettingService, OnlineConsole2Service onlineConsole2Service,
			TPILogger logger) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.onlineConsole2Service = onlineConsole2Service;
		this.logger = logger;
	}

	@PostMapping(value = "/onlineConsole2/api")
	public ResponseEntity<?> onlineconsole2(HttpServletRequest request, @RequestBody CurrentLogReq req) {

		List<OnlineConsole> data = null;

		if (tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			data = onlineConsole2Service.outputlog(req);
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN...");
		}

		return ResponseEntity.ok(data);
	}

	@PostMapping(value = "/onlineConsole2/showAllClient")
	public ResponseEntity<?> showAllClient(HttpServletRequest request) {

		AllClientInfoData allClientInfoData = new AllClientInfoData();

		if (!tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return ResponseEntity.ok(allClientInfoData);
		}

		if (TPILogger.lc != null) {
			TPILogger.lc.send(new RequireAllClientListPacket());
			myWaitLock();
			allClientInfoData.setAllClientList((LinkedList<ClientKeeper>) TPILogger.lc.paramObj.get("allClientList"));
			allClientInfoData.setAllComposerList((LinkedList<ComposerInfoData>) TPILogger.lc.paramObj
					.get(RequireAllClientListPacket.allComposerListStr));
			// lc.paramObj.put("CPU..etc", DpaaSystemInfo);
		} else {
			TPILogger.tl.error("<Keeper Server> Lost Connection");
		}
		//allClientInfoData.getAllClientList().get(0).getMem();
		return ResponseEntity.ok(allClientInfoData);
	}

	@PostMapping(value = "/onlineConsole2/showAllThreadStatus")
	public ResponseEntity<?> showAllThreadStatus(HttpServletRequest request) {

		List<UndertowMetricsPacket> undertowMetricsInfos = Collections.emptyList();

		if (!tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return ResponseEntity.ok(undertowMetricsInfos);
		}

		if (TPILogger.lc != null) {
			TPILogger.lc.send(new RequireUndertowMetricsInfosPacket());
			Object infos = TPILogger.lc.paramObj.get("undertowMetricsInfos");
			if (infos != null) {
				undertowMetricsInfos = getUndertowMetricsInfos(infos);
			}

		} else {
			TPILogger.tl.error("<Keeper Server> Lost Connection");
		}

		return ResponseEntity.ok(undertowMetricsInfos);
	}

	private List<UndertowMetricsPacket> getUndertowMetricsInfos(Object infos) {
		// 檢查是否為 null
		if (infos == null) {
			return Collections.emptyList();
		}

		// 檢查是否為 Collection
		if (!(infos instanceof Collection<?>)) {
			return Collections.emptyList();
		}

		Collection<?> collection = (Collection<?>) infos;

		// 檢查集合是否為空
		if (collection.isEmpty()) {
			return Collections.emptyList();
		}

		// 檢查第一個元素類型
		Object firstElement = collection.iterator().next();
		if (!(firstElement instanceof UndertowMetricsPacket)) {
			return Collections.emptyList();
		}

		// 轉換並排序
		Collection<UndertowMetricsPacket> metrics = (Collection<UndertowMetricsPacket>) collection;
		return metrics.stream().filter(Objects::nonNull) // 過濾掉 null 元素
				.sorted(Comparator.comparing(UndertowMetricsPacket::getName, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
	}
	
	@PostMapping(value = "/onlineConsole2/showAllUrlStatus")
	public ResponseEntity<?> showAllUrlStatus(HttpServletRequest request) {

		List<UrlStatusPacket> urlStatusInfos = Collections.emptyList();

		if (!tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return ResponseEntity.ok(urlStatusInfos);
		}

		if (TPILogger.lc != null) {
			TPILogger.lc.send(new RequireUrlStatusInfosPacket());
			Object infos = TPILogger.lc.paramObj.get("urlStatusInfos");
			if (infos != null) {
				urlStatusInfos = getUrlStatusInfos(infos);
			}

		} else {
			TPILogger.tl.error("<Keeper Server> Lost Connection");
		}

		return ResponseEntity.ok(urlStatusInfos);
	}
	
	private List<UrlStatusPacket> getUrlStatusInfos(Object infos) {
		// 檢查是否為 null
		if (infos == null) {
			return Collections.emptyList();
		}

		// 檢查是否為 Collection
		if (!(infos instanceof Collection<?>)) {
			return Collections.emptyList();
		}

		Collection<?> collection = (Collection<?>) infos;

		// 檢查集合是否為空
		if (collection.isEmpty()) {
			return Collections.emptyList();
		}

		// 檢查第一個元素類型
		Object firstElement = collection.iterator().next();
		if (!(firstElement instanceof UrlStatusPacket)) {
			return Collections.emptyList();
		}

		// 轉換並排序
		Collection<UrlStatusPacket> packets = (Collection<UrlStatusPacket>) collection;
		return packets.stream().filter(Objects::nonNull) // 過濾掉 null 元素
				.sorted(Comparator.comparing(UrlStatusPacket::getName, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
	}

	@GetMapping(value = "/onlineConsole2/changeLoggerLevel", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String changeLoggerLevel(HttpServletRequest request) {
		String loggerLevel = request.getParameter("loggerLevel");

		if (tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			logger.loggerLevel = loggerLevel;
			onlineConsole2Service.changeLoggerLevel(loggerLevel);
			
			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.SETTING.value());

			// error 才能一直印出來
			// logger.error("<font size=18>切換logger Level為: " + loggerLevel + " </font>");

			// 傳送出去通知
			ChangeLoggerLevelPacket packet = new ChangeLoggerLevelPacket();
			packet.loggerLevel = loggerLevel;
			// TPILogger.tl.info("[Contrller Start] change log level: " + loggerLevel);
			TPILogger.lc.send(packet);
			// TPILogger.tl.info("[Contrller End] change log level: " + loggerLevel);
			return "success";
		} else {
			return "FORBIDDEN...";
		}
	}

	@GetMapping(value = "/onlineConsole2/currentLoggerLevel", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String currentLoggerLevel(HttpServletRequest request) {
		if (tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return onlineConsole2Service.currentLoggerLevel();
		} else {
			return "FORBIDDEN...";
		}
	}

	private void myWaitLock() {
		synchronized (RequireAllClientListPacket.waitKey) {
			try {
				RequireAllClientListPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
	}

}