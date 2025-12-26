package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0147Req;
import tpi.dgrv4.dpaa.vo.DPB0147Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.cache.proxy.AuthoritiesCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class DPB0147Service {

	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final TsmpOrganizationDao tsmpOrganizationDao;
	private final AuthoritiesDao authoritiesDao;
	private final TsmpRoleDao tsmpRoleDao;
	private final DgrAuditLogService dgrAuditLogService;
	private final TsmpUserDao tsmpUserDao;
	private final TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private final TsmpSettingService tsmpSettingService;
	private final DaoGenericCacheService daoGenericCacheService;
	private final AuthoritiesCacheProxy authoritiesCacheProxy;
	
	@Transactional
	public DPB0147Resp updateIdPUser(TsmpAuthorization auth, DPB0147Req req, InnerInvokeParam iip) {
		DPB0147Resp resp = new DPB0147Resp();
		try {

			// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
			boolean acIdpUsernameB64EncodeVal = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
			TPILogger.tl.debug("AC_IDP_USERNAME_B64_ENCODE : " + acIdpUsernameB64EncodeVal);
			
			req.switchCusIdpTypeUserName(acIdpUsernameB64EncodeVal);

			// 寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_IDP_USER.value());

			checkParam(req);

			DgrAcIdpUser userVo = getDgrAcIdpUserDao().findById(Long.valueOf(req.getLongId())).orElse(null);
			if (userVo == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
			boolean isCus = DgrIdPType.CUS.equals(req.getIdpType());//是否為 "CUS" IdP type

			String userName = req.getUserName();
			String newUserName = req.getNewUserName();
			boolean isUserNameDiffer = isUserNameDiffer(userName, newUserName, isCus, acIdpUsernameB64EncodeVal);
			String newOrgId = req.getNewOrgId();
			List<String> newRoleIdList = req.getNewRoleIdList();
			String orgId = req.getOrgId();
			List<String> roleIdList = req.getRoleIdList();
			boolean tokenFlag = false;
			String authName = auth.getUserNameForQuery();
			checkPermissions(authName, userName);

			if (!newOrgId.equals(orgId)) {
				tokenFlag = true;
				userVo.setOrgId(newOrgId);
			}
			if (isUserNameDiffer) {
				tokenFlag = true;
				// 檢查 userName是否與使用者帳號重複
				TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(newUserName);
				if (tsmpUser != null) {
					throw TsmpDpAaRtnCode._1540.throwing();
				}

				userVo.setUserName(newUserName);
				deleteAuthorities(userName, iip); // 先刪除舊username角色對應
				createAuthorities(newUserName, newRoleIdList, iip); // 新增新角色對應

			} else {
				if (this.isRoleIdDiffer(roleIdList, newRoleIdList)) {
					tokenFlag = true;
					deleteAuthorities(userName, iip);// 先刪除舊角色對應

					createAuthorities(userName, newRoleIdList, iip);// 新增新角色對應
				}
				userVo.setUserName(userName);
			}
			updateIdPUserTable(auth, userVo, req, iip);

//			(若ROFILEUPDATE_INVALIDATE_TOKEN為true && 有異動到TSMP_USER資料)則要註銷Token
			boolean settingFalg = getTsmpSettingService().getVal_PROFILEUPDATE_INVALIDATE_TOKEN();
			if (settingFalg && tokenFlag) {
				invalidateToken(userName);
			}

			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	protected void checkPermissions(String authName, String userName) {
		boolean settingFlag = getTsmpSettingService().getVal_USER_UPDATE_BY_SELF();
		List<Authorities> authoritiesList = getAuthoritiesCacheProxy().findByUsername(authName);
		boolean roleFlag = authoritiesList.stream().anyMatch(authorities -> "1000".equals(authorities.getAuthority()));
		// ADMIN角色可修改自己 &&是否允許修正自身帳號(預設是可以) && 自己不可異動自己的資訊
		if (!roleFlag && settingFlag && authName.equals(userName)) {
			throw TsmpDpAaRtnCode._1219.throwing();
		}
	}

	private void createAuthorities(String userName, List<String> newRoleIdList, InnerInvokeParam iip) {

		newRoleIdList.forEach(roleId -> {
			Authorities authVo = new Authorities();
			authVo.setUsername(userName);
			authVo.setAuthority(roleId);
			authVo = getAuthoritiesDao().save(authVo);
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, Authorities.class.getSimpleName(),
					TableAct.C.value(), null, authVo);// C

		});
	}

	private void invalidateToken(String userName) {
		List<TsmpTokenHistory> tokenHistories = getTsmpTokenHistoryDao().findByUserName(userName);
		Date now = DateTimeUtil.now();
		String revokedStatus = null;
		String rftRevokedStatus = null;
		if (!CollectionUtils.isEmpty(tokenHistories)) {
			for (TsmpTokenHistory tsmpTokenHistory : tokenHistories) {

				revokedStatus = tsmpTokenHistory.getRevokedStatus();
				if (!StringUtils.hasLength(revokedStatus)) {
					tsmpTokenHistory.setRevokedStatus("R");
					tsmpTokenHistory.setRevokedAt(now);
				}

				rftRevokedStatus = tsmpTokenHistory.getRftRevokedStatus();
				if (!StringUtils.hasLength(rftRevokedStatus)) {
					tsmpTokenHistory.setRftRevokedStatus("R");
					tsmpTokenHistory.setRftRevokedAt(now);
				}

			}

			getTsmpTokenHistoryDao().saveAllAndFlush(tokenHistories);
			// 清除快取
			getDaoGenericCacheService().clearAndNotify();
		}
	}

	private void deleteAuthorities(String userName, InnerInvokeParam iip) {
		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
		getAuthoritiesDao().deleteAll(authoritiesList);
		if (iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (Authorities authorities : authoritiesList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, authorities); // 舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, Authorities.class.getSimpleName(),
						TableAct.D.value(), oldRowStr, null);// D
			}
		}

	}

	private void updateIdPUserTable(TsmpAuthorization auth, DgrAcIdpUser userVo, DPB0147Req req, InnerInvokeParam iip) {
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, userVo); // 舊資料統一轉成 String
		userVo.setUserAlias(req.getNewUserAlias());
		userVo.setUserStatus(req.getNewStatus());
		userVo.setUserEmail(req.getNewUserEmail());
		userVo.setIdpType(req.getNewIdpType());
		userVo.setUpdateUser(auth.getUserName());
		userVo.setUpdateDateTime(DateTimeUtil.now());
		getDgrAcIdpUserDao().save(userVo);

		// 寫入 Audit Log D
		String lineNumber3 = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber3, DgrAcIdpUser.class.getSimpleName(),
				TableAct.U.value(), oldRowStr, userVo);// U

	}

	private boolean isUserNameDiffer(String userName, String newUserName, boolean isCus, boolean acIdpUsernameB64EncodeVal) {
		if (!userName.equals(newUserName)) {
			List<DgrAcIdpUser> list = getDgrAcIdpUserDao().findByUserName(newUserName);
			if (list.size() > 0) {
				if(isCus || acIdpUsernameB64EncodeVal){
					newUserName = ServiceUtil.decodeBase64URL(newUserName);
				}
				
				throw TsmpDpAaRtnCode._1353.throwing("{{userName}}", newUserName);
			}
			return true;
		}
		return false;

	}

	public boolean isRoleIdDiffer(List<String> roleIdList, List<String> newRoleIdList) {
		if (roleIdList.size() != newRoleIdList.size()) {
			return true;
		} else if (!newRoleIdList.containsAll(roleIdList)) {
			return true;
		} else {
			return false;
		}
	}

	private void checkParam(DPB0147Req req) {
		String newOrgId = req.getNewOrgId();
		List<String> newRoleIdList = req.getNewRoleIdList();
		String longId = req.getLongId();

		if (!StringUtils.hasText(longId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		if (!StringUtils.hasText(newOrgId)) {
			// 組織名稱:必填參數
			throw TsmpDpAaRtnCode._1250.throwing();
		}

		if (newRoleIdList == null || newRoleIdList.size() == 0) {
			// 角色清單:必填參數
			throw TsmpDpAaRtnCode._1249.throwing();
		}

		if (newOrgId.length() > 30) {
			// 組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1253.throwing("30", String.valueOf(newOrgId.length()));
		}

		Optional<TsmpOrganization> orgOptional = getTsmpOrganizationDao().findById(newOrgId);
		if (!orgOptional.isPresent()) {
			// 組織名稱不存在
			throw TsmpDpAaRtnCode._1229.throwing();
		}

		newRoleIdList.forEach(roleId -> {
			Optional<TsmpRole> roleOptional = getTsmpRoleDao().findById(roleId);
			if (!roleOptional.isPresent()) {
				// 角色不存在
				throw TsmpDpAaRtnCode._1230.throwing();
			}
		});
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

	protected AuthoritiesCacheProxy getAuthoritiesCacheProxy() {
		return authoritiesCacheProxy;
	}

}
