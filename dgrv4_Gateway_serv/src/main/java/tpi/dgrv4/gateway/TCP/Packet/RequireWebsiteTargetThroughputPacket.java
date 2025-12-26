package tpi.dgrv4.gateway.TCP.Packet;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class RequireWebsiteTargetThroughputPacket implements Packet_i {
	public final static String sumWebsiteTargetThroughput = "sumWebsiteTargetThroughput";
	public String sumJson;

	public String websiteName;
	public Long minTimestampSec;
	public final static Object waitKey = new Object();

	public RequireWebsiteTargetThroughputPacket() {
	}

	@Override
	public void runOnClient(LinkerClient lc) {

		try {
			lc.paramObj.put(sumWebsiteTargetThroughput, sumJson);

		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}finally {
			synchronized (RequireWebsiteTargetThroughputPacket.waitKey) {
				RequireWebsiteTargetThroughputPacket.waitKey.notifyAll();
			}
		}

	}

	@Override
	public void runOnServer(LinkerServer ls) {
		try {
			if (websiteName != null) {
				Map<String, Map<String, Integer>> sumMap = null;
				ObjectMapper objectMapper = new ObjectMapper();
				for (int i = 0; i < CommunicationServer.cs.connClinet.size(); i++) {
					LinkerServer server = CommunicationServer.cs.connClinet.get(i);
					String strJson = (String) server.paramObj.get(WebsiteTargetThroughputPacket.websiteTargetThroughput);
					if(strJson == null || strJson.isBlank()) {
						continue;
					}
					Map<Long, Map<String, Map<String, Map<String, Integer>>>> serverMap = objectMapper.readValue(strJson, new TypeReference<Map<Long, Map<String, Map<String, Map<String, Integer>>>>>() {});
					Long dataSec = serverMap.keySet().iterator().next();//只會有一筆
					if(dataSec >= minTimestampSec) {
						Map<String, Map<String, Integer>> websiteNameMap = serverMap.get(dataSec).get(websiteName);
						if(websiteNameMap == null) {
							continue;
						}
						if(sumMap == null) {
							sumMap = new HashMap<>(websiteNameMap);
						}else {
							for (String targetUrlKey : websiteNameMap.keySet()) {
								Map<String, Integer> targetUrlMap = websiteNameMap.get(targetUrlKey);
								for (String typeKey : targetUrlMap.keySet()) {
									Integer frequence = targetUrlMap.get(typeKey);
									Map<String, Integer> sumTargetUrlMap = sumMap.get(targetUrlKey);
									if (sumTargetUrlMap != null) {
										if (sumTargetUrlMap.get(typeKey) != null) {
											sumTargetUrlMap.put(typeKey, sumTargetUrlMap.get(typeKey) + frequence);
										} else {
											sumTargetUrlMap.put(typeKey, frequence);
										}
									} else {
										sumTargetUrlMap = new HashMap<String, Integer>();
										sumTargetUrlMap.put(typeKey, frequence);
										sumMap.put(targetUrlKey, sumTargetUrlMap);
									}
								}
							}
						}
					}
				}
				
				if(sumMap != null) {
					sumJson = objectMapper.writeValueAsString(sumMap);
				}
				
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		ls.send(this);

	}

}
