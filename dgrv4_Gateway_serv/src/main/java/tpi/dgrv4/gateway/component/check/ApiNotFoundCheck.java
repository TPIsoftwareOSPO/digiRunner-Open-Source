package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
@Component
public class ApiNotFoundCheck implements ICheck{
	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeService tsmpRtnCodeService;

	@Autowired
	public ApiNotFoundCheck(TsmpSettingService tsmpSettingService, TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}
	@Override
	public DgrRtnCode getRtnCode() {
		// TODO Auto-generated method stub
		return DgrRtnCode._0125;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode apiNotFound_tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (apiNotFound_tsmpRtnCode!=null) {
			return apiNotFound_tsmpRtnCode.getTsmpRtnMsg();	
		}else {
			return getRtnCode().getDefaultMessage();
		}
	}


}
