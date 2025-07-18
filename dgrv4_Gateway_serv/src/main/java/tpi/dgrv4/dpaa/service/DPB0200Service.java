package tpi.dgrv4.dpaa.service;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0200Req;
import tpi.dgrv4.dpaa.vo.DPB0200Resp;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * [ZH] 測試RDB連線資訊
 * [EN] Test RDB connection information
 */

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class DPB0200Service {
	
	private final TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
	private final TsmpSettingService tsmpSettingService;
	private final ConfigurableApplicationContext configurableApplicationContext;
	private final DPB0189Service dpb0189Service;
	
	private final static String SUCCESS = "Connect success";
	private final static String FAIL = "Connect fail";

	public DPB0200Resp testRdbConnection(TsmpAuthorization authorization, DPB0200Req req) {
		DPB0200Resp resp = new DPB0200Resp();
		
		String msg = null;
		Boolean success = false;
		String jdbcUrl = req.getJdbcUrl();
		String username = req.getUserName();
		String mima = req.getMima();
		mima = getDpb0189Service().decryptMima(mima);
		String connName = req.getConnName();
		boolean isDefaultDb = "APIM-default-DB".equalsIgnoreCase(connName);
		HikariConfig config = null;
		
		//如果不是預設DB就設定連線資訊
		if(!isDefaultDb) {
			if (!StringUtils.hasText(jdbcUrl))
				throw TsmpDpAaRtnCode._1350.throwing("{{jdbcUrl}}");

			if (!StringUtils.hasText(username)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{username}}");
			}
			config = new HikariConfig();
			config.setJdbcUrl(jdbcUrl);
			config.setUsername(username);
			config.setPassword(getTsmpSettingService().getENCPlainVal(mima));
//			//設定測試多次
//			config.setInitializationFailTimeout(1);
//			config.setMaximumPoolSize(2);
		}

		try {
			HikariDataSource dataSource = null;
			if(isDefaultDb) {
				//取得現有的DB連線
				dataSource = getConfigurableApplicationContext().getBean(HikariDataSource.class);
			}else {
				dataSource = new HikariDataSource(config);
			}
			
			//測試連線
			try (Connection connection = dataSource.getConnection()) {
				TPILogger.tl.debug("jdbcUrl=" + dataSource.getJdbcUrl());
				TPILogger.tl.debug("username=" + dataSource.getUsername());
				if (connection.isValid(5000)) {
					msg = SUCCESS;
					success = true;
				} else {
					msg = FAIL;
					success = false;
				}
			} catch (SQLException e) {
				msg = e.getMessage();
				success = false;
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			} finally {
				if(!isDefaultDb) {
					dataSource.close();
				}
			}
		} catch (Exception e) {
			msg = e.getMessage();
			success = false;
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		resp.setMsg(msg);
		resp.setSuccess(success);
		return resp;
	}
 
	protected TsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
		return this.tsmpCoreTokenHelperCacheProxy;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ConfigurableApplicationContext getConfigurableApplicationContext() {
		return configurableApplicationContext;
	}
	
	
}
