package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpClientVo;

@Service
public class TsmpClientTableInitializer {
	
	private  List<TsmpClientVo> tsmpClientlist = new LinkedList<>();
    
	public List<TsmpClientVo> insertTsmpClient() {
		try {
	    	String clientId;
	    	String clientName;
	    	String clientStatus;
	    	Integer tps;
	    	String emails;
	    	String owner;
	    	String createUser;
	    	Integer apiQuota;
	    	Integer apiUsed;
	    	Integer cPriority;
	    	Integer pwdFailTimes;
			String securityLevelId;
			String clientSecret;
	    	createTsmpClient((clientId = "YWRtaW5Db25zb2xl"), (clientName = "adminConsole"), (clientStatus = "1"), (tps = 10) ,(emails = "166bea517d-b44db8@inbox.mailtrap.io") ,(owner = "TSMP") ,(createUser = "manager"), (apiQuota = 0), (apiUsed = 0),  (cPriority = 5), (pwdFailTimes = 0), (securityLevelId = "SYSTEM"), (clientSecret = null));

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return tsmpClientlist;
	}
	
	
	protected void createTsmpClient(String clientId, String clientName, String clientStatus, Integer tps, String emails,
			String owner, String createUser, Integer apiQuota, Integer apiUsed, Integer cPriority, Integer pwdFailTimes,
			String securityLevelId, String clientSecret) {

		TsmpClientVo tsmpClient = new TsmpClientVo();
		tsmpClient.setClientId(clientId);
		tsmpClient.setClientName(clientName);
		tsmpClient.setClientStatus(clientStatus);
		tsmpClient.setTps(tps);
		tsmpClient.setEmails(emails);
		tsmpClient.setCreateTime(DateTimeUtil.now());
		tsmpClient.setOwner(owner);
		tsmpClient.setCreateUser(createUser);

		tsmpClient.setApiQuota(apiQuota);
		tsmpClient.setApiUsed(apiUsed);
		tsmpClient.setcPriority(cPriority);
		tsmpClient.setPwdFailTimes(pwdFailTimes);
		tsmpClient.setSecurityLevelId(securityLevelId);
		tsmpClient.setClientSecret(clientSecret);
		tsmpClientlist.add(tsmpClient);
	}
}
