package tpi.dgrv4.dpaa.service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import tpi.dgrv4.dpaa.vo.AA0326Req;
import tpi.dgrv4.dpaa.vo.AA0326Resp;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Service
public class AA0326Service {

	private TsmpSettingService tsmpSettingService;

	@Autowired
	public AA0326Service(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public AA0326Resp getComposerAllFlag(AA0326Req req, HttpServletRequest httpReq) throws JsonProcessingException {

		AA0326Resp resp = new AA0326Resp();

		String composerLogInterval = getTsmpSettingService().getVal_COMPOSER_LOG_INTERVAL();
		resp.setComposerLogInterval(composerLogInterval);

		String composerLogSize = getTsmpSettingService().getVal_COMPOSER_LOG_SIZE();
		resp.setComposerLogSize(composerLogSize);

		String composerLogSwicth = getTsmpSettingService().getVal_COMPOSER_LOG_SWICTH();
		resp.setComposerLogSwicth(composerLogSwicth);
		
		String composerLogMaxFiles = getTsmpSettingService().getVal_COMPOSER_LOG_MAX_FILES();
		resp.setComposerLogMaxFiles(composerLogMaxFiles);
		
		String composerRequestTimeout = getTsmpSettingService().getVal_COMPOSER_REQUEST_TIMEOUT();
		resp.setComposerRequestTimeout(composerRequestTimeout);

		return resp;

	}

	public TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	public void setTsmpSettingService(TsmpSettingService tsmpSettingService) {
		this.tsmpSettingService = tsmpSettingService;
	}

}
