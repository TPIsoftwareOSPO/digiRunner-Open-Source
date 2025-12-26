package tpi.dgrv4.common.constant;

public enum TsmpDpModule {
    DP("11", "tsmpdpaa", "digiRunner", "digiRunner"),
    DP2("12", "tsmpdpaa", "digiRunner", "digiRunner"),
    DP3("13", "tsmpdpaa", "digiRunner", "digiRunner"),
    DP4("14", "tsmpdpaa", "digiRunner", "digiRunner"),
    DP5("15", "tsmpdpaa", "digiRunner", "digiRunner"),
    DP10("20", "tsmpdpaa", "digiRunner", "digiRunner")
    ;

	private String groupId;
    private String name;
    private String engDesc;
    private String chiDesc;

    private TsmpDpModule(String groupId, String name, String engDesc, String chiDesc){
        this.groupId = groupId;
        this.name = name;
        this.engDesc = engDesc;
        this.chiDesc = chiDesc;
    }

    public String getGroupId() {
        return this.groupId;
    }
   
    public String getName() {
    	return this.name;
    }

    public String getEngDesc() {
    	return this.engDesc;
    }

    public String getChiDesc() {
    	return this.chiDesc;
    }
}
