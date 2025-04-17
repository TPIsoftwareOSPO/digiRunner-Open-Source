package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Component
public class XssCheck implements ICheck{

	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeService tsmpRtnCodeService;
	
	@Autowired
	public XssCheck(TsmpSettingService tsmpSettingService, TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	public boolean check(String str, boolean isSettingDecideCheck) {
		boolean isEnabled = true;
		if(isSettingDecideCheck) {
			isEnabled = getTsmpSettingService().getVal_CHECK_XSS_ENABLE();
		}
		if(isEnabled) {
			str = str.toLowerCase();
			if(str.indexOf("<script") > -1 || str.indexOf("</script>") > -1 || str.indexOf("eval(") > -1
					|| str.indexOf("expression(") > -1 || str.indexOf("javascript:") > -1 || str.indexOf("vbscript:") > -1
					|| str.indexOf("onload=") > -1 || str.indexOf("src=") > -1) {
				return true;
			}
		}
		return false;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}
	
	
	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._9930;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (tsmpRtnCode!=null) {
			return tsmpRtnCode.getTsmpRtnMsg();	
		}else {
			return getRtnCode().getDefaultMessage();
		}
	}

	
}
