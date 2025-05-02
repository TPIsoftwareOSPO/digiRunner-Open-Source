package tpi.dgrv4.gateway.TCP.Packet;

import java.util.HashMap;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class NodeInfoPacket implements Packet_i {

	public static final String nodeInfo = "nodeInfo";

	public static final String mainInfo = "mainInfo";

	public static final String deferrableInfo = "deferrableInfo";

	public static final String refreshInfo = "refreshInfo";

	public static final String updateTimeInfo = "updateTimeInfo";

	public static final String versionInfo = "versionInfo";

	public static final String upTimeInfo = "upTimeInfo";

	public static final String startTimeInfo = "startTimeInfo";

	public static final String cFileNameInfo = "cFileNameInfo";

	public static final String serverPortInfo = "serverPortInfo";

	public static final String serverServletContextPathInfo = "serverServletContextPathInfo";

	public static final String serverSslEnalbedInfo = "serverSslEnalbedInfo";

	public static final String springProfilesActiveInfo = "springProfilesActiveInfo";

	public static final String keeperServerIpInfo = "keeperServerIpInfo";

	public static final String keeperServerPortInfo = "keeperServerPortInfo";

	public static final String rcdCacheSizeInfo = "rcdCacheSizeInfo";

	public static final String daoCacheSizeInfo = "daoCacheSizeInfo";

	public static final String fixedCacheSizeInfo = "fixedCacheSizeInfo";

	public static final String webLocalIPInfo = "webLocalIPInfo";

	public static final String fqdnInfo = "fqdnInfo";

	public static final String esQueue = "esQueue";

	public static final String rdbQueue = "rdbQueue";

	public static final String CPU = "CPU";
	public static final String MEM = "MEM";
	public static final String H_USED = "H_USED";
	public static final String H_FREE = "H_FREE";
	public static final String H_TOTAL = "H_TOTAL";

	public static final String API_ReqThroughput = "API_ReqThroughput";
	public static final String API_RespThroughput = "API_RespThroughput";

	public static final String lastUpdateTimeClientInfo = "lastUpdateTimeClient";
	public static final String lastUpdateTimeAPIInfo = "lastUpdateTimeAPI";
	public static final String lastUpdateTimeSettingInfo = "lastUpdateTimeSetting";
	public static final String lastUpdateTimeTokenInfo = "lastUpdateTimeToken";
	
	public static final String DBINFO = "dbInfo";
	public static final String DBCONNECT = "dbConnect";

	public static final String METASPACE = "METASPACE";

	public String main;

	public String deferrable;

	public String refresh;

	public String version;

	public String updateTime;

	public String upTime;

	public String startTime;

	public String serverPort;

	public String serverServletContextPath;

	public String serverSslEnalbed;

	public String springProfilesActive;

	public String keeperServerIp;

	public String keeperServerPort;

	public String rcdCacheSize;

	public String daoCacheSize;

	public String fixedCacheSize;

	public String webLocalIP;

	public String fqdn;

	public String ES_Queue;

	public String RDB_Queue;

	public String cpu;

	public String mem;
	
	public String metaSpace;

	public String h_used;

	public String h_free;

	public String h_total;

	public String api_ReqThroughputSize;

	public String api_RespThroughputSize;

	// 儲存客戶端資料最後更新的時間戳
	public String lastUpdateTimeClient;
	// 儲存 API 資料最後更新的時間戳
	public String lastUpdateTimeAPI;
	// 儲存設定資料最後更新的時間戳
	public String lastUpdateTimeSetting;
	// 儲存 Token 最後更新的時間戳
	public String lastUpdateTimeToken;
	
	public boolean http;

	public String username;

	public String ldUrl;

	public String cusIp;

	public Integer cusPort;

	// 0:dg產品 1:客製包
	public String projectType;

	public String keeperServerApi;

	public String dbInfo;

	public String dbConnect;

	public NodeInfoPacket() {
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (http) {
				// 客製包訊息 online console 對應 update time
				this.updateTime = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒).orElse(null);				
				CommunicationServer.cs.httpNodeInfo.put(username, this);
			} else {
				if (ls.paramObj.containsKey(nodeInfo) == false) {
					ls.paramObj.put(nodeInfo, new HashMap<String, String>());
				}
				HashMap<String, String> nodeInfoData = (HashMap<String, String>) ls.paramObj.get(nodeInfo);
				nodeInfoData.put(mainInfo, main);
				nodeInfoData.put(deferrableInfo, deferrable);
				nodeInfoData.put(refreshInfo, refresh);
				nodeInfoData.put(versionInfo, version);
				nodeInfoData.put(updateTimeInfo, updateTime);
				nodeInfoData.put(upTimeInfo, upTime);
				nodeInfoData.put(startTimeInfo, startTime);
				nodeInfoData.put(serverPortInfo, serverPort);
				nodeInfoData.put(serverServletContextPathInfo, serverServletContextPath);
				nodeInfoData.put(serverSslEnalbedInfo, serverSslEnalbed);
				nodeInfoData.put(springProfilesActiveInfo, springProfilesActive);
				nodeInfoData.put(keeperServerIpInfo, keeperServerIp);
				nodeInfoData.put(keeperServerPortInfo, keeperServerPort);
				nodeInfoData.put(rcdCacheSizeInfo, rcdCacheSize);
				nodeInfoData.put(daoCacheSizeInfo, daoCacheSize);
				nodeInfoData.put(fixedCacheSizeInfo, fixedCacheSize);
				nodeInfoData.put(webLocalIPInfo, webLocalIP);
				nodeInfoData.put(fqdnInfo, fqdn);
				nodeInfoData.put(esQueue, ES_Queue);
				nodeInfoData.put(rdbQueue, RDB_Queue);

				nodeInfoData.put(CPU, cpu);
				nodeInfoData.put(METASPACE, metaSpace);
				nodeInfoData.put(MEM, mem);
				nodeInfoData.put(H_USED, h_used);
				nodeInfoData.put(H_FREE, h_free);
				nodeInfoData.put(H_TOTAL, h_total);
				nodeInfoData.put(DBCONNECT, dbConnect);
				if (TPILogger.dbConnByApi) {
					nodeInfoData.put(DBINFO, dbInfo);
				}

				// API轉發吞吐量資訊
				nodeInfoData.put(API_ReqThroughput, api_ReqThroughputSize);
				nodeInfoData.put(API_RespThroughput, api_RespThroughputSize);
				
				nodeInfoData.put(lastUpdateTimeClientInfo, lastUpdateTimeClient);
				nodeInfoData.put(lastUpdateTimeAPIInfo, lastUpdateTimeAPI);
				nodeInfoData.put(lastUpdateTimeSettingInfo, lastUpdateTimeSetting);
				nodeInfoData.put(lastUpdateTimeTokenInfo, lastUpdateTimeToken);
			}			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

}
