package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpClientGroupVo;

@Service
public class TsmpClientGroupTableInitializer {

    private  List<TsmpClientGroupVo> tsmpClientGroupList = new LinkedList<>();
    
    public List<TsmpClientGroupVo> insertTsmpClientGroup() {
    	try {
        	String clientId;
        	String groupId;
        	createTsmpClientGroup((clientId = "YWRtaW5Db25zb2xl"), (groupId = "1000"));
        	
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
    	return tsmpClientGroupList;
    }
    
	protected void createTsmpClientGroup(String clientId, String groupId) {
			TsmpClientGroupVo tsmpClientGroup = new TsmpClientGroupVo();
			tsmpClientGroup.setClientId(clientId);
			tsmpClientGroup.setGroupId(groupId);
			tsmpClientGroupList.add(tsmpClientGroup);
	}
}
