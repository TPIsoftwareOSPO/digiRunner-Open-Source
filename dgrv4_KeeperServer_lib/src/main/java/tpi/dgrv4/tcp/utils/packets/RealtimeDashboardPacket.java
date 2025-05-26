package tpi.dgrv4.tcp.utils.packets;

import java.util.UUID;

import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class RealtimeDashboardPacket implements Packet_i {

	private String name;
	private long success;
	private long fail;
	private long badAttempt401;
	private long badAttempt403;
	private long badAttemptOthers;
	private int dbTotal;
	private int dbActive;
	private int dbIdle;
	private int dbWaiting;
	private int cpuCore;
	private String cpuUsage;
	private String memFree;
	private String memTotal;
	private String memMax;
	private String countryRoadActiveCount;
	private String countryRoadPoolSize;
	private String highwayActiveCount;
	private String highwayPoolSize;
	private long updateTime;
	
	public String uuid = "(__" + UUID.randomUUID().toString() + "__)";
	

	@Override
	public void runOnServer(LinkerServer ls) {

		CommunicationServer.cs.realtimeDashboardInfos.put(name, this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {

	}

	public int getDbTotal() {
		return dbTotal;
	}

	public void setDbTotal(int dbTotal) {
		this.dbTotal = dbTotal;
	}

	public int getDbActive() {
		return dbActive;
	}

	public void setDbActive(int dbActive) {
		this.dbActive = dbActive;
	}

	public int getDbIdle() {
		return dbIdle;
	}

	public void setDbIdle(int dbIdle) {
		this.dbIdle = dbIdle;
	}

	public int getDbWaiting() {
		return dbWaiting;
	}

	public void setDbWaiting(int dbWaiting) {
		this.dbWaiting = dbWaiting;
	}

	public int getCpuCore() {
		return cpuCore;
	}

	public void setCpuCore(int cpuCore) {
		this.cpuCore = cpuCore;
	}

	public String getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(String cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public String getMemFree() {
		return memFree;
	}

	public void setMemFree(String memFree) {
		this.memFree = memFree;
	}

	public String getMemTotal() {
		return memTotal;
	}

	public void setMemTotal(String memTotal) {
		this.memTotal = memTotal;
	}

	public String getMemMax() {
		return memMax;
	}

	public void setMemMax(String memMax) {
		this.memMax = memMax;
	}

	public String getCountryRoadPoolSize() {
		return countryRoadPoolSize;
	}

	public void setCountryRoadPoolSize(String countryRoadPoolSize) {
		this.countryRoadPoolSize = countryRoadPoolSize;
	}

	public String getCountryRoadActiveCount() {
		return countryRoadActiveCount;
	}

	public void setCountryRoadActiveCount(String countryRoadActiveCount) {
		this.countryRoadActiveCount = countryRoadActiveCount;
	}

	public String getHighwayActiveCount() {
		return highwayActiveCount;
	}

	public void setHighwayActiveCount(String highwayActiveCount) {
		this.highwayActiveCount = highwayActiveCount;
	}

	public String getHighwayPoolSize() {
		return highwayPoolSize;
	}

	public void setHighwayPoolSize(String highwayPoolSize) {
		this.highwayPoolSize = highwayPoolSize;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSuccess() {
		return success;
	}

	public void setSuccess(long success) {
		this.success = success;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public long getBadAttempt401() {
		return badAttempt401;
	}

	public void setBadAttempt401(long badAttempt401) {
		this.badAttempt401 = badAttempt401;
	}

	public long getBadAttempt403() {
		return badAttempt403;
	}

	public void setBadAttempt403(long badAttempt403) {
		this.badAttempt403 = badAttempt403;
	}

	public long getBadAttemptOthers() {
		return badAttemptOthers;
	}

	public void setBadAttemptOthers(long badAttemptOthers) {
		this.badAttemptOthers = badAttemptOthers;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	
}
