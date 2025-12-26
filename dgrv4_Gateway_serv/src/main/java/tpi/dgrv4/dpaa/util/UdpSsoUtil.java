package tpi.dgrv4.dpaa.util;


import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.UdpEnvItem;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class UdpSsoUtil {
	private static TPILogger logger = TPILogger.tl;

	public static List<UdpEnvItem> getEnvList(List<TsmpDpItems> itemsList, String itemNo, String userIp, 
			String paramStr_en) {
		String networks = "";
		List<UdpEnvItem> dataList = new ArrayList<UdpEnvItem>();
		for (TsmpDpItems tsmpDpItems : itemsList) {
			String envUrl = tsmpDpItems.getParam1();
			if (!StringUtils.hasText(envUrl)) {
				logger.debug(String.format("未設定 Param1 的值: %s - %s", itemNo, tsmpDpItems.getSubitemNo()));
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			networks = tsmpDpItems.getParam2();
			if (!StringUtils.hasText(networks)) {
				logger.debug(String.format("未設定 Param2 的值: %s - %s", itemNo, tsmpDpItems.getSubitemNo()));
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			/*
			 * 判斷User IP是否在網段中
			 */
			String[] networkArray = networks.split(",");
			for (String network : networkArray) {
				boolean isInNetwork = ServiceUtil.isIpInNetwork(userIp, network);
				if(isInNetwork) {
					envUrl = envUrl + paramStr_en;
					UdpEnvItem item = new UdpEnvItem();
					item.setEnvName(tsmpDpItems.getSubitemName());
					item.setEnvUrl(envUrl);
					dataList.add(item);
					break;
				}
			}
		}
		
		if (CollectionUtils.isEmpty(dataList)) {
			logger.debug(String.format("[TSMP_DP_ITEMS] User IP 不在可登入的網段中, userIp=%s, network=%s", userIp, networks));
			throw TsmpDpAaRtnCode._1514.throwing();
		}
		
		return dataList;
	}
}
