package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpSecurityLevelVo;

@Service
public class TsmpSecurityLevelInitializer {
	
	private  List<TsmpSecurityLevelVo> tsmpSecurityLevelList = new LinkedList<>();

	public List<TsmpSecurityLevelVo> insertTsmpSecurityLevel() {
		try {
	    	String securityLevelId;
	    	String securityLevelName;
	    	String securityLevelDesc;
			
	    	createTsmpSecurityLevel((securityLevelId = "A"),(securityLevelName = "A"),(securityLevelDesc = "A Security Level"));
	    	createTsmpSecurityLevel((securityLevelId = "B"),(securityLevelName = "B"),(securityLevelDesc = "B Security Level"));
	    	createTsmpSecurityLevel((securityLevelId = "C"),(securityLevelName = "C"),(securityLevelDesc = "C Security Level"));
	    	createTsmpSecurityLevel((securityLevelId = "D"),(securityLevelName = "D"),(securityLevelDesc = "D Security Level"));
	    	createTsmpSecurityLevel((securityLevelId = "SYSTEM"),(securityLevelName = "System Security"),(securityLevelDesc = "System security"));
	    	
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		
		return tsmpSecurityLevelList;
	}

	protected void createTsmpSecurityLevel(String securityLevelId, String securityLevelName, String securityLevelDesc) {
			TsmpSecurityLevelVo tsmpSecurityLevel = new TsmpSecurityLevelVo();
			tsmpSecurityLevel.setSecurityLevelId(securityLevelId);
			tsmpSecurityLevel.setSecurityLevelName(securityLevelName);
			tsmpSecurityLevel.setSecurityLevelDesc(securityLevelDesc);
			tsmpSecurityLevelList.add(tsmpSecurityLevel);
	}
}
