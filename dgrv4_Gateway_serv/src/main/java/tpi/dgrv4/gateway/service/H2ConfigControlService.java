package tpi.dgrv4.gateway.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class H2ConfigControlService {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private HotSwappableTargetSource targetSource;
    
    private final Lock swapLock = new ReentrantLock();

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DataSourceProperties properties;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private ConfigurableApplicationContext applicationContext;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private GenericCache genericCache;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DaoGenericCache daoGenericCache;

    public String suspendDataSource() {
        try {
            HikariDataSource current = (HikariDataSource) getTargetSource().getTarget();

            if (current != null && !current.isClosed()) {
                // 1. First, get a connection to execute the H2 SHUTDOWN command.
                try (Connection conn = current.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("SHUTDOWN COMPACT");
                } catch (Exception e) {
                    // The connection is expected to be dropped after SHUTDOWN is executed. This is normal.
                    TPILogger.tl.info("H2Config shutdown command triggered expected exception: " + e.getMessage());
                }

                // 2. Then, close the connection pool.
                current.close();
            }
            return "H2Config shutdown gracefully. File is safe to copy.";
        } catch (Exception e) {
            TPILogger.tl.error("Error suspending data source: " + e);
            return "Error suspending: " + e.getMessage();
        }
    }

    /**
     * Recreates and enables the database connection pool based on the current settings.
     */
    public void resumeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getProperties().getUrl());
        config.setUsername(getProperties().getUsername());
        config.setPassword(getProperties().getPassword());
        config.setDriverClassName(getProperties().getDriverClassName());

        HikariDataSource oldDataSource;

        // Use a ReentrantLock which is virtual-thread-friendly
        swapLock.lock();
        try {
            HikariDataSource newDataSource = new HikariDataSource(config);
            oldDataSource = (HikariDataSource) getTargetSource().swap(newDataSource);
            TPILogger.tl.info("Data source hot-swapped successfully. New connections will use the reloaded database.");
        } finally {
            swapLock.unlock();
        }

        // Gracefully close the old data source in a separate thread to avoid blocking.
        if (oldDataSource != null) {
            new Thread(() -> {
                TPILogger.tl.info("Gracefully shutting down the old data source...");
                oldDataSource.close();
                TPILogger.tl.info("Old data source shut down successfully.");
            }).start();
        }

        // Asynchronously clear caches to avoid blocking new requests.
        new Thread(() -> {
            TPILogger.tl.warn("Clearing all application caches in the background after database failback...");
            getGenericCache().clear();
            getDaoGenericCache().clear();
            TPILogger.tl.warn("Background cache clearing completed.");
        }).start();
    }


    public void poolSoftEvict() {
        try {
            HikariDataSource hikaridataSource = getApplicationContext().getBean(HikariDataSource.class);
            if (hikaridataSource == null) {
                throw TsmpDpAaRtnCode._1559.throwing("HikariDataSource not found.");
            }

            HikariPoolMXBean pool = hikaridataSource.getHikariPoolMXBean();
            if (pool == null) {
                throw TsmpDpAaRtnCode._1559.throwing("HikariPoolMXBean not available.");
            }

            pool.softEvictConnections();
            TPILogger.tl.info("Soft restart: HikariCP connections evicted.");
        } catch (Exception ex) {
            throw TsmpDpAaRtnCode._1559.throwing("Soft restart error :" + ex);
        }
    }
}