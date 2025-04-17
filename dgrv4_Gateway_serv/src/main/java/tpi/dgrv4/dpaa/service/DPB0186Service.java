package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0186Req;
import tpi.dgrv4.dpaa.vo.DPB0186Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.constant.DgrIdPReqBodyType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0186Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;
	private TsmpClientDao tsmpClientDao;

	@Autowired
	public DPB0186Service(DgrGtwIdpInfoADao dgrGtwIdpInfoADao, TsmpClientDao tsmpClientDao) {
		super();
		this.dgrGtwIdpInfoADao = dgrGtwIdpInfoADao;
		this.tsmpClientDao = tsmpClientDao;
	}

	public DPB0186Resp createGtwIdPInfo_api(TsmpAuthorization authorization, DPB0186Req req) {
		DPB0186Resp resp = new DPB0186Resp();
		try {
			checkParm(req);
			DgrGtwIdpInfoA a = new DgrGtwIdpInfoA();
			a.setApiMethod(req.getApiMethod());
			a.setApiUrl(req.getApiUrl());
			a.setClientId(req.getClientId());
			a.setCreateUser(authorization.getUserName());
			String icon = StringUtils.hasLength(req.getIconFile()) ? req.getIconFile() : IdPHelper.DEFULT_ICON_FILE;
			a.setIconFile(icon);
			a.setIdtEmail(req.getIdtEmail());
			a.setIdtName(req.getIdtName());
			a.setIdtPicture(req.getIdtPicture());
			a.setPageTitle(req.getPageTitle());
			a.setRemark(req.getRemark());
			a.setReqBody(req.getReqBody());
			a.setReqBodyType(req.getReqBodyType());
			a.setReqHeader(req.getReqHeader());
			a.setStatus(req.getStatus());
			a.setSucByField(req.getSucByField());
			a.setSucByType(req.getSucByType());
			a.setSucByValue(req.getSucByValue());
			a.setIdtLightId(req.getIdtLightId());
			a.setIdtRoleName(req.getIdtRoleName());
			getDgrGtwIdpInfoADao().save(a);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	private void checkParm(DPB0186Req req) {
		String clientId = req.getClientId();
		if (!StringUtils.hasLength(clientId)) {
			throw TsmpDpAaRtnCode._2025.throwing("{{clientId}}");
		}
		TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
		if (client == null) {
			throw TsmpDpAaRtnCode._1344.throwing();
		}
		String reqHeader = req.getReqHeader();
		if (StringUtils.hasLength(reqHeader)) {
			checkFormate(reqHeader, "request header");
		}
		
		String reqBodyType = req.getReqBodyType();
		String reqBody = req.getReqBody();
		if (DgrIdPReqBodyType.FORM_DATA.isValueEquals(reqBodyType) // F：form-data
				|| DgrIdPReqBodyType.X_WWW_FORM_URLENCODED.isValueEquals(reqBodyType)) { // X：x-www-form-urlencoded
			if (!StringUtils.hasLength(reqBody)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{request body}}");
			}
			checkFormate(reqBody, "request body");

		} else if (DgrIdPReqBodyType.RAW.isValueEquals(reqBodyType)) { // R：raw
			if (!StringUtils.hasLength(reqBody)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{request body}}");
			}
		}

		String sucByType = req.getSucByType();
		if ("R".equals(sucByType)) {
			if (!StringUtils.hasLength(req.getSucByField())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{sucByField}}");
			}
			if (!StringUtils.hasLength(req.getSucByValue())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{sucByValue}}");
			}
		}
	}

	private void checkFormate(String json, String filed) {
		try {
			ObjectMapper om = new ObjectMapper();
			List<Map<String, String>> mapList = om.readValue(json, new TypeReference<List<Map<String, String>>>() {
			});
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1352.throwing("{{" + filed + "}}");
		}
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {

		return dgrGtwIdpInfoADao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;

	}
}
