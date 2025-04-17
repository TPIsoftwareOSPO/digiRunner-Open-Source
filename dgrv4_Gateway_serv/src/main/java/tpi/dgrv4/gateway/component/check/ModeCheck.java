package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;

@Component
public class ModeCheck implements ICheck {
	private TPILogger logger = TPILogger.tl;
	
	private Environment env;
	private TsmpRtnCodeService tsmpRtnCodeService;
	
	private static final String MODE = "digiRunner.gtw.mode";

	@Autowired
	public ModeCheck(Environment env, TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.env = env;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	public Boolean check(String uri) {
		String mode = getMode();

		if ("onlyAC".equalsIgnoreCase(mode)) {

			if (uri.length() >= 7 && (uri.substring(0, 7).equals("/dgrv4/"))
					|| (uri.length() >= 8 && (uri.substring(0, 8).equals("/kibana/")))
					|| (uri.length() >= 16 && (uri.substring(0, 16).equals("/_plugin/kibana/")))
					|| (uri.length() >= 10 && (uri.substring(0, 10).equals("/http-api/")))
					|| (uri.length() >= 18 && (uri.substring(0, 18).equals("/website/composer/")))) {
				return true;
			}
			logger.debug("digiRunner.gtw.mode = " + mode);
			return false;
		} else if ("onlyGTW".equalsIgnoreCase(mode)) {
			if (((uri.length() >= 9 && (uri.substring(0, 9).equals("/website/")))
					&& !(uri.length() >= 18 && (uri.substring(0, 18).equals("/website/composer/"))))
					|| (!(uri.length() >= 9 && (uri.substring(0, 9).equals("/website/")))
							&& (!(uri.length() >= 8 && (uri.substring(0, 8).equals("/kibana/")))
									&& !(uri.length() >= 16 && (uri.substring(0, 16).equals("/_plugin/kibana/")))
									&& !(uri.length() >= 10 && (uri.substring(0, 10).equals("/http-api/")))
									&& !(uri.length() >= 7 && (uri.substring(0, 7).equals("/dgrv4/")))))) {
				return true;
			}

			logger.debug("digiRunner.gtw.mode = " + mode);
			return false;
		} else if ("Both".equalsIgnoreCase(mode)) {
			return true;
		}
		logger.debug("digiRunner.gtw.mode = " + mode);
		return false;
	}

	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._1219;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (tsmpRtnCode != null) {
			return tsmpRtnCode.getTsmpRtnMsg() + " (Please check your GTW mode)";
		} else {
			return getRtnCode().getDefaultMessage() + " (Please check your GTW mode)";
		}
	}

	private String getMode() {
		return getEnvironment().getProperty(MODE);
	}

	protected Environment getEnvironment() {
		return env;
	}

	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}

}
