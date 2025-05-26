package tpi.dgrv4.dpaa.service;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0222Req;
import tpi.dgrv4.dpaa.vo.DPB0222Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0222Service {

	private DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0222Service(DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao) {
		super();
		this.dgrAcIdpInfoCusDao = dgrAcIdpInfoCusDao;
	}

	@Transactional
	public DPB0222Resp createIdPInfo_cus(TsmpAuthorization authorization, DPB0222Req req) {

		try {

			checkParm(req);

			DgrAcIdpInfoCus info = createIdPInfo(authorization.getUserName(), req);

			return getResp(info);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private DPB0222Resp getResp(DgrAcIdpInfoCus dgrAcIdpInfoCus) {
		DPB0222Resp resp = new DPB0222Resp();

		if (dgrAcIdpInfoCus != null) {

			resp.setCusId( //
					RandomSeqLongUtil.toHexString( //
							dgrAcIdpInfoCus.getAcIdpInfoCusId(), RandomLongTypeEnum.YYYYMMDD));

			resp.setCusName(dgrAcIdpInfoCus.getAcIdpInfoCusName());
			resp.setCusStatus(dgrAcIdpInfoCus.getCusStatus());
			resp.setCusLoginUrl(dgrAcIdpInfoCus.getCusLoginUrl());
			resp.setCusBackendLoginUrl(dgrAcIdpInfoCus.getCusBackendLoginUrl());
			resp.setCusUserDataUrl(dgrAcIdpInfoCus.getCusUserDataUrl());
			resp.setCreateDateTime(dgrAcIdpInfoCus.getCreateDateTime());
			resp.setCreateUser(dgrAcIdpInfoCus.getCreateUser());
			resp.setUpdateDateTime(dgrAcIdpInfoCus.getUpdateDateTime());
			resp.setUpdateUser(dgrAcIdpInfoCus.getUpdateUser());
		}

		return resp;
	}

	private DgrAcIdpInfoCus createIdPInfo(String username, DPB0222Req req) {

		String cusName = req.getCusName();
		String cusStatus = req.getCusStatus();
		String cusLoginUrl = req.getCusLoginUrl();
		String cusBackendLoginUrl = req.getCusBackendLoginUrl();
		String cusUserDataUrl = req.getCusUserDataUrl();

		DgrAcIdpInfoCus info = new DgrAcIdpInfoCus();

		info.setAcIdpInfoCusName(cusName);
		info.setCusStatus(cusStatus);
		info.setCusLoginUrl(cusLoginUrl);
		info.setCusBackendLoginUrl(cusBackendLoginUrl);
		info.setCusUserDataUrl(cusUserDataUrl);
		info.setCreateUser(username);

		info = getDgrAcIdpInfoCusDao().save(info);
		return info;
	}

	private void checkParm(DPB0222Req req) {

		String cusName = req.getCusName();
		String cusStatus = req.getCusStatus();
		String cusLoginUrl = req.getCusLoginUrl();
		String cusBackendLoginUrl = req.getCusBackendLoginUrl();
		String cusUserDataUrl = req.getCusUserDataUrl();

		// 檢查 cusName
		if (cusName != null && cusName.length() > 100) {
			throw TsmpDpAaRtnCode._1351.throwing("cusName", 100 + "", cusName.length() + "");
		}

		// 檢查 cusStatus
		if (cusStatus == null || cusStatus.isEmpty()) {
			throw TsmpDpAaRtnCode._1350.throwing("cusStatus");
		} else if (!cusStatus.equals("Y") && !cusStatus.equals("N")) {
			throw TsmpDpAaRtnCode._1352.throwing("cusStatus");
		}

		// 檢查 cusLoginUrl
		validateUrl(cusLoginUrl, "cusLoginUrl");

		// 檢查 cusBackendLoginUrl
		validateUrl(cusBackendLoginUrl, "cusBackendLoginUrl");

		// 檢查 cusUserDataUrl
		validateUrl(cusUserDataUrl, "cusUserDataUrl");

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

	protected DgrAcIdpInfoCusDao getDgrAcIdpInfoCusDao() {
		return dgrAcIdpInfoCusDao;
	}

}
