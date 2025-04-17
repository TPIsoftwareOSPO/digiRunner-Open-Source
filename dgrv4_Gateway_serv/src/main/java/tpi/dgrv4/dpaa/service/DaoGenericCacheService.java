package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.gateway.TCP.Packet.ClearDaoCacheProxyPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DaoGenericCacheService {
	private DaoGenericCache daoGenericCache;
	
	@Autowired
	public DaoGenericCacheService(DaoGenericCache daoGenericCache) {
		super();
		this.daoGenericCache = daoGenericCache;
	}

	public void clearAndNotify() {
		if (getDaoGenericCache().getCacheMap() != null) {
			getDaoGenericCache().clear();
			if (TPILogger.lc == null) {
				return;
			}
			TPILogger.tl.info("Client [" + TPILogger.lc.userName + "] IDaoGenericCache is totally cleared.");
			// Notify
			synchronized (TPILogger.lc) {
				try {
					TPILogger.lc.send(new ClearDaoCacheProxyPacket());
				} catch (Exception e) {
					TPILogger.tl.warn(String.format("Failed to notify node to clear dao cache proxy: %s", e.getMessage()));
				}
			}
		}
	}

	protected DaoGenericCache getDaoGenericCache() {
		return daoGenericCache;
	}
	
}
