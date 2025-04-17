package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0002Req;
import tpi.dgrv4.dpaa.vo.AA0002Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0002Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpUserDao tsmpUserDao;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	@Autowired
	public AA0002Service(TsmpUserDao tsmpUserDao, DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	public AA0002Resp queryUserDataByLoginUser(TsmpAuthorization auth, AA0002Req req) {
		AA0002Resp resp = new AA0002Resp();

		try {
			String idPType = auth.getIdpType();
			if (DgrIdPType.LDAP.equals(idPType) // LDAP
					|| DgrIdPType.MLDAP.equals(idPType) // MLDAP
					|| DgrIdPType.API.equals(idPType) // API
					|| DgrIdPType.CUS.equals(idPType) // CUS
			) {
				// 1.以 SSO AC IdP (LDAP / MLDAP / API)的方式登入
				String userNameForQuery = auth.getUserNameForQuery();
				DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao()
						.findFirstByUserNameAndIdpType(userNameForQuery, idPType);
				
				String userAlias = dgrAcIdpUser.getUserAlias();
				String userName = dgrAcIdpUser.getUserName();
				if (StringUtils.hasLength(userAlias)) {
					resp.setUserAlias(userAlias);
				} else {
					// 沒有值則顯示 user name
					resp.setUserAlias(userName);
				}

			} else if (DgrIdPType.GOOGLE.equals(idPType) //
					|| DgrIdPType.MS.equals(idPType) //
					|| DgrIdPType.OIDC.equals(idPType) //
			) {
				// 2.以 SSO AC IdP (GOOGLE / MS / OIDC)的方式登入
				resp.setIdTokenJwtstr(auth.getIdTokenJwtstr());

			} else {
				// 3.以 AC 方式登入
				TsmpUser user = getTsmpUserDao().findFirstByUserName(auth.getUserName());
				if (user != null) {
					resp.setUserID(user.getUserId());
					resp.setUserAlias(user.getUserAlias());
				}
			}
 
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}
