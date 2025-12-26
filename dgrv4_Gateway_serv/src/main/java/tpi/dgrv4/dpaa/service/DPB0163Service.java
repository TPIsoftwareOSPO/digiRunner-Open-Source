package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0163Req;
import tpi.dgrv4.dpaa.vo.DPB0163Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
/**
 * 建立 AC IdP 使用者的資料
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class DPB0163Service {
	private final AuthoritiesDao authoritiesDao;
	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final DgrAuditLogService dgrAuditLogService;
	private final TsmpUserDao tsmpUserDao;
	private final TsmpSettingService tsmpSettingService;

	@Transactional
	public DPB0163Resp createIdPUser(TsmpAuthorization authorization, DPB0163Req req, InnerInvokeParam iip) {
		DPB0163Resp resp = new DPB0163Resp();
		// 寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_IDP_USER.value());

		try {
			List<String> roleIdList = req.getRoleIdList();
			String userName = req.getUserName();
			
			// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
			String acIdpUsernameB64EncodeKey = getTsmpSettingService().getKey_AC_IDP_USERNAME_B64_ENCODE();
			boolean acIdpUsernameB64EncodeValue = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
			TPILogger.tl.debug(acIdpUsernameB64EncodeKey + " : " + acIdpUsernameB64EncodeValue);
			
			boolean isCus = DgrIdPType.CUS.equals(req.getIdpType());//是否為 "CUS" IdP type
			
			if (!isCus && !acIdpUsernameB64EncodeValue && userName.indexOf(".") > 0) {
				// "CUS" 以外的 IdP type 且 沒有打開編碼 且 包含"."
				// 使用者名稱 '%s', 不能包含 ".", 請確認 'AC_IDP_USERNAME_B64_ENCODE' 是否要設定''為'true'
				String errMsg = String.format(
						"Username '%s' cannot contain '.', Please confirm that Setting '%s' should be set to 'true'.",
						userName, acIdpUsernameB64EncodeKey);
				throw TsmpDpAaRtnCode._1559.throwing(errMsg);
			}
			
			if (isCus || acIdpUsernameB64EncodeValue) {
				// IdP type 為 "CUS" 或 有打開編碼
				userName = ServiceUtil.encodeBase64URL(userName);
			}

			List<DgrAcIdpUser> list = getDgrAcIdpUserDao().findByUserName(userName);
			if (list.size() > 0) {
				if(isCus || acIdpUsernameB64EncodeValue){
					userName = ServiceUtil.decodeBase64URL(userName);
				}
				throw TsmpDpAaRtnCode._1353.throwing("{{userName}}", userName);
			}
			
			// 檢查 userName是否與使用者帳號重複
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
			if (tsmpUser != null) {
				throw TsmpDpAaRtnCode._1540.throwing();
			}

			if (roleIdList != null && !roleIdList.isEmpty()) {
				for (String roleId : roleIdList) {
					addAuthoritiesTable(authorization, roleId, userName, iip);
				}
			}
			
			addDgrAcIdpUserTable(authorization, req, iip, userName);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	private void addAuthoritiesTable(TsmpAuthorization authorization, String roleId, String userName,
			InnerInvokeParam iip) {
		Authorities auth = new Authorities();
		auth.setAuthority(roleId);
		auth.setUsername(userName);
		auth = getAuthoritiesDao().save(auth);
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, Authorities.class.getSimpleName(), TableAct.C.value(),
				null, auth);// C

	}

	private void addDgrAcIdpUserTable(TsmpAuthorization authorization, DPB0163Req req, InnerInvokeParam iip, String userName) {
		DgrAcIdpUser dgrAcIdpUser = new DgrAcIdpUser();
		dgrAcIdpUser.setOrgId(req.getOrgId());
		dgrAcIdpUser.setUserAlias(req.getUserAlias());
		dgrAcIdpUser.setUserEmail(req.getUserEmail());
		dgrAcIdpUser.setUserStatus(req.getStatus());
		dgrAcIdpUser.setIdpType(req.getIdpType());
		dgrAcIdpUser.setUserName(userName);
		dgrAcIdpUser.setCreateUser(authorization.getUserName());
		dgrAcIdpUser = getDgrAcIdpUserDao().save(dgrAcIdpUser);
		// 寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, DgrAcIdpUser.class.getSimpleName(), TableAct.C.value(),
				null, dgrAcIdpUser);// C
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() { 
		return this.authoritiesDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}
}
