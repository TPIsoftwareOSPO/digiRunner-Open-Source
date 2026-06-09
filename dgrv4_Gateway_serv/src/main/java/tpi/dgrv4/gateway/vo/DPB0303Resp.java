package tpi.dgrv4.gateway.vo;

import lombok.Data;

@Data
public class DPB0303Resp {
    private Boolean success;
    private String message;
    private Long syncId;


    public static DPB0303Resp success(Long syncId) {
        DPB0303Resp resp = new DPB0303Resp();
        resp.setSuccess(true);
        resp.setMessage("SYNCING HAS STARTED");
        resp.setSyncId(syncId);
        return resp;
    }


    public static DPB0303Resp fail(String message) {
        DPB0303Resp resp = new DPB0303Resp();
        resp.setSuccess(false);
        resp.setMessage(message);
        return resp;
    }

}
