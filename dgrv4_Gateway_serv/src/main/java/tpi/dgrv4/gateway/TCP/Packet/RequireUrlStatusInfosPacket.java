package tpi.dgrv4.gateway.TCP.Packet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class RequireUrlStatusInfosPacket implements Packet_i {
	
	public final static Object waitKey = new Object();

	private Collection<UrlStatusPacket> urlStatusInfos;

	public Collection<UrlStatusPacket> getUndertowMetricsInfos() {
		return urlStatusInfos;
	}

	public void setUrlStatusInfos(Collection<UrlStatusPacket> urlStatusInfos) {
		this.urlStatusInfos = urlStatusInfos;
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		urlStatusInfos = CommunicationServer.cs.urlStatusInfos.getAllStatus();
		
		// 將有效的 ExternalUrlStatusPacket 物件加入到列表中。
		insertExternalUrlStatusToList();
		
		ls.send(this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			lc.paramObj.put("urlStatusInfos", urlStatusInfos);
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} finally {
			synchronized (RequireUrlStatusInfosPacket.waitKey) {
				RequireUrlStatusInfosPacket.waitKey.notifyAll(); // SonarQube
			}
		}
	}

	/**
	 * 將有效的 ExternalUrlStatusPacket 物件加入到列表中。
	 */
	private void insertExternalUrlStatusToList() {
		// 檢查 CommunicationServer 或其 ExternalUrlStatusInfoMap 是否為 null，若是則直接返回
		if (CommunicationServer.cs == null || CommunicationServer.cs.ExternalUrlStatusInfoMap == null) {
			return;
		}

		// 從 CommunicationServer 獲取 ExternalUrlStatusInfoMap
		ConcurrentHashMap<String, Packet_i> map = CommunicationServer.cs.ExternalUrlStatusInfoMap;
		// 處理 map 中的所有封包
		processPackets(map);
	}

	/**
	 * 遍歷並處理每一個封包。
	 * 
	 * @param map 存儲封包的 ConcurrentHashMap
	 */
	private void processPackets(ConcurrentHashMap<String, Packet_i> map) {
		// 獲取 map 的迭代器
		Iterator<Map.Entry<String, Packet_i>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Packet_i> entry = iterator.next();
			// 處理當前封包
			handlePacket(entry, iterator);
		}
	}

	/**
	 * 處理單一封包，判斷是否為 ExternalUrlStatusPacket 並檢查是否過期。
	 * 
	 * @param entry    當前正在處理的 Map.Entry
	 * @param iterator map 的迭代器，用於移除過期或無效的封包
	 */
	private void handlePacket(Map.Entry<String, Packet_i> entry, Iterator<Map.Entry<String, Packet_i>> iterator) {
		// 獲取 Map.Entry 的值
		Packet_i packet = entry.getValue();
		// 判斷該值是否為 ExternalUrlStatusPacket 類型
		if (!(packet instanceof ExternalUrlStatusPacket)) {
			// 若不是，則從 map 中移除
			iterator.remove();
			return;
		}

		// 向下轉型為 ExternalUrlStatusPacket
		ExternalUrlStatusPacket externalUrlStatusPacket = (ExternalUrlStatusPacket) packet;
		// 檢查封包是否過期
		if (isPacketExpired(externalUrlStatusPacket)) {
			// 若過期，則從 map 中移除
			// 因 UndertowMetricsInfo 不是 node 資料, 故不用做 新增失去聯繫的 DGR 節點資訊
			iterator.remove();
		} else {
			// 若未過期，則加入到 urlStatusInfos 列表中
			var urlStatusPacket = externalUrlStatusPacket.getUrlStatusPacket();
			urlStatusInfos.add(urlStatusPacket);
		}
	}

	/**
	 * 判斷封包是否過期。
	 * 
	 * @param packet 要檢查的 ExternalUrlStatusPacket 封包
	 * @return 封包是否過期
	 */
	private boolean isPacketExpired(ExternalUrlStatusPacket packet) {
		// 獲取封包的最後更新時間
		long lastUpdateTime = packet.getLastUpdateTime();
		// 獲取當前時間
		long currentTime = System.currentTimeMillis();
		// 判斷當前時間是否大於封包最後更新時間 60 秒
		return currentTime - lastUpdateTime > 60000; // 10000 毫秒等於 60 秒
	}
}
