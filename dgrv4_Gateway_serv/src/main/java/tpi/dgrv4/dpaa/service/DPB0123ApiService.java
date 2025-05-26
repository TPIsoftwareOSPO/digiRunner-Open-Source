package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0123ApiResp;
import tpi.dgrv4.entity.entity.LdapAuthResult;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.LdapAuthResultDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0123ApiService {

	private TPILogger logger = TPILogger.tl;

	private LdapAuthResultDao ldapAuthResultDao;
	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeDao tsmpRtnCodeDao;

	@Autowired
	public DPB0123ApiService(LdapAuthResultDao ldapAuthResultDao, TsmpSettingService tsmpSettingService,
			TsmpRtnCodeDao tsmpRtnCodeDao) {
		super();
		this.ldapAuthResultDao = ldapAuthResultDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
	}
	
	public DPB0123ApiResp doubleCheckLogin(HttpServletRequest req) {
		Map<String, String> parameters = new HashMap<>();
		req.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				parameters.put(k, vs[0]);
			}
		});

		return doubleCheckLogin(parameters);
	}

	protected DPB0123ApiResp doubleCheckLogin(Map<String, String> parameters) {

		DPB0123ApiResp resp = null;

		// check param
		resp = checkParam(parameters);
		if (resp != null) {
			return resp;
		}

		String userName = parameters.get("username");
		String codeChallenge = parameters.get("codeChallenge");
		String locale = parameters.get("locale");

		try {
			String flagName = null;
			Integer timeout = null;
			try {
				flagName = TsmpSettingDao.Key.SSO_TIMEOUT;
				timeout = getTsmpSettingService().getVal_SSO_TIMEOUT();
			} catch (TsmpDpAaException e) {
				this.logger.debug("查無資料:{}" + flagName);
				String rtnCode = "1298";
				TsmpRtnCode tsmpRtnCode = qureyRtnCodeByPk(rtnCode, locale);
				resp = new DPB0123ApiResp();
				resp.setCode(tsmpRtnCode.getTsmpRtnCode());
				resp.setMessage(tsmpRtnCode.getTsmpRtnMsg() + ":" + flagName);
				resp.setLogin("N");
				return resp;
			}

			// 確認 User 在 N 分鐘內是否有登入
			boolean isLogin = isLogin(userName, codeChallenge, timeout);

			resp = new DPB0123ApiResp();
			if (!isLogin) {
				String rtnCode = "1510";
				TsmpRtnCode dpb0123api_tsmpRtnCode = qureyRtnCodeByPk(rtnCode, locale);
				resp.setCode(dpb0123api_tsmpRtnCode.getTsmpRtnCode());
				resp.setMessage(dpb0123api_tsmpRtnCode.getTsmpRtnMsg());
				resp.setLogin("N");
			} else {
				String rtnCode = "1100";
				TsmpRtnCode dpb0123api_tsmpRtnCode = qureyRtnCodeByPk(rtnCode, locale);
				resp.setCode("0");
				resp.setMessage(dpb0123api_tsmpRtnCode.getTsmpRtnMsg());
				resp.setLogin("Y");
			}

			return resp;

		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			resp = new DPB0123ApiResp();
			String rtnCode = "1297";
			TsmpRtnCode tsmpRtnCode = qureyRtnCodeByPk(rtnCode, locale);
			resp.setCode(rtnCode);
			resp.setMessage(tsmpRtnCode.getTsmpRtnMsg() + ":" + e.getMessage());
			resp.setLogin("N");

			return resp;
		}
	}

	/**
	 * 確認 User 在 N 分鐘內是否有登入, 比對資料庫是否有匹配的 User Name 和 處理過的 UUID (需為最新的, 且只能用一次)
	 * 
	 * @param userName
	 * @param codeChallenge
	 * @param timeout
	 * @return
	 */
	private boolean isLogin(String userName, String codeChallenge, int timeout) {
		LocalDateTime now = LocalDateTime.now();
		Date queryStartDate = getQueryStartDate(now, timeout);// 取得 now - N分鐘

		this.logger.debug("userName:" + userName);
		this.logger.debug("codeChallenge:" + codeChallenge);
		this.logger.debug("timeout:" + timeout);
		this.logger.debug("now:" + now);
		this.logger.debug("queryStartDate:" + queryStartDate);

		List<LdapAuthResult> list = getLdapAuthResultDao()
				.findByUserNameAndCreateDateTimeAfterOrderByCreateDateTimeDesc(userName, queryStartDate);
		if (CollectionUtils.isEmpty(list)) {// 在N分鐘內,查無UUID資料
			// 資料表 LDAP_AUTH_RESULT 沒有匹配的 User Name 和 處理過的 UUID"
			this.logger.debug("Data table LDAP_AUTH_RESULT has no matching User Name and processed UUID");
			return false;
		}

		LdapAuthResult result = list.get(0);// 取得最新的資料
		if (result == null) {
			return false;
		}

		if (!result.getCodeChallenge().equals(codeChallenge)) {// 傳入的UUID不是最新的,或查無傳入的UUID資料
			// 資料表 LDAP_AUTH_RESULT 沒有匹配的 User Name 和 處理過的 UUID, 或不是最新的 UUID
			this.logger.debug("Data table LDAP_AUTH_RESULT does not match User Name "
					+ "and processed UUID, or is not the latest UUID");
			return false;
		}

		if (result.getUseDateTime() != null) {// 傳入的UUID,已被使用過
			// 資料表 LDAP_AUTH_RESULT 的 User Name 和 處理過的 UUID, 已被使用過
			this.logger
					.debug("The User Name and the processed UUID of the data table LDAP_AUTH_RESULT, have been used");
			return false;
		}

		// 資料表 LDAP_AUTH_RESULT 的 User Name 和 處理過的 UUID, 找到匹配的資料
		this.logger.debug("User Name and processed UUID of data table LDAP_AUTH_RESULT, find matching data");

		// 更新這筆資料的 use_date_time = 現在時間, 表示已使用
		result.setUseDateTime(DateTimeUtil.now());
		getLdapAuthResultDao().saveAndFlush(result);
		return true;
	}

	private DPB0123ApiResp checkParam(Map<String, String> parameters) {
		String userName = parameters.get("username");
		String codeChallenge = parameters.get("codeChallenge");
		String locale = parameters.get("locale");

		if (!StringUtils.hasLength(locale)) {
			DPB0123ApiResp resp = new DPB0123ApiResp();
			resp.setCode("1296");
			resp.setMessage("Missing required parameter: [{{locale}}]");
			resp.setLogin("N");
			return resp;
		}

		if (!StringUtils.hasLength(userName)) {
			String rtnCode = "1350";
			TsmpRtnCode tsmpRtnCode = qureyRtnCodeByPk(rtnCode, locale);
			String msg = tsmpRtnCode.getTsmpRtnMsg();
			msg = msg.replace("{{0}}", "{{username}}");

			DPB0123ApiResp resp = new DPB0123ApiResp();
			resp.setCode(tsmpRtnCode.getTsmpRtnCode());
			resp.setMessage(msg);
			resp.setLogin("N");
			return resp;
		}

		if (!StringUtils.hasLength(codeChallenge)) {
			String rtnCode = "1350";
			TsmpRtnCode tsmpRtnCode = qureyRtnCodeByPk(rtnCode, locale);
			String msg = tsmpRtnCode.getTsmpRtnMsg();
			msg = msg.replace("{{0}}", "{{codeChallenge}}");

			DPB0123ApiResp resp = new DPB0123ApiResp();
			resp.setCode(tsmpRtnCode.getTsmpRtnCode());
			resp.setMessage(msg);
			resp.setLogin("N");
			return resp;
		}

		return null;
	}

	protected Date getQueryStartDate(LocalDateTime ldt, int minute) {
		Date dpb0123api_date = minusMinute(ldt, minute);
		return dpb0123api_date;
	}

	/**
	 * 減N分鐘
	 * 
	 * @param dt
	 * @param days
	 * @return
	 */
	private Date minusMinute(LocalDateTime ldt, int minute) {
		ldt = ldt.minusMinutes(minute);
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	private TsmpRtnCode qureyRtnCodeByPk(String rtnCode, String locale) {
		TsmpRtnCode dpb0123api_tsmpRtnCode = null;
		TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode, locale);
		Optional<TsmpRtnCode> opt_rtnCode = getTsmpRtnCodeDao().findById(id);

		// 若查不到資料則拋出 1298 錯誤。
		if (!opt_rtnCode.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		} else {
			dpb0123api_tsmpRtnCode = opt_rtnCode.get();
		}

		return dpb0123api_tsmpRtnCode;
	}

	protected LdapAuthResultDao getLdapAuthResultDao() {
		return ldapAuthResultDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return tsmpRtnCodeDao;
	}
}