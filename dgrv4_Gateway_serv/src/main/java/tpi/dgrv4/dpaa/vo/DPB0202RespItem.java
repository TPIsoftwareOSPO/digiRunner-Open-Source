package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0202RespItem {

	/** 轉換後的ID, YYYYMMDD格式 EX: 20230425_b52f599d */
	private String id;

	/** ID, long格式 EX: 2315699193398647197 */
	private String longId;

	/** digiRunner 的 client_id */
	private String clientId;

	/** 狀態, Y: 啟用, N: 停用 */
	private String status;

	/** 說明 */
	private String remark;

	/** RDB連線資訊的名稱 */
	private String connectionName;

	/** 查詢RDB的SQL(Prepare Statement) */
	private String sqlPtmt;

	/** 圖示檔案, 有做 Base64Encode */
	private String iconFile;

	/** 登入頁標題 */
	private String pageTitle;

	/** 建立日期 */
	private Date createDateTime;

	/** 建立人員 */
	private String createUser;

	/** 更新日期 */
	private Date updateDateTime;

	/** 更新人員 */
	private String updateUser;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getSqlPtmt() {
		return sqlPtmt;
	}

	public void setSqlPtmt(String sqlPtmt) {
		this.sqlPtmt = sqlPtmt;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
}
