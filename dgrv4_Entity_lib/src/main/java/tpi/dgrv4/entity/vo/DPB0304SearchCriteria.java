package tpi.dgrv4.entity.vo;

import lombok.Data;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;

import java.util.Map;


@Data
public class DPB0304SearchCriteria {

    private String syncType;

    /**
     * 狀態過濾
     * - R: 執行中
     * - D: 成功
     * - E: 失敗
     * - null: 查詢全部
     */
    private String status;
    private String[] keyword;

    private DgrH2ConfigSyncHistory lastId;
    private Integer pageSize;

    private Map<String, String> sortBy;
}