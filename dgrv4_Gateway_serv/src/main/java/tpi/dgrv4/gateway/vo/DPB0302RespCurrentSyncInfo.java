package tpi.dgrv4.gateway.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DPB0302RespCurrentSyncInfo {

    /**
     * 同步類型：SCHEDULE（排程）/ MANUAL（手動）
     */
    private String syncType;

    private Long scheduleId;
    /**
     * 同步ID
     */
    private String syncId;

    /**
     * 狀態：RUNNING / DONE / ERROR
     */
    private String status;

    /**
     * 錯誤訊息（僅當 status 為 ERROR 時有值）
     */
    private String errorMessage;

    /**
     * 進度：0-100
     */
    private Integer progress;

    /**
     * 當前步驟描述
     */
    private String currentStep;

    /**
     * 來源節點ID
     */
    private String sourceId;

    /**
     * 目標節點ID列表
     */
    private List<String> targetIds;

    /**
     * 開始時間
     */
    private Date startTime;

    /**
     * 已執行時間（秒）
     */
    private Long elapsed;
}
