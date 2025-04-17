package tpi.dgrv4.gateway.component.check;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Component
public class HostHeaderCheck implements ICheck {

	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeService tsmpRtnCodeService;

	@Autowired
	public HostHeaderCheck(TsmpSettingService tsmpSettingService, TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	/**
	 * 若 Host Header 不符合安全檢查就 true, 否則 false
	 */
	public boolean check(HttpServletRequest httpReq) {
		try {
			String hostHeader = httpReq.getHeader("host");
			String forwardHostHeader = httpReq.getHeader("X-Forwarded-Host");
			String reqURL = httpReq.getRequestURL().toString();
			String reqURI = httpReq.getRequestURI().toString();

			return check(hostHeader, forwardHostHeader, reqURL, reqURI);
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return true;
		}
	}

	public boolean check(String hostHeader, String forwardHostHeader, String reqURL, String reqURI) {
		String errMsg = null;

		// 1.若為不需要檢查的 URI, 則不檢查, 返回 false
		boolean isCheckUrl = isCheckUrl(reqURL, reqURI);
		if (!isCheckUrl) {
			return false;
		}

		// 2.沒有值則不檢查, 返回 false
		String dgrHostHeader = getTsmpSettingService().getVal_DGR_HOST_HEADER();
		if (!StringUtils.hasLength(dgrHostHeader)) {
			// Table [TSMP_SETTING] 沒有值
			errMsg = "Table [TSMP_SETTING] id = 'DGR_HOST_HEADER' has no value.";
			TPILogger.tl.debug(errMsg);
			return false;
		}

		// 若值為 "*" , 則不檢查, 返回 false
		if ("*".equals(dgrHostHeader)) {
			return false;
		}

		String[] dgrHostArr = dgrHostHeader.split(",");

		// 3.驗證 "host" 是否在白名單,如果不在則返回 true
		if (StringUtils.hasLength(hostHeader)) {// Header "host"有值才檢查
			boolean isMach = false;
			for (String hostVal : dgrHostArr) {
				if (hostHeader.equals(hostVal)) {// 比對 HTTP head 的 "host" 和 DB 中的值是否相同
					isMach = true;
					break;
				}
			}
			if (isMach == false) {
				errMsg = "..." + "Invalid header 'host': " + hostHeader;
				errMsg += "\n\t..." + "Table [TSMP_SETTING] 'DGR_HOST_HEADER' value: " + dgrHostHeader;
				errMsg += "\n\t..." + "Request URL: " + reqURL;
				TPILogger.tl.debug(errMsg);
				return true;
			}
		}

		// 4.驗證 "X-Forwarded-Host" 是否在白名單,如果不在則返回 true
		if (StringUtils.hasLength(forwardHostHeader)) {// Header "X-Forwarded-Host" 有值才檢查
			boolean isMach = false;
			for (String hostVal : dgrHostArr) {
				if (forwardHostHeader.equals(hostVal)) {// 比對 HTTP head 的 "X-Forwarded-Host" 和 DB 中的值是否相同
					isMach = true;
					break;
				}
			}
			if (isMach == false) {
				errMsg = "..." + "Invalid header 'X-Forwarded-Host': " + forwardHostHeader;
				errMsg += "\n\t..." + "Table [TSMP_SETTING] 'DGR_HOST_HEADER' value: " + dgrHostHeader;
				errMsg += "\n\t..." + "Request URL: " + reqURL;
				TPILogger.tl.debug(errMsg);
				return true;
			}
		}

		return false;
	}

	/**
	 * 判斷此 URI 是否要做安全檢查 <br>
	 * 例如以下為不用檢查: <br>
	 * https://127.0.0.1:8442/dgrv4/version <br>
	 * https://127.0.0.1:8440/editor/hello
	 */
	protected boolean isCheckUrl(String reqURL, String reqURI) {
		String[] dgrInternalTxIdArr = { //
				// 內部使用
				"version", // /dgrv4/version
				"hello", // /editor/hello
				// 以下為 Composer 使用
				"DPB0143", // /dgrv4/11/DPB0143 , 驗證 token 用
				"AA0322", // /dgrv4/11/AA0322 , 儲存 API flow
				"AA0323", // /dgrv4/11/AA0323 , 取得 API flow
				"AA0325", // /dgrv4/11/AA0325 , 取得 timeStamp
				"DPB0142", // /dgrv4/11/DPB0142 , 取得 authCode
		};

		boolean isCheck = true;// 預設要檢查
		if (!StringUtils.hasLength(reqURI)) {
			return isCheck;
		}

		// 取得 URI 最後一個 / 後的值
		int flag = reqURI.lastIndexOf("/");
		String value = reqURI.substring(flag + 1);

		// 判斷是否為不用檢查安全性的 URI
		for (String dgrInternalTxId : dgrInternalTxIdArr) {
			if (dgrInternalTxId.equals(value)) {
				isCheck = false;
				String nowStr = getNowTimestamp();
				
				// URL 不需要檢查 host header
				String errMsg = "..." + "URL does not check the host header. timestamp: " + nowStr;
				errMsg += "\n\t..." + "Request URL: " + reqURL;
				TPILogger.tl.trace(errMsg);// Composer一秒打一次 API,會一直印訊息,要用 trace
				break;
			}
		}

		return isCheck;
	}

	private String getNowTimestamp() {
		long nowTime = System.currentTimeMillis();
		Date nowDate = new Date(nowTime);
		String nowStr = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2).orElse(nowTime + "");
		return nowStr;
	}

	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._9978;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (tsmpRtnCode != null) {
			return tsmpRtnCode.getTsmpRtnMsg();
		} else {
			return getRtnCode().getDefaultMessage();
		}
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return tsmpRtnCodeService;
	}
}
