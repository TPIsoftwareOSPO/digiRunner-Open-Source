package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0146Req;
import tpi.dgrv4.dpaa.vo.DPB0146Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.constant.DgrAcIdpUserStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class DPB0146Service {

	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final TsmpOrganizationDao tsmpOrganizationDao;
	private final AuthoritiesDao authoritiesDao;
	private final TsmpRoleDao tsmpRoleDao;
	private final TsmpSettingService tsmpSettingService;

	public DPB0146Resp queryIdPUserDetail(TsmpAuthorization auth, DPB0146Req req) {
		DPB0146Resp resp = new DPB0146Resp();
		try {

			if (!StringUtils.hasText(req.getLongId())) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			DgrAcIdpUser userVo = getDgrAcIdpUserDao().findById(Long.valueOf(req.getLongId())).orElse(null);
			if (userVo == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}

			if (userVo.getOrgId() != null) {
				resp.setOrgId(userVo.getOrgId());
				Optional<TsmpOrganization> orgOpt = getTsmpOrganizationDao().findById(userVo.getOrgId());
				if (orgOpt.isPresent()) {
					resp.setOrgName(orgOpt.get().getOrgName());
				}
			}

			Map<String, List<String>> map = getRoleData(userVo.getUserName());
			List<String> roleIdList = map.get("roleIdList");
			List<String> roleAliasList = map.get("roleAliasList");

			String statusName = DgrAcIdpUserStatus.getText(userVo.getUserStatus());

			resp.setRoleId(roleIdList);
			resp.setRoleAlias(roleAliasList);
			resp.setLongId(req.getLongId());
			resp.setIdpType(userVo.getIdpType());
			resp.setStatus(userVo.getUserStatus());
			resp.setStatusName(statusName);
			resp.setUserAlias(userVo.getUserAlias());
			resp.setUserName(userVo.getUserName());
			resp.setUserNameOrig(userVo.getUserName());
			resp.setUserEmail(userVo.getUserEmail());

			processingBeforeResponse(resp);

			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private void processingBeforeResponse(DPB0146Resp resp) {

		if (resp == null) {
			return;
		}

		// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
		boolean acIdpUsernameB64EncodeValue = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
		TPILogger.tl.debug("AC_IDP_USERNAME_B64_ENCODE : " + acIdpUsernameB64EncodeValue);
		
		if (DgrIdPType.CUS.equalsIgnoreCase(resp.getIdpType()) || acIdpUsernameB64EncodeValue) {
			String userName = ServiceUtil.decodeBase64URL(resp.getUserName());
			resp.setUserName(userName);
		}
	}

	public Map<String, List<String>> getRoleData(String userName) {
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
				TsmpRole tsmpRole = optRole.get();
				roleIdList.add(tsmpRole.getRoleId());

				String roleAlias = tsmpRole.getRoleAlias();
				if (StringUtils.hasLength(roleAlias)) {
					roleAliasList.add(roleAlias);

				} else {
					String msg = "unknow:" + tsmpRole.getRoleName();
					roleAliasList.add(msg);
				}
			}
		});

		map.put("roleIdList", roleIdList);
		map.put("roleAliasList", roleAliasList);
		return map;
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
}
