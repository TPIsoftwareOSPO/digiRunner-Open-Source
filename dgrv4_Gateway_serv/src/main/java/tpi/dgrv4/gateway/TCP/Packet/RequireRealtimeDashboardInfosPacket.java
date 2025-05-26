package tpi.dgrv4.gateway.TCP.Packet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.RealtimeDashboardPacket;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class RequireRealtimeDashboardInfosPacket implements Packet_i {

	private Collection<RealtimeDashboardPacket> realtimeDashboardInfos;

	public Collection<RealtimeDashboardPacket> getRealtimeDashboardInfos() {
		return realtimeDashboardInfos;
	}

	public void setRealtimeDashboardInfos(Collection<RealtimeDashboardPacket> realtimeDashboardInfos) {
		this.realtimeDashboardInfos = realtimeDashboardInfos;
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		realtimeDashboardInfos = CommunicationServer.cs.realtimeDashboardInfos.getAllValue();
		
		// 將有效的 ExternalRealtimeDashboardPacket 物件加入到列表中。
		// Add a valid ExternalRealtimeDashboardPacket object to the list.
		insertExternalRealtimeDashboardToList();
		
		ls.send(this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		lc.paramObj.put("realtimeDashboardInfos", realtimeDashboardInfos);
	}

	/**
	 * 將有效的 ExternalRealtimeDashboardPacket 物件加入到列表中。
	 * Add a valid ExternalRealtimeDashboardPacket object to the list.
	 */
	private void insertExternalRealtimeDashboardToList() {
		// 檢查 CommunicationServer 或其 ExternalRealtimeDashboardInfoMap 是否為 null，若是則直接返回
		// Check whether CommunicationServer or its ExternalRealtimeDashboardInfoMap is null, if so, return directly
		if (CommunicationServer.cs == null || CommunicationServer.cs.ExternalRealtimeDashboardInfoMap == null) {
			return;
		}

		// 從 CommunicationServer 獲取 ExternalRealtimeDashboardInfoMap
		//Get ExternalRealtimeDashboardInfoMap from CommunicationServer
		ConcurrentHashMap<String, Packet_i> map = CommunicationServer.cs.ExternalRealtimeDashboardInfoMap;
		// 處理 map 中的所有封包
		processPackets(map);
	}

	/**
	 * 遍歷並處理每一個封包。
	 * Go through and process each packet.
	 * @param map 存儲封包的 ConcurrentHashMap
	 *            ConcurrentHashMap that stores packets
	 */
	private void processPackets(ConcurrentHashMap<String, Packet_i> map) {
		// 獲取 map 的迭代器
		// Get the iterator of map
		Iterator<Map.Entry<String, Packet_i>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Packet_i> entry = iterator.next();
			// 處理當前封包
			// Process the current packet
			handlePacket(entry, iterator);
		}
	}

	/**
	 * 處理單一封包，判斷是否為 ExternalUrlStatusPacket 並檢查是否過期。
	 * Process a single packet to determine whether it is an ExternalUrlStatusPacket and check whether it has expired.
	 * @param entry    當前正在處理的 Map.Entry
	 *                 The Map.Entry currently being processed
	 * @param iterator map 的迭代器，用於移除過期或無效的封包
	 *                     used to remove expired or invalid packets
	 */
	private void handlePacket(Map.Entry<String, Packet_i> entry, Iterator<Map.Entry<String, Packet_i>> iterator) {
		// 獲取 Map.Entry 的值
		// Get the value of Map.Entry
		Packet_i packet = entry.getValue();
		// 判斷該值是否為 ExternalUrlStatusPacket 類型
		// Determine whether the value is of type ExternalUrlStatusPacket
		if (!(packet instanceof ExternalRealtimeDashboardPacket)) {
			// 若不是，則從 map 中移除
			// If not, remove it from map
			iterator.remove();
			return;
		}

		// 向下轉型為 ExternalRealtimeDashboardPacket
		// Downcast to ExternalRealtimeDashboardPacket
		ExternalRealtimeDashboardPacket externalRealtimeDashboardPacket = (ExternalRealtimeDashboardPacket) packet;
		// 檢查封包是否過期
		// Check whether the packet has expired
		if (isPacketExpired(externalRealtimeDashboardPacket)) {
			// 若過期，則從 map 中移除
			// If it expires, it is removed from the map
			// 因 UndertowMetricsInfo 不是 node 資料, 故不用做 新增失去聯繫的 DGR 節點資訊
			// Since UndertowMetricsInfo is not node data, there is no need to add the lost DGR node information.
			iterator.remove();
		} else {
			// 若未過期，則加入到 realtimeDashboardInfos 列表中
			// If it has not expired, it will be added to the realtimeDashboardInfos list.
			var realtimeDashboardPacket = externalRealtimeDashboardPacket.getRealtimeDashboardPacket();
			if(realtimeDashboardPacket == null) {
				iterator.remove();
			}else {
				realtimeDashboardInfos.add(realtimeDashboardPacket);
			}
			
		}
	}

	/**
	 * 判斷封包是否過期。
	 * Determine whether the packet has expired.
	 * @param packet 要檢查的 ExternalUrlStatusPacket 封包
	 *        packet The ExternalUrlStatusPacket packet to check
	 * @return 封包是否過期
	 *         Whether the packet has expired
	 */
	private boolean isPacketExpired(ExternalRealtimeDashboardPacket packet) {
		// 獲取封包的最後更新時間
		// Get the last update time of the packet
		long lastUpdateTime = packet.getLastUpdateTime();
		// 獲取當前時間
		// Get current time
		long currentTime = System.currentTimeMillis();
		// 判斷當前時間是否大於封包最後更新時間 10 秒
		// Determine whether the current time is greater than the last update time of the packet by 10 seconds
		return currentTime - lastUpdateTime > 10000; // 10000 毫秒等於 10 秒 //10000 milliseconds equals 10 seconds
	}
}
