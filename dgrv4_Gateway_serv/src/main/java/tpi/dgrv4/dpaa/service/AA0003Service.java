package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0003Req;
import tpi.dgrv4.dpaa.vo.AA0003Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.constant.DgrAcIdpUserStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0003Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpUserDao tsmpUserDao;
	private UsersDao usersDao;
	private AuthoritiesDao authoritiesDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	
	@Autowired
	public AA0003Service(TsmpOrganizationDao tsmpOrganizationDao, TsmpUserDao tsmpUserDao, UsersDao usersDao,
			AuthoritiesDao authoritiesDao, TsmpRoleDao tsmpRoleDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpUserDao = tsmpUserDao;
		this.usersDao = usersDao;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	@Transactional
	public AA0003Resp queryTUserDetail(TsmpAuthorization auth, AA0003Req req, ReqHeader reqHeader) {
		AA0003Resp resp = null;
		try {
			String userId = ServiceUtil.nvl(req.getUserID());
			String userName = ServiceUtil.nvl(req.getUserName());

			if ("null".equalsIgnoreCase(userId) && StringUtils.hasText(userName)
					&& userName.toLowerCase().indexOf("b64.") == 0) {
				// 若userId為字串"null",且userName為"b64."開頭
				// 1.以 SSO AC IdP 的方式登入
				resp = new AA0003Resp();
				String idPType = auth.getIdpType();
				resp.setIdPType(idPType);

				if (DgrIdPType.LDAP.equals(idPType) // LDAP
						|| DgrIdPType.MLDAP.equals(idPType) // MLDAP
						|| DgrIdPType.API.equals(idPType) // API
						|| DgrIdPType.CUS.equals(idPType)) // CUS
				{
					String userNameForQuery = auth.getUserNameForQuery();
					DgrAcIdpUser idpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery,
							idPType);
					resp = getIdpUserData(idpUser, userNameForQuery, resp);

				} else {// GOOGLE 或 MS
					resp.setIdTokenJwtstr(auth.getIdTokenJwtstr());
				}

			} else {
				// 2.以 AC 方式登入
				if (!StringUtils.hasLength(userId) || !StringUtils.hasLength(userName)) {
					// 1296:缺少必填參數
					throw TsmpDpAaRtnCode._1296.throwing();
				}

				resp = qureyTUserByPk(userId, userName, reqHeader.getLocale());
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	public AA0003Resp qureyTUserByPk(String userId, String userName, String locale) throws Exception {
		AA0003Resp resp = null;
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);

		// 用"userID"與"userName"對TSMP_USER資料表(USER_ID與USER_NAME欄位)做查詢，若沒有找到資料就throw RTN
		// CODE 1231。
		if (tsmpUser == null) {
			throw TsmpDpAaRtnCode._1231.throwing();
		} else {
			resp = getAA0003Data(tsmpUser, locale);
		}

		return resp;
	}

	private AA0003Resp getAA0003Data(TsmpUser tsmpUser, String locale) {
		AA0003Resp resp = new AA0003Resp();
		String orgName = "";
		String userName = "";
		String createDateStr = "";
		String logonDateStr = "";

		Date createDate = tsmpUser.getCreateTime();
		Date logonDate = tsmpUser.getLogonDate();
		Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(createDate, null);
		Optional<String> logonDateOpt = DateTimeUtil.dateTimeToString(logonDate, null);

		if (createDateOpt.isPresent())
			createDateStr = createDateOpt.get();

		if (logonDateOpt.isPresent())
			logonDateStr = logonDateOpt.get();

		Optional<TsmpOrganization> orgOpt = getTsmpOrganizationDao().findById(tsmpUser.getOrgId());
		if (orgOpt.isPresent())
			orgName = orgOpt.get().getOrgName();

		String status = getItemsParam("ENABLE_FLAG", tsmpUser.getUserStatus(), locale);

		userName = tsmpUser.getUserName();

		Map<String, List<String>> map = getRoleData(userName);
		List<String> roleIdList = map.get("roleIdList");
		List<String> roleAliasList = map.get("roleAliasList");

		resp.setUserID(tsmpUser.getUserId());
		resp.setUserName(tsmpUser.getUserName());
		resp.setUserAlias(tsmpUser.getUserAlias());
		resp.setOrgName(orgName);

		resp.setRoleID(roleIdList);

		resp.setRoleAlias(roleAliasList); // 角色名稱
		resp.setUserMail(ServiceUtil.nvl(tsmpUser.getUserEmail()));
		resp.setLogonDate(logonDateStr);
		resp.setCreateDate(createDateStr);
		resp.setStatus(tsmpUser.getUserStatus());
		resp.setStatusName(status);
		resp.setPwdFailTimes(ServiceUtil.nvl(tsmpUser.getPwdFailTimes()));
		resp.setOrgId(tsmpUser.getOrgId());

		return resp;
	}

	private AA0003Resp getIdpUserData(DgrAcIdpUser idpUser, String locale, AA0003Resp resp) {
		if (idpUser == null) {
			throw TsmpDpAaRtnCode._1231.throwing();
		} else {
			String orgName = "";
			String userName = idpUser.getUserName();
			String orgId = idpUser.getOrgId();
			Optional<TsmpOrganization> orgOpt = getTsmpOrganizationDao().findById(orgId);
			if (orgOpt.isPresent()) {
				orgName = orgOpt.get().getOrgName();
			}

			Map<String, List<String>> map = getRoleData(userName);
			List<String> roleAliasList = map.get("roleAliasList");
			String statusName = DgrAcIdpUserStatus.getText(idpUser.getUserStatus());

			resp.setUserName(userName);
			resp.setOrgId(orgId);
			resp.setOrgName(orgName);
			resp.setRoleAlias(roleAliasList); // 角色名稱
			resp.setStatus(statusName);
		}
		return resp;
	}

	private String getItemsParam(String itemNo, String param1, String locale) {
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale(itemNo, param1, locale);
		if (dpItem == null) {
			return null;
		}
		String subitemName = dpItem.getSubitemName();
		return subitemName;
	}

	private Map<String, List<String>> getRoleData(String userName) {
		Map<String, List<String>> map = new HashMap<>();
		List<String> roleIdList = new ArrayList<>();
		List<String> roleAliasList = new ArrayList<>();

		// 迴圈裡面再跑query 資料庫 ->效率會不好,但是因為有做分頁所以跑的資料量不會太多+JAP有一級快取的功能所以在分頁做此動作可接受
		// 一級快取 -> 在還沒有commit之前 有搜尋過的資料會存在快取
		List<Authorities> authUserName = getAuthoritiesDao().findByUsername(userName);
		authUserName.forEach((auth) -> {
			String authId = auth.getAuthority();
			Optional<TsmpRole> optRole = getTsmpRoleDao().findById(authId);
			if (optRole.isPresent()) {
				roleIdList.add(optRole.get().getRoleId());
				String roleAlias = optRole.get().getRoleAlias();
				if (StringUtils.hasLength(roleAlias)) {
					roleAliasList.add(roleAlias);

				} else {
					String msg = "unknow:" + optRole.get().getRoleName();
					roleAliasList.add(msg);
				}
			}
		});

		map.put("roleIdList", roleIdList);
		map.put("roleAliasList", roleAliasList);
		return map;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected UsersDao getUsersDao() {
		return this.usersDao;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}
