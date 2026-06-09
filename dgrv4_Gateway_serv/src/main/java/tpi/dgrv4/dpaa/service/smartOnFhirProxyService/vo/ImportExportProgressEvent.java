package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

/**
 * 匯入/匯出進度事件
 * 用於 SSE 事件傳遞進度資訊
 */
public class ImportExportProgressEvent {

    // 事件類型
    private String type;              // "progress", "complete", "error"
    
    // 進度資訊
    private Integer current;          // 當前處理筆數
    private Integer total;            // 總筆數
    private Integer percentage;       // 百分比（0-100）
    
    // 狀態與訊息
    private String status;            // "processing", "success", "failed"
    private String message;           // 狀態描述訊息
    
    // 資料內容（根據類型不同而異）
    private Object data;
    
    // 統計資訊（完成時使用）
    private Integer successCount;
    private Integer failureCount;
    private Integer totalCount;
    private String summary;
    
    private String fileName;
    
    public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ImportExportProgressEvent() {
    }

    public ImportExportProgressEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "ImportExportProgressEvent [type=" + type + ", current=" + current + ", total=" + total
                + ", percentage=" + percentage + ", status=" + status + ", message=" + message + ", data=" + data
                + ", successCount=" + successCount + ", failureCount=" + failureCount + ", totalCount=" + totalCount
                + ", summary=" + summary + "]";
    }
}

