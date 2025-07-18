package tpi.dgrv4.gateway.vo;

import lombok.Data;

@Data
public class ActionRecord {
    private String action;
    private Boolean success;
    private long actionTime;
    private String nodeName;
    private String operationType;
    private String msg;
    private String mainJobId;
    private String targetDbConnect;

    public ActionRecord(String action, Boolean success, long actionTime, String nodeName, String operationType, String msg, String mainJobId, String targetDbConnect) {
        this.action = action;
        this.success = success;
        this.actionTime = actionTime;
        this.nodeName = nodeName;
        this.operationType = operationType;
        this.msg = msg;
        this.mainJobId = mainJobId;
        this.targetDbConnect = targetDbConnect;
    }

    // Getter 和 Setter 方法
}
