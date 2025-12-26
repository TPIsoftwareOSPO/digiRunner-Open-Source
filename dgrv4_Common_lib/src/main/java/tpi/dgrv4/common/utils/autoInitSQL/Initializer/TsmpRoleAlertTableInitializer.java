package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpRoleAlertVo;

@Service
public class TsmpRoleAlertTableInitializer {

	private  List<TsmpRoleAlertVo> tsmpAlertVoList = new LinkedList<TsmpRoleAlertVo>();
	
	
	public List<TsmpRoleAlertVo> insertTsmpRoleAlert() {
		try {
			String roleId;
			Long alertId;
			
			createTsmpRoleAlert((roleId = "1000"), (alertId = 1L));
			createTsmpRoleAlert((roleId = "1000"), (alertId = 2L));
			createTsmpRoleAlert((roleId = "1000"), (alertId = 3L));
		
		
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return tsmpAlertVoList;
	}


	protected void createTsmpRoleAlert(String roleId, Long alertId) {
		TsmpRoleAlertVo tsmpRoleAlertVo = new TsmpRoleAlertVo();
		tsmpRoleAlertVo.setRoleId(roleId);
		tsmpRoleAlertVo.setAlertId(alertId);
		tsmpAlertVoList.add(tsmpRoleAlertVo);
	}
    
}
