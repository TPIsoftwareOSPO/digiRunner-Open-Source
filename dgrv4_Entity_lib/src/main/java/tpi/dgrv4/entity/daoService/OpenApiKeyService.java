package tpi.dgrv4.entity.daoService;

import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.OpenApiKeyUtil;
import tpi.dgrv4.common.component.cache.proxy.ITsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.ITsmpDpItems;

@Service
public class OpenApiKeyService {

	private static ITsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	/**
	 * 取得畫面要顯示的 API Key 狀態中文 <br>
	 * 1. ex: 啟用 / 停用 / 啟用(逾期) / 啟用(逾期)(已展期) / 啟用(已展期)  <br>
	 * 2. 若啟用的 API Key效期已過,狀態顯示"啟用(逾期)" <br>
	 * 3. 若tsmp_open_apikey.rollover_flag = "Y", 則顯示 "(已展期)" <br>
	 * 
	 * @param expiredAtStr, 格式: yyyy/MM/dd
	 * @param status
	 * @param rolloverFlag
	 * @return
	 */
	public static String getStatusName(String expiredAtStr, String status, String rolloverFlag, String locale) {
		ITsmpDpItems vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("ENABLE_FLAG", status, locale);
		String statusName = "";
		if(vo != null) {
			statusName = vo.getSubitemName();// 狀態中文
		}
		
		boolean isExpired = OpenApiKeyUtil.isExpired(expiredAtStr, status);
		if(isExpired) {//若啟用的API Key效期已過,在畫面狀態顯示"啟用(逾期)"
			vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("OPEN_API_KEY", "OVERDUE", locale);
			if(vo != null) {
				statusName += "(" + vo.getSubitemName() + ")";
			}
		}
		
		if("Y".equals(rolloverFlag)) {
			vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("OPEN_API_KEY", "RENEWED", locale);
			if(vo != null) {
				statusName += "(" + vo.getSubitemName() + ")";
			}
		}
		return statusName;
	}
    
	public static ITsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	public static void setTsmpDpItemsCacheProxy(ITsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		OpenApiKeyService.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}
}
