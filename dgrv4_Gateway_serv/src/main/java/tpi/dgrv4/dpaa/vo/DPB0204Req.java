package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0204Req extends ReqValidator {
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

	/** 查詢RDB的SQL參數 */
	private String sqlParams;

	/** RDB存放密碼所使用的演算法 */
	private String userMimaAlg;

	/** RDB的密碼欄位名稱 */
	private String userMimaColName;

	/** ID token 的 sub(唯一值) 值,對應RDB的欄位 */
	private String idtSub;

	/** ID token 的 name 值,對應RDB的欄位 */
	private String idtName;

	/** ID token 的 email 值,對應RDB的欄位 */
	private String idtEmail;

	/** ID token 的 picture 值,對應RDB的欄位 */
	private String idtPicture;

	/** 圖示檔案, 有做 Base64Encode */
	private String iconFile;

	/** 登入頁標題 */
	private String pageTitle;

	@Override
	public String toString() {
		return "DPB0204Req [clientId=" + clientId + ", status=" + status + ", remark=" + remark + ", connectionName="
				+ connectionName + ", sqlPtmt=" + sqlPtmt + ", sqlParams=" + sqlParams + ", userMimaAlg=" + userMimaAlg
				+ ", userMimaColName=" + userMimaColName + ", idtSub=" + idtSub + ", idtName=" + idtName + ", idtEmail="
				+ idtEmail + ", idtPicture=" + idtPicture + ", iconFile=" + iconFile + ", pageTitle=" + pageTitle + "]";
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

	public String getSqlParams() {
		return sqlParams;
	}

	public void setSqlParams(String sqlParams) {
		this.sqlParams = sqlParams;
	}

	public String getUserMimaAlg() {
		return userMimaAlg;
	}

	public void setUserMimaAlg(String userMimaAlg) {
		this.userMimaAlg = userMimaAlg;
	}

	public String getUserMimaColName() {
		return userMimaColName;
	}

	public void setUserMimaColName(String userMimaColName) {
		this.userMimaColName = userMimaColName;
	}

	public String getIdtSub() {
		return idtSub;
	}

	public void setIdtSub(String idtSub) {
		this.idtSub = idtSub;
	}

	public String getIdtName() {
		return idtName;
	}

	public void setIdtName(String idtName) {
		this.idtName = idtName;
	}

	public String getIdtEmail() {
		return idtEmail;
	}

	public void setIdtEmail(String idtEmail) {
		this.idtEmail = idtEmail;
	}

	public String getIdtPicture() {
		return idtPicture;
	}

	public void setIdtPicture(String idtPicture) {
		this.idtPicture = idtPicture;
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

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("status")
				.isRequired()
				.pattern(RegexpConstant.Y_OR_N, TsmpDpAaRtnCode._2007.getCode(), null)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("connectionName")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("sqlPtmt")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("sqlParams")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("userMimaAlg")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("userMimaColName")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("idtSub")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("pageTitle")
				.isRequired()
				.build(),
		});
	}
}
