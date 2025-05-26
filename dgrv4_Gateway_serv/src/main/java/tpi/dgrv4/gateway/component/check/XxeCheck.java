package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Component
public class XxeCheck implements ICheck{

	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeService tsmpRtnCodeService;
	
	@Autowired
	public XxeCheck(TsmpSettingService tsmpSettingService, TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	public boolean check(String str, boolean isSettingDecideCheck) {
		boolean isEnabled = true;
		if(isSettingDecideCheck) {
			isEnabled = getTsmpSettingService().getVal_CHECK_XXE_ENABLE();
		}
		if(isEnabled) {
			str = str.toUpperCase();
			if(str.indexOf("<!DOCTYPE") > -1 || str.indexOf("<\\!DOCTYPE") > -1 || str.indexOf("<!ENTITY") > -1
					|| str.indexOf("<\\!ENTITY") > -1 ) {
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
