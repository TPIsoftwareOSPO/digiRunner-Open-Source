package tpi.dgrv4.gateway.vo;

import lombok.Data;

import java.util.List;
@Data
public class DPB0303Req {
    /**
     * 來源節點 ID
     */
    private String sourceId;

    /**
     * 目標節點 ID 列表
     */
    private List<String> targetIds;
}
