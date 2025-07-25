package tpi.dgrv4.common.constant;

import tpi.dgrv4.common.exceptions.ITsmpDpAaError;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;

/**
 * 定義Return Code
 * Tip: 錯誤盡可能地定義明確，以避免定義過多模糊的錯誤，例如下載檔案失敗，可以明確指出是因為檔案不存在而造成的。
 */
public enum TsmpDpAaRtnCode implements ITsmpDpAaError<TsmpDpAaException> {
	SUCCESS(TsmpDpModule.DP, "00", "成功"),
	NO_APP_CATE(TsmpDpModule.DP, "01", "查無應用實例分類"),
	NO_APP(TsmpDpModule.DP, "02", "查無應用實例"),
	NO_CATE_ID(TsmpDpModule.DP, "03", "沒有傳入分類Id"),
	_1104(TsmpDpModule.DP, "04", "查無主題分類"),
	NO_API_IN_THEME(TsmpDpModule.DP, "05", "查無主題API內容"),
	NO_THEME_ID(TsmpDpModule.DP, "06", "沒有傳入主題Id"),
	NO_API_BASIC_INFO(TsmpDpModule.DP, "07", "查無API基本資料"),
	NO_API_DETAIL(TsmpDpModule.DP, "08", "查無API明細資料"),
	NO_API_THEME_INFO(TsmpDpModule.DP, "09", "查無API主題資料"),
	NO_API_MODULE_INFO(TsmpDpModule.DP, "10", "查無API Module資料"),
	NO_API_ORG_INFO(TsmpDpModule.DP, "11", "查無API 組織資料"),
	NO_APP_INFO(TsmpDpModule.DP, "12", "查無應用實例相關資料"),
	NO_APP_ID(TsmpDpModule.DP, "13", "沒有傳入應用實例Id"),
	NO_FAQ_INFO(TsmpDpModule.DP, "14", "查無Faq資料"),
	NO_FAQ_ANS(TsmpDpModule.DP, "15", "查無Faq答案"),
	NO_API_INFO(TsmpDpModule.DP, "16", "查無API資料"),
	_1117(TsmpDpModule.DP, "17", "查無關於網站"),
	NO_SITE_MAP(TsmpDpModule.DP, "18", "查無網站地圖"),
	FAIL_CREATE_MEMBER(TsmpDpModule.DP, "19", "新增會員失敗"),
	FAIL_SEND_RESET_PWD_VERIFY_MAIL(TsmpDpModule.DP, "20", "重設密碼驗證信寄出失敗"),
	INVALID_RESET_PWD_VERIFY_CODE(TsmpDpModule.DP, "21", "無效的密碼重設驗證碼"),
	NO_MEMBER_INFO(TsmpDpModule.DP, "22", "查無會員資料"),
	FAIL_UPDATE_MEMBER_INFO(TsmpDpModule.DP, "23", "會員資料更新失敗"),
	FAIL_RESEND_MEMBER_REVIEW(TsmpDpModule.DP, "24", "重送審查會員失敗"),
	NO_AUTHORIZED_API(TsmpDpModule.DP, "25", "查無授權API"),
	NO_APPLIED_API(TsmpDpModule.DP, "27", "查無申請的API"),
	REQUIRED_APPLY_DESC(TsmpDpModule.DP, "28", "申請說明為必填"),
	NO_FILE(TsmpDpModule.DP, "29", "查無檔案"),
	NO_IMG(TsmpDpModule.DP, "30", "查無圖檔"),
	NO_FILE_CATE_CODE(TsmpDpModule.DP, "31", "查無檔案分類代碼"),
	NO_CATE_ID_2(TsmpDpModule.DP, "32", "查無分類Id"),
	NO_INCLUDING_FILE(TsmpDpModule.DP, "33", "未包含檔案"),
	NO_FILE_ID(TsmpDpModule.DP, "34", "查無FileId"),
	NO_UNAUTHORIZED_API_INFO(TsmpDpModule.DP, "35", "查無未授權API資料"),
	NO_UNAUTHORIZED_API_DETAIL(TsmpDpModule.DP, "36", "查無未授權API明細"),
	NO_UNAUTHORIZED_API_MODULE(TsmpDpModule.DP, "37", "查無未授權API模組"),
	FAIL_AUTHORIZE_API(TsmpDpModule.DP, "38", "授權API失敗"),
	NO_AUTHORIZATION_API_HISTORY(TsmpDpModule.DP, "39", "查無授權API歷程資料"),
	NO_DISAPPROVED_MEMBER(TsmpDpModule.DP, "40", "查無未放行會員"),
	FAIL_MEMBER_QUALIFICATION(TsmpDpModule.DP, "41", "會員資格處理失敗"),
	EMPTY_STATUS_OR_START_END_DATE(TsmpDpModule.DP, "42", "狀態或起/迄日不可為空"),
	EMPTY_CATE_NAME(TsmpDpModule.DP, "43", "分類名稱不可為空"),
	NO_APP_CATE_DATA(TsmpDpModule.DP, "44", "查無實例分類資料"),
	NO_APP_CATE_DATA_BY_CODE(TsmpDpModule.DP, "45", "依代碼查無實例分類資料"),
	FAIL_UPDATE_APP_CATE(TsmpDpModule.DP, "46", "更新實例分類失敗"),
	FAIL_DELETE_APP_CATE(TsmpDpModule.DP, "47", "刪除實例分類失敗"),
	FAIL_CREATE_APP_REQUIRED(TsmpDpModule.DP, "48", "新增實例缺少必填欄位"),
	FAIL_CREATE_APP(TsmpDpModule.DP, "49", "新增實例失敗"),
	NO_APP_DATA(TsmpDpModule.DP, "50", "查無實例資料"),
	FAIL_UPDATE_APP(TsmpDpModule.DP, "51", "更新實例失敗"),
	FAIL_DELETE_APP(TsmpDpModule.DP, "52", "刪除實例失敗"),
	NO_API_LIST(TsmpDpModule.DP, "53", "查無API清單"),
	FAIL_CREATE_THEME_CATE(TsmpDpModule.DP, "54", "主題分類新增失敗"),
	NO_THEME_CATE(TsmpDpModule.DP, "55", "查無主題分類清單"),
	NO_THEME_CATE_BY_ID(TsmpDpModule.DP, "56", "依照Id查無主題分類"),
	FAIL_UPDATE_THEME_CATE(TsmpDpModule.DP, "57", "主題分類更新失敗"),
	FAIL_DELETE_THEME_CATE(TsmpDpModule.DP, "58", "主題分類刪除失敗"),
	FAIL_CREATE_FAQ_REQUIRED(TsmpDpModule.DP, "59", "新增常見問答缺少必填欄位"),
	NO_FAQ_DATA(TsmpDpModule.DP, "60", "查無常見問答資料"),
	FAIL_QUERY_FAQ_REQUIRED(TsmpDpModule.DP, "61", "查詢常見問答缺少參數"),
	FAIL_UPDATE_FAQ_REQUIRED(TsmpDpModule.DP, "62", "update常見問答缺少參數"),
	FAIL_UPDATE_FAQ(TsmpDpModule.DP, "63", "update常見問答失敗"),
	NO_FAQ_ID(TsmpDpModule.DP, "64", "查無常見問答Id"),
	FAIL_DELETE_FAQ(TsmpDpModule.DP, "65", "delete常見問答失敗"),
	FAIL_SAVE_ABOUT(TsmpDpModule.DP, "66", "關於網站存檔失敗"),
	FAIL_SAVE_ABOUT_REQUIRED(TsmpDpModule.DP, "67", "關於網站存檔缺少參數"),
	FAIL_QUERY_ABOUT_MULTI_DATA(TsmpDpModule.DP, "69", "查詢錯誤關於網站, 出現多筆資料"),
	FAIL_CREATE_NODE(TsmpDpModule.DP, "70", "網站地圖新增節點失敗"),
	FAIL_CREATE_NODE_REQUIRED(TsmpDpModule.DP, "71", "網站地圖新增節點缺少參數"),
	FAIL_UPDATE_NODE(TsmpDpModule.DP, "72", "網站地圖更新節點失敗"),
	FAIL_UPDATE_NODE_REQUIRED(TsmpDpModule.DP, "73", "網站地圖更新節點缺少參數"),
	FAIL_DELETE_NODE(TsmpDpModule.DP, "74", "網站地圖刪除節點失敗"),
	FAIL_DELETE_NODE_REQUIRED(TsmpDpModule.DP, "75", "網站地圖刪除節點缺少參數"),
	FAIL_UPDATE_SIGN_MSG_REQUIRED(TsmpDpModule.DP, "76", "簽核訊息設定缺少參數"),
	FAIL_UPDATE_SIGN_MSG(TsmpDpModule.DP, "77", "簽核訊息設定失敗"),
	NO_SIGN_MSG_SETTING(TsmpDpModule.DP, "78", "查無簽核訊息設定"),
	FAIL_QUERY_SIGN_MSG_MULTI_DATA(TsmpDpModule.DP, "79", "查詢錯誤簽核訊息設定, 出現多筆資料"),
	NO_TSMP_USER(TsmpDpModule.DP, "80", "查無TsmpUser資料"),
	NO_MODULE_DATA(TsmpDpModule.DP, "81", "查無 module 資料"),
	FAIL_SAVE_DENIED_MODULE(TsmpDpModule.DP, "82", "deniedModule存檔失敗"),
	NO_API_DOCS(TsmpDpModule.DP, "83", "查無可用的 api-docs 資料"),
	_1184(TsmpDpModule.DP, "84", "reqHeader參數or開關不正確"),
	_1185(TsmpDpModule.DP, "85", "paramType不匹配"),
	_1186(TsmpDpModule.DP, "86", "apiKey or URL 為空"),
	ERROR_API_AUTHORIZED(TsmpDpModule.DP, "87", "API已授權"),
	ERROR_PASSWORD(TsmpDpModule.DP, "88", "會員密碼最少6碼"),
	ERROR_API_APPLIED(TsmpDpModule.DP, "89", "API已申請"),
	ERROR_CLIENT_ID_EXISTS(TsmpDpModule.DP, "90", "會員帳號已存在"),
	ERROR_DATA_EDITED(TsmpDpModule.DP, "91", "資料已被異動"),
	ERROR_DELETE_ACTIVE_DATA(TsmpDpModule.DP, "92", "狀態為啟用不可刪除"),
	NO_AUTH_TO_APPLY(TsmpDpModule.DP, "93", "您沒有權限申請此API"),
	FAIL_CREATE_NEWS_REQUIRED(TsmpDpModule.DP, "94", "新增[公告消息]缺少必填資料"),
	_1195(TsmpDpModule.DP, "95", "新增[公告消息]上傳檔案存檔失敗"),
	_1196(TsmpDpModule.DP, "96", "更新[公告消息]缺少必填資料"),
	_1197(TsmpDpModule.DP, "97", "資料已被異動, 更新[公告消息]失敗"),
	_1198(TsmpDpModule.DP, "98", "查無[公告消息]清單資料"),
	SYSTEM_ERROR(TsmpDpModule.DP, "99", "系統錯誤"),
	// v3.4
	FAIL_DELETE_NEWS(TsmpDpModule.DP2, "00", "無法完整刪除指定的[公告消息]資料"),
	_1201(TsmpDpModule.DP2, "01", "編碼不正確"),
	NO_ITEMS_DATA(TsmpDpModule.DP2, "02", "查無類型清單"),
	FAIL_CREATE_File(TsmpDpModule.DP2, "03", "檔案寫入錯誤"),
	_1203(TsmpDpModule.DP2, "03", "檔案寫入錯誤"),
	_1204(TsmpDpModule.DP2, "04", "找不到主題分類圖檔"),
	_1206(TsmpDpModule.DP2, "06", "無法替換主題分類圖檔"),
	_1208(TsmpDpModule.DP2, "08", "查無主題"),
	_1209(TsmpDpModule.DP2, "09", "無法完整刪除指定的[主題]資料"),
	_1210(TsmpDpModule.DP2, "10", "關卡與角色存檔錯誤"),
	_1211(TsmpDpModule.DP2, "11", "查無關卡與角色"),
	FAIL_CREATE_NEWS(TsmpDpModule.DP2, "12", "新增消息失敗"),
	_1213(TsmpDpModule.DP2, "13", "新增審核申請失敗"),
	_1214(TsmpDpModule.DP2, "14", "重新送審失敗"),
	_1215(TsmpDpModule.DP2, "15", "查無待審單"),
	_1216(TsmpDpModule.DP2, "16", "查詢待審單發生錯誤"),
	_1217(TsmpDpModule.DP2, "17", "單據查詢錯誤"),
	_1218(TsmpDpModule.DP2, "18", "單據審核/變更失敗"),
	_1219(TsmpDpModule.DP2, "19", "沒有權限"),
	_1220(TsmpDpModule.DP2, "20", "儲存失敗，資料長度過大"),
	_1221(TsmpDpModule.DP2, "21", "所選的資料有部份不屬於您的組織,異動失敗"),
	_1222(TsmpDpModule.DP2, "22", "異動失敗,因為不屬於您的組織"),
	_1223(TsmpDpModule.DP2, "23", "更新申請單失敗"),
	_1224(TsmpDpModule.DP2, "24", "[公告消息]無法查詢"),
	_1226(TsmpDpModule.DP2, "26", "重複的回覆代碼"),
	_1227(TsmpDpModule.DP2, "27", "生效日期不可小於今天"),
	_1228(TsmpDpModule.DP2, "28", "該用戶端尚未註冊完成，無法申請API"),
	_1229(TsmpDpModule.DP2, "29", "組織名稱不存在"),
	_1230(TsmpDpModule.DP2, "30", "角色不存在"),
	_1231(TsmpDpModule.DP2, "31", "使用者不存在"),
	_1232(TsmpDpModule.DP2, "32", "使用者名稱已存在"),
	_1233(TsmpDpModule.DP2, "33", "檔案不得為空檔"),
	_1234(TsmpDpModule.DP2, "34", "新密碼不可為空"),
	_1235(TsmpDpModule.DP2, "35", "新密碼與原密碼相同"),
	_1236(TsmpDpModule.DP2, "36", "原密碼不可為空"),
	_1237(TsmpDpModule.DP2, "37", "至少輸入一項"),
	_1238(TsmpDpModule.DP2, "38", "原密碼不正確"),
	_1239(TsmpDpModule.DP2, "39", "角色代號重複"),
	_1240(TsmpDpModule.DP2, "40", "角色名稱重複"),
	_1241(TsmpDpModule.DP2, "41", "功能不存在 (含locale)"),
	_1242(TsmpDpModule.DP2, "42", "週期表單轉換錯誤，請重新設定"),
	_1243(TsmpDpModule.DP2, "43", "該角色有使用者"),
	_1244(TsmpDpModule.DP2, "44", "使用者E-mail:只能為Email格式"),
	_1245(TsmpDpModule.DP2, "45", "使用者帳號:只能輸入英文字母(a~z,A~Z)及數字且不含空白"),
	_1246(TsmpDpModule.DP2, "46", "使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1247(TsmpDpModule.DP2, "47", "使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1248(TsmpDpModule.DP2, "48", "密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1249(TsmpDpModule.DP2, "49", "角色清單:必填參數"),
	_1250(TsmpDpModule.DP2, "50", "組織名稱:必填參數"),
	_1251(TsmpDpModule.DP2, "51", "當前排程狀態不允許異動"),
	_1252(TsmpDpModule.DP2, "52", "使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1253(TsmpDpModule.DP2, "53", "組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1256(TsmpDpModule.DP2, "56", "新角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1257(TsmpDpModule.DP2, "57", "使用者帳號:必填參數"),
	_1258(TsmpDpModule.DP2, "58", "使用者名稱:必填參數"),
	_1259(TsmpDpModule.DP2, "59", "密碼:必填參數"),
	_1260(TsmpDpModule.DP2, "60", "使用者E-mail:必填參數"),
	_1261(TsmpDpModule.DP2, "61", "狀態:必填參數"),
	_1262(TsmpDpModule.DP2, "62", "可授權角色不存在"),
	_1263(TsmpDpModule.DP2, "63", "可授權角色:長度限制 [{{0}}] 字內，[{{1}}] 長度[{{2}}] 個字"),
	_1264(TsmpDpModule.DP2, "64", "登入角色不存在"),
	_1265(TsmpDpModule.DP2, "65", "登入角色:長度限制 [{{0}}] 字內，長度[{{1}}] 個字"),
	_1266(TsmpDpModule.DP2, "66", "可授權角色:必填參數"),
	_1267(TsmpDpModule.DP2, "67", "登入角色:必填參數"),
	_1268(TsmpDpModule.DP2, "68", "登入角色的可授權角色清單已經存在"),
	_1269(TsmpDpModule.DP2, "69", "組織名稱已存在"),
	_1270(TsmpDpModule.DP2, "70", "使用者帳號已存在"),
	_1271(TsmpDpModule.DP2, "71", "上層組織名稱:必填參數"),
	_1272(TsmpDpModule.DP2, "72", "上層組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1273(TsmpDpModule.DP2, "73", "組織單位ID:必填參數"),
	_1274(TsmpDpModule.DP2, "74", "組織單位ID:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1275(TsmpDpModule.DP2, "75", "組織代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1276(TsmpDpModule.DP2, "76", "聯絡人電話:必填參數"),
	_1277(TsmpDpModule.DP2, "77", "聯絡人電話:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1278(TsmpDpModule.DP2, "78", "聯絡人姓名:必填參數"),
	_1279(TsmpDpModule.DP2, "79", "聯絡人姓名:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1280(TsmpDpModule.DP2, "80", "聯絡人信箱:必填參數"),
	_1281(TsmpDpModule.DP2, "81", "聯絡人信箱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1282(TsmpDpModule.DP2, "82", "新角色名稱已存在"),
	_1283(TsmpDpModule.DP2, "83", "新角色名稱:必填參數"),
	_1284(TsmpDpModule.DP2, "84", "[{{0}}] 不得重複"),
	_1285(TsmpDpModule.DP2, "85", "Return code 參數不符合多國語系定義"),
	_1286(TsmpDpModule.DP2, "86", "更新失敗"),
	_1287(TsmpDpModule.DP2, "87", "刪除失敗"),
	_1288(TsmpDpModule.DP2, "88", "新增失敗"),
	_1289(TsmpDpModule.DP2, "89", "查無 locale [{{0}}] 的 rtn code [{{1}}] 訊息"),
	_1290(TsmpDpModule.DP2, "90", "參數錯誤"),
	_1291(TsmpDpModule.DP2, "91", "檔案解析錯誤"),
	_1292(TsmpDpModule.DP2, "92", "工作佇列已滿, 請稍後再執行"),
	_1293(TsmpDpModule.DP2, "93", "資料庫錯誤"),
	_1294(TsmpDpModule.DP2, "94", "無查詢權限"),
	_1295(TsmpDpModule.DP2, "95", "日期格式不正確"),
	_1296(TsmpDpModule.DP2, "96", "缺少必填參數"),
	_1297(TsmpDpModule.DP2, "97", "執行錯誤"),
	_1298(TsmpDpModule.DP2, "98", "查無資料"),
	_1299(TsmpDpModule.DP2, "99", "參數驗證錯誤"),	// BcryptParam 驗證失敗時拋出
	
	_1300(TsmpDpModule.DP3, "00", "角色代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1301(TsmpDpModule.DP3, "01", "角色代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白"),
	_1302(TsmpDpModule.DP3, "02", "角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1303(TsmpDpModule.DP3, "03", "節點不可以移動到子節點"),
	_1304(TsmpDpModule.DP3, "04", "組織包含未刪除的API"),
	_1305(TsmpDpModule.DP3, "05", "該組織包含未刪除的用戶"),
	_1306(TsmpDpModule.DP3, "06", "組織包含未刪除的Java模組"),
	_1307(TsmpDpModule.DP3, "07", "組織包含未刪除的.Net模組"),
	_1308(TsmpDpModule.DP3, "08", "組織包含未刪除的子組織"),
	_1309(TsmpDpModule.DP3, "09", "功能清單:必填參數"),
	_1311(TsmpDpModule.DP3, "11", "聯絡人信箱:只能為Email格式"),
	_1312(TsmpDpModule.DP3, "12", "上層單位組織：不可以選擇節點自己本身"),
	_1313(TsmpDpModule.DP3, "13", "使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白"),
	_1314(TsmpDpModule.DP3, "14", "功能代碼:必填參數"),
	_1315(TsmpDpModule.DP3, "15", "功能代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1316(TsmpDpModule.DP3, "16", "語系:必填參數"),
	_1317(TsmpDpModule.DP3, "17", "語系:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1318(TsmpDpModule.DP3, "18", "功能描述:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1319(TsmpDpModule.DP3, "19", "功能名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1320(TsmpDpModule.DP3, "20", "功能名稱(英文):長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1321(TsmpDpModule.DP3, "21", "功能名稱(英文):只能輸入英數、_、- "),
	_1322(TsmpDpModule.DP3, "22", "用戶端帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1323(TsmpDpModule.DP3, "23", "用戶端代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1324(TsmpDpModule.DP3, "24", "用戶端代號:必填參數"),
	_1325(TsmpDpModule.DP3, "25", "用戶端代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白"),
	_1326(TsmpDpModule.DP3, "26", "用戶端名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1327(TsmpDpModule.DP3, "27", "用戶端名稱:必填參數"),
	_1328(TsmpDpModule.DP3, "28", "簽呈編號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1329(TsmpDpModule.DP3, "29", "密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1330(TsmpDpModule.DP3, "30", "密碼:必填參數"),
	_1331(TsmpDpModule.DP3, "31", "電子郵件帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1332(TsmpDpModule.DP3, "32", "電子郵件帳號:只能為Email格式"),
	_1333(TsmpDpModule.DP3, "33", "擁有者:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1334(TsmpDpModule.DP3, "34", "擁有者:必填參數"),
	_1335(TsmpDpModule.DP3, "35", "狀態:必填參數"),
	_1336(TsmpDpModule.DP3, "36", "開放狀態:必填參數"),
	_1337(TsmpDpModule.DP3, "37", "開始日期:只能輸入日期格式"),
	_1338(TsmpDpModule.DP3, "38", "到期日期:只能輸入日期格式"),
	_1339(TsmpDpModule.DP3, "39", "服務時間:只能輸入時間格式"),
	_1340(TsmpDpModule.DP3, "40", "用戶端代號已存在"),
	_1341(TsmpDpModule.DP3, "41", "用戶端名稱已存在"),
	_1342(TsmpDpModule.DP3, "42", "用戶端帳號已存在"),
	_1343(TsmpDpModule.DP3, "43", "用戶端帳號:必填參數"),
	_1344(TsmpDpModule.DP3, "44", "用戶端不存在"),
	_1345(TsmpDpModule.DP3, "45", "主機名稱:必填參數"),
	_1346(TsmpDpModule.DP3, "46", "主機IP:必填參數"),
	_1347(TsmpDpModule.DP3, "47", "主機名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1348(TsmpDpModule.DP3, "48", "主機IP:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1349(TsmpDpModule.DP3, "49", "主機IP:格式錯誤"),
	_1350(TsmpDpModule.DP3, "50", "[{{0}}] 為必填欄位"),
	_1351(TsmpDpModule.DP3, "51", "[{{0}}] 長度限制 [{{1}}] 字內，您輸入[{{2}}] 個字"),
	_1352(TsmpDpModule.DP3, "52", "[{{0}}] 格式不正確"),
	_1353(TsmpDpModule.DP3, "53", "[{{0}}] 已存在: {{1}}"),
	_1354(TsmpDpModule.DP3, "54", "[{{0}}] 不存在: {{1}}"),
	_1355(TsmpDpModule.DP3, "55", "[{{0}}] 不得小於 {{1}}, 您輸入 {{2}}"),
	_1356(TsmpDpModule.DP3, "56", "[{{0}}] 不得大於 {{1}}, 您輸入 {{2}}"),
	_1357(TsmpDpModule.DP3, "57", "您的角色並未授權使用 API txID [{{0}}]"),
	_1358(TsmpDpModule.DP3, "58", "[群組名稱]超過群組選取上限205，請重新選取"),
	_1359(TsmpDpModule.DP3, "59", "建立新Client Group時，必須是SYSTEM Group"),
	_1360(TsmpDpModule.DP3, "60", "群組不存在"),
	_1362(TsmpDpModule.DP3, "62", "功能代碼:必填參數"),
	_1363(TsmpDpModule.DP3, "63", "安全等級:必填參數"),
	_1364(TsmpDpModule.DP3, "64", "安全等級不存在"),
	_1365(TsmpDpModule.DP3, "65", "用戶端的SECURITY LEVEL ID與群組的SECURITY LEVEL ID不符合，群組ID為[{{0}}]"),
	_1366(TsmpDpModule.DP3, "66", "開始日期與到期日期必須填寫"),
	_1367(TsmpDpModule.DP3, "67", "到期時間不可小於開始時間"),
	_1368(TsmpDpModule.DP3, "68", "到期日期不可小於開始日期"),
	_1369(TsmpDpModule.DP3, "69", "開始時間與到期時間必須填寫"),
	_1372(TsmpDpModule.DP3, "72", "未知類型的Grant Type: [{{0}}]"),
	_1379(TsmpDpModule.DP3, "79", "此 Open API Key 已執行過展期"),
	_1381(TsmpDpModule.DP3, "81", "開始時間:格式不正確"),
	_1383(TsmpDpModule.DP3, "83", "結束時間:格式不正確"),
	_1384(TsmpDpModule.DP3, "84", "[{{0}}] 長度至少須 [{{1}}] 字，您輸入[{{2}}] 個字"),
	_1385(TsmpDpModule.DP3, "85", "模組名稱:必填參數"),
	_1393(TsmpDpModule.DP3, "93", "Module名稱:必填參數"),
	_1395(TsmpDpModule.DP3, "95", "API Key:必填參數"),
	_1397(TsmpDpModule.DP3, "97", "授權核身種類:[{{0}}]不存在"),
	_1398(TsmpDpModule.DP3, "98", "群組名稱已存在"),
	_1399(TsmpDpModule.DP3, "99", "群組代碼已存在"),
	_1400(TsmpDpModule.DP4, "00", "API: [{{0}}]不存在"),
	_1401(TsmpDpModule.DP4, "01", "用戶端狀態不正常：[{{0}}]"),
	_1402(TsmpDpModule.DP4, "02", "虛擬群組的API數量上限為 [{{0}}]，您選擇 [{{1}}]"),
	_1403(TsmpDpModule.DP4, "03", "無法刪除，請解除用戶端的虛擬授權設定"),
	_1404(TsmpDpModule.DP4, "04", "無法刪除，請解除群組的授權核身種類: {{0}}"),
    _1405(TsmpDpModule.DP4, "05", "請輸入網址格式"),
	_1406(TsmpDpModule.DP4, "06", "[{{0}}] 數量不可少於 [{{1}}]，您選擇 [{{2}}]"),
	_1407(TsmpDpModule.DP4, "07", "[{{0}}] 數量不可超過 [{{1}}]，您選擇 [{{2}}]"),
	_1408(TsmpDpModule.DP4, "08", "新密碼與再次確認密碼不一致"),
	_1410(TsmpDpModule.DP4, "10", "群組[{{0}}]不存在"),
	_1411(TsmpDpModule.DP4, "11", "部署容器已經啟用"),
	_1412(TsmpDpModule.DP4, "12", "部署容器已經停用"),
	_1413(TsmpDpModule.DP4, "13", "無法啟用，部署容器未綁定任何節點"),
	_1414(TsmpDpModule.DP4, "14", "無法刪除，部署容器啟用中"),
	_1415(TsmpDpModule.DP4, "15", "NodeTaskNotifiers發生錯誤"),
	_1416(TsmpDpModule.DP4, "16", "查詢日期區間不得超過 {{0}} 天"),
	_1417(TsmpDpModule.DP4, "17", "該群組有用戶端"),
	_1418(TsmpDpModule.DP4, "18", "請先匯入外部系統介接規格"),
	_1419(TsmpDpModule.DP4, "19", "主機名稱已存在"),
	_1420(TsmpDpModule.DP4, "20", "主機不存在"),
	_1421(TsmpDpModule.DP4, "21", "註冊主機心跳必須停用"),
	_1422(TsmpDpModule.DP4, "22", "註冊主機被註冊API參考"),
	_1423(TsmpDpModule.DP4, "23", "主機名稱{{0}}有重複相同資料"),
	_1424(TsmpDpModule.DP4, "24", "未指定主機位址(host)"),
	_1425(TsmpDpModule.DP4, "25", "主機IP{{0}}有重複相同資料"),
	_1426(TsmpDpModule.DP4, "26", "無法使用此模組名稱，已由匯入方式建立"),
	_1427(TsmpDpModule.DP4, "27", "例外類型只能為N、W、M、D"),
	_1428(TsmpDpModule.DP4, "28", "告警名稱已存在"),
	_1429(TsmpDpModule.DP4, "29", "告警設定不存在"),
	_1430(TsmpDpModule.DP4, "30", "取得授權碼失敗，請重新再試"),
	_1431(TsmpDpModule.DP4, "31", "授權碼不可用"),
	_1432(TsmpDpModule.DP4, "32", "授權類型不正確"),
	_1433(TsmpDpModule.DP4, "33", "非對稱式加密失敗：[{{0}}]"),
	_1434(TsmpDpModule.DP4, "34", "非對稱式解密失敗：[{{0}}]"),
	_1435(TsmpDpModule.DP4, "35", "時間範圍只能為T、W、M、D"),
	_1436(TsmpDpModule.DP4, "36", "找不到已啟動或最近上傳的模組"),
	_1437(TsmpDpModule.DP4, "37", "一個部署容器中不可綁定兩個相同的模組：[{{0}}]"),
	_1438(TsmpDpModule.DP4, "38", "刪除失敗，模組啟用中"),
	_1439(TsmpDpModule.DP4, "39", "刪除失敗，模組已綁定"),
	_1440(TsmpDpModule.DP4, "40", "請填寫密碼或是重置密碼"),
	_1441(TsmpDpModule.DP4, "41", "請填寫密碼"),
	_1442(TsmpDpModule.DP4, "42", "其他組織單位已存在相同名稱的模組：[{{0}}]"),
	_1443(TsmpDpModule.DP4, "43", "不支援的檔案類型"),
	_1444(TsmpDpModule.DP4, "44", "不可匯出 Java 或 .NET 模組的 API"),
    _1445(TsmpDpModule.DP4, "45", "檔案中未包含任何 API 資料"),
	_1446(TsmpDpModule.DP4, "46", "例外日期、開始時間與結束時間不可填寫"),
	_1447(TsmpDpModule.DP4, "47", "例外日期不可填寫"),
	_1448(TsmpDpModule.DP4, "48", "開始時間與結束時間格式不正確"),
	_1449(TsmpDpModule.DP4, "49", "開始時間與結束時間為必填"),
	_1450(TsmpDpModule.DP4, "50", "例外日期為必填"),
	_1451(TsmpDpModule.DP4, "51", "例外日期格式不正確"),
    _1452(TsmpDpModule.DP4, "52", "{{0}} API 失敗：apiKey=[{{1}}], moduleName=[{{2}}], msg={{3}}"),
    _1453(TsmpDpModule.DP4, "53", "Composer Flow 轉型失敗：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}"),
    _1454(TsmpDpModule.DP4, "54", "寫入 Composer 資料錯誤：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}"),
	_1455(TsmpDpModule.DP4, "55", "向 digiRunner 取得 Token 失敗"),
	_1456(TsmpDpModule.DP4, "56", "查無組合API"),
	_1457(TsmpDpModule.DP4, "57", "模組已經綁定"),
	_1458(TsmpDpModule.DP4, "58", "模組已經解除綁定"),
	_1459(TsmpDpModule.DP4, "59", "不支援操作此架構的模組"),
    _1460(TsmpDpModule.DP4, "60", "尚未確認部署，無法啟用組合API"),
    _1461(TsmpDpModule.DP4, "61", "尚有 API 未下架，確定繼續執行？"),
    _1462(TsmpDpModule.DP4, "62", "尚有 API 正在申請單流程中，確定繼續執行？"),
    _1463(TsmpDpModule.DP4, "63", "刪除失敗，API啟用中"),
    _1464(TsmpDpModule.DP4, "64", "刪除失敗，API仍有相關的模組紀錄"),
    _1465(TsmpDpModule.DP4, "65", "有些是透過匯入外部介接規格而註冊的 API，確定繼續執行？"),
    _1466(TsmpDpModule.DP4, "66", "僅可更新註冊或組合API"),
    _1467(TsmpDpModule.DP4, "67", "僅可更新Java模組或.NET模組的API"),
    _1468(TsmpDpModule.DP4, "68", "Log欄位與回應值為必填欄位"),
    _1469(TsmpDpModule.DP4, "69", "模組名稱與API Key為必填欄位"),
    _1470(TsmpDpModule.DP4, "70", "5分鐘內不可重置signBlock"),
    _1471(TsmpDpModule.DP4, "71", "密碼錯誤超過上限"),
    _1472(TsmpDpModule.DP4, "72", "使用者已鎖定"),
    _1473(TsmpDpModule.DP4, "73", "使用者已停權"),
    _1474(TsmpDpModule.DP4, "74", "設定檔缺少參數 [{{0}}]"),
    _1475(TsmpDpModule.DP4, "75", "無法綁定，部署容器尚未啟用: {{0}}"),
    _1476(TsmpDpModule.DP4, "76", "無法解除綁定，已經沒有其他部署容器啟用此系統模組了"),
	_1477(TsmpDpModule.DP4, "77", "用戶帳號：僅可輸入英數字、底線「_」及橫線「-」"),
	_1478(TsmpDpModule.DP4, "78", "申請內容說明：長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1479(TsmpDpModule.DP4, "79", "模組已經啟用"),
	_1480(TsmpDpModule.DP4, "80", "模組已經停用"),
	_1481(TsmpDpModule.DP4, "81", "TSMP_API 資料與 TSMP_API_REG 不一致: ApiKey={{0}}, ModuleName={{1}}"),
	_1482(TsmpDpModule.DP4, "82", "此安全等級有未刪除的Group: {{0}}"),
	_1483(TsmpDpModule.DP4, "83", "此安全等級有未刪除的Client: {{0}}"),
	_1484(TsmpDpModule.DP4, "84", "註冊主機不存在"),
	_1485(TsmpDpModule.DP4, "85", "註冊主機與客戶端不符"),
	_1486(TsmpDpModule.DP4, "86", "註冊主機監控尚未啟用"),
	_1487(TsmpDpModule.DP4, "87", "主機狀態只能為A、S"),
	_1488(TsmpDpModule.DP4, "88", "註冊主機為必填"),
	_1489(TsmpDpModule.DP4, "89", "註冊主機:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1490(TsmpDpModule.DP4, "90", "註冊主機狀態:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),
	_1491(TsmpDpModule.DP4, "91", "執行工作不存在: {{0}}"),
	_1492(TsmpDpModule.DP4, "92", "客製包不存在"),
	_1496(TsmpDpModule.DP4, "96", "登入 digiRunner 失敗"),
	_1497(TsmpDpModule.DP4, "97", "介接規格錯誤：[{{0}}] - [{{1}}]"),
	_1498(TsmpDpModule.DP4, "98", "HTTP error：[{{0}}]"),
	_1499(TsmpDpModule.DP4, "99", "介接邏輯錯誤：[{{0}}] - [{{1}}]"),
	_1500(TsmpDpModule.DP5, "00", "Base64 Decode 錯誤: {{0}}"),
	_1501(TsmpDpModule.DP5, "01", "群組維護：{{0}}，授權範圍：{{1}}目前有綁定，請先移除。"),
	_1502(TsmpDpModule.DP5, "02", "API列表目前有資料，請先刪除。"),
	_1503(TsmpDpModule.DP5, "03", "Http Method:必填參數"),
	_1504(TsmpDpModule.DP5, "04", "可能是髒資料導致系統無法匹配，({{0}})"),
	_1510(TsmpDpModule.DP5, "10", "LDAP驗證失敗"),
	_1511(TsmpDpModule.DP5, "11", "LDAP未啟用"),
	_1512(TsmpDpModule.DP5, "12", "LDAP連線失敗"),
	_1513(TsmpDpModule.DP5, "13", "User IP 不在可登入的網段中"),
	_1514(TsmpDpModule.DP5, "14", "您所在的網段,無法登入"),
	_1515(TsmpDpModule.DP5, "15", "單個用戶不能存在於多個群組中"),
	_1516(TsmpDpModule.DP5, "16", "使用者沒有任何群組"),
	_1517(TsmpDpModule.DP5, "17", "不合法的SSO登入錯誤"),
	_1518(TsmpDpModule.DP5, "18", "此使用者不能以SSO方式登入"),
	_1519(TsmpDpModule.DP5, "19", "使用者未登入,請透過digiRunner 開啟Composer"),
	_1520(TsmpDpModule.DP5, "20", "digiRunner拒絕存取"),
	_1522(TsmpDpModule.DP5, "22", "CApiKey驗證失敗"),
	_1523(TsmpDpModule.DP5, "23", "時區必須填寫"),
	_1524(TsmpDpModule.DP5, "24", "proxy path({{0}})和目標URL({{1}})的{p}數量不匹配"),
	_1525(TsmpDpModule.DP5, "25", "Kibana Connection timed out"),
	_1526(TsmpDpModule.DP5, "26", "Kibana Unauthorized"),
	_1527(TsmpDpModule.DP5, "27", "{{0}} 僅可輸入整數"),
	_1528(TsmpDpModule.DP5, "28", "機率總數必須是100，但卻是{{0}}"),
	_1529(TsmpDpModule.DP5, "29", "設定Kibana版本錯誤"),
	_1530(TsmpDpModule.DP5, "30", "Composer Server連線失敗"),
	_1531(TsmpDpModule.DP5, "31", "至少需要一組目標URL"),
	_1532(TsmpDpModule.DP5, "32", "網站反向代理資料不存在"),
	_1533(TsmpDpModule.DP5, "33", "ID Token 不合法"),
	_1534(TsmpDpModule.DP5, "34", "缺少digiRunner URL"),
	_1535(TsmpDpModule.DP5, "35", "缺少Application ID"),
	_1536(TsmpDpModule.DP5, "36", "缺少User ID"),
	_1537(TsmpDpModule.DP5, "37", "缺少檔案編號"),
	_1539(TsmpDpModule.DP5, "39", "使用者帳號與Delegate AC User重複"),
	_1540(TsmpDpModule.DP5, "40", "Delegate AC User與使用者帳號重複"),
	_1547(TsmpDpModule.DP5, "47", "[用戶端帳號:{{0}}]授權範圍設定的API加授權設定的總數量超過{{1}}"),
	_1548(TsmpDpModule.DP5, "48", "異動系統預設資料請先解鎖(Setting的DEFAULT_DATA_CHANGE_ENABLED)"),
	_1549(TsmpDpModule.DP5, "49", "設定啟用停用日期不可為今日"),
	_1550(TsmpDpModule.DP5, "50", "啟用日期與停用日期不可為同日"),
	_1551(TsmpDpModule.DP5, "51", "不可包含已啟用的API"),
	_1552(TsmpDpModule.DP5, "52", "不可包含已停用的API"),
	_1553(TsmpDpModule.DP5, "53", "啟用日期需要大於停用日期"),
	_1554(TsmpDpModule.DP5, "54", "停用日期需要大於啟用日期"),
	_1555(TsmpDpModule.DP5, "55", "必須先移除預定啟用日期"),
	_1556(TsmpDpModule.DP5, "56", "必須先移除預定停用日期"),
	_1557(TsmpDpModule.DP5, "57", "啟用日期需要小於停用日期"),
	_1558(TsmpDpModule.DP5, "58", "停用日期需要小於啟用日期"),
	_1559(TsmpDpModule.DP5, "59", "{{0}}"),
	_1561(TsmpDpModule.DP5, "61", "對稱式加密失敗：{{0}}"),
	_1562(TsmpDpModule.DP5, "62", "對稱式解密失敗：{{0}}"),

	_1565(TsmpDpModule.DP5, "65", "查無簽核關卡設定，應設定簽核關卡"),
	_1566(TsmpDpModule.DP4, "66", "結束日期不可小於開始日期"),
	
	_2000(TsmpDpModule.DP10, "00", "必填"),
	_2001(TsmpDpModule.DP10, "01", "最大長度為 [{{0}}]"),
	_2002(TsmpDpModule.DP10, "02", "最小長度為 [{{0}}]"),
	_2003(TsmpDpModule.DP10, "03", "必須包含 [{{0}}]"),
	_2004(TsmpDpModule.DP10, "04", "不得包含 [{{0}}]"),
	_2005(TsmpDpModule.DP10, "05", "數值不可大於 [{{0}}]"),
	_2006(TsmpDpModule.DP10, "06", "數值不可小於 [{{0}}]"),
	_2007(TsmpDpModule.DP10, "07", "格式不正確"),
	_2008(TsmpDpModule.DP10, "08", "僅可輸入英數字、底線「_」及橫線「-」"),
	_2009(TsmpDpModule.DP10, "09", "最少選擇 [{{0}}] 項"),
	_2010(TsmpDpModule.DP10, "10", "最多選擇 [{{0}}] 項"),
	_2011(TsmpDpModule.DP10, "11", "僅可輸入英數字、底線「_」、橫線「-」及句號「.」"),
	_2012(TsmpDpModule.DP10, "12", "大約"),
	_2013(TsmpDpModule.DP10, "13", "每一天"),
	_2014(TsmpDpModule.DP10, "14", "每個月 {{0}} 號"),
	_2015(TsmpDpModule.DP10, "15", "每星期{{0}}"),
	_2016(TsmpDpModule.DP10, "16", "的 {{0}}"),
	_2017(TsmpDpModule.DP10, "17", "每十分鐘"),
	_2018(TsmpDpModule.DP10, "18", "每半小時"),
	_2019(TsmpDpModule.DP10, "19", "每一小時"),
	_2020(TsmpDpModule.DP10, "20", "每 {{0}} 小時"),
	_2021(TsmpDpModule.DP10, "21", "僅可輸入數字"),
	_2022(TsmpDpModule.DP10, "22", "僅可輸入中英數字、底線「_」及橫線「-」"),
	_2023(TsmpDpModule.DP10, "23", "僅可輸入英數字、底線「_」、橫線「-」、點「.」及「@」"),
	_2024(TsmpDpModule.DP10, "24", "IDP資訊已存在"),
	_2025(TsmpDpModule.DP10, "25", "[{{0}}] : 必填"),
	_2026(TsmpDpModule.DP10, "26", "批量更新失敗 : {{0}}"),
	_2027(TsmpDpModule.DP10, "27", "該目錄無法再新增功能"),
	_2031(TsmpDpModule.DP10, "31", "已經上架"),
	_2032(TsmpDpModule.DP10, "32", "已經下架"),
	_2033(TsmpDpModule.DP10, "33", "上架日期需要大於下架日期"),
	_2034(TsmpDpModule.DP10, "34", "下架日期需要大於上架日期"),
	_2035(TsmpDpModule.DP10, "35", "必須先移除預定上架日期"),
	_2036(TsmpDpModule.DP10, "36", "必須先移除預定下架日期"),
	_2037(TsmpDpModule.DP10, "37", "上架日期需要小於下架日期"),
	_2038(TsmpDpModule.DP10, "38", "下架日期需要小於上架日期"),
    ;

	private TsmpDpModule module;
    private String seq;
    private String defaultMessage;

    private TsmpDpAaRtnCode(TsmpDpModule module, String seq, String defaultMessage) {
    	this.module = module;
        this.seq = seq;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public TsmpDpModule getModule() {
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

//    @Override
//    public void setDefaultMessage(String message) {
//    	this.defaultMessage = message;
//    }

}
