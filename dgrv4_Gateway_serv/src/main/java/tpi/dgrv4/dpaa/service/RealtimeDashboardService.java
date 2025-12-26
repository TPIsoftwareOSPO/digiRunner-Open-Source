package tpi.dgrv4.dpaa.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ITomcatMetricsService;
import tpi.dgrv4.tcp.utils.packets.RealtimeDashboardPacket;

@Service
public class RealtimeDashboardService {
	public static final Map<String, Long> realtimeDashobardApiMap = new ConcurrentHashMap<>();
	public final static String SUCCESS = "success";
	public final static String FAIL = "fail";
	public final static String BAD_ATTEMPT_401 = "badAttempt401";
	public final static String BAD_ATTEMPT_403 = "badAttempt403";
	public final static String BAD_ATTEMPT_OTHERS = "badAttemptOthers";
	
//	@Autowired(required = false)
	private ITomcatMetricsService undertowMetricsService;
	
	private HikariDataSource dataSource;
	
	@Autowired
	public RealtimeDashboardService(@Nullable ITomcatMetricsService undertowMetricsService, HikariDataSource dataSource) {
		super();
		this.undertowMetricsService = undertowMetricsService;
		this.dataSource = dataSource;
	}

	public RealtimeDashboardPacket getPacket(String name) {
		try {
			RealtimeDashboardPacket packet = new RealtimeDashboardPacket();
			packet.setName(name);
			
			// JVM memory
			Long freeMemory = Runtime.getRuntime().freeMemory();
			Long totalMemory = Runtime.getRuntime().totalMemory();
			Long maxMemory = Runtime.getRuntime().maxMemory();
			packet.setMemFree(String.format("%,d %s", (freeMemory / 1024 / 1024), "MB"));
			packet.setMemTotal(String.format("%,d %s", (totalMemory / 1024 / 1024), "MB"));
			
			packet.setMemMax(String.format("%,d %s", (maxMemory / 1024 / 1024), "MB"));
			//cpu core
			int cpuCores = Runtime.getRuntime().availableProcessors();
			packet.setCpuCore(cpuCores);
			
			
			//threadStatus
//			if (undertowMetricsService != null) {
//				packet.setCountryRoadActiveCount(String.format("%,d",undertowMetricsService.getAsyncWorkerPool().getActiveCount()));
//				packet.setCountryRoadPoolSize(String.format("%,d",undertowMetricsService.getAsyncWorkerPool().getPoolSize()));
//				packet.setHighwayActiveCount(String.format("%,d",undertowMetricsService.getAsyncWorkerHighwayPool().getActiveCount()));
//				packet.setHighwayPoolSize(String.format("%,d",undertowMetricsService.getAsyncWorkerHighwayPool().getPoolSize()));
//			}
			
			//db
			HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();		
	        int totalConnections = poolMXBean.getTotalConnections();
	        int activeConnections = poolMXBean.getActiveConnections();
	        int idleConnections = poolMXBean.getIdleConnections();
	        int waitingConnection = poolMXBean.getThreadsAwaitingConnection();
	        packet.setDbActive(activeConnections);
	        packet.setDbIdle(idleConnections);
	        packet.setDbTotal(totalConnections);
	        packet.setDbWaiting(waitingConnection);
			
	        //api number
	        packet.setSuccess(realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.SUCCESS, k -> 0L));
	        packet.setFail(realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.FAIL, k -> 0L));
	        packet.setBadAttempt401(realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.BAD_ATTEMPT_401, k -> 0L));
	        packet.setBadAttempt403(realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.BAD_ATTEMPT_403, k -> 0L));
	        packet.setBadAttemptOthers(realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.BAD_ATTEMPT_OTHERS, k -> 0L));
	        
	        //packet time
	        packet.setUpdateTime(System.currentTimeMillis());
	        
	        return packet;
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}
	
}
