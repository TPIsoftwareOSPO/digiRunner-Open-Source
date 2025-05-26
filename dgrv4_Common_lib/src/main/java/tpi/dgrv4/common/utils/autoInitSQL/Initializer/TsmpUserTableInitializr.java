package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpUserVo;

@Service
public class TsmpUserTableInitializr {

	List<TsmpUserVo> tsmpUserlist = new LinkedList<>();

	public List<TsmpUserVo> insertTsmpUser() {
		try {
			// 系統預設的使用者、角色及組織
			String userId;
			String userName;
			String userAlias;
			String userStatus;
			String userEmail;
			String createUser;
			int pwdFailTimes;
			String orgId;
			createTsmpUser((userId = "1000000000"), (userName = "manager"), (userAlias = "Manager"), (userStatus = "1"),(userEmail = "manager@thinkpower.com.tw"), (createUser = "manager"), (pwdFailTimes = 0),(orgId = "100000"));
		
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		
		return tsmpUserlist;
	}

	protected void createTsmpUser(String userId, String userName, String userAlias, String userStatus, String userEmail,
			String createUser, int pwdFailTimes, String orgId) {

		TsmpUserVo tsmpUser = new TsmpUserVo();
		tsmpUser.setUserId(userId);
		tsmpUser.setUserName(userName);
		tsmpUser.setUserAlias(userAlias);
		tsmpUser.setUserStatus(userStatus);
		tsmpUser.setUserEmail(userEmail);
		tsmpUser.setCreateUser(createUser);
		tsmpUser.setCreateTime(DateTimeUtil.now());
		tsmpUser.setPwdFailTimes(pwdFailTimes);
		tsmpUser.setOrgId(orgId);
		tsmpUserlist.add(tsmpUser);

	}

}
