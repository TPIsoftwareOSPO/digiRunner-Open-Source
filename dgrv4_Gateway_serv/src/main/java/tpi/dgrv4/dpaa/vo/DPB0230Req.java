package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0230Req {
    private List<String> idList;

    private String enable;

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }
}
