package tpi.dgrv4.gateway.TCP.Packet;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.MaskUtil;
import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.DgrNodeLostContact;
import tpi.dgrv4.entity.repository.DgrNodeLostContactDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.ComposerInfoData;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class RequireAllClientListPacket implements Packet_i {

	LinkedList<ClientKeeper> allClientList = new LinkedList<ClientKeeper>();

	LinkedList<ComposerInfoData> allComposerList = new LinkedList<ComposerInfoData>();

	public static final String allComposerListStr = "allComposerListStr";

	public final static Object waitKey = new Object();
	
	public String uuid = "(__" + UUID.randomUUID().toString() + "__)";

	private long lastUpdateTimeClient = -1;
	private long lastUpdateTimeAPI = -1;
	private long lastUpdateTimeSetting = -1;
	private long lastUpdateTimeToken = -1;

	public RequireAllClientListPacket() {
//		super();
	}

	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			lc.paramObj.put("allClientList", allClientList);
			lc.paramObj.put(allComposerListStr, allComposerList);

			TPILogger.updateTimestamp(TPILogger.lastUpdateTimeAPI, lastUpdateTimeAPI);
			TPILogger.updateTimestamp(TPILogger.lastUpdateTimeClient, lastUpdateTimeClient);
			TPILogger.updateTimestamp(TPILogger.lastUpdateTimeSetting, lastUpdateTimeSetting);
			TPILogger.updateTimestamp(TPILogger.lastUpdateTimeToken, lastUpdateTimeToken);

			// allClientList.forEach((c)->{
			// System.out.println("...allClientList:" + c);1
			// });
			// System.out.println("________________________");
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} finally {
			synchronized (RequireAllClientListPacket.waitKey) {
				RequireAllClientListPacket.waitKey.notifyAll(); // SonarQube
			}
		}
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			LinkedList<LinkerServer> mustRemoveConn = new LinkedList<LinkerServer>();
			Set<String> mustRemoveConnNameAndIp = new HashSet<String>();

			printoutAllClient();

			for (int i = CommunicationServer.cs.connClinet.size() - 1; i >= 0; i--) {
				LinkerServer server = CommunicationServer.cs.connClinet.get(i);
				ClientKeeper c = new ClientKeeper();
				c.setUsername(server.userName);
				c.setIp(server.getRemoteIP());
				c.setPort(server.getRemotePort());
				allClientList.add(c);

				if (server.paramObj.containsKey(NodeInfoPacket.nodeInfo)) {
					HashMap<String, String> nodeInfoData = (HashMap<String, String>) server.paramObj
							.get(NodeInfoPacket.nodeInfo);
					c.setMain(nodeInfoData.get(NodeInfoPacket.mainInfo));
					c.setDeferrable(nodeInfoData.get(NodeInfoPacket.deferrableInfo));
					c.setRefresh(nodeInfoData.get(NodeInfoPacket.refreshInfo));
					c.setVersion(nodeInfoData.get(NodeInfoPacket.versionInfo));
					c.setUpdateTime(nodeInfoData.get(NodeInfoPacket.updateTimeInfo));

					c.setCpu(nodeInfoData.get(NodeInfoPacket.CPU_TITLE));
					c.setMem(nodeInfoData.get(NodeInfoPacket.MEM_TITLE));
					c.setMetaSpace(nodeInfoData.get(NodeInfoPacket.METASPACE_TITLE));
					c.setH_used(nodeInfoData.get(NodeInfoPacket.H_USED_TITLE));
					c.setH_free(nodeInfoData.get(NodeInfoPacket.H_FREE_TITLE));
					c.setH_total(nodeInfoData.get(NodeInfoPacket.H_TOTAL_TITLE));

					// API轉發吞吐量資訊
					c.setApi_ReqThroughputSize(nodeInfoData.get(NodeInfoPacket.API_ReqThroughput));
					c.setApi_RespThroughputSize(nodeInfoData.get(NodeInfoPacket.API_RespThroughput));
					// DB連線資訊 密碼部分要隱碼
					// 最後資訊要 base64 , ex:b64.xxx
					if (TPILogger.dbConnByApi) {
						String dbInfo = nodeInfoData.get(NodeInfoPacket.DBINFO_TITLE);
						ObjectMapper oMapper = new ObjectMapper();
						JsonNode jn = oMapper.readTree(dbInfo);
						findAndModifyValues(jn);
						dbInfo = jn.toPrettyString();
						if (StringUtils.hasLength(dbInfo)) {
							c.setCusInfo("b64." + Base64Util.base64Encode(dbInfo.getBytes()));
						} else {
							c.setCusInfo(null);
						}

					}
					// 目前的DB連線帳號
					c.setDbConnect(nodeInfoData.get(NodeInfoPacket.DBCONNECT_TITLE));
					// 如果更新時間超過 n000, 則移除
					Optional<Date> date = DateTimeUtil.stringToDateTime(c.getUpdateTime(), DateTimeFormatEnum.西元年月日時分秒);
					Date updateDate = date.orElse(DateTimeUtil.now());
					long diffTime = DateTimeUtil.now().getTime() - updateDate.getTime();

					// 進行 ip + name 的檢查，如果與名單重複，代表為舊資料，需要移除。
					String ip = c.getIp();
					String name = c.getUsername();
					String ipAndName = ip + name;
					boolean mustRemove = mustRemoveConnNameAndIp.contains(ipAndName);

					int mainSize = -1;
					try {
						// Composer 節點沒有 mainSize
						mainSize = Integer.parseInt(c.getMain());
					} catch (Exception e) {
						mainSize = 0;
					}

					// 移除舊有資料
					if (mustRemove) {
						// 移除
						mustRemoveConn.add(server);
						TPILogger.tl.error("欲移除 socket 連線 userName:" + server.userName + ",\tRemoteIP:"
								+ server.getRemoteIP() + ", diffTime:" + diffTime + ", mainSize:" + mainSize);
					}

					// 這裡是 Composer 的節點移除
					if (diffTime > 3000 && mainSize == -1) {
						// 超過時間了, 移除
						mustRemoveConn.add(server);
						TPILogger.tl.error("欲移除 socket 連線 userName:" + server.userName + ",\tRemoteIP:"
								+ server.getRemoteIP() + ", diffTime:" + diffTime + ", mainSize:" + mainSize);
					}

					// 將最新的 ip 與 name 加入列表之中
					mustRemoveConnNameAndIp.add(ipAndName);

					c.setUpTime(nodeInfoData.get(NodeInfoPacket.upTimeInfo));
					c.setStartTime(nodeInfoData.get(NodeInfoPacket.startTimeInfo));
					c.setServerPort(nodeInfoData.get(NodeInfoPacket.serverPortInfo));
					c.setServerSslEnalbed(nodeInfoData.get(NodeInfoPacket.serverSslEnalbedInfo));
					c.setSpringProfilesActive(nodeInfoData.get(NodeInfoPacket.springProfilesActiveInfo));
					c.setServerServletContextPath(nodeInfoData.get(NodeInfoPacket.serverServletContextPathInfo));
					c.setKeeperServerIp(nodeInfoData.get(NodeInfoPacket.keeperServerIpInfo));
					c.setKeeperServerPort(nodeInfoData.get(NodeInfoPacket.keeperServerPortInfo));
					c.setRcdCacheSize(nodeInfoData.get(NodeInfoPacket.rcdCacheSizeInfo));
					c.setDaoCacheSize(nodeInfoData.get(NodeInfoPacket.daoCacheSizeInfo));
					c.setFixedCacheSize(nodeInfoData.get(NodeInfoPacket.fixedCacheSizeInfo));
					c.setWebLocalIP(nodeInfoData.get(NodeInfoPacket.webLocalIPInfo));
					c.setFqdn(nodeInfoData.get(NodeInfoPacket.fqdnInfo));
					c.setEsQueue(nodeInfoData.get(NodeInfoPacket.esQueue));
					c.setRdbQueue(nodeInfoData.get(NodeInfoPacket.rdbQueue));

					String lastUpdateTimeAPIInfo = nodeInfoData.get(NodeInfoPacket.lastUpdateTimeAPIInfo);
					String lastUpdateTimeClientInfo = nodeInfoData.get(NodeInfoPacket.lastUpdateTimeClientInfo);
					String lastUpdateTimeSettingInfo = nodeInfoData.get(NodeInfoPacket.lastUpdateTimeSettingInfo);
					String lastUpdateTimeTokenInfo = nodeInfoData.get(NodeInfoPacket.lastUpdateTimeTokenInfo);

					lastUpdateTimeAPI = Math.max(lastUpdateTimeAPI, Long.valueOf(lastUpdateTimeAPIInfo));
					lastUpdateTimeClient = Math.max(lastUpdateTimeClient, Long.valueOf(lastUpdateTimeClientInfo));
					lastUpdateTimeSetting = Math.max(lastUpdateTimeSetting, Long.valueOf(lastUpdateTimeSettingInfo));
					lastUpdateTimeToken = Math.max(lastUpdateTimeToken, Long.valueOf(lastUpdateTimeTokenInfo));

					c.setLastUpdateTimeAPI(lastUpdateTimeAPIInfo);
					c.setLastUpdateTimeClient(lastUpdateTimeClientInfo);
					c.setLastUpdateTimeSetting(lastUpdateTimeSettingInfo);
					c.setLastUpdateTimeToken(lastUpdateTimeTokenInfo);
				}
			}

			// 客製包資訊
			ConcurrentHashMap<String, Packet_i> cusPacketlist = CommunicationServer.cs.httpNodeInfo;
			Iterator<ConcurrentHashMap.Entry<String, Packet_i>> iterator = cusPacketlist.entrySet().iterator();
			while (iterator.hasNext()) {
				ConcurrentHashMap.Entry<String, Packet_i> entry = iterator.next();
				Packet_i packet = entry.getValue();
				NodeInfoPacket nodeInfoPacket = (NodeInfoPacket) packet;
				ClientKeeper clientKeeper = new ClientKeeper();
				String updateTime = nodeInfoPacket.updateTime;
				// 若超過10秒 要跳過 並移除
				boolean jumpOverAndRemove = checkCusPacketlist(updateTime);
				if (jumpOverAndRemove) {
					// 移除
					iterator.remove();
				} else {
					clientKeeper.setKeeperServerApi(nodeInfoPacket.keeperServerApi);
					clientKeeper.setProjectType(nodeInfoPacket.projectType);
					clientKeeper.setIp(nodeInfoPacket.cusIp);
					clientKeeper.setPort(nodeInfoPacket.cusPort);
					clientKeeper.setServerPort(nodeInfoPacket.serverPort);
					clientKeeper.setFqdn(nodeInfoPacket.webLocalIP);
					clientKeeper.setWebLocalIP(nodeInfoPacket.webLocalIP);
					clientKeeper.setUsername(nodeInfoPacket.username);
					clientKeeper.setVersion(nodeInfoPacket.version);
					clientKeeper.setStartTime(nodeInfoPacket.startTime);
					clientKeeper.setUpTime(nodeInfoPacket.upTime);
					clientKeeper.setUpdateTime(nodeInfoPacket.updateTime);
					clientKeeper.setCpu(nodeInfoPacket.cpu);
					clientKeeper.setMetaSpace(nodeInfoPacket.metaSpace);
					clientKeeper.setMem(nodeInfoPacket.mem);
					clientKeeper.setH_used(nodeInfoPacket.h_used);
					clientKeeper.setH_free(nodeInfoPacket.h_free);
					clientKeeper.setH_total(nodeInfoPacket.h_total);
					clientKeeper.setLivenessUrlPath(nodeInfoPacket.ldUrl);
					clientKeeper.setDbConnect(nodeInfoPacket.dbConnect);
				
					if (jumpOverAndRemove) {
						
					}
					
					// 這裡做隱碼
					String dbInfo = nodeInfoPacket.dbInfo;
					if (StringUtils.hasLength(dbInfo)) {
					ObjectMapper oMapper = new ObjectMapper();
					JsonNode jn = oMapper.readTree(dbInfo);
					findAndModifyValues(jn);
					dbInfo = jn.toPrettyString();
			
						clientKeeper.setCusInfo("b64." + Base64Util.base64Encode(dbInfo.getBytes()));
					} else {
						clientKeeper.setCusInfo(null);
					}
					allClientList.add(clientKeeper);
				}
			}

			// 移除 socket 連線
			for (LinkerServer linkerServer : mustRemoveConn) {
				// 暫不做強制移除
				CommunicationServer.cs.doDisconnectProc(linkerServer);
				TPILogger.tl.error("欲移除 socket 連線 userName:" + linkerServer.userName + ",\tRemoteIP:"
						+ linkerServer.getRemoteIP());
			}

			for (LinkerServer server : CommunicationServer.cs.connClinet) {
				try {
					//因為composer常有不明的錯誤, 所以自己try..catch
					if (server.paramObj.containsKey(ComposerInfoPacket.composerInfo)) {
						HashMap<String, ComposerInfoData> composerInfoHM = (HashMap<String, ComposerInfoData>) server.paramObj
								.get(ComposerInfoPacket.composerInfo);
						Object[] arrComposerKey = null;
						synchronized (composerInfoHM) {
							arrComposerKey = composerInfoHM.keySet().toArray();
						}
						for (Object key : arrComposerKey) {
							ComposerInfoData composerInfoData = composerInfoHM.get((String)key);
							if(composerInfoData == null) {
								TPILogger.tl.debug("composerId "+ key +" is null");
								continue;
							}
							GregorianCalendar composerTS = new GregorianCalendar();
							composerTS.setTimeInMillis(composerInfoData.getTs());
							//注意:存活秒數有改,在CApiKeyService的getRtnTempList也要改
							composerTS.add(GregorianCalendar.SECOND, 10);
	
							GregorianCalendar now = new GregorianCalendar();
							if (now.after(composerTS)) {
								// 移除
								if (TPILogger.lc.paramObj.containsKey(TPILogger.dgrNodeLostContactDaoStr)) {
									DgrNodeLostContactDao dgrNodeLostContactDao = (DgrNodeLostContactDao) TPILogger.lc.paramObj
											.get(TPILogger.dgrNodeLostContactDaoStr);
	
									TPILogger.tl.debug("now_Millis="+now.getTimeInMillis() + ", composerInfoData="+composerInfoData);
									Long timestamp = System.currentTimeMillis();
									DgrNodeLostContact vo = new DgrNodeLostContact();
									vo.setIp(composerInfoData.getRemoteIP());
									vo.setLostTime(TimeZoneUtil.long2UTCstring(timestamp));
									vo.setNodeName(composerInfoData.getComposerID());
									vo.setPort(Integer.parseInt(composerInfoData.getWebServerPort()));
									vo.setCreateTimestamp(timestamp);
									dgrNodeLostContactDao.saveAndFlush(vo);
								}
								synchronized (composerInfoHM) {
									composerInfoHM.remove((String)key);
								}
							} else {
								// 保留
								allComposerList.add(composerInfoData);
							}
						}
					}
				}catch(Exception e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		// 將有效的 ExternalDgrInfoPacket 物件加入到列表中。
		insertExternalDgrInfoToList();

		ls.send(this);
	}

	private boolean checkCusPacketlist(String updateTime) {
		// updateTime格式 2024-04-16 09:41:37
		LocalDateTime localDateTime = LocalDateTime.parse(updateTime,
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
		Instant updateTimeInstant = zonedDateTime.toInstant();
		Instant now = Instant.now();
		Duration duration = Duration.between(updateTimeInstant, now);
		return duration.getSeconds() > 10;
	}

	/**
	 * 將有效的 ExternalDgrInfoPacket 物件加入到列表中。
	 */
	private void insertExternalDgrInfoToList() {
		// 檢查 CommunicationServer 或其 ExternalDgrInfoMap 是否為 null，若是則直接返回
		if (CommunicationServer.cs == null || CommunicationServer.cs.ExternalDgrInfoMap == null) {
			return;
		}

		// 從 CommunicationServer 獲取 ExternalDgrInfoMap
		ConcurrentHashMap<String, Packet_i> map = CommunicationServer.cs.ExternalDgrInfoMap;
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
	 * 處理單一封包，判斷是否為 ExternalDgrInfoPacket 並檢查是否過期。
	 * 
	 * @param entry    當前正在處理的 Map.Entry
	 * @param iterator map 的迭代器，用於移除過期或無效的封包
	 */
	private void handlePacket(Map.Entry<String, Packet_i> entry, Iterator<Map.Entry<String, Packet_i>> iterator) {
		// 獲取 Map.Entry 的值
		Packet_i packet = entry.getValue();
		// 判斷該值是否為 ExternalDgrInfoPacket 類型
		if (!(packet instanceof ExternalDgrInfoPacket)) {
			// 若不是，則從 map 中移除
			iterator.remove();
			return;
		}

		// 向下轉型為 ExternalDgrInfoPacket
		ExternalDgrInfoPacket externalDgrInfoPacket = (ExternalDgrInfoPacket) packet;
		// 檢查封包是否過期
		if (isPacketExpired(externalDgrInfoPacket)) {
			// 若過期，則從 map 中移除
			addLostDgrInfo(externalDgrInfoPacket);
			iterator.remove();
		} else {
			// 若未過期，則加入到 allClientList 列表中
			allClientList.add(externalDgrInfoPacket.getClientKeeper());
		}
	}

	/**
	 * 新增失去聯繫的 DGR 節點資訊
	 * 
	 * @param externalDgrInfoPacket 外部 DGR 資訊封包
	 */
	private void addLostDgrInfo(ExternalDgrInfoPacket externalDgrInfoPacket) {
		// 檢查外部 DGR 資訊封包及其 ClientKeeper 是否為 null
		if (externalDgrInfoPacket == null || externalDgrInfoPacket.getClientKeeper() == null) {
			return;
		}

		// 從 TPILogger 的參數物件中取得 DgrNodeLostContactDao 實例
		DgrNodeLostContactDao dgrNodeLostContactDao = (DgrNodeLostContactDao) TPILogger.lc.paramObj
				.get(TPILogger.dgrNodeLostContactDaoStr);

		// 取得外部 DGR 資訊封包中的 ClientKeeper
		ClientKeeper clientKeeper = externalDgrInfoPacket.getClientKeeper();

		// 取得當前時間戳記
		Long timestamp = System.currentTimeMillis();
		// 建立 DgrNodeLostContact 物件
		DgrNodeLostContact vo = new DgrNodeLostContact();
		// 設定失去聯繫節點的 IP
		vo.setIp(clientKeeper.getIp());
		// 將時間戳記轉換為 UTC 字串並設定為失去聯繫時間
		vo.setLostTime(TimeZoneUtil.long2UTCstring(timestamp));
		// 設定失去聯繫節點的名稱
		vo.setNodeName(clientKeeper.getUsername());
		// 設定失去聯繫節點的埠號
		vo.setPort(clientKeeper.getPort());
		// 設定建立時間戳記
		vo.setCreateTimestamp(timestamp);
		// 儲存 DgrNodeLostContact 物件到資料庫並立即更新
		dgrNodeLostContactDao.saveAndFlush(vo);
	}

	/**
	 * 判斷封包是否過期。
	 * 
	 * @param packet 要檢查的 ExternalDgrInfoPacket 封包
	 * @return 封包是否過期
	 */
	private boolean isPacketExpired(ExternalDgrInfoPacket packet) {
		// 獲取封包的最後更新時間
		long lastUpdateTime = packet.getLastUpdateTime();
		// 獲取當前時間
		long currentTime = System.currentTimeMillis();
		// 判斷當前時間是否大於封包最後更新時間 10 秒
		return currentTime - lastUpdateTime > 10000; // 10000 毫秒等於 10 秒
	}

	/**
	 * print All client info
	 */
	public void printoutAllClient() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append("getRemoteIP()\t");
		sb.append("isConnected()\t");
		sb.append("getRemotePort()\t");
		sb.append("userName\n");
		for (LinkerServer server : CommunicationServer.cs.connClinet) {
			sb.append(server.getRemoteIP() + "\t");
			sb.append(server.isConnected() + "\t\t");
			sb.append(server.getRemotePort() + "\t\t");
			sb.append(server.userName + "\n");
		}
//		logger.debug(sb.toString());
	}
	private static void findAndModifyValues(JsonNode node) {
		List<String> keyWord = Arrays.asList("dbMima1", "dbMima2");
		List<String> keysToFind = new ArrayList<>();
		keysToFind .addAll(TPILogger.maskKeysArr);
		keysToFind .addAll(keyWord);
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                for (String key : keysToFind) {
                    if (fieldName.equals(key)) {
                        String oldValue = objectNode.get(fieldName).asText();
                        String newValue = MaskUtil.maskAnyPosition(oldValue,3,3);
                        objectNode.put(fieldName, newValue);
                    }
                }
                findAndModifyValues(objectNode.get(fieldName));
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                findAndModifyValues(arrayItem);
            }
        }
    }
}
