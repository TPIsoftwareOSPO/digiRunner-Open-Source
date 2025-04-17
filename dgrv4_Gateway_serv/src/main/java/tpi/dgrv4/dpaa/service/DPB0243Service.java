package tpi.dgrv4.dpaa.service;

import java.net.URL;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0243Req;
import tpi.dgrv4.dpaa.vo.DPB0243Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoCusDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0243Service {

	private DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao;
	private TsmpClientDao tsmpClientDao;

	@Autowired
	public DPB0243Service(DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao, TsmpClientDao tsmpClientDao) {
		super();
		this.dgrGtwIdpInfoCusDao = dgrGtwIdpInfoCusDao;
		this.tsmpClientDao = tsmpClientDao;
	}

	@Transactional
	public DPB0243Resp updateGtwIdPInfo(TsmpAuthorization authorization, DPB0243Req req) {

		try {
			checkParm(req);

			DgrGtwIdpInfoCus info = updateGtwIdPInfo(authorization.getUserName(), req);

			return getResp(info);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private DPB0243Resp getResp(DgrGtwIdpInfoCus dgrGtwIdpInfoCus) {

		DPB0243Resp resp = new DPB0243Resp();

		if (dgrGtwIdpInfoCus != null) {
			resp.setGtwIdpInfoCusId(
					RandomSeqLongUtil.toHexString(dgrGtwIdpInfoCus.getGtwIdpInfoCusId(), RandomLongTypeEnum.YYYYMMDD));

			resp.setClientId(dgrGtwIdpInfoCus.getClientId());
			resp.setStatus(dgrGtwIdpInfoCus.getStatus());
			resp.setCusLoginUrl(dgrGtwIdpInfoCus.getCusLoginUrl());
			resp.setCusUserDataUrl(dgrGtwIdpInfoCus.getCusUserDataUrl());
			resp.setCreateDateTime(dgrGtwIdpInfoCus.getCreateDateTime());
			resp.setCreateUser(dgrGtwIdpInfoCus.getCreateUser());
			resp.setUpdateDateTime(dgrGtwIdpInfoCus.getUpdateDateTime());
			resp.setUpdateUser(dgrGtwIdpInfoCus.getUpdateUser());
			
			resp.setPageTitle(dgrGtwIdpInfoCus.getPageTitle());
			resp.setIconFile(dgrGtwIdpInfoCus.getIconFile());
		}

		return resp;
	}

	private DgrGtwIdpInfoCus updateGtwIdPInfo(String username, DPB0243Req req) {

		String gtwIdpInfoCusId = req.getGtwIdpInfoCusId();
		String clientId = req.getClientId();
		Long id = getGtwIdpInfoCusId(gtwIdpInfoCusId);

		Optional<DgrGtwIdpInfoCus> opt = getDgrGtwIdpInfoCusDao().findByGtwIdpInfoCusIdAndClientId(id, clientId);
		if (opt.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		String status = req.getStatus();
		String cusLoginUrl = req.getCusLoginUrl();
		String cusUserDataUrl = req.getCusUserDataUrl();
		String pageTitle = req.getPageTitle();
		String iconFile = req.getIconFile();

		DgrGtwIdpInfoCus info = opt.get();

		info.setStatus(status);
		info.setCusLoginUrl(cusLoginUrl);
		info.setCusUserDataUrl(cusUserDataUrl);

		info.setPageTitle(pageTitle);
		info.setIconFile(iconFile);

		info.setUpdateDateTime(DateTimeUtil.now());
		info.setUpdateUser(username);

		return getDgrGtwIdpInfoCusDao().save(info);
	}

	private Long getGtwIdpInfoCusId(String gtwIdpInfoCusId) {

		if (!StringUtils.hasText(gtwIdpInfoCusId)) {
			throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
		}

		return isValidLong(gtwIdpInfoCusId) ? Long.parseLong(gtwIdpInfoCusId)
				: RandomSeqLongUtil.toLongValue(gtwIdpInfoCusId);
	}

	private boolean isValidLong(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}

		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void checkParm(DPB0243Req req) {

		String status = req.getStatus();
		String cusLoginUrl = req.getCusLoginUrl();
		String cusUserDataUrl = req.getCusUserDataUrl();

		String clientId = req.getClientId();

		if (clientId == null || clientId.isEmpty()) {
			throw TsmpDpAaRtnCode._1350.throwing("clientId");
		} else {
			TsmpClient client = getTsmpClientDao().findFirstByClientId(clientId);
			if (client == null) {
				throw TsmpDpAaRtnCode._1344.throwing();
			}
		}

		// 檢查 status
		if (status == null || status.isEmpty()) {
			throw TsmpDpAaRtnCode._1350.throwing("status");
		} else if (!status.equals("Y") && !status.equals("N")) {
			throw TsmpDpAaRtnCode._1352.throwing("status");
		}

		// 檢查 cusLoginUrl
		validateUrl(cusLoginUrl, "cusLoginUrl");

		// 檢查 cusUserDataUrl
		validateUrl(cusUserDataUrl, "cusUserDataUrl");

		String pageTitle = req.getPageTitle();
		if (pageTitle != null && pageTitle.length() > 400) {
			throw TsmpDpAaRtnCode._1351.throwing("pageTitle", 400 + "", pageTitle.length() + "");
		}

		String iconFile = req.getIconFile();
		if (iconFile != null && iconFile.length() > 4000) {
			throw TsmpDpAaRtnCode._1351.throwing("iconFile", 4000 + "", iconFile.length() + "");
		}

	}

	private void validateUrl(String url, String fieldName) {
		if (url == null || url.isEmpty()) {
			throw TsmpDpAaRtnCode._1350.throwing(fieldName);
		} else if (url.length() > 2000) {
			throw TsmpDpAaRtnCode._1351.throwing(fieldName, 2000 + "", url.length() + "");
		} else if (!isValidUrl(url)) {
			throw TsmpDpAaRtnCode._1352.throwing(fieldName);
		}
	}

	private boolean isValidUrl(String url) {
		try {
			new URL(url).toURI();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected DgrGtwIdpInfoCusDao getDgrGtwIdpInfoCusDao() {
		return dgrGtwIdpInfoCusDao;
	}
}
