package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Component
public class SqlInjectionCheck implements ICheck {

	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeService tsmpRtnCodeService;
	
	@Autowired
	public SqlInjectionCheck(TsmpSettingService tsmpSettingService, TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	public boolean check(String str, boolean isSettingDecideCheck) {
		boolean isEnabled = true;
		if(isSettingDecideCheck) { 
			isEnabled = getTsmpSettingService().getVal_CHECK_SQL_INJECTION_ENABLE();
		}
		if(isEnabled) {
			if(str.indexOf("'") > -1 || str.indexOf(";") > -1) {
				return true;
			}
		}
		return false;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	@Override
	public String getMessage(String locale) {
		TsmpRtnCode sqlInjection_tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (sqlInjection_tsmpRtnCode!=null) {
			return sqlInjection_tsmpRtnCode.getTsmpRtnMsg();	
		}else {
			return getRtnCode().getDefaultMessage();
		}
	}
	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}

	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._9926;
	}

	
	
}
