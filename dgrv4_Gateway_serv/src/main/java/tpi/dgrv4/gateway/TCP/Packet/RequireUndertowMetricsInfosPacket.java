package tpi.dgrv4.gateway.TCP.Packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.UndertowMetricsPacket;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class RequireUndertowMetricsInfosPacket implements Packet_i {

	private Collection<UndertowMetricsPacket> undertowMetricsInfos;

	public Collection<UndertowMetricsPacket> getUndertowMetricsInfos() {
		return undertowMetricsInfos;
	}

	public void setUndertowMetricsInfos(Collection<UndertowMetricsPacket> undertowMetricsInfos) {
		this.undertowMetricsInfos = undertowMetricsInfos;
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		undertowMetricsInfos = CommunicationServer.cs.undertowMetricsInfos.getAllMetrics();
		
		// 將有效的 ExternalUndertowMetricsInfosPacket 物件加入到列表中。
		insertExternalUndertowMetricsInfoToList();
		
		ls.send(this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		lc.paramObj.put("undertowMetricsInfos", undertowMetricsInfos);
	}
	
	/**
	 * 將有效的 ExternalUndertowMetricsInfosPacket 物件加入到列表中。
	 */
	private void insertExternalUndertowMetricsInfoToList() {
		// 檢查 CommunicationServer 或其 ExternalUndertowMetricsInfoMap 是否為 null，若是則直接返回
		if (CommunicationServer.cs == null || CommunicationServer.cs.ExternalUndertowMetricsInfoMap == null) {
			return;
		}

		// 從 CommunicationServer 獲取 ExternalUndertowMetricsInfoMap
		ConcurrentHashMap<String, Packet_i> map = CommunicationServer.cs.ExternalUndertowMetricsInfoMap;
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
	 * 處理單一封包，判斷是否為 ExternalUndertowMetricsInfosPacket 並檢查是否過期。
	 * 
	 * @param entry    當前正在處理的 Map.Entry
	 * @param iterator map 的迭代器，用於移除過期或無效的封包
	 */
	private void handlePacket(Map.Entry<String, Packet_i> entry, Iterator<Map.Entry<String, Packet_i>> iterator) {
		// 獲取 Map.Entry 的值
		Packet_i packet = entry.getValue();
		// 判斷該值是否為 ExternalUndertowMetricsInfosPacket 類型
		if (!(packet instanceof ExternalUndertowMetricsInfosPacket)) {
			// 若不是，則從 map 中移除
			iterator.remove();
			return;
		}

		// 向下轉型為 ExternalUndertowMetricsInfosPacket
		ExternalUndertowMetricsInfosPacket externalUndertowMetricsInfosPacket = (ExternalUndertowMetricsInfosPacket) packet;
		// 檢查封包是否過期
		if (isPacketExpired(externalUndertowMetricsInfosPacket)) {
			// 若過期，則從 map 中移除
			
			// 因 UndertowMetricsInfo 不是 node 資料, 故不用做 新增失去聯繫的 DGR 節點資訊
			// addLostDgrInfo(externalUndertowMetricsInfosPacket);
			
			iterator.remove();
		} else {
			// 若未過期，則加入到 undertowMetricsInfos 列表中
			var undertowMetricsPacket = externalUndertowMetricsInfosPacket.getUndertowMetricsPacket();
			undertowMetricsInfos.add(undertowMetricsPacket);
		}
	}

	/**
	 * 判斷封包是否過期。
	 * 
	 * @param packet 要檢查的 ExternalUndertowMetricsInfosPacket 封包
	 * @return 封包是否過期
	 */
	private boolean isPacketExpired(ExternalUndertowMetricsInfosPacket packet) {
		// 獲取封包的最後更新時間
		long lastUpdateTime = packet.getLastUpdateTime();
		// 獲取當前時間
		long currentTime = System.currentTimeMillis();
		// 判斷當前時間是否大於封包最後更新時間 60 秒
		return currentTime - lastUpdateTime > 60000; // 10000 毫秒等於 60 秒
	}
}
