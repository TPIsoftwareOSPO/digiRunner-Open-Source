package tpi.dgrv4.gateway.vo;

import java.util.ArrayList;
import java.util.List;

public class QueryMailLogResp {

    /** 查無資料時為空陣列, 不拋查無資料 */
    private List<QueryMailLogData> dataList = new ArrayList<>();

    public List<QueryMailLogData> getDataList() {
        return dataList;
    }

    public void setDataList(List<QueryMailLogData> dataList) {
        this.dataList = dataList;
    }
}
