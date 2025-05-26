package tpi.dgrv4.entity.exceptions;

public enum DgrModule {
	
	DP0("01", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP("11", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP2("12", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP3("13", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP4("14", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP5("15", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP10("20", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW),
	DP99("99", DgrModule.TSMPDPAA, DgrModule.DGR_EN, DgrModule.DGR_TW), 
	;

	private static final String TSMPDPAA = "tsmpdpaa";
	private static final String DGR_EN = "digiRunner Developer Portal";
	private static final String DGR_TW = "digiRunner-入口網";
	private String groupId;
    private String name;
    private String engDesc;
    private String chiDesc;

    private DgrModule(String groupId, String name, String engDesc, String chiDesc){
        this.groupId = groupId;
        this.engDesc = engDesc;
        this.name = name;
        this.chiDesc = chiDesc;
    }

    public String getGroupId() {
        return this.groupId;
    }
   
    public String getEngDesc() {
    	return this.engDesc;
    }

    public String getChiDesc() {
    	return this.chiDesc;
    }
    
    public String getName() {
    	return this.name;
    }

}
