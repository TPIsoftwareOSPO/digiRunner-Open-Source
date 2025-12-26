package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpGroupVo;

@Service
public class TsmpGroupTableInitializer {
	
    private  List<TsmpGroupVo> tsmpGroupList = new LinkedList<>();
    
    
    public List<TsmpGroupVo> insertTsmpGroup() {
		try {
	    	String groupId;
	    	String groupName;
	    	String createUser;
	    	String groupAlias;
	    	String groupDesc;
	    	String groupAccess;
	    	String securityLevelId;
	    	Integer allowDays;
	    	Integer allowTimes;
	    	String vgroupId;
	    	String vgroupName;
	    	String vgroupFlag;
	    	createTsmpGroup((groupId = "1000"), (groupName = "SMS"), (createUser = "manager"), (groupAlias = "Admin Console"), (groupDesc = null), (groupAccess = null), (securityLevelId = null), (allowDays = null), (allowTimes = null), (vgroupId = null), (vgroupName = null), (vgroupFlag = "0"));
	    	
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		
		return tsmpGroupList;
	}
	
	protected void createTsmpGroup(String groupId, String groupName, String createUser, String groupAlias, String groupDesc, String groupAccess, //
			String securityLevelId, Integer allowDays, Integer allowTimes, String vgroupId, String vgroupName, String vgroupFlag) {
			TsmpGroupVo tsmpGroup = new TsmpGroupVo();
			tsmpGroup.setGroupId(groupId);
			tsmpGroup.setGroupName(groupName);
			tsmpGroup.setCreateTime(DateTimeUtil.now());
			tsmpGroup.setCreateUser(createUser);
			tsmpGroup.setUpdateTime(null);
			tsmpGroup.setUpdateUser(null);
			tsmpGroup.setGroupAlias(groupAlias);
			tsmpGroup.setGroupDesc(groupDesc);
			tsmpGroup.setGroupAccess(groupAccess);
			tsmpGroup.setSecurityLevelId(StringUtils.hasLength(securityLevelId) ? securityLevelId : "SYSTEM");
			tsmpGroup.setAllowDays(Objects.isNull(allowDays) ? 0 : allowDays);
			tsmpGroup.setAllowTimes(Objects.isNull(allowTimes) ? 0 : allowTimes);
			tsmpGroup.setVgroupId(vgroupId);
			tsmpGroup.setVgroupName(vgroupName);
			tsmpGroup.setVgroupFlag(Objects.isNull(vgroupFlag) ? "0" : vgroupFlag);
			tsmpGroupList.add(tsmpGroup);
	}
}
