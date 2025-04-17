package tpi.dgrv4.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoCusDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class GtwIdPTitleService {

	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;
	private DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao;

	@Autowired
	public GtwIdPTitleService(DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao, DgrGtwIdpInfoADao dgrGtwIdpInfoADao,
			DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao, DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao) {
		super();
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
		this.dgrGtwIdpInfoADao = dgrGtwIdpInfoADao;
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
		this.dgrGtwIdpInfoCusDao = dgrGtwIdpInfoCusDao;
	}

	public ResponseEntity<String> getTitle(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) {
		try {
			String clientId = httpReq.getParameter("client_id");
			String pageTitle = getTitle(idPType, clientId);

			return ResponseEntity.ok(pageTitle);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	public String getTitle(String idPType, String clientId) throws Exception {
		String pageTitle = IdPHelper.DEFULT_PAGE_TITLE;// 預設值

		// 沒有 client_id
		if (!StringUtils.hasLength(clientId)) {
			// 缺少必填參數 '%s'
			String errMsg = String.format(IdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "client_id");
			TPILogger.tl.debug(errMsg);
			return pageTitle;
		}

		if (DgrIdPType.LDAP.equals(idPType)) {// LDAP
			// 查詢狀態為 "Y", 且建立時間最新的
			DgrGtwIdpInfoL dgrGtwIdpInfoL = getDgrGtwIdpInfoLDao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(clientId, "Y");
			if (dgrGtwIdpInfoL != null && StringUtils.hasLength(dgrGtwIdpInfoL.getPageTitle())) {
				pageTitle = dgrGtwIdpInfoL.getPageTitle();
			}

		} else if (DgrIdPType.API.equals(idPType)) {// API
			// 查詢狀態為 "Y", 且建立時間最新的
			DgrGtwIdpInfoA dgrGtwIdpInfoA = getDgrGtwIdpInfoADao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(clientId, "Y");
			if (dgrGtwIdpInfoA != null && StringUtils.hasLength(dgrGtwIdpInfoA.getPageTitle())) {
				pageTitle = dgrGtwIdpInfoA.getPageTitle();
			}

		} else if (DgrIdPType.JDBC.equals(idPType)) {// JDBC
			// 查詢狀態為 "Y", 且建立時間最新的
			DgrGtwIdpInfoJdbc dgrGtwIdpInfoJdbc = getDgrGtwIdpInfoJdbcDao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(clientId, "Y");
			if (dgrGtwIdpInfoJdbc != null && StringUtils.hasLength(dgrGtwIdpInfoJdbc.getPageTitle())) {
				pageTitle = dgrGtwIdpInfoJdbc.getPageTitle();
			}
		} else if (DgrIdPType.CUS.equals(idPType)) {// CUS
			// 查詢狀態為 "Y", 且建立時間最新的
			Optional<DgrGtwIdpInfoCus> opt = getDgrGtwIdpInfoCusDao()
					.findFirstByClientIdAndStatusOrderByGtwIdpInfoCusIdDesc(clientId, "Y");

			if (opt.isPresent()) {
				DgrGtwIdpInfoCus dgrGtwIdpInfoCus = opt.get();
				if (StringUtils.hasLength(dgrGtwIdpInfoCus.getPageTitle())) {
					pageTitle = dgrGtwIdpInfoCus.getPageTitle();
				}
			}
		}

		return pageTitle;
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {
		return dgrGtwIdpInfoADao;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}

	protected DgrGtwIdpInfoCusDao getDgrGtwIdpInfoCusDao() {
		return dgrGtwIdpInfoCusDao;
	}

}
