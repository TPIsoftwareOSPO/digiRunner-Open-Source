package tpi.dgrv4.dpaa.component.req;

public class DpReqServiceUpdateReq_D3 extends DpReqServiceUpdateReq {

	/** 新的用戶ID */
	private String _clientId;

	/** 新的用戶名稱 */
	private String _clientName;

	/** 新的用戶電子郵件, 可設定多組email，用逗號分隔 */
	private String _emails;

	/** 新的密碼, 由 Javascript.Base64encode 後傳來 */
	private String _clientBlock;

	/** 開放權限, 使用Bcrypt設計, itemNo = 'API_AUTHORITY' */
	private String _encPublicFlag;

	public DpReqServiceUpdateReq_D3() {
	}

	public void set_clientId(String _clientId) {
		this._clientId = _clientId;
	}

	public String get_clientName() {
		return _clientName;
	}

	public void set_clientName(String _clientName) {
		this._clientName = _clientName;
	}

	public String get_emails() {
		return _emails;
	}
	
	public String get_clientId() {
		return _clientId;
	}

	public void set_emails(String _emails) {
		this._emails = _emails;
	}

	public String get_clientBlock() {
		return _clientBlock;
	}

	public void set_clientBlock(String _clientBlock) {
		this._clientBlock = _clientBlock;
	}

	public void set_encPublicFlag(String _encPublicFlag) {
		this._encPublicFlag = _encPublicFlag;
	}

	public String get_encPublicFlag() {
		return _encPublicFlag;
	}
}
