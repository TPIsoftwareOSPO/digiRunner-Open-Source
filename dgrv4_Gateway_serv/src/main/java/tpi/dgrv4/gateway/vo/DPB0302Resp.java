package tpi.dgrv4.gateway.vo;

import lombok.Data;

import java.util.List;

@Data
public class DPB0302Resp {
    private List<DPB0302RespItem> nodeInfoList;
    private Boolean btnStatus;
    private DPB0302RespCurrentSyncInfo currentSync;
}
