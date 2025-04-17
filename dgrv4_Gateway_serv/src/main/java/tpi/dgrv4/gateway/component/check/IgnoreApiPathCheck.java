package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.gateway.service.TsmpSettingService;

@Component
public class IgnoreApiPathCheck {

	private TsmpSettingService tsmpSettingService;

	@Autowired
	public IgnoreApiPathCheck(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public boolean check(String str) {
		boolean isEnabled = getTsmpSettingService().getVal_CHECK_IGNORE_API_PATH_ENABLE();
		if (isEnabled) {
			String ignoreApiPath = getTsmpSettingService().getVal_IGNORE_API_PATH();
			if (StringUtils.hasLength(ignoreApiPath)) {
				String[] arrIgnoreApiPath = ignoreApiPath.split(",");
				for (String path : arrIgnoreApiPath) {
					if (path.indexOf("**") > -1) {
						path = path.replaceAll("\\*\\*", ".*");
						boolean match = str.matches(path);
						if (match) {
							return true;
						}
					} else if (path.equals(str)) {
						return true;
					}
				}
			}

		}
		return false;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
