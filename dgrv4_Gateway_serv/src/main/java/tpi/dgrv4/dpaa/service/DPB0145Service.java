package tpi.dgrv4.dpaa.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0145Req;
import tpi.dgrv4.dpaa.vo.DPB0145Resp;
import tpi.dgrv4.dpaa.vo.DPB0145RespItem;
import tpi.dgrv4.dpaa.vo.DPB0146Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.constant.DgrAcIdpUserStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0145Service {

	private TPILogger logger = TPILogger.tl;
	
	private DPB0146Service dPB0146Service;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private ObjectMapper objectMapper;
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public DPB0145Service(DPB0146Service dPB0146Service, DgrAcIdpUserDao dgrAcIdpUserDao, ObjectMapper objectMapper,
			TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.dPB0146Service = dPB0146Service;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.objectMapper = objectMapper;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	public DPB0145Resp queryIdPUserList(TsmpAuthorization auth, DPB0145Req req) {
		DPB0145Resp resp = new DPB0145Resp();
		try {
			// 只能查詢自己本身組織(含底下的)的成員(2023.08.07)
			String orgId = auth.getOrgId();
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId,
					Integer.MAX_VALUE);
			List<DgrAcIdpUser> list = getDgrAcIdpUserDao().queryAllByOrgList(orgDescList);
			if (CollectionUtils.isEmpty(list)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<DPB0145RespItem> dataList = new ArrayList<>();
			for (DgrAcIdpUser userVo : list) {
				IdTokenData idTokenData = IdTokenUtil.getIdTokenData(userVo.getIdTokenJwtstr());
				String picture = idTokenData.userPicture;
				String statusName = DgrAcIdpUserStatus.getText(userVo.getUserStatus());

				DPB0145RespItem itemVo = new DPB0145RespItem();
				itemVo.setIcon(picture);
				itemVo.setId(RandomSeqLongUtil.toHexString(userVo.getAcIdpUserId(), RandomLongTypeEnum.YYYYMMDD));
				itemVo.setIdpType(userVo.getIdpType());
				itemVo.setLongId(String.valueOf(userVo.getAcIdpUserId()));
				itemVo.setStatus(userVo.getUserStatus());
				itemVo.setUserAlias(userVo.getUserAlias());
				itemVo.setUserName(userVo.getUserName());
				itemVo.setUserNameOrig(userVo.getUserName());
				itemVo.setStatusName(statusName);

				Map<String, List<String>> map = getDpb0146Service().getRoleData(userVo.getUserName());
				List<String> roleIdList = map.get("roleIdList");
				List<String> roleAliasList = map.get("roleAliasList");
				if (userVo.getOrgId() != null) {
					itemVo.setOrgId(userVo.getOrgId());
					Optional<TsmpOrganization> orgOpt = getTsmpOrganizationDao().findById(userVo.getOrgId());
					if (orgOpt.isPresent()) {
						itemVo.setOrgName(orgOpt.get().getOrgName());
					}
				}
				itemVo.setRoleId(roleIdList);
				itemVo.setRoleAlias(roleAliasList);

				processingBeforeResponse(itemVo);

				dataList.add(itemVo);
			}

			resp.setDataList(dataList);
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private void processingBeforeResponse(DPB0145RespItem resp) {

		if (resp == null) {
			return;
		}

		if (DgrIdPType.CUS.equalsIgnoreCase(resp.getIdpType())) {
			String userName = ServiceUtil.decodeBase64URL(resp.getUserName());
			resp.setUserName(userName);
		}

	}

	private String getIcon(String jwt) throws JsonMappingException, JsonProcessingException {
		if (StringUtils.hasText(jwt)) {
			String[] arrJwt = jwt.split("\\.");
			if (arrJwt.length == 3) {
				byte[] arrDecode = Base64Util.base64URLDecode(arrJwt[1]);
				String strDecode = new String(arrDecode, StandardCharsets.UTF_8);
				JsonNode rootNode = getObjectMapper().readTree(strDecode);
				JsonNode pictureNode = rootNode.get("picture");
				if (pictureNode != null) {
					return pictureNode.asText();
				} else {
					return null;
				}
			}
		}

		return jwt;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected DPB0146Service getDpb0146Service() {
		return dPB0146Service;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
