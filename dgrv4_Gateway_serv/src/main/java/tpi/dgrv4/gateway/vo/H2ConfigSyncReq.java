package tpi.dgrv4.gateway.vo;

import lombok.Data;

import java.util.List;

@Data
public class H2ConfigSyncReq {

    private String sourceId;
    private List<String> targetIds;
    private String filePath;

}
