package tpi.dgrv4.gateway.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.LoggerLevelConstant;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OCInLogs;
import tpi.dgrv4.gateway.vo.OCInMetadata;
import tpi.dgrv4.gateway.vo.OCInMetrics;
import tpi.dgrv4.gateway.vo.OCInReq;
import tpi.dgrv4.gateway.vo.OCInResp;

@Service
public class OCInService {

	public OCInResp OCInService(OCInReq req, String ip, int port) {
		OCInMetadata metadata = req.getMetadata();
		OCInMetrics metrics = req.getMetrics();
		List<OCInLogs> reqLogs = req.getLogs();
		String webLocalIP = req.getWebLocalIP();
		String serverPort = req.getServerPort();

		if (!StringUtils.hasLength(webLocalIP) || !StringUtils.hasLength(serverPort)) {
			TPILogger.tl.error("req is exile parame");
			throw DgrRtnCode._1296.throwing();
		}

		checkMetadata(metadata);
		sendNodeInfoPacket(metadata, metrics, ip, port, webLocalIP, serverPort, req.getDbConnect(), req.getCusInfo());

		if (!CollectionUtils.isEmpty(reqLogs)) {
			checkReqLogs(reqLogs);
			printLogs(reqLogs);
		}
		return null;
	}

	private void checkReqLogs(List<OCInLogs> reqLogs) {

		for (OCInLogs ocInLogs : reqLogs) {
			Integer loggerLevel = ocInLogs.getLoggerLevel();
			String logs = ocInLogs.getLogs();
			if (loggerLevel == null || !StringUtils.hasLength(logs)) {
				TPILogger.tl.error("req.OCInLogs is exile parame");
				throw DgrRtnCode._1296.throwing();
			}
		}

	}

	private void checkMetadata(OCInMetadata metadata) {

		if (ObjectUtils.isEmpty(metadata)) {
			TPILogger.tl.error("req.metadata isEmpty");
			throw DgrRtnCode._1296.throwing();
		}
		String id = metadata.getId();
		String name = metadata.getName();
		String ver = metadata.getVer();
		String livenessDetectionAPI = metadata.getLivenessDetectionAPI();

		if (!StringUtils.hasLength(id) || !StringUtils.hasLength(name) || !StringUtils.hasLength(ver)
				|| !StringUtils.hasLength(livenessDetectionAPI)) {
			TPILogger.tl.error("req.metadata is exile parame");
			throw DgrRtnCode._1296.throwing();
		}
	}

	private void printLogs(List<OCInLogs> reqLogs) {
		for (OCInLogs ocInLogs : reqLogs) {
			Integer logLevel = ocInLogs.getLoggerLevel();
			String log = ocInLogs.getLogs();
			boolean value = LoggerLevelConstant.isLevelValid(logLevel);
			if (!value) {
				System.out.println(log);
			} else {
				// 根据logLevel打印log
				switch (logLevel) {
				case 0: // APILOG
					TPILogger.tl.debug(log);
					break;
				case 200: // ERROR
					TPILogger.tl.error(log);
					break;
				case 300: // WARN
					TPILogger.tl.warn(log);
					break;
				case 400: // INFO
					TPILogger.tl.info(log);
					break;
				case 500: // DEBUG
					TPILogger.tl.debug(log);
					break;
				case 600: // TRACE
					TPILogger.tl.trace(log);
					break;
				}
			}
		}
	}

	private void sendNodeInfoPacket(OCInMetadata metadata, OCInMetrics metrics, String ip, int port, String webLocalIP,
			String serverPort, String dbConnect, String cusInfo) {
		NodeInfoPacket nodeInfoPacket = new NodeInfoPacket();
		nodeInfoPacket.http = true;
		nodeInfoPacket.projectType = "1";
		String userName = metadata.getName() + "(" + metadata.getId() + ")";
		nodeInfoPacket.username = userName;
		nodeInfoPacket.version = metadata.getVer();
		nodeInfoPacket.ldUrl = metadata.getLivenessDetectionAPI();
		nodeInfoPacket.webLocalIP = webLocalIP;
		nodeInfoPacket.serverPort = serverPort;
		nodeInfoPacket.cusIp = ip;
		nodeInfoPacket.cusPort = port;
		nodeInfoPacket.keeperServerApi = metadata.getKeeperServerApi();
		if (!ObjectUtils.isEmpty(metrics)) {
			checkMetrics(metrics);
			nodeInfoPacket.startTime = metrics.getStartupTime().toString();
			nodeInfoPacket.upTime = DateTimeUtil.secondsToDaysHoursMinutesSeconds(metrics.getUpTime());
			nodeInfoPacket.cpu = metrics.getCpu();
			nodeInfoPacket.mem = metrics.getMem();
			nodeInfoPacket.h_used = metrics.gethUsed();
			nodeInfoPacket.h_free = metrics.gethFree();
			nodeInfoPacket.h_total = metrics.gethTotal();
		}
		nodeInfoPacket.dbConnect = dbConnect;
		nodeInfoPacket.dbInfo = cusInfo;
		TPILogger.lc.send(nodeInfoPacket);
	}

	private void checkMetrics(OCInMetrics metrics) {
		Long startupTime = metrics.getStartupTime();
		Long upTime = metrics.getUpTime();
		String cpu = metrics.getCpu();
		String mem = metrics.getMem();
		String hUsed = metrics.gethUsed();
		String hFree = metrics.gethFree();
		String hTotal = metrics.gethTotal();

		if (startupTime == null || !StringUtils.hasLength(cpu) || !StringUtils.hasLength(mem)
				|| !StringUtils.hasLength(hUsed) || !StringUtils.hasLength(hFree) || !StringUtils.hasLength(hTotal)) {
			TPILogger.tl.error("req.metrics is exile parame");
			throw DgrRtnCode._1296.throwing();
		}

	}

}
