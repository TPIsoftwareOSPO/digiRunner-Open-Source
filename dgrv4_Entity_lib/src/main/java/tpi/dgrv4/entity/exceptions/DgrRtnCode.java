package tpi.dgrv4.entity.exceptions;

/**
 * 定義Return Code
 * Tip: 錯誤盡可能地定義明確，以避免定義過多模糊的錯誤，例如下載檔案失敗，可以明確指出是因為檔案不存在而造成的。
 */
public enum DgrRtnCode implements DgrError<DgrException> {
	_0125(DgrModule.DP0,"25","API不存在"),
	SUCCESS(DgrModule.DP, "00", "成功"),
	NO_FILE(DgrModule.DP, "29", "查無檔案"),
	_1191(DgrModule.DP, "91", "資料已被異動"),
	SYSTEM_ERROR(DgrModule.DP, "99", "系統錯誤"),
	_1202(DgrModule.DP2, "02", "查無類型清單"),
	_1219(DgrModule.DP2, "19", "Permission denied"), //沒有權限, John: 這裡要用英文 shutdown API 會用到
	_1227(DgrModule.DP2, "27", "生效日期不可小於今天"),
	_1230(DgrModule.DP2, "30", "角色不存在"),
	_1231(DgrModule.DP2, "31", "使用者不存在"),
	_1239(DgrModule.DP2, "39", "角色代號重複"),
	_1240(DgrModule.DP2, "40", "角色名稱重複"),
	_1241(DgrModule.DP2, "41", "功能不存在 (含locale)"),
	_1251(DgrModule.DP2, "51", "當前排程狀態不允許異動"),
	_1261(DgrModule.DP2, "61", "狀態:必填參數"),
	_1264(DgrModule.DP2, "64", "登入角色不存在"),
	_1284(DgrModule.DP2, "84", "[{{0}}] 不得重複"),
	_1285(DgrModule.DP2, "85", "Return code 參數不符合多國語系定義"),
	_1286(DgrModule.DP2, "86", "更新失敗"),
	_1287(DgrModule.DP2, "87", "刪除失敗"),
	_1289(DgrModule.DP2, "89", "查無 locale [{{0}}] 的 rtn code [{{1}}] 訊息"),
	_1288(DgrModule.DP2, "88", "新增失敗"),
	_1290(DgrModule.DP2, "90", "參數錯誤"),
	_1292(DgrModule.DP2, "92", "工作佇列已滿, 請稍後再執行"),
	_1293(DgrModule.DP2, "93", "資料庫錯誤"),
	_1295(DgrModule.DP2, "95", "日期格式不正確"),
	_1296(DgrModule.DP2, "96", "缺少必填參數"),
	_1297(DgrModule.DP2, "97", "執行錯誤"),
	_1298(DgrModule.DP2, "98", "查無資料"),
	_1300(DgrModule.DP3, "00", "角色代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1301(DgrModule.DP3, "01", "角色代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白"),
	_1302(DgrModule.DP3, "02", "角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1309(DgrModule.DP3, "09", "功能清單:必填參數"),
	_1344(DgrModule.DP3, "44", "用戶端不存在"),
	_1350(DgrModule.DP3, "50", "[{{0}}] 為必填欄位"),
	_1351(DgrModule.DP3, "51", "[{{0}}] 長度限制 [{{1}}] 字內，您輸入[{{2}}] 個字"),
	_1352(DgrModule.DP3, "52", "[{{0}}] 格式不正確"),
	_1355(DgrModule.DP3, "55", "[{{0}}] 不得小於 {{1}}, 您輸入 {{2}}"),
	_1356(DgrModule.DP3, "56", "[{{0}}] 不得大於 {{1}}, 您輸入 {{2}}"),
	_1357(DgrModule.DP3, "57", "您的角色並未授權使用 API txID [{{0}}]"),
	_1384(DgrModule.DP3, "84", "[{{0}}] 長度至少須 [{{1}}] 字，您輸入[{{2}}] 個字"),
	_1406(DgrModule.DP4, "06", "[{{0}}] 數量不可少於 [{{1}}]，您選擇 [{{2}}]"),
	_1407(DgrModule.DP4, "07", "[{{0}}] 數量不可超過 [{{1}}]，您選擇 [{{2}}]"),
	_1410(DgrModule.DP4, "10", "群組[{{0}}]不存在"),
	_1433(DgrModule.DP4, "33", "非對稱式加密失敗：[{{0}}]"),
	_1434(DgrModule.DP4, "34", "非對稱式解密失敗：[{{0}}]"),
	_1472(DgrModule.DP4, "72", "使用者已鎖定"),
	_1474(DgrModule.DP4, "74", "設定檔缺少參數 [{{0}}]"),
	_1491(DgrModule.DP4, "91", "執行工作不存在: {{0}}"),
	_1510(DgrModule.DP5, "10", "驗證失敗"),
	_1511(DgrModule.DP5, "11", "LDAP未啟用"),
	_1512(DgrModule.DP5, "12", "連線失敗"),
	_1514(DgrModule.DP5, "14", "您所在的網段,無法登入"),
	_1515(DgrModule.DP5, "15", "單個用戶不能存在於多個群組中"),
	_1516(DgrModule.DP5, "16", "使用者沒有任何群組"),
	_1541(DgrModule.DP5, "41", "Token已失效"),
	_2000(DgrModule.DP10, "00", "必填"),
	_2001(DgrModule.DP10, "01", "最大長度為 [{{0}}]"),
	_2002(DgrModule.DP10, "02", "最小長度為 [{{0}}]"),
	_2003(DgrModule.DP10, "03", "必須包含 [{{0}}]"),
	_2004(DgrModule.DP10, "04", "不得包含 [{{0}}]"),
	_2005(DgrModule.DP10, "05", "數值不可大於 [{{0}}]"),
	_2006(DgrModule.DP10, "06", "數值不可小於 [{{0}}]"),
	_2007(DgrModule.DP10, "07", "格式不正確"),
	_2008(DgrModule.DP10, "08", "僅可輸入英數字、底線「_」及橫線「-」"),
	_2009(DgrModule.DP10, "09", "最少選擇 [{{0}}] 項"),
	_2010(DgrModule.DP10, "10", "最多選擇 [{{0}}] 項"),
	_1497(DgrModule.DP4, "97", "介接規格錯誤：[{{0}}] - [{{1}}]"),
	_9901(DgrModule.DP99, "01", "System error"),
	_9906(DgrModule.DP99, "06", "Client requests exceeds TPS limit"),
	_9912(DgrModule.DP99, "12", "API was disabled"),	
	_9926(DgrModule.DP99, "26", "Invalid Character"),
	_9930(DgrModule.DP99, "30", "Invalid String"),
	_9978(DgrModule.DP99, "78", "不合法的 Host Header"),
    ;

	private DgrModule module;
    private String seq;
    private String defaultMessage;

    private DgrRtnCode(DgrModule module, String seq, String defaultMessage) {
    	this.module = module;
        this.seq = seq;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public DgrModule getModule() {
    	return this.module;
    }

    @Override
    public String getSeq() {
        return this.seq;
    }

    @Override
    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    public static DgrRtnCode parse(String code) {
		for(DgrRtnCode item : DgrRtnCode.values()) {
			if(item.getCode().equals(code)) {
				return item;
			}
		}
		return null;
	}

}
