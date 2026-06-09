package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;
import java.util.Map;

/**
 * 匯入驗證結果 DTO
 * 用於預前匯入驗證，回報每筆資料的驗證結果
 */
public class ImportValidationResultDto {

    // UUID（可選，由請求方傳入，原樣回傳；若請求未傳入則為 null）
    private String uuid;

    // 驗證結果
    private Boolean success; // true: 驗證通過, false: 驗證失敗
    private String errorMessage; // 錯誤訊息（驗證失敗時）
    private List<String> errorDetails; // 詳細錯誤列表

    // 操作類型
    private String operationType; // "create" 或 "update"

    // 資料識別
    private String sofProxyId; // Proxy ID（更新時才有）
    private String sofProxyName; // Proxy 名稱

    // Version 衝突資訊（僅更新且有衝突時）
    private Boolean hasVersionConflict;
    private String currentVersion; // DB 中的 version
    private String importVersion; // 匯入資料的 version

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getSofProxyId() {
        return sofProxyId;
    }

    public void setSofProxyId(String sofProxyId) {
        this.sofProxyId = sofProxyId;
    }

    public String getSofProxyName() {
        return sofProxyName;
    }

    public void setSofProxyName(String sofProxyName) {
        this.sofProxyName = sofProxyName;
    }

    public Boolean getHasVersionConflict() {
        return hasVersionConflict;
    }

    public void setHasVersionConflict(Boolean hasVersionConflict) {
        this.hasVersionConflict = hasVersionConflict;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getImportVersion() {
        return importVersion;
    }

    public void setImportVersion(String importVersion) {
        this.importVersion = importVersion;
    }

    /**
     * Version 衝突欄位詳情
     */
    public static class VersionConflictDetail {
        private Object currentValue; // DB 中的值
        private Object importValue; // 匯入的值

        public VersionConflictDetail() {
        }

        public VersionConflictDetail(Object currentValue, Object importValue) {
            this.currentValue = currentValue;
            this.importValue = importValue;
        }

        public Object getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(Object currentValue) {
            this.currentValue = currentValue;
        }

        public Object getImportValue() {
            return importValue;
        }

        public void setImportValue(Object importValue) {
            this.importValue = importValue;
        }

        @Override
        public String toString() {
            return "VersionConflictDetail [currentValue=" + currentValue + ", importValue=" + importValue + "]";
        }
    }

    @Override
    public String toString() {
        return "ImportValidationResultDto [uuid=" + uuid + ", success=" + success + ", errorMessage=" + errorMessage
                + ", errorDetails=" + errorDetails + ", operationType=" + operationType + ", sofProxyId=" + sofProxyId
                + ", sofProxyName=" + sofProxyName + ", hasVersionConflict=" + hasVersionConflict
                + ", currentVersion=" + currentVersion + ", importVersion=" + importVersion + "]";
    }
}
