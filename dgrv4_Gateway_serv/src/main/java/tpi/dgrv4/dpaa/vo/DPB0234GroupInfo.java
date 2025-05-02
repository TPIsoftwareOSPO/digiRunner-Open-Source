package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0234GroupInfo {
    private String groupId;
    private String groupName;
    private String groupAlias;
    private List<DPB0234ApiDataItem> apiDataList;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAlias() {
        return groupAlias;
    }

    public void setGroupAlias(String groupAlias) {
        this.groupAlias = groupAlias;
    }

    public List<DPB0234ApiDataItem> getApiDataList() {
        return apiDataList;
    }

    public void setApiDataList(List<DPB0234ApiDataItem> apiDataList) {
        this.apiDataList = apiDataList;
    }
}
