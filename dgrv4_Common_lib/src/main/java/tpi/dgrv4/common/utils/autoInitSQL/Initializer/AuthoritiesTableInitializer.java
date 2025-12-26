package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.AuthoritiesVo;
@Service
public class AuthoritiesTableInitializer {
	
	private List<AuthoritiesVo> authoritieslist = new LinkedList<>();

	public List<AuthoritiesVo> insertAuthorities() {
		try {
			String userName;
			String authority;
			createAuthorities((userName = "manager"), (authority = "1000"));
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return authoritieslist;
	}
    
	protected void createAuthorities(String userName, String authority) {
		AuthoritiesVo authorities = new AuthoritiesVo();
		authorities.setUsername(userName);
		authorities.setAuthority(authority);
		authoritieslist.add(authorities);
	}
}
