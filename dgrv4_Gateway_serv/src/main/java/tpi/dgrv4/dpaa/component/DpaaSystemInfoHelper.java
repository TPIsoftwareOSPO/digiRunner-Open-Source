package tpi.dgrv4.dpaa.component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;

import org.apache.catalina.User;

import com.sun.management.OperatingSystemMXBean;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DpaaSystemInfo;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class DpaaSystemInfoHelper {
    private static class InstanceHolder {
        private static final DpaaSystemInfoHelper INSTANCE = new DpaaSystemInfoHelper();
    }
    
    private final String appPath;
    private final OperatingSystemMXBean osMXBean;
    private final SystemInfo systemInfo;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<DpaaSystemInfo> cachedInfo;

    private DpaaSystemInfoHelper() {
        this.appPath = FileHelper.filterPath(new File(".").getAbsolutePath(), false);
        this.osMXBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
        this.systemInfo = new SystemInfo();
        this.cachedInfo = new AtomicReference<>(new DpaaSystemInfo());
        
        // 使用守護線程來執行定期更新
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "SystemInfoUpdater");
            thread.setDaemon(true);
            return thread;
        };
        
        this.scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        
        // 註冊 JVM 關閉鉤子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
            	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
                scheduler.shutdownNow();
    		    Thread.currentThread().interrupt();
            }
        }));
        
        // 啟動定期更新任務
        startUpdateTask();
    }

    public static DpaaSystemInfoHelper getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private void startUpdateTask() {
        // 初始化時立即更新一次
        updateSystemInfo();
        
        
//        scheduler.scheduleAtFixedRate(
//        	    要執行的任務,
//        	    等待多久才開始第一次執行,
//        	    每次執行的間隔時間,
//        	    時間單位
//        	)
        // 每5秒更新一次
        scheduler.scheduleAtFixedRate(this::updateSystemInfo, 5, 5, TimeUnit.SECONDS);
    }

    private void updateSystemInfo() {
        try {
            DpaaSystemInfo newInfo = new DpaaSystemInfo();
            updateCpuAndMem(newInfo);
            updateDiskInfo(newInfo);
            updateRuntimeInfo(newInfo);
            cachedInfo.set(newInfo);
        } catch (Exception e) {
            TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
        }
    }

    public DpaaSystemInfo getCachedSystemInfo() {
        return cachedInfo.get();
    }

    // 原有的公開方法，使用快取資料
    public void setCpuUsedRateAndMem(DpaaSystemInfo infoVo) {
        if (infoVo != null) {
            DpaaSystemInfo cached = cachedInfo.get();
            infoVo.setCpu(cached.getCpu());
            infoVo.setMem(cached.getMem());
        }
    }

    public void setDiskInfo(DpaaSystemInfo infoVo) {
        if (infoVo != null) {
            DpaaSystemInfo cached = cachedInfo.get();
            infoVo.setDused(cached.getDused());
            infoVo.setDtotal(cached.getDtotal());
            infoVo.setDfs(cached.getDfs());
            infoVo.setDusage(cached.getDusage());
            infoVo.setDavail(cached.getDavail());
        }
    }

    public void setRuntimeInfo(DpaaSystemInfo infoVo) {
        if (infoVo != null) {
            DpaaSystemInfo cached = cachedInfo.get();
            infoVo.setHtotal(cached.getHtotal());
            infoVo.setHmax(cached.getHmax());
            infoVo.setHfree(cached.getHfree());
            infoVo.setHused(cached.getHused());
        }
    }

    private static final double THRESHOLD = 0.85; // 85% 記憶體使用率警戒線
    // 內部更新方法
    private void updateCpuAndMem(DpaaSystemInfo dpaaSystemInfo) {
        try {
            OperatingSystem os = systemInfo.getOperatingSystem();
            int processId = os.getProcessId();
            OSProcess currentProcess = os.getProcess(processId);
            int logicalProcessorCount = systemInfo.getHardware().getProcessor().getLogicalProcessorCount();
            float cpu = getCpuUsedRate(currentProcess, logicalProcessorCount);
            int mem = to1024MegaConversion(currentProcess.getResidentSetSize());
            dpaaSystemInfo.setCpu(cpu);
            dpaaSystemInfo.setMem(mem);
        } catch (Exception e) {
            dpaaSystemInfo.setCpu(0f);
            dpaaSystemInfo.setMem(0);
            TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
        }
    }
    
    private long getUsedMemorySimple() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void updateDiskInfo(DpaaSystemInfo dpaaSystemInfo) {
        OperatingSystem os = systemInfo.getOperatingSystem();
        FileSystem fs = os.getFileSystem();
        List<OSFileStore> fsArray = fs.getFileStores();
        OSFileStore osFileStore = null;

        String lastPath = "";
        for (OSFileStore fsStore : fsArray) {
            String mount = FileHelper.filterPath(fsStore.getMount(), false);
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
        String diskFileSystem = "";
        if (osFileStore != null) {
            diskFileSystem = Optional.ofNullable(osFileStore.getMount()).orElse("");
        }

        float diskUsagePercent = 0;
        if (totalSpace != 0) {
            diskUsagePercent = new BigDecimal(diskUsed)
                    .divide(new BigDecimal(totalSpace), 4, BigDecimal.ROUND_HALF_UP)
                    .floatValue();
        }

        dpaaSystemInfo.setDused(diskUsed);
        dpaaSystemInfo.setDtotal(totalSpace);
        dpaaSystemInfo.setDfs(diskFileSystem);
        dpaaSystemInfo.setDusage(diskUsagePercent);
        dpaaSystemInfo.setDavail(freeSpace);
    }

    private void updateRuntimeInfo(DpaaSystemInfo dpaaSystemInfo) {
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
            dpaaSystemInfo.setMetaspacePercent(monitorMetaspace());
        }
    }
    
    private double monitorMetaspace() {
        try {
			MemoryUsage metaspace = ManagementFactory.getMemoryPoolMXBeans().stream()
					.filter(bean -> "Metaspace".equals(bean.getName())).findFirst()
					.orElseThrow(() -> new RuntimeException("Metaspace memory pool not found")).getUsage();

			var usedMB = metaspace.getUsed() / (1024 * 1024);
			var committedMB = metaspace.getCommitted() / (1024 * 1024);
			var maxMB = metaspace.getMax() / (1024 * 1024); // 你設置的 512MB
			
	        // 改用與最大容量的比較
	        var usagePercentage = (double) metaspace.getUsed() / metaspace.getMax() * 100;
                          
            if (usagePercentage > 80) {
				TPILogger.tl.warn("""
						\n\tMetaspace.getUsed()::\t%d
						\tMetaspace.Committed()::\t%d
						\tMetaspace.getMax()::\t\t%d
						\tMetaspace usage is high!::\t%.2f%%
						""".formatted(usedMB, committedMB, maxMB, usagePercentage));
            }
            return usagePercentage;
        } catch (Exception e) {
        		TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
        }
        return 0.0;
    }

    private float getCpuUsedRate(OSProcess currentProcess, int logicalProcessorCount) {
        return BigDecimal.valueOf(this.osMXBean.getProcessCpuLoad())
                .setScale(4, RoundingMode.HALF_UP).floatValue();
    }

    private int to1024MegaConversion(long value) {
        BigDecimal b = new BigDecimal(value);
        b = b.divide(new BigDecimal(1024 * 1024), BigDecimal.ROUND_HALF_UP);
        return b.intValue();
    }
}