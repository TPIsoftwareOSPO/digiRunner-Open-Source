package tpi.dgrv4.dpaa.constant;

public class TsmpNodeTaskNames {

	private static final String PREFIX = "tsmp.smd.ag.";

	public static final String DEPLOY_CONTAINER_ENABLE = PREFIX + "dc.enable";

	public static final String DEPLOY_CONTAINER_DISABLE = PREFIX + "dc.disable";

	public static final String DEPLOY_CONTAINER_REFRESH = PREFIX + "dc.refresh";

	public static final String DEFAULT_DC_REFRESH = PREFIX + "ddc.refresh";
	
	public static final String Clean_AllCache = "tsmp.ncr.cleanNodeAllCache";
	
	public static final String Clean_CacheByKey = "tsmp.ncr.cleanNodeCacheByKey";
	
	public static final String Clean_CacheByTableName = "tsmp.ncr.cleanNodeCacheByTableName";
	
	public static final String LOGOUT = "tsmp.smd.tg.token.logout";

}