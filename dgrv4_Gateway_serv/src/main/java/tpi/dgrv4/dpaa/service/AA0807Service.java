package tpi.dgrv4.dpaa.service;


import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0807Req;
import tpi.dgrv4.dpaa.vo.AA0807Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0807Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRoleDao tsmpRoleDao;
	private AuthoritiesDao authoritiesDao;
	private TsmpRegHostDao tsmpRegHostDao;

	@Autowired
	public AA0807Service(TsmpRoleDao tsmpRoleDao, AuthoritiesDao authoritiesDao, TsmpRegHostDao tsmpRegHostDao) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRegHostDao = tsmpRegHostDao;
	}

	public AA0807Resp queryRegHostByRegHostID(TsmpAuthorization authorization, AA0807Req req) {
		AA0807Resp resp = new AA0807Resp();
		try {
			String regHostId = nvl(req.getRegHostID());
			boolean isNeedCheck = isNeedCheck(authorization);
			TsmpRegHost tsmpRegHost = getTsmpRegHostDao().findByReghostId(regHostId);
			
			if (tsmpRegHost != null ) {
				if(isNeedCheck) {
					String crateUser = nvl(tsmpRegHost.getCreateUser());
					String userName = nvl(authorization.getUserName());
					if(crateUser.equals(userName)) {
						resp.setRegHost(nvl(tsmpRegHost.getReghost()));
					}else {
						resp.setRegHost("");
					}
				}else {
					resp.setRegHost(nvl(tsmpRegHost.getReghost()));
				}
			}else {
				resp.setRegHost("");
			}
				
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	/**
	 * TSMP_ROLE.ROLE_ID = AUTHORITIES.AUTHORITY AND
	 * TSMP_ROLE.ROLE_NAME IN ( 'ADMIN' OR 'API_ADMIN' ) AND
	 * AUTHORITIES.USERNAME = TsmpAuthorization.userName
	 * 
	 * @param authorization
	 * @return
	 */
	private boolean isNeedCheck(TsmpAuthorization authorization) {
		boolean isNeedCheck = true;
		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(nvl(authorization.getUserNameForQuery()));
			for (Authorities authorities : authoritiesList) {
				Optional<TsmpRole> optRole = getTsmpRoleDao().findById(nvl(authorities.getAuthority()));
				if(optRole.isPresent()) {
					String roleName = nvl(optRole.get().getRoleName());
					if("ADMIN".equals(roleName) || "API_ADMIN".equals(roleName)) {
						return false ;
					}
			}
			
		}
		return isNeedCheck;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}
	
	protected TsmpRegHostDao getTsmpRegHostDao() {
		return tsmpRegHostDao;
	}
	
}
