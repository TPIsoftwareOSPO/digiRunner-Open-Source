package tpi.dgrv4.gateway.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class DbSyncHistory {
    /**
     * 同步類型：SCHEDULE / MANUAL
     */
    private String syncType;

    /**
     * 同步ID
     */
    private Long syncId;

    /**
     * 狀態：COMPLETED / FAILED
     */
    private String status;

    /**
     * 進度：0-100
     */
    private Integer progress;

    /**
     * 最終步驟
     */
    private String currentStep;

    /**
     * 來源節點
     */
    private String sourceId;

    /**
     * 目標節點列表
     */
    private List<String> targetIds;

    /**
     * 開始時間
     */
    private Date startTime;

    /**
     * 結束時間
     */
    private Date endTime;

    /**
     * 執行時長（秒）
     */
    private Integer duration;

    /**
     * 錯誤訊息
     */
    private String errorMessage;
}
