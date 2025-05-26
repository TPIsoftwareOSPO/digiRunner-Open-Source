package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Objects;

public class DPB0234RespItem {
    private String groupId;
    private String groupAlias;
    private String groupName;
    private List<String> clientIdList;
    private List<String> clientNameList;
    private List<DPB0234ApiDataItem> apiDataList;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupAlias() {
        return groupAlias;
    }

    public void setGroupAlias(String groupAlias) {
        this.groupAlias = groupAlias;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getClientIdList() {
        return clientIdList;
    }

    public void setClientIdList(List<String> clientIdList) {
        this.clientIdList = clientIdList;
    }

    public List<String> getClientNameList() {
        return clientNameList;
    }

    public void setClientNameList(List<String> clientNameList) {
        this.clientNameList = clientNameList;
    }

    public List<DPB0234ApiDataItem> getApiDataList() {
        return apiDataList;
    }

    public void setApiDataList(List<DPB0234ApiDataItem> apiDataList) {
        this.apiDataList = apiDataList;
    }

    @Override
    public String toString() {
        return "DPB0234RespItem{" +
                "groupId='" + groupId + '\'' +
                ", groupAlias='" + groupAlias + '\'' +
                ", groupName='" + groupName + '\'' +
                ", clientIdList=" + clientIdList +
                ", clientNameList=" + clientNameList +
                ", apiDataList=" + apiDataList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DPB0234RespItem that = (DPB0234RespItem) o;
        return Objects.equals(groupId, that.groupId); // 使用 groupId 作為唯一識別
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }
}
