package tpi.dgrv4.common.component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import com.sun.management.OperatingSystemMXBean;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import tpi.dgrv4.common.vo.CommonSystemInfo;

public class SystemInfoHelper {

	private final String appPath;

	private OperatingSystemMXBean osMXBean;

	// 前次系統監測值
	/*
	 * 2021.07.26 改呼叫library的API取得CPU使用率 private OSProcess previousProcessStatus;
	 */

	public SystemInfoHelper() {
		this.appPath = CommonFileHelper.filterPath(new File(".").getAbsolutePath(), false);
		this.osMXBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
	}

	public SystemInfo getSystemInfo() {
		return new SystemInfo();
	}

	public CommonSystemInfo getCpuUsedRateAndMem() {
		CommonSystemInfo vo = new CommonSystemInfo();
		setCpuUsedRateAndMem(vo);
		return vo;
	}

	public void setCpuUsedRateAndMem(CommonSystemInfo dpaaSystemInfo) {
		SystemInfo systemInfo = getSystemInfo();
		setCpuUsedRateAndMem(systemInfo, dpaaSystemInfo);
	}

	public void setCpuUsedRateAndMem(SystemInfo systemInfo, CommonSystemInfo dpaaSystemInfo) {

	

		
		OperatingSystem os = systemInfo.getOperatingSystem();
		int processId = os.getProcessId();
		OSProcess currentProcess = os.getProcess(processId);
		int logicalProcessorCount = systemInfo.getHardware().getProcessor().getLogicalProcessorCount();
		float cpu = getCpuUsedRate(currentProcess, logicalProcessorCount);
		int mem = to1024MegaConversion(currentProcess.getResidentSetSize());
		dpaaSystemInfo.setCpu(cpu);
		dpaaSystemInfo.setMem(mem);
		
	}

	public CommonSystemInfo getRuntimeInfo() {
		CommonSystemInfo vo = new CommonSystemInfo();
		setRuntimeInfo(vo);
		return vo;
	}

	public void setRuntimeInfo(CommonSystemInfo dpaaSystemInfo) {
		if (dpaaSystemInfo != null) {
			Runtime runtime = Runtime.getRuntime();
			int htotal = to1024MegaConversion(runtime.totalMemory());
			dpaaSystemInfo.setHtotal(htotal);
			int hmax = to1024MegaConversion(runtime.maxMemory());
			dpaaSystemInfo.setHmax(hmax);
			int hfree = to1024MegaConversion(runtime.freeMemory());
			dpaaSystemInfo.setHfree(hfree);
			int hused = htotal - hfree;
			dpaaSystemInfo.setHused(hused);
		}
	}

	public CommonSystemInfo getDiskInfo() {
		CommonSystemInfo vo = new CommonSystemInfo();
		setDiskInfo(vo);
		return vo;
	}

	public void setDiskInfo(CommonSystemInfo dpaaSystemInfo) {
		SystemInfo systemInfo = getSystemInfo();
		setDiskInfo(systemInfo, dpaaSystemInfo);
	}

	public void setDiskInfo(SystemInfo systemInfo, CommonSystemInfo dpaaSystemInfo) {


		OperatingSystem os = systemInfo.getOperatingSystem();
		FileSystem fs = os.getFileSystem();
		List<OSFileStore> fsArray = fs.getFileStores();
		OSFileStore osFileStore = null;

		String lastPath = "";
		for (OSFileStore fsStore : fsArray) {
			String mount = fsStore.getMount();
			// 轉成系統預設的路徑分割符號
			mount = CommonFileHelper.filterPath(mount, false);

			// 只監控 digiLogs 所在的磁碟區(可能為mount)
			if (this.appPath.startsWith(mount)) {
				if (mount.length() > lastPath.length()) {
					osFileStore = fsStore;
				}
				lastPath = mount;
			}
		}

		long totalSpace = osFileStore == null ? 0 : osFileStore.getTotalSpace();
		long freeSpace = osFileStore == null ? 0 : osFileStore.getUsableSpace();
		long diskUsed = totalSpace - freeSpace;
		// 為相容於目前設計, 僅取Root "/" (Linux) or "C:\" (Windows)
		String diskFileSystem = "";
		if (osFileStore != null) {
			diskFileSystem = Optional.ofNullable(osFileStore.getMount()).orElse("");
		}

		// 判斷硬碟空間是否為零
		float diskUsagePercent = 0;
		if (totalSpace != 0) {
			diskUsagePercent = new BigDecimal(diskUsed) //
					.divide(new BigDecimal(totalSpace), 4, BigDecimal.ROUND_HALF_UP) //
					.floatValue();
		}

		dpaaSystemInfo.setDused(diskUsed);
		dpaaSystemInfo.setDtotal(totalSpace);
		dpaaSystemInfo.setDfs(diskFileSystem);
		dpaaSystemInfo.setDusage(diskUsagePercent);
		dpaaSystemInfo.setDavail(freeSpace);
		
	}

	private float getCpuUsedRate(OSProcess currentProcess, int logicalProcessorCount) {
		/*
		 * 2021.07.26 改呼叫library的API取得CPU使用率 long currentKernelTime =
		 * currentProcess.getKernelTime(); long currentUserTime =
		 * currentProcess.getUserTime(); long currentUpTime =
		 * currentProcess.getUpTime();
		 * 
		 * long previousKernelTime = 0; long previousUserTime = 0; long previousUpTime =
		 * 0; if (this.previousProcessStatus != null) { previousKernelTime =
		 * this.previousProcessStatus.getKernelTime(); previousUserTime =
		 * this.previousProcessStatus.getUserTime(); previousUpTime =
		 * this.previousProcessStatus.getUpTime(); }
		 * 
		 * this.previousProcessStatus = currentProcess;
		 * 
		 * int diffKernelTime = Math.toIntExact(currentKernelTime - previousKernelTime);
		 * int diffUserTime = Math.toIntExact(currentUserTime - previousUserTime); int
		 * diffUpTime = Math.toIntExact(currentUpTime - previousUpTime);
		 * 
		 * float cpuRate = (100f * (diffKernelTime + diffUserTime) / diffUpTime);
		 * 
		 * if (Float.compare(cpuRate, Float.POSITIVE_INFINITY) == 0){ cpuRate = 100.0f;
		 * }else if (Float.compare(cpuRate, Float.NEGATIVE_INFINITY) == 0 ||
		 * Float.compare(cpuRate, Float.NaN) == 0){ cpuRate = 0.0f; } return
		 * BigDecimal.valueOf(cpuRate).setScale(2, RoundingMode.HALF_UP).floatValue();
		 */

		/*
		 * 2021.08.07 已知 currentProcess.calculateCpuPercent() 得到的值 等於 (diffKernelTime +
		 * diffUserTime) / diffUpTime double cpuRate =
		 * currentProcess.calculateCpuPercent(); return BigDecimal.valueOf(cpuRate) //
		 * .divide(BigDecimal.valueOf(logicalProcessorCount), 4, RoundingMode.HALF_UP)
		 * // .floatValue();
		 */
		return BigDecimal.valueOf(this.osMXBean.getProcessCpuLoad()) //
				.setScale(4, RoundingMode.HALF_UP).floatValue();
	}

	/**
	 * 將 byte 轉為 megabytes (四捨五入為整數)
	 * 
	 * @param value
	 * @return
	 */
	private int to1024MegaConversion(long value) {
		BigDecimal b = new BigDecimal(value);
		b = b.divide(new BigDecimal(1024 * 1024), BigDecimal.ROUND_HALF_UP);
		return b.intValue();
	}

}
