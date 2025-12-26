package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Setter;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0553Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0553Service {

	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private TsmpSettingService tsmpSettingService;

	public AA0553Resp getPwdStrengthInfo() {
		AA0553Resp resp = new AA0553Resp();
		try {
			String acPwdStrength = getTsmpSettingService().getVal_PWD_STRENGTH();
			String acPwdStrengthDesc = getTsmpSettingService().getVal_PWD_STRENGTH_DESC();
			String clientPwdStrength = getTsmpSettingService().getVal_CLIENT_PWD_STRENGTH();
			String clientPwdStrengthDesc = getTsmpSettingService().getVal_CLIENT_PWD_STRENGTH_DESC();
			
			resp.setAcPwdStrength(acPwdStrength)
			.setAcPwdStrengthDesc(acPwdStrengthDesc)
			.setClientPwdStrength(clientPwdStrength)
			.setClientPwdStrengthDesc(clientPwdStrengthDesc);
			
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	
}
