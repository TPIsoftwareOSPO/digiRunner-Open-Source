package tpi.dgrv4.dpaa.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class DPB0304Req {
    private Map<String, String> sortBy;
    private String lastSyncId;
    private String syncType;
    private String status;
    private String keyword;
}
