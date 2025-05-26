package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@RestController

@RequestMapping("/dgrv4")
public class OnlineConsole2TestController {

	private TPILogger logger;
	private TsmpSettingService tsmpSettingService; 

	@Autowired
	public OnlineConsole2TestController(TPILogger logger, TsmpSettingService tsmpSettingService) {
		super();
		this.logger = logger;
		this.tsmpSettingService = tsmpSettingService;
	}

	@GetMapping(value = "/onlineConsole2/testonlineconsole2", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String testonlineconsole2(HttpServletRequest request) {
		
		if (!tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return "FORBIDDEN...";
		}
		
		String time = request.getParameter("time");
		logger.trace("I AM '<span id='userName'>" + TPILogger.lc.userName + "</span>'...");
		if (time != null) {
			for (int i = 1; i <= Integer.parseInt(time); i++) {
				logger.info("I AM '<span id='userName'>" + TPILogger.lc.userName + "</span>'..." + i);
			}
		} else {
			logger.debug("I AM '<span id='userName'>" + TPILogger.lc.userName + "</span>'");
		}
		return "success";
	}
}
