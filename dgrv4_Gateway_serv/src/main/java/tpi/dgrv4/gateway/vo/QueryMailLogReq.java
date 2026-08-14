package tpi.dgrv4.gateway.vo;

import java.util.List;

public class QueryMailLogReq {

    /** 必填, 1~100 筆; 對應 TSMP_DP_MAIL_LOG.REF_CODE */
    private List<String> refCodes;

    /** 選填, yyyy/MM/dd; 用於縮小掃描範圍 */
    private String startDate;

    /** 選填, yyyy/MM/dd; 用於縮小掃描範圍 */
    private String endDate;

    public List<String> getRefCodes() {
        return refCodes;
    }

    public void setRefCodes(List<String> refCodes) {
        this.refCodes = refCodes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "QueryMailLogReq [refCodes=" + refCodes + ", startDate=" + startDate + ", endDate=" + endDate + "]";
    }
}
