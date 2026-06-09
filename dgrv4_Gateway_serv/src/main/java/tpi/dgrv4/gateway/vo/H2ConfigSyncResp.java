package tpi.dgrv4.gateway.vo;

import lombok.Data;

@Data
public class H2ConfigSyncResp {
    private String filePath;
    private boolean success;
    private Exception errorMessage;
    // 添加這個方法來設置結果並釋放等待
    public void setResult(boolean success, String filePath, Exception errorMessage) {
        this.success = success;
        this.filePath = filePath;
        this.errorMessage = errorMessage;
    }


}
