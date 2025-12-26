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
			boolean isAC = true;
			boolean isGetIdpUserData = false;
			
			if (DgrIdPType.GOOGLE.equals(idPType) //
					|| DgrIdPType.MS.equals(idPType) //
					|| DgrIdPType.OIDC.equals(idPType)) { //
				// 以 SSO AC IdP (GOOGLE / MS / OIDC)的方式登入
				isAC = false;
				
				if (auth.getIdTokenJwtstr() == null) {
					// 若 Delegate AC User 先建立 "OIDC" 使用者，再使用 其他 IdP 登入(AC_IDP_LOGIN_IGNORE_TYPE =
					// true)，會沒有 Id Token 資料
					isGetIdpUserData = true;

				} else {
					resp.setIdTokenJwtstr(auth.getIdTokenJwtstr());
				}
			}
			
			if (DgrIdPType.LDAP.equals(idPType) // LDAP
					|| DgrIdPType.MLDAP.equals(idPType) // MLDAP
					|| DgrIdPType.API.equals(idPType) // API
					|| DgrIdPType.CUS.equals(idPType) // CUS
					|| isGetIdpUserData) {
				// 以 SSO AC IdP (LDAP / MLDAP / API)的方式登入 
				isAC = false;
				
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
			}

			if(isAC) {
				// 以 AC 方式登入
				TsmpUser user = getTsmpUserDao().findFirstByUserName(auth.getUserName());
				if (user != null) {
					resp.setUserID(user.getUserId());
					resp.setUserAlias(user.getUserAlias());
					//If the setting FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED=false, its LogonDate() will not be null. You can refer to TptokenService to search for LogonDate
					//若setting的FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED=false,它LogonDate()不會是null,可參考TptokenService搜尋LogonDate
					if(user.getLogonDate() != null) {
						resp.setFirstTimeLogin(false);
					}else {
						resp.setFirstTimeLogin(true);
					}
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
