package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.UdpSsoUtil;
import tpi.dgrv4.dpaa.vo.UdpEnvDataReq;
import tpi.dgrv4.dpaa.vo.UdpEnvDataResp;
import tpi.dgrv4.dpaa.vo.UdpEnvItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class UdpEnvDataService {
	
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	public UdpEnvDataService(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public UdpEnvDataResp getUdpEnvData(TsmpAuthorization auth, HttpServletRequest httpReq, UdpEnvDataReq req, 
			ReqHeader reqHeader, HttpHeaders headers) {
		UdpEnvDataResp resp = null;
		String udpParam = req.getUdpParam();
		
		try {
			logger.debug(String.format("http header size = %d", headers.entrySet().size()));
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				List<String> val = entry.getValue();
				logger.debug(String.format("http key [{}] = {}", key, val.toString()));
			}
			String userIp = StringUtils.isEmpty(headers.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr() : headers.getFirst("x-forwarded-for");
			
			resp = getUdpEnvData(userIp, udpParam, reqHeader.getLocale());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	protected UdpEnvDataResp getUdpEnvData(String userIp, String paramStr_en, String locale) {
		this.logger.debug(String.format("userIp: %s", userIp));
		
		if(!StringUtils.hasText(paramStr_en)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		UdpEnvDataResp resp = new UdpEnvDataResp();
		
		String itemNo = "UDPSSO";
		List<TsmpDpItems> itemList = getTsmpDpItems(itemNo, locale);
		List<UdpEnvItem> updEnvData_dataList = UdpSsoUtil.getEnvList(itemList, itemNo, userIp, paramStr_en);
		resp.setDataList(updEnvData_dataList);
		
		return resp;
	}
	
	private List<TsmpDpItems> getTsmpDpItems(String itemNo, String locale) {
		List<TsmpDpItems> updEnvData_itemList = getTsmpDpItemsCacheProxy().findByItemNoAndLocale(itemNo, locale);
		if (updEnvData_itemList == null || updEnvData_itemList.size() == 0) {
			this.logger.debug(String.format("未設定 itemNo 的值: %s", itemNo));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return updEnvData_itemList;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}
