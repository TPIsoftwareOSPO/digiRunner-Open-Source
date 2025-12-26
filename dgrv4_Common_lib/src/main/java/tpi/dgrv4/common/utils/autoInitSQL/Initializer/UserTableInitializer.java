package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.UsersVo;

@Service
public class UserTableInitializer {

	List<UsersVo> userlist = new LinkedList<>();

	public List<UsersVo> insertUsers() {
		String userName;
		String mima;
		int enabled;
		try {

//      password=Bcrypt(Base64Encode(manager123))
		createUsers((userName = "manager"), (mima = "$2y$12$C8B6D4SzvM7qL1NUo4Uwa.daXzIRlYFkeZ62fhnVeK4smJL6ZqFra"),(enabled = 1));
		
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return userlist;
	}

	protected void createUsers(String userName, String password, int enabled) {

		UsersVo users = new UsersVo();
		users.setUserName(userName);
		users.setPassword(password);
		users.setUserStatus(enabled);
		userlist.add(users);

	}


}
