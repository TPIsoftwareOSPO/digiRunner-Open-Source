package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpRtnCodeVo;

@Service
public class TsmpRtnCodeTableInitializer {
    
    private  List<TsmpRtnCodeVo> tsmpRtnCodelist = new LinkedList<>();
    
	public List<TsmpRtnCodeVo> insertTsmpRtnCode() {
		try {
	        String tsmpRtnCodeColumn;
	        String locale;
	        String tsmpRtnDesc;
	        String tsmpRtnMsg;

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1100"),(locale = "zh-TW"), (tsmpRtnMsg = "成功"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1100"),(locale = "en-US"), (tsmpRtnMsg = "Success"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1101"),(locale = "en-US"),(tsmpRtnMsg = "No application instance classification"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1101"),(locale = "zh-TW"),(tsmpRtnMsg = "查無應用實例分類"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1102"),(locale = "en-US"),(tsmpRtnMsg = "No application instance"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1102"),(locale = "zh-TW"),(tsmpRtnMsg = "查無應用實例"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1103"),(locale = "en-US"),(tsmpRtnMsg = "No incoming category Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1103"),(locale = "zh-TW"),(tsmpRtnMsg = "沒有傳入分類Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1104"),(locale = "en-US"),(tsmpRtnMsg = "No subject classification"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1104"),(locale = "zh-TW"),(tsmpRtnMsg = "查無主題分類"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1105"),(locale = "en-US"),(tsmpRtnMsg = "No subject API content"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1105"),(locale = "zh-TW"),(tsmpRtnMsg = "查無主題API內容"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1106"),(locale = "en-US"),(tsmpRtnMsg = "No subject Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1106"),(locale = "zh-TW"),(tsmpRtnMsg = "沒有傳入主題Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1107"),(locale = "en-US"),(tsmpRtnMsg = "No basic API information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1107"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API基本資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1108"),(locale = "en-US"),(tsmpRtnMsg = "No API details"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1108"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API明細資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1109"),(locale = "en-US"),(tsmpRtnMsg = "No API theme information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1109"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API主題資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1110"),(locale = "en-US"),(tsmpRtnMsg = "No API Module information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1110"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API Module資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1111"),(locale = "en-US"),(tsmpRtnMsg = "No API organization information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1111"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API 組織資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1112"),(locale = "en-US"),(tsmpRtnMsg = "No relevant information on application examples"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1112"),(locale = "zh-TW"),(tsmpRtnMsg = "查無應用實例相關資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1113"),(locale = "en-US"),(tsmpRtnMsg = "No application instance Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1113"),(locale = "zh-TW"),(tsmpRtnMsg = "沒有傳入應用實例Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1114"),(locale = "en-US"),(tsmpRtnMsg = "No Faq information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1114"),(locale = "zh-TW"),(tsmpRtnMsg = "查無Faq資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1115"),(locale = "en-US"),(tsmpRtnMsg = "No Faq Answer"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1115"),(locale = "zh-TW"),(tsmpRtnMsg = "查無Faq答案"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1116"),(locale = "en-US"),(tsmpRtnMsg = "No API information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1116"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1117"),(locale = "en-US"),(tsmpRtnMsg = "No website"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1117"),(locale = "zh-TW"),(tsmpRtnMsg = "查無關於網站"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1118"),(locale = "en-US"),(tsmpRtnMsg = "No site map"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1118"),(locale = "zh-TW"),(tsmpRtnMsg = "查無網站地圖"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1119"),(locale = "en-US"),(tsmpRtnMsg = "Failed to add member"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1119"),(locale = "zh-TW"),(tsmpRtnMsg = "新增會員失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1120"),(locale = "en-US"),(tsmpRtnMsg = "Reset password verification letter failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1120"),(locale = "zh-TW"),(tsmpRtnMsg = "重設密碼驗證信寄出失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1121"),(locale = "en-US"),(tsmpRtnMsg = "Invalid password reset verification code"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1121"),(locale = "zh-TW"),(tsmpRtnMsg = "無效的密碼重設驗證碼"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1122"),(locale = "en-US"),(tsmpRtnMsg = "No member information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1122"),(locale = "zh-TW"),(tsmpRtnMsg = "查無會員資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1123"),(locale = "en-US"),(tsmpRtnMsg = "Member information update failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1123"),(locale = "zh-TW"),(tsmpRtnMsg = "會員資料更新失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1124"),(locale = "en-US"),(tsmpRtnMsg = "Resend review member failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1124"),(locale = "zh-TW"),(tsmpRtnMsg = "重送審查會員失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1125"),(locale = "en-US"),(tsmpRtnMsg = "No authorization API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1125"),(locale = "zh-TW"),(tsmpRtnMsg = "查無授權API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1127"),(locale = "en-US"),(tsmpRtnMsg = "No apply API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1127"),(locale = "zh-TW"),(tsmpRtnMsg = "查無申請的API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1128"),(locale = "en-US"),(tsmpRtnMsg = "Apply instructions are required"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1128"),(locale = "zh-TW"),(tsmpRtnMsg = "申請說明為必填"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1129"),(locale = "en-US"),(tsmpRtnMsg = "No files found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1129"),(locale = "zh-TW"),(tsmpRtnMsg = "查無檔案"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1130"),(locale = "en-US"),(tsmpRtnMsg = "No picture"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1130"),(locale = "zh-TW"),(tsmpRtnMsg = "查無圖檔"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1131"),(locale = "en-US"),(tsmpRtnMsg = "No archive classification code"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1131"),(locale = "zh-TW"),(tsmpRtnMsg = "查無檔案分類代碼"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1132"),(locale = "en-US"),(tsmpRtnMsg = "No Category Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1132"),(locale = "zh-TW"),(tsmpRtnMsg = "查無分類Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1133"),(locale = "en-US"),(tsmpRtnMsg = "File not included"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1133"),(locale = "zh-TW"),(tsmpRtnMsg = "未包含檔案"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1134"),(locale = "en-US"),(tsmpRtnMsg = "No FileId"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1134"),(locale = "zh-TW"),(tsmpRtnMsg = "查無FileId"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1135"),(locale = "en-US"),(tsmpRtnMsg = "No unauthorized API information found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1135"),(locale = "zh-TW"),(tsmpRtnMsg = "查無未授權API資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1136"),(locale = "en-US"),(tsmpRtnMsg = "No unauthorized API details"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1136"),(locale = "zh-TW"),(tsmpRtnMsg = "查無未授權API明細"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1137"),(locale = "en-US"),(tsmpRtnMsg = "No unauthorized API module found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1137"),(locale = "zh-TW"),(tsmpRtnMsg = "查無未授權API模組"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1138"),(locale = "en-US"),(tsmpRtnMsg = "Authorization API failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1138"),(locale = "zh-TW"),(tsmpRtnMsg = "授權API失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1139"),(locale = "en-US"),(tsmpRtnMsg = "Unauthorized API history data"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1139"),(locale = "zh-TW"),(tsmpRtnMsg = "查無授權API歷程資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1140"),(locale = "en-US"),(tsmpRtnMsg = "No unreleased members"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1140"),(locale = "zh-TW"),(tsmpRtnMsg = "查無未放行會員"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1141"),(locale = "en-US"),(tsmpRtnMsg = "Membership processing failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1141"),(locale = "zh-TW"),(tsmpRtnMsg = "會員資格處理失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1142"),(locale = "en-US"),(tsmpRtnMsg = "Status or start / end date cannot be empty"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1142"),(locale = "zh-TW"),(tsmpRtnMsg = "狀態或起/迄日不可為空"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1143"),(locale = "en-US"),(tsmpRtnMsg = "Category name cannot be empty"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1143"),(locale = "zh-TW"),(tsmpRtnMsg = "分類名稱不可為空"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1144"),(locale = "en-US"),(tsmpRtnMsg = "No case classification information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1144"),(locale = "zh-TW"),(tsmpRtnMsg = "查無實例分類資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1145"),(locale = "en-US"),(tsmpRtnMsg = "No instance classified data by code"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1145"),(locale = "zh-TW"),(tsmpRtnMsg = "依代碼查無實例分類資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1146"),(locale = "en-US"),(tsmpRtnMsg = "Update instance classification failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1146"),(locale = "zh-TW"),(tsmpRtnMsg = "更新實例分類失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1147"),(locale = "en-US"),(tsmpRtnMsg = "Failed to delete instance classification"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1147"),(locale = "zh-TW"),(tsmpRtnMsg = "刪除實例分類失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1148"),(locale = "en-US"),(tsmpRtnMsg = "New instance is missing required field"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1148"),(locale = "zh-TW"),(tsmpRtnMsg = "新增實例缺少必填欄位"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1149"),(locale = "en-US"),(tsmpRtnMsg = "Failed to add an instance"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1149"),(locale = "zh-TW"),(tsmpRtnMsg = "新增實例失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1150"),(locale = "en-US"),(tsmpRtnMsg = "No instance information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1150"),(locale = "zh-TW"),(tsmpRtnMsg = "查無實例資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1151"),(locale = "en-US"),(tsmpRtnMsg = "Update instance failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1151"),(locale = "zh-TW"),(tsmpRtnMsg = "更新實例失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1152"),(locale = "en-US"),(tsmpRtnMsg = "Failed to delete instance"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1152"),(locale = "zh-TW"),(tsmpRtnMsg = "刪除實例失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1153"),(locale = "en-US"),(tsmpRtnMsg = "No API list"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1153"),(locale = "zh-TW"),(tsmpRtnMsg = "查無API 清單"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1154"),(locale = "en-US"),(tsmpRtnMsg = "Failed to add topic category"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1154"),(locale = "zh-TW"),(tsmpRtnMsg = "主題分類新增失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1155"),(locale = "en-US"),(tsmpRtnMsg = "No subject category list"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1155"),(locale = "zh-TW"),(tsmpRtnMsg = "查無主題分類清單"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1156"),(locale = "en-US"),(tsmpRtnMsg = "No subject classification according to Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1156"),(locale = "zh-TW"),(tsmpRtnMsg = "依照Id查無主題分類"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1157"),(locale = "en-US"),(tsmpRtnMsg = "Topic category update failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1157"),(locale = "zh-TW"),(tsmpRtnMsg = "主題分類更新失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1158"),(locale = "en-US"),(tsmpRtnMsg = "Topic category deletion failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1158"),(locale = "zh-TW"),(tsmpRtnMsg = "主題分類刪除失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1159"),(locale = "en-US"),(tsmpRtnMsg = "New FAQs are missing required fields"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1159"),(locale = "zh-TW"),(tsmpRtnMsg = "新增常見問答缺少必填欄位"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1160"),(locale = "en-US"),(tsmpRtnMsg = "No FAQs found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1160"),(locale = "zh-TW"),(tsmpRtnMsg = "查無常見問答資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1161"),(locale = "en-US"),(tsmpRtnMsg = "The query FAQ is missing parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1161"),(locale = "zh-TW"),(tsmpRtnMsg = "查詢常見問答缺少參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1162"),(locale = "en-US"),(tsmpRtnMsg = "Update FAQ is missing parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1162"),(locale = "zh-TW"),(tsmpRtnMsg = "update常見問答缺少參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1163"),(locale = "en-US"),(tsmpRtnMsg = "Update FAQ failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1163"),(locale = "zh-TW"),(tsmpRtnMsg = "update常見問答失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1164"),(locale = "en-US"),(tsmpRtnMsg = "No FAQ Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1164"),(locale = "zh-TW"),(tsmpRtnMsg = "查無常見問答Id"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1165"),(locale = "en-US"),(tsmpRtnMsg = "delete FAQ failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1165"),(locale = "zh-TW"),(tsmpRtnMsg = "delete常見問答失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1166"),(locale = "en-US"),(tsmpRtnMsg = "About website archiving failure"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1166"),(locale = "zh-TW"),(tsmpRtnMsg = "關於網站存檔失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1167"),(locale = "en-US"),(tsmpRtnMsg = "About missing parameters for website archiving"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1167"),(locale = "zh-TW"),(tsmpRtnMsg = "關於網站存檔缺少參數"),(tsmpRtnDesc = ""));        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1169"),(locale = "en-US"),(tsmpRtnMsg = "Query error about the website, multiple data appears"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1169"),(locale = "zh-TW"),(tsmpRtnMsg = "查詢錯誤關於網站, 出現多筆資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1170"),(locale = "en-US"),(tsmpRtnMsg = "Failed to add a node on the site map"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1170"),(locale = "zh-TW"),(tsmpRtnMsg = "網站地圖新增節點失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1171"),(locale = "en-US"),(tsmpRtnMsg = "Site map new node missing parameter"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1171"),(locale = "zh-TW"),(tsmpRtnMsg = "網站地圖新增節點缺少參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1172"),(locale = "en-US"),(tsmpRtnMsg = "Site map update node failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1172"),(locale = "zh-TW"),(tsmpRtnMsg = "網站地圖更新節點失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1173"),(locale = "en-US"),(tsmpRtnMsg = "Site map update node is missing parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1173"),(locale = "zh-TW"),(tsmpRtnMsg = "網站地圖更新節點缺少參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1174"),(locale = "en-US"),(tsmpRtnMsg = "Site map failed to delete node"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1174"),(locale = "zh-TW"),(tsmpRtnMsg = "網站地圖刪除節點失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1175"),(locale = "en-US"),(tsmpRtnMsg = "Site map delete node missing parameter"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1175"),(locale = "zh-TW"),(tsmpRtnMsg = "網站地圖刪除節點缺少參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1176"),(locale = "en-US"),(tsmpRtnMsg = "Sign-off message setting missing parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1176"),(locale = "zh-TW"),(tsmpRtnMsg = "簽核訊息設定缺少參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1177"),(locale = "en-US"),(tsmpRtnMsg = "Sign-off message setting failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1177"),(locale = "zh-TW"),(tsmpRtnMsg = "簽核訊息設定失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1178"),(locale = "en-US"),(tsmpRtnMsg = "No sign-off message setting"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1178"),(locale = "zh-TW"),(tsmpRtnMsg = "查無簽核訊息設定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1179"),(locale = "en-US"),(tsmpRtnMsg = "Query error signing message settings, multiple data appears"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1179"),(locale = "zh-TW"),(tsmpRtnMsg = "查詢錯誤簽核訊息設定, 出現多筆資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1180"),(locale = "en-US"),(tsmpRtnMsg = "No TsmpUser information found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1180"),(locale = "zh-TW"),(tsmpRtnMsg = "查無TsmpUser資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1181"),(locale = "en-US"),(tsmpRtnMsg = "No module information found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1181"),(locale = "zh-TW"),(tsmpRtnMsg = "查無 module 資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1182"),(locale = "en-US"),(tsmpRtnMsg = "deniedModule archive failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1182"),(locale = "zh-TW"),(tsmpRtnMsg = "deniedModule存檔失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1183"),(locale = "en-US"),(tsmpRtnMsg = "No api-docs available"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1183"),(locale = "zh-TW"),(tsmpRtnMsg = "查無可用的 api-docs 資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1184"),(locale = "en-US"),(tsmpRtnMsg = "The reqHeader parameter or switch is incorrect"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1184"),(locale = "zh-TW"),(tsmpRtnMsg = "reqHeader參數or開關不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1185"),(locale = "en-US"),(tsmpRtnMsg = "paramType does not match"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1185"),(locale = "zh-TW"),(tsmpRtnMsg = "paramType不匹配"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1186"),(locale = "en-US"),(tsmpRtnMsg = "apiKey or URL is empty"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1186"),(locale = "zh-TW"),(tsmpRtnMsg = "apiKey or URL 為空"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1187"),(locale = "en-US"),(tsmpRtnMsg = "API authorized"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1187"),(locale = "zh-TW"),(tsmpRtnMsg = "API已授權"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1188"),(locale = "en-US"),(tsmpRtnMsg = "Member password must be at least 6 codes"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1188"),(locale = "zh-TW"),(tsmpRtnMsg = "會員密碼最少6碼"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1189"),(locale = "en-US"),(tsmpRtnMsg = "API has applied"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1189"),(locale = "zh-TW"),(tsmpRtnMsg = "API已申請"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1190"),(locale = "en-US"),(tsmpRtnMsg = "Member account already exists"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1190"),(locale = "zh-TW"),(tsmpRtnMsg = "會員帳號已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1191"), (locale = "zh-TW"), (tsmpRtnMsg = "資料已被異動"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1191"), (locale = "en-US"), (tsmpRtnMsg = "Information has been changed"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1192"),(locale = "en-US"),(tsmpRtnMsg = "Status is enabled and cannot be deleted"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1192"),(locale = "zh-TW"),(tsmpRtnMsg = "狀態為啟用不可刪除"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1193"),(locale = "en-US"),(tsmpRtnMsg = "You do not have permission to apply for this API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1193"),(locale = "zh-TW"),(tsmpRtnMsg = "您沒有權限申請此API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1194"),(locale = "en-US"),(tsmpRtnMsg = "Added [Announcement Message] missing required information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1194"),(locale = "zh-TW"),(tsmpRtnMsg = "新增[公告消息]缺少必填資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1195"),(locale = "en-US"),(tsmpRtnMsg = "Added [Announcement Message] upload file archive failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1195"),(locale = "zh-TW"),(tsmpRtnMsg = "新增[公告消息]上傳檔案存檔失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1196"),(locale = "zh-TW"),(tsmpRtnMsg = "更新[公告消息]缺少必填資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1196"),(locale = "en-US"),(tsmpRtnMsg = "Update [Announcement Message] Missing required information"),(tsmpRtnDesc = ""));  
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1197"),(locale = "zh-TW"),(tsmpRtnMsg = "資料已被異動, 更新[公告消息]失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1197"),(locale = "en-US"),(tsmpRtnMsg = "Update [Announcement Message] Missing required information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1198"),(locale = "en-US"),(tsmpRtnMsg = "No [Announcement News] list information"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1198"),(locale = "zh-TW"),(tsmpRtnMsg = "查無[公告消息]清單資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1199"), (locale = "en-US"), (tsmpRtnMsg = "system error"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1199"), (locale = "zh-TW"), (tsmpRtnMsg = "系統錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1200"),(locale = "en-US"),(tsmpRtnMsg = "Unable to completely delete the specified [Announcement Message] material"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1200"),(locale = "zh-TW"),(tsmpRtnMsg = "無法完整刪除指定的[公告消息]資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1201"),(locale = "en-US"),(tsmpRtnMsg = "Incorrect encoding"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1201"),(locale = "zh-TW"),(tsmpRtnMsg = "編碼不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1203"),(locale = "en-US"),(tsmpRtnMsg = "File write error"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1203"),(locale = "zh-TW"),(tsmpRtnMsg = "檔案寫入錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1202"), (locale = "en-US"), (tsmpRtnMsg = "No type list"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1202"), (locale = "zh-TW"), (tsmpRtnMsg = "查無類型清單"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1204"),(locale = "en-US"),(tsmpRtnMsg = "Can''t find topic classification"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1204"),(locale = "zh-TW"),(tsmpRtnMsg = "找不到主題分類圖檔"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1206"),(locale = "en-US"),(tsmpRtnMsg = "Unable to replace the theme classification file"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1206"),(locale = "zh-TW"),(tsmpRtnMsg = "無法替換主題分類圖檔"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1208"),(locale = "en-US"),(tsmpRtnMsg = "no subject"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1208"),(locale = "zh-TW"),(tsmpRtnMsg = "查無主題"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1209"),(locale = "en-US"),(tsmpRtnMsg = "Unable to completely delete the specified [subject] data"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1209"),(locale = "zh-TW"),(tsmpRtnMsg = "無法完整刪除指定的[主題]資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1210"),(locale = "en-US"),(tsmpRtnMsg = "Level and role archive errors"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1210"),(locale = "zh-TW"),(tsmpRtnMsg = "關卡與角色存檔錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1211"),(locale = "en-US"),(tsmpRtnMsg = "No level and role"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1211"),(locale = "zh-TW"),(tsmpRtnMsg = "查無關卡與角色"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1212"),(locale = "en-US"),(tsmpRtnMsg = "Failed to add message"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1212"),(locale = "zh-TW"),(tsmpRtnMsg = "新增消息失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1213"),(locale = "en-US"),(tsmpRtnMsg = "New review apply failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1213"),(locale = "zh-TW"),(tsmpRtnMsg = "新增審核申請失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1214"),(locale = "en-US"),(tsmpRtnMsg = "Resubmission failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1214"),(locale = "zh-TW"),(tsmpRtnMsg = "重新送審失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1215"),(locale = "en-US"),(tsmpRtnMsg = "No pending orders"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1215"),(locale = "zh-TW"),(tsmpRtnMsg = "查無待審單"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1216"),(locale = "en-US"),(tsmpRtnMsg = "Inquiry about pending order error"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1216"),(locale = "zh-TW"),(tsmpRtnMsg = "查詢待審單發生錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1217"),(locale = "en-US"),(tsmpRtnMsg = "Document query error"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1217"),(locale = "zh-TW"),(tsmpRtnMsg = "單據查詢錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1218"),(locale = "en-US"),(tsmpRtnMsg = "Document review / change failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1218"),(locale = "zh-TW"),(tsmpRtnMsg = "單據審核/變更失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1219"), (locale = "en-US"), (tsmpRtnMsg = "Permission denied"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1219"), (locale = "zh-TW"), (tsmpRtnMsg = "沒有權限"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1220"), (locale = "en-US"), (tsmpRtnMsg = "Failed to save, the data length is too large"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1220"), (locale = "zh-TW"), (tsmpRtnMsg = "儲存失敗，資料長度過大"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1221"), (locale = "en-US"), (tsmpRtnMsg = "Some of the selected data do not belong to your organization, the transaction fails"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1221"), (locale = "zh-TW"), (tsmpRtnMsg = "所選的資料有部份不屬於您的組織,異動失敗"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1222"),(locale = "en-US"),(tsmpRtnMsg = "The transaction failed because it does not belong to your organization"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1222"),(locale = "zh-TW"),(tsmpRtnMsg = "異動失敗,因為不屬於您的組織"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1223"),(locale = "en-US"),(tsmpRtnMsg = "Failed to update apply form"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1223"),(locale = "zh-TW"),(tsmpRtnMsg = "更新申請單失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1224"),(locale = "en-US"),(tsmpRtnMsg = "[Announcement Message] Unable to query"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1224"),(locale = "zh-TW"),(tsmpRtnMsg = "[公告消息]無法查詢"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1226"),(locale = "zh-TW"),(tsmpRtnMsg = "重複的回覆代碼"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1226"),(locale = "en-US"),(tsmpRtnMsg = "Duplicate return code"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1227"), (locale = "zh-TW"), (tsmpRtnMsg = "生效日期不可小於今天"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1227"), (locale = "en-US"), (tsmpRtnMsg = "Dates in the past is not allowed on Effective date"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1228"),(locale = "zh-TW"),(tsmpRtnMsg = "該用戶端尚未註冊完成，無法申請API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1228"),(locale = "en-US"),(tsmpRtnMsg = "The client you chose is required to be registered"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1229"),(locale = "en-US"),(tsmpRtnMsg = "Organization not exist"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1229"),(locale = "zh-TW"),(tsmpRtnMsg = "組織名稱不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1230"), (locale = "zh-TW"), (tsmpRtnMsg = "角色不存在"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1230"), (locale = "en-US"), (tsmpRtnMsg = "Role not exist"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1231"), (locale = "zh-TW"), (tsmpRtnMsg = "使用者不存在"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1231"), (locale = "en-US"), (tsmpRtnMsg = "User not exist"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1232"),(locale = "en-US"),(tsmpRtnMsg = "TUser name duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1232"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1233"),(locale = "en-US"),(tsmpRtnMsg = "File cannot be empty"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1233"),(locale = "zh-TW"),(tsmpRtnMsg = "檔案不得為空檔"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1234"),(locale = "en-US"),(tsmpRtnMsg = "New Password cannot be empty"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1234"),(locale = "zh-TW"),(tsmpRtnMsg = "新密碼不可為空"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1235"),(locale = "en-US"),(tsmpRtnMsg = "New Password was the same as Original Password"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1235"),(locale = "zh-TW"),(tsmpRtnMsg = "新密碼與原密碼相同"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1236"),(locale = "en-US"),(tsmpRtnMsg = "Original Password cannot be empty"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1236"),(locale = "zh-TW"),(tsmpRtnMsg = "原密碼不可為空"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1237"),(locale = "en-US"),(tsmpRtnMsg = "Enter at least one"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1237"),(locale = "zh-TW"),(tsmpRtnMsg = "至少輸入一項"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1238"),(locale = "en-US"),(tsmpRtnMsg = "Original password is incorrect"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1238"),(locale = "zh-TW"),(tsmpRtnMsg = "原密碼不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1239"),(locale = "en-US"),(tsmpRtnMsg = "Role alias duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1239"),(locale = "zh-TW"),(tsmpRtnMsg = "角色代號重複"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1240"),(locale = "en-US"),(tsmpRtnMsg = "Role name duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1240"),(locale = "zh-TW"),(tsmpRtnMsg = "角色名稱重複"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1241"),(locale = "en-US"),(tsmpRtnMsg = "Function not found with locale"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1241"),(locale = "zh-TW"),(tsmpRtnMsg = "功能不存在 含locale"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1242"),(locale = "en-US"),(tsmpRtnMsg = "An error occured while transiting the schedule form, try to reset"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1242"),(locale = "zh-TW"),(tsmpRtnMsg = "週期表單轉換錯誤，請重新設定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1243"),(locale = "en-US"),(tsmpRtnMsg = "Role used by user"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1243"),(locale = "zh-TW"),(tsmpRtnMsg = "該角色有使用者"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1244"),(locale = "en-US"),(tsmpRtnMsg = "User email:Email format only"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1244"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者E-mail:只能為Email格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1245"),(locale = "en-US"),(tsmpRtnMsg = "User account: only English letters (a~z, A~Z) and numbers can be entered without blanks"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1245"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者帳號:只能輸入英文字母(a~z,A~Z)及數字且不含空白"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1246"),(locale = "en-US"),(tsmpRtnMsg = "User account: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1246"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1247"),(locale = "en-US"),(tsmpRtnMsg = "User name: Length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1247"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1248"),(locale = "en-US"),(tsmpRtnMsg = "Password: Length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1248"),(locale = "zh-TW"),(tsmpRtnMsg = "密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1249"),(locale = "en-US"),(tsmpRtnMsg = "Role list: required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1249"),(locale = "zh-TW"),(tsmpRtnMsg = "角色清單:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1250"),(locale = "en-US"),(tsmpRtnMsg = "Organization name: required parameter"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1250"),(locale = "zh-TW"),(tsmpRtnMsg = "組織名稱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1251"), (locale = "zh-TW"), (tsmpRtnMsg = "當前排程狀態不允許異動"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1251"), (locale = "en-US"), (tsmpRtnMsg = "Inconsistent operation with current scheduler status"), (tsmpRtnDesc = ""));     
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1252"),(locale = "en-US"),(tsmpRtnMsg = "User mail:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1252"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1253"),(locale = "en-US"),(tsmpRtnMsg = "Organization name:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1253"),(locale = "zh-TW"),(tsmpRtnMsg = "組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1256"),(locale = "en-US"),(tsmpRtnMsg = "New role name:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1256"),(locale = "zh-TW"),(tsmpRtnMsg = "新角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1257"),(locale = "en-US"),(tsmpRtnMsg = "User account: required parameter"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1257"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者帳號:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1258"),(locale = "en-US"),(tsmpRtnMsg = "User name: required parameter"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1258"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者名稱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1259"),(locale = "en-US"),(tsmpRtnMsg = "Password: required parameter"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1259"),(locale = "zh-TW"),(tsmpRtnMsg = "密碼:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1260"),(locale = "en-US"),(tsmpRtnMsg = "User E-mail: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1260"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者E-mail:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1261"), (locale = "zh-TW"), (tsmpRtnMsg = "狀態:必填參數"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1261"), (locale = "en-US"), (tsmpRtnMsg = "Status: required parameter"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1262"),(locale = "en-US"),(tsmpRtnMsg = "Authorizable role  [{{0}}]  does not exist"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1262"),(locale = "zh-TW"),(tsmpRtnMsg = "可授權角色 [{{0}}] 不存在"),(tsmpRtnDesc = ""));  
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1263"),(locale = "en-US"),(tsmpRtnMsg = "Authorizable roles: length limit [{{0}}] characters, [{{1}}] length [{{2}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1263"),(locale = "zh-TW"),(tsmpRtnMsg = "可授權角色:長度限制 [{{0}}] 字內,[{{1}}] 長度[{{2}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1264"), (locale = "zh-TW"), (tsmpRtnMsg = "登入角色不存在"), (tsmpRtnDesc = ""));  
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1264"), (locale = "en-US"), (tsmpRtnMsg = "Login role does not exist"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1266"),(locale = "en-US"),(tsmpRtnMsg = "Authorizable roles: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1266"),(locale = "zh-TW"),(tsmpRtnMsg = "可授權角色:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1267"),(locale = "en-US"),(tsmpRtnMsg = "Login role: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1267"),(locale = "zh-TW"),(tsmpRtnMsg = "登入角色:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1268"),(locale = "en-US"),(tsmpRtnMsg = "The list of authorized roles for login roles already exists"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1268"),(locale = "zh-TW"),(tsmpRtnMsg = "登入角色的可授權角色清單已經存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1269"),(locale = "en-US"),(tsmpRtnMsg = "Organization name duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1269"),(locale = "zh-TW"),(tsmpRtnMsg = "組織名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1270"),(locale = "en-US"),(tsmpRtnMsg = "User account duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1270"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者帳號已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1271"),(locale = "en-US"),(tsmpRtnMsg = "Upper layer organization name: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1271"),(locale = "zh-TW"),(tsmpRtnMsg = "上層組織名稱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1273"),(locale = "en-US"),(tsmpRtnMsg = "Organization ID: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1273"),(locale = "zh-TW"),(tsmpRtnMsg = "組織單位ID:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1282"),(locale = "en-US"),(tsmpRtnMsg = "New role name duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1282"),(locale = "zh-TW"),(tsmpRtnMsg = "新角色名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1283"),(locale = "en-US"),(tsmpRtnMsg = "New role name: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1283"),(locale = "zh-TW"),(tsmpRtnMsg = "新角色名稱:必填參數"),(tsmpRtnDesc = ""));
	       
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1284"), (locale = "en-US"), (tsmpRtnMsg = "Duplicated value: [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1284"), (locale = "zh-TW"), (tsmpRtnMsg = "[{{0}}] 不得重複"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1285"), (locale = "en-US"), (tsmpRtnMsg = "Return code parameter does not meet the definition of multi-language"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1285"), (locale = "zh-TW"), (tsmpRtnMsg = "Return code 參數不符合多國語系定義"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1286"), (locale = "zh-TW"), (tsmpRtnMsg = "更新失敗"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1286"), (locale = "en-US"), (tsmpRtnMsg = "Update failed"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1287"), (locale = "zh-TW"), (tsmpRtnMsg = "刪除失敗"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1287"), (locale = "en-US"), (tsmpRtnMsg = "Delete failed"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1288"),(locale = "en-US"),(tsmpRtnMsg = "Add user fail"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1288"),(locale = "zh-TW"),(tsmpRtnMsg = "新增失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1289"), (locale = "en-US"), (tsmpRtnMsg = "No Rtn Code  [{{1}}] messages from Locale [{{0}}] "), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1289"), (locale = "zh-TW"), (tsmpRtnMsg = "查無 Locale [{{0}}] 的 Rtn Code [{{1}}] 訊息"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1290"), (locale = "zh-TW"), (tsmpRtnMsg = "參數錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1290"), (locale = "en-US"), (tsmpRtnMsg = "Parameter error"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1291"),(locale = "en-US"),(tsmpRtnMsg = "File parsing error"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1291"),(locale = "zh-TW"),(tsmpRtnMsg = "檔案解析錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1292"), (locale = "en-US"), (tsmpRtnMsg = "Work Queue is full,please execute later"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1292"), (locale = "zh-TW"), (tsmpRtnMsg = "工作佇例已滿,請稍後再執行"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1293"), (locale = "en-US"), (tsmpRtnMsg = "Database error"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1293"), (locale = "zh-TW"), (tsmpRtnMsg = "資料庫錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1295"), (locale = "zh-TW"), (tsmpRtnMsg = "日期格式不正確"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1295"), (locale = "en-US"), (tsmpRtnMsg = "Date format is incorrect"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1294"),(locale = "en-US"),(tsmpRtnMsg = "No query permission"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1294"),(locale = "zh-TW"),(tsmpRtnMsg = "無查詢權限"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1296"), (locale = "zh-TW"), (tsmpRtnMsg = "缺少必填參數"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1296"), (locale = "en-US"), (tsmpRtnMsg = "Missing required parameter"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1297"), (locale = "en-US"), (tsmpRtnMsg = "Execution error"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1297"), (locale = "zh-TW"), (tsmpRtnMsg = "執行錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1298"), (locale = "zh-TW"), (tsmpRtnMsg = "查無資料"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1298"), (locale = "en-US"), (tsmpRtnMsg = "No information found"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1299"),(locale = "en-US"),(tsmpRtnMsg = "Parameter validation error"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1299"),(locale = "zh-TW"),(tsmpRtnMsg = "參數驗證錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1303"),(locale = "en-US"),(tsmpRtnMsg = "The organization parent node cannot move to its own sub-organization"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1303"),(locale = "zh-TW"),(tsmpRtnMsg = "節點不可以移動到子節點"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1304"),(locale = "en-US"),(tsmpRtnMsg = "The organization contains undeleted API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1304"),(locale = "zh-TW"),(tsmpRtnMsg = "組織包含未刪除的API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1305"),(locale = "en-US"),(tsmpRtnMsg = "The organization contains undeleted Users"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1305"),(locale = "zh-TW"),(tsmpRtnMsg = "該組織包含未刪除的用戶"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1306"),(locale = "en-US"),(tsmpRtnMsg = "The organization contains undeleted Java Module"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1306"),(locale = "zh-TW"),(tsmpRtnMsg = "組織包含未刪除的Java模組"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1307"),(locale = "en-US"),(tsmpRtnMsg = "The organization contains undeleted .Net Module"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1307"),(locale = "zh-TW"),(tsmpRtnMsg = "組織包含未刪除的.Net模組"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1308"),(locale = "en-US"),(tsmpRtnMsg = "The organization contains undeleted sub organization"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1308"),(locale = "zh-TW"),(tsmpRtnMsg = "組織包含未刪除的子組織"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1309"),(locale = "en-US"),(tsmpRtnMsg = "Function list: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1309"),(locale = "zh-TW"),(tsmpRtnMsg = "功能清單:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1311"),(locale = "en-US"),(tsmpRtnMsg = "Contact person mail:Email format only"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1311"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人信箱:只能為Email格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1312"),(locale = "en-US"),(tsmpRtnMsg = "Upper layer organization: You cannot choose the node itself"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1312"),(locale = "zh-TW"),(tsmpRtnMsg = "上層單位組織：不可以選擇節點自己本身"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1330"),(locale = "en-US"),(tsmpRtnMsg = "Password: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1330"),(locale = "zh-TW"),(tsmpRtnMsg = "密碼:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1334"),(locale = "zh-TW"),(tsmpRtnMsg = "擁有者:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1334"),(locale = "en-US"),(tsmpRtnMsg = "Owner: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1335"),(locale = "zh-TW"),(tsmpRtnMsg = "狀態:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1335"),(locale = "en-US"),(tsmpRtnMsg = "Status: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1336"),(locale = "zh-TW"),(tsmpRtnMsg = "開放狀態:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1336"),(locale = "en-US"),(tsmpRtnMsg = "Open state: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1337"),(locale = "zh-TW"),(tsmpRtnMsg = "開始日期:只能輸入日期格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1337"),(locale = "en-US"),(tsmpRtnMsg = "Start date:Only date format can be entered"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1338"),(locale = "zh-TW"),(tsmpRtnMsg = "到期日期:只能輸入日期格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1338"),(locale = "en-US"),(tsmpRtnMsg = "End date:Only date format can be entered"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1339"),(locale = "zh-TW"),(tsmpRtnMsg = "服務時間:只能輸入時間格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1339"),(locale = "en-US"),(tsmpRtnMsg = "Service time:Only time format can be entered"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1340"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端代號已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1340"),(locale = "en-US"),(tsmpRtnMsg = "Client code duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1341"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1341"),(locale = "en-US"),(tsmpRtnMsg = "Client name duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1342"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端帳號已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1342"),(locale = "en-US"),(tsmpRtnMsg = "Client account duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1343"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端帳號:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1343"),(locale = "en-US"),(tsmpRtnMsg = "Client account: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1344"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1344"),(locale = "en-US"),(tsmpRtnMsg = "Client does not exist"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1345"),(locale = "zh-TW"),(tsmpRtnMsg = "主機名稱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1345"),(locale = "en-US"),(tsmpRtnMsg = "Host name: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1346"),(locale = "zh-TW"),(tsmpRtnMsg = "主機IP:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1346"),(locale = "en-US"),(tsmpRtnMsg = "Host IP: Required parameters"),(tsmpRtnDesc = "")); 
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1357"), (locale = "zh-TW"), (tsmpRtnMsg = "您的角色並未授權使用 API txID [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1357"), (locale = "en-US"), (tsmpRtnMsg = "The roles assigned to you are not authorized to call API txID [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1410"), (locale = "zh-TW"), (tsmpRtnMsg = "群組[{{0}}]不存在"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1410"), (locale = "en-US"), (tsmpRtnMsg = "Group [{{0}}] does not exist"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1433"), (locale = "zh-TW"), (tsmpRtnMsg = "非對稱式加密失敗：[{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1433"), (locale = "en-US"), (tsmpRtnMsg = "Asymmetric encryption error: [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1434"), (locale = "zh-TW"), (tsmpRtnMsg = "非對稱式解密失敗：[{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1434"), (locale = "en-US"), (tsmpRtnMsg = "Asymmetric decryption error: [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1474"), (locale = "zh-TW"), (tsmpRtnMsg = "設定檔缺少參數 [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1474"), (locale = "en-US"), (tsmpRtnMsg = "The profile is missing parameters [{{0}}]"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1491"),(locale = "en-US"),(tsmpRtnMsg = "BeanName Not Found: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1491"),(locale = "zh-TW"),(tsmpRtnMsg = "執行工作不存在: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1492"),(locale = "en-US"),(tsmpRtnMsg = "Custom package does not exist"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1492"),(locale = "zh-TW"),(tsmpRtnMsg = "客製包不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1496"),(locale = "en-US"),(tsmpRtnMsg = "Failed to log in to digiRunner"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1496"),(locale = "zh-TW"),(tsmpRtnMsg = "登入 digiRunner 失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1497"),(locale = "en-US"),(tsmpRtnMsg = "Invoke API specification error：[{{0}}] - [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1497"),(locale = "zh-TW"),(tsmpRtnMsg = "介接規格錯誤：[{{0}}] - [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1498"),(locale = "en-US"),(tsmpRtnMsg = "HTTP error：[{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1498"),(locale = "zh-TW"),(tsmpRtnMsg = "HTTP error：[{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1499"),(locale = "en-US"),(tsmpRtnMsg = "Invoke API logic error：[{{0}}] - [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1499"),(locale = "zh-TW"),(tsmpRtnMsg = "介接邏輯錯誤：[{{0}}] - [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1500"),(locale = "zh-TW"),(tsmpRtnMsg = "Base64 Decode 錯誤: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1500"),(locale = "en-US"),(tsmpRtnMsg = "Base64 Decode error: {{0}}"),(tsmpRtnDesc = ""));
	        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1503"),(locale = "en-US"),(tsmpRtnMsg = "Http Method: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1503"),(locale = "zh-TW"),(tsmpRtnMsg = "Http Method:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1510"),(locale = "zh-TW"),(tsmpRtnMsg = "LDAP驗證失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1510"),(locale = "en-US"),(tsmpRtnMsg = "LDAP verification failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1511"),(locale = "zh-TW"),(tsmpRtnMsg = "LDAP未啟用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1511"),(locale = "en-US"),(tsmpRtnMsg = "LDAP is not enabled"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1512"),(locale = "zh-TW"),(tsmpRtnMsg = "LDAP連線失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1512"),(locale = "en-US"),(tsmpRtnMsg = "LDAP connection failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1513"),(locale = "zh-TW"),(tsmpRtnMsg = "User IP 不在可登入的網段中"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1513"),(locale = "en-US"),(tsmpRtnMsg = "User IP is not in the network segment that can be logged in"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1515"), (locale = "zh-TW"), (tsmpRtnMsg = "單個用戶不能存在於多個群組中"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1515"), (locale = "en-US"), (tsmpRtnMsg = "A single user cannot exist in multiple group"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1516"), (locale = "zh-TW"), (tsmpRtnMsg = "使用者沒有任何群組"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1516"), (locale = "en-US"), (tsmpRtnMsg = "User already exists with No Group"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1520"),(locale = "en-US"),(tsmpRtnMsg = "Permission denied by digiRunner"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1520"),(locale = "zh-TW"),(tsmpRtnMsg = "digiRunner拒絕存取"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1522"),(locale = "zh-TW"),(tsmpRtnMsg = "CApiKey驗證失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1522"),(locale = "en-US"),(tsmpRtnMsg = "CApiKey verification failed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1523"),(locale = "zh-TW"),(tsmpRtnMsg = "時區必須填寫"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1523"),(locale = "en-US"),(tsmpRtnMsg = "Time zone must be filled in"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2000"),(locale = "en-US"),(tsmpRtnMsg = "Required"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2000"),(locale = "zh-TW"),(tsmpRtnMsg = "必填"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2001"),(locale = "en-US"),(tsmpRtnMsg = "Max length [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2001"),(locale = "zh-TW"),(tsmpRtnMsg = "最大長度為 [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2002"),(locale = "en-US"),(tsmpRtnMsg = "Min length [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2002"),(locale = "zh-TW"),(tsmpRtnMsg = "最小長度為 [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2003"),(locale = "en-US"),(tsmpRtnMsg = "Must contain [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2003"),(locale = "zh-TW"),(tsmpRtnMsg = "必須包含 [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2004"),(locale = "en-US"),(tsmpRtnMsg = "Must not contain [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2004"),(locale = "zh-TW"),(tsmpRtnMsg = "不得包含 [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2005"),(locale = "en-US"),(tsmpRtnMsg = "No greater than [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2005"),(locale = "zh-TW"),(tsmpRtnMsg = "數值不可大於 [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2006"),(locale = "en-US"),(tsmpRtnMsg = "No less than [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2006"),(locale = "zh-TW"),(tsmpRtnMsg = "數值不可小於 [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2007"),(locale = "en-US"),(tsmpRtnMsg = "Incorrect format"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2007"),(locale = "zh-TW"),(tsmpRtnMsg = "格式不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2008"),(locale = "en-US"),(tsmpRtnMsg = "Only alphanumeric characters, ''_'' and ''-'' are accepted"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2008"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可輸入英數字、底線「_」及橫線「-」"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2009"),(locale = "en-US"),(tsmpRtnMsg = "Select at least [{{0}}] items"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2009"),(locale = "zh-TW"),(tsmpRtnMsg = "最少選擇 [{{0}}] 項"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2010"),(locale = "en-US"),(tsmpRtnMsg = "Select at most [{{0}}] items"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2010"),(locale = "zh-TW"),(tsmpRtnMsg = "最多選擇 [{{0}}] 項"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2011"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可輸入英數字、底線「_」、橫線「-」及句號「.」"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2012"),(locale = "en-US"),(tsmpRtnMsg = "Approximately"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2012"),(locale = "zh-TW"),(tsmpRtnMsg = "大約"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2013"),(locale = "en-US"),(tsmpRtnMsg = "Every day"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2013"),(locale = "zh-TW"),(tsmpRtnMsg = "每一天"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2014"),(locale = "en-US"),(tsmpRtnMsg = "Every month on the {{0}} day"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2014"),(locale = "zh-TW"),(tsmpRtnMsg = "每個月 {{0}} 號"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2015"),(locale = "en-US"),(tsmpRtnMsg = "Every month on every {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2015"),(locale = "zh-TW"),(tsmpRtnMsg = "每星期{{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2016"),(locale = "en-US"),(tsmpRtnMsg = " at {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2016"),(locale = "zh-TW"),(tsmpRtnMsg = "的 {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2017"),(locale = "en-US"),(tsmpRtnMsg = "Every 10 minutes"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2017"),(locale = "zh-TW"),(tsmpRtnMsg = "每十分鐘"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2018"),(locale = "en-US"),(tsmpRtnMsg = "Every half an hour"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2018"),(locale = "zh-TW"),(tsmpRtnMsg = "每半小時"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2019"),(locale = "en-US"),(tsmpRtnMsg = "Every hour"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2019"),(locale = "zh-TW"),(tsmpRtnMsg = "每一小時"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2020"),(locale = "en-US"),(tsmpRtnMsg = "Every {{0}} hours"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2020"),(locale = "zh-TW"),(tsmpRtnMsg = "每 {{0}} 小時"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2021"),(locale = "en-US"),(tsmpRtnMsg = "Only numbers are accepted"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2021"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可輸入數字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2022"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可輸入中英數字、底線「_」及橫線「-」"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2023"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可輸入英數字、底線「_」、橫線「-」、點「.」及「@」"),(tsmpRtnDesc = ""));
	        
	        
	        //=================================未排序==================================================
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1402"),(locale = "en-US"),(tsmpRtnMsg = "The amount of API can be up to a maximum of [{{0}}], you selected [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1402"),(locale = "zh-TW"),(tsmpRtnMsg = "虛擬群組的API數量上限為 [{{0}}],您選擇 [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1403"),(locale = "zh-TW"),(tsmpRtnMsg = "無法刪除,請解除用戶端的虛擬授權設定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1404"),(locale = "zh-TW"),(tsmpRtnMsg = "無法刪除,請解除群組的授權核身種類: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1406"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 數量不可少於 [{{1}}],您選擇 [{{2}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1407"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 數量不可超過 [{{1}}],您選擇 [{{2}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1413"),(locale = "zh-TW"),(tsmpRtnMsg = "無法啟用,部署容器未綁定任何節點"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1414"),(locale = "zh-TW"),(tsmpRtnMsg = "無法刪除,部署容器啟用中"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1426"),(locale = "zh-TW"),(tsmpRtnMsg = "無法使用此模組名稱,已由匯入方式建立"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1427"),(locale = "en-US"),(tsmpRtnMsg = "The exception type can only be N, W, M, D"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1430"),(locale = "zh-TW"),(tsmpRtnMsg = "取得授權碼失敗,請重新再試"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1435"),(locale = "zh-TW"),(tsmpRtnMsg = "The time range can only be T, W, M, D"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1438"),(locale = "zh-TW"),(tsmpRtnMsg = "刪除失敗,模組啟用中"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1439"),(locale = "zh-TW"),(tsmpRtnMsg = "刪除失敗,模組已綁定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1446"),(locale = "en-US"),(tsmpRtnMsg = "Exception date, start time and end time cannot be filled in"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1452"),(locale = "zh-TW"),(tsmpRtnMsg = "{{0}} API 失敗：apiKey=[{{1}}], moduleName=[{{2}}], msg={{3}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1452"),(locale = "en-US"),(tsmpRtnMsg = "{{0}} API Failed. ApiKey=[{{1}}], moduleName=[{{2}}], msg={{3}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1453"),(locale = "zh-TW"),(tsmpRtnMsg = "Composer Flow 轉型失敗：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1453"),(locale = "en-US"),(tsmpRtnMsg = "Fail to parse flow. Apikey=[{{0}}], moduleName=[{{1}}], msg={{2}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1454"),(locale = "zh-TW"),(tsmpRtnMsg = "寫入 Composer 資料錯誤：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1454"),(locale = "en-US"),(tsmpRtnMsg = "Fail to write Composer data. Apikey=[{{0}}], moduleName=[{{1}}], msg={{2}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1460"),(locale = "zh-TW"),(tsmpRtnMsg = "尚未確認部署,無法啟用組合API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1461"),(locale = "zh-TW"),(tsmpRtnMsg = "尚有 API 未下架,確定繼續執行？"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1461"),(locale = "en-US"),(tsmpRtnMsg = "Some APIs are still launched, continue?"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1462"),(locale = "zh-TW"),(tsmpRtnMsg = "尚有 API 正在申請單流程中,確定繼續執行？"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1462"),(locale = "en-US"),(tsmpRtnMsg = "Some APIs are included in application forms which are still in progress, continue?"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1463"),(locale = "zh-TW"),(tsmpRtnMsg = "刪除失敗,API啟用中"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1464"),(locale = "zh-TW"),(tsmpRtnMsg = "刪除失敗,API仍有相關的模組紀錄"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1465"),(locale = "zh-TW"),(tsmpRtnMsg = "有些是透過匯入外部介接規格而註冊的 API,確定繼續執行？"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1465"),(locale = "en-US"),(tsmpRtnMsg = "Some APIs are registered by importing OpenAPI Spec, continue?"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1475"),(locale = "zh-TW"),(tsmpRtnMsg = "無法綁定,部署容器尚未啟用: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1476"),(locale = "zh-TW"),(tsmpRtnMsg = "無法解除綁定,已經沒有其他部署容器啟用此系統模組了"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1478"),(locale = "zh-TW"),(tsmpRtnMsg = "申請內容說明：長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1481"),(locale = "en-US"),(tsmpRtnMsg = "Data in TSMP_API is not consistent with TSMP_API_REG: ApiKey={{0}}, ModuleName={{1}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1481"),(locale = "zh-TW"),(tsmpRtnMsg = "TSMP_API 資料與 TSMP_API_REG 不一致: ApiKey={{0}}, ModuleName={{1}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1487"),(locale = "en-US"),(tsmpRtnMsg = "The host status can only be A, S"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1489"),(locale = "en-US"),(tsmpRtnMsg = "Register Host : length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1489"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1490"),(locale = "en-US"),(tsmpRtnMsg = "Register Host Status: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1490"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機狀態:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1501"),(locale = "en-US"),(tsmpRtnMsg = "Group maintenance: {{0}}, authorization scope: {{1}} is currently bound, please remove it first."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1501"),(locale = "zh-TW"),(tsmpRtnMsg = "群組代碼：{{0}},授權範圍代碼：{{1}}目前有綁定,請先移除。"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1502"),(locale = "en-US"),(tsmpRtnMsg = "The API list currently has data, please delete it first."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1502"),(locale = "zh-TW"),(tsmpRtnMsg = "API列表目前有資料,請先刪除。"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1504"),(locale = "zh-TW"),(tsmpRtnMsg = "可能是髒資料導致系統無法匹配,{{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1504"),(locale = "en-US"),(tsmpRtnMsg = "It may be that dirty data caused the system to fail to match, {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1514"),(locale = "zh-TW"),(tsmpRtnMsg = "您所在的網段,無法登入"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1519"),(locale = "en-US"),(tsmpRtnMsg = "User is not logged in,please open Composer through digiRunner"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1519"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者未登入,請透過digiRunner 開啟Composer"),(tsmpRtnDesc = ""));
	       
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1398"),(locale =  "en-US"),(tsmpRtnMsg = "Group name exists"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1398"),(locale =  "zh-TW"),(tsmpRtnMsg = "群組名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1399"),(locale =  "en-US"),(tsmpRtnMsg = "Group code exists"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1399"),(locale =  "zh-TW"),(tsmpRtnMsg = "群組代碼已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1400"),(locale =  "en-US"),(tsmpRtnMsg = "API: [{{0}}] does not  found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1400"),(locale =  "zh-TW"),(tsmpRtnMsg = "API: [{{0}}]不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1401"),(locale =  "en-US"),(tsmpRtnMsg = "API: [{{0}}]不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1401"),(locale =  "zh-TW"),(tsmpRtnMsg = "用戶端狀態不正常：[{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1477"),(locale = "en-US"),(tsmpRtnMsg = "Client ID: Only alphanumeric characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2011"),(locale = "en-US"),(tsmpRtnMsg = "Only alphanumeric characters, ''_'', ''-'' and ''.'' are accepted"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2022"),(locale = "en-US"),(tsmpRtnMsg = "Only Chinese and English numbers, underscore ''_'' and horizontal line ''-'' can be input "),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2023"),(locale = "en-US"),(tsmpRtnMsg = "Only alphanumeric characters, ''_'', ''-'', ''.'' and ''@'' are accepted"),(tsmpRtnDesc = ""));
	        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1265"),(locale = "en-US"),(tsmpRtnMsg = "Login role: length limit [{{0}}] characters, length [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1265"),(locale = "zh-TW"),(tsmpRtnMsg = "登入角色:長度限制 [{{0}}] 字內,長度[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1272"),(locale = "en-US"),(tsmpRtnMsg = "Upper layer organization name:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1272"),(locale = "zh-TW"),(tsmpRtnMsg = "上層組織名稱:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1274"),(locale = "en-US"),(tsmpRtnMsg = "Organization ID:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1274"),(locale = "zh-TW"),(tsmpRtnMsg = "組織單位ID:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1275"),(locale = "en-US"),(tsmpRtnMsg = "Organization code:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1275"),(locale = "zh-TW"),(tsmpRtnMsg = "組織代碼:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1277"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人電話:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1279"),(locale = "en-US"),(tsmpRtnMsg = "Contact person name:length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1279"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人姓名:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1281"),(locale = "en-US"),(tsmpRtnMsg = "Contact person mail: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1281"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人信箱:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1300"),(locale = "en-US"),(tsmpRtnMsg = "Role code: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1300"),(locale = "zh-TW"),(tsmpRtnMsg = "角色代號:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1301"),(locale = "en-US"),(tsmpRtnMsg = "Role code: only English letters a~z, A~Z and numbers can be entered without blanks"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1301"),(locale = "zh-TW"),(tsmpRtnMsg = "角色代號:只能輸入英文字母a~z,A~Z及數字且不含空白"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1302"),(locale = "en-US"),(tsmpRtnMsg = "Role Name: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1302"),(locale = "zh-TW"),(tsmpRtnMsg = "角色名稱:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1313"),(locale = "en-US"),(tsmpRtnMsg = "User account: Only English letters a~z, A~Z 、@ and numbers can be input without blank"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1313"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者帳號：只能輸入英文字母a~z,A~Z、@及數字且不含空白"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1315"),(locale = "en-US"),(tsmpRtnMsg = "Function code: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1315"),(locale = "zh-TW"),(tsmpRtnMsg = "功能代碼:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1317"),(locale = "en-US"),(tsmpRtnMsg = "Language: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1317"),(locale = "zh-TW"),(tsmpRtnMsg = "語系:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1318"),(locale = "en-US"),(tsmpRtnMsg = "Function description: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1318"),(locale = "zh-TW"),(tsmpRtnMsg = "功能描述:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1319"),(locale = "en-US"),(tsmpRtnMsg = "Function name: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1319"),(locale = "zh-TW"),(tsmpRtnMsg = "功能名稱:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1320"),(locale = "en-US"),(tsmpRtnMsg = "Function name English: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1320"),(locale = "zh-TW"),(tsmpRtnMsg = "功能名稱英文:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1322"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端帳號:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1322"),(locale = "en-US"),(tsmpRtnMsg = "Client account: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1323"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端代號:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1323"),(locale = "en-US"),(tsmpRtnMsg = "Client code: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1325"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端代號:只能輸入英文字母a~z,A~Z及數字且不含空白"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1325"),(locale = "en-US"),(tsmpRtnMsg = "Client code: only English letters a~z, A~Z and numbers can be entered without blanks"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1326"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端名稱:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1326"),(locale = "en-US"),(tsmpRtnMsg = "Client name: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1328"),(locale = "zh-TW"),(tsmpRtnMsg = "簽呈編號:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1328"),(locale = "en-US"),(tsmpRtnMsg = "Sign number: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1329"),(locale = "zh-TW"),(tsmpRtnMsg = "密碼:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1329"),(locale = "en-US"),(tsmpRtnMsg = "Password: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1331"),(locale = "zh-TW"),(tsmpRtnMsg = "電子郵件帳號:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1331"),(locale = "en-US"),(tsmpRtnMsg = "Email account: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1333"),(locale = "zh-TW"),(tsmpRtnMsg = "擁有者:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1333"),(locale = "en-US"),(tsmpRtnMsg = "Owner: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1347"),(locale = "zh-TW"),(tsmpRtnMsg = "主機名稱:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1347"),(locale = "en-US"),(tsmpRtnMsg = "Host name: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1348"),(locale = "zh-TW"),(tsmpRtnMsg = "主機IP:長度限制 [{{0}}] 字內,您輸入[{{1}}] 個字"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1348"),(locale = "en-US"),(tsmpRtnMsg = "Host IP: length limit [{{0}}] characters, you enter [{{1}}] characters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1351"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 長度限制 [{{1}}] 字內,您輸入[{{2}}] 個字"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1355"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 不得小於 {{1}}, 您輸入 {{2}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1356"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 不得大於 {{1}}, 您輸入 {{2}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1358"),(locale = "zh-TW"),(tsmpRtnMsg = "[群組名稱]超過群組選取上限205,請重新選取"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1359"),(locale = "zh-TW"),(tsmpRtnMsg = "建立新Client Group時,必須是SYSTEM Group"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1359"),(locale = "en-US"),(tsmpRtnMsg = "When Create New Client Group, must be SYSTEM group"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1365"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端的SECURITY LEVEL ID與群組的SECURITY LEVEL ID不符合,群組ID為[{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1384"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 長度至少須 [{{1}}] 字,您輸入[{{2}}] 個字"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1487"),(locale = "zh-TW"),(tsmpRtnMsg = "主機狀態只能為A、S"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1488"),(locale = "en-US"),(tsmpRtnMsg = "Register Host is required"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1488"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機為必填"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1482"),(locale = "en-US"),(tsmpRtnMsg = "TSecurityLevel contains undeleted Group: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1482"),(locale = "zh-TW"),(tsmpRtnMsg = "此安全等級有未刪除的Group: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1483"),(locale = "en-US"),(tsmpRtnMsg = "TSecurityLevel contains undeleted Client: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1483"),(locale = "zh-TW"),(tsmpRtnMsg = "此安全等級有未刪除的Client: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1484"),(locale = "en-US"),(tsmpRtnMsg = "Register Host not found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1484"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1485"),(locale = "en-US"),(tsmpRtnMsg = "Register Host and Client not match"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1485"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機與客戶端不符"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1486"),(locale = "en-US"),(tsmpRtnMsg = "Register Host monitor not enabled"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1486"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機監控尚未啟用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1479"),(locale = "en-US"),(tsmpRtnMsg = "Module is already activated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1479"),(locale = "zh-TW"),(tsmpRtnMsg = "模組已經啟用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1480"),(locale = "en-US"),(tsmpRtnMsg = "Module is currently deactivated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1480"),(locale = "zh-TW"),(tsmpRtnMsg = "模組已經停用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1477"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶帳號：僅可輸入英數字、底線「_」及橫線「-」"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1478"),(locale = "en-US"),(tsmpRtnMsg = "Application description: No more than [{{0}}] characters [{{1}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1476"),(locale = "en-US"),(tsmpRtnMsg = "Failed to unbind. This is the last system module bound to DC."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1466"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可更新註冊或組合API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1466"),(locale = "en-US"),(tsmpRtnMsg = "This API is only available for Registered or Composed API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1467"),(locale = "zh-TW"),(tsmpRtnMsg = "僅可更新Java模組或.NET模組的API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1467"),(locale = "en-US"),(tsmpRtnMsg = "Only available for updating APIs in Java or .NET module"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1468"),(locale = "zh-TW"),(tsmpRtnMsg = "Log欄位與回應值為必填欄位"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1468"),(locale = "en-US"),(tsmpRtnMsg = "Log field and response value are required fields"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1469"),(locale = "zh-TW"),(tsmpRtnMsg = "模組名稱與API ID為必填欄位"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1469"),(locale = "en-US"),(tsmpRtnMsg = "Module name and API ID are required fields"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1470"),(locale = "en-US"),(tsmpRtnMsg = "Cannot reset signBlock within 5 mins"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1470"),(locale = "zh-TW"),(tsmpRtnMsg = "5分鐘內不可重置signBlock"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1471"),(locale = "en-US"),(tsmpRtnMsg = "Password fail over upper limit"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1471"),(locale = "zh-TW"),(tsmpRtnMsg = "密碼錯誤超過上限"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1472"),(locale = "en-US"),(tsmpRtnMsg = "Tuser locked"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1472"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者已鎖定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1473"),(locale = "en-US"),(tsmpRtnMsg = "Tuser disabled"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1473"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者已停權"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1475"),(locale = "en-US"),(tsmpRtnMsg = "Failed to bind module. DC is not active: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1464"),(locale = "en-US"),(tsmpRtnMsg = "Some APIs are still referenced by modules"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1463"),(locale = "en-US"),(tsmpRtnMsg = "Fail to delete. API is activated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1460"),(locale = "en-US"),(tsmpRtnMsg = "Fail to activate Composed API which is not deployment confirmed."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1455"),(locale = "zh-TW"),(tsmpRtnMsg = "向 digiRunner 取得 Token 失敗"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1455"),(locale = "en-US"),(tsmpRtnMsg = "Fail to get token from digiRunner"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1456"),(locale = "zh-TW"),(tsmpRtnMsg = "查無組合API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1456"),(locale = "en-US"),(tsmpRtnMsg = "No API found or is not a composed API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1457"),(locale = "zh-TW"),(tsmpRtnMsg = "模組已經綁定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1457"),(locale = "en-US"),(tsmpRtnMsg = "Module is bound already"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1458"),(locale = "zh-TW"),(tsmpRtnMsg = "模組已經解除綁定"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1458"),(locale = "en-US"),(tsmpRtnMsg = "Module is currently unbound"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1459"),(locale = "zh-TW"),(tsmpRtnMsg = "不支援操作此架構的模組"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1459"),(locale = "en-US"),(tsmpRtnMsg = "This structure of module is end-of-support"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1447"),(locale = "zh-TW"),(tsmpRtnMsg = "例外日期不可填寫"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1447"),(locale = "en-US"),(tsmpRtnMsg = "Exception date cannot be filled"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1448"),(locale = "zh-TW"),(tsmpRtnMsg = "開始時間與結束時間格式不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1448"),(locale = "en-US"),(tsmpRtnMsg = "Incorrect start time and end time format"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1449"),(locale = "zh-TW"),(tsmpRtnMsg = "開始時間與結束時間為必填"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1449"),(locale = "en-US"),(tsmpRtnMsg = "Start time and end time are required"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1450"),(locale = "zh-TW"),(tsmpRtnMsg = "例外日期為必填"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1450"),(locale = "en-US"),(tsmpRtnMsg = "Exception date is required"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1451"),(locale = "zh-TW"),(tsmpRtnMsg = "例外日期格式不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1451"),(locale = "en-US"),(tsmpRtnMsg = "Exception date format is incorrect"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1439"),(locale = "en-US"),(tsmpRtnMsg = "Delete failed. Module is bound to DC."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1440"),(locale = "zh-TW"),(tsmpRtnMsg = "請填寫密碼或是重置密碼"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1440"),(locale = "en-US"),(tsmpRtnMsg = "Please fill in the password or reset the password"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1441"),(locale = "zh-TW"),(tsmpRtnMsg = "請填寫密碼"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1441"),(locale = "en-US"),(tsmpRtnMsg = "Please fill in the password"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1442"),(locale = "zh-TW"),(tsmpRtnMsg = "其他組織單位已存在相同名稱的模組：[{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1442"),(locale = "en-US"),(tsmpRtnMsg = "Upload moduleName duplicate with Org: [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1443"),(locale = "zh-TW"),(tsmpRtnMsg = "不支援的檔案類型"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1443"),(locale = "en-US"),(tsmpRtnMsg = "Unsupport file type"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1444"),(locale = "zh-TW"),(tsmpRtnMsg = "不可匯出 Java 或 .NET 模組的 API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1444"),(locale = "en-US"),(tsmpRtnMsg = "Can not export Java or .NET API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1445"),(locale = "zh-TW"),(tsmpRtnMsg = "檔案中未包含任何 API 資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1445"),(locale = "en-US"),(tsmpRtnMsg = "API detail not found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1446"),(locale = "zh-TW"),(tsmpRtnMsg = "例外日期、開始時間與結束時間不可填寫"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1438"),(locale = "en-US"),(tsmpRtnMsg = "Delete failed. Module is active."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1436"),(locale = "en-US"),(tsmpRtnMsg = "Module not found. No activated or latest update module."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1436"),(locale = "zh-TW"),(tsmpRtnMsg = "找不到已啟動或最近上傳的模組"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1437"),(locale = "zh-TW"),(tsmpRtnMsg = "一個部署容器中不可綁定兩個相同的模組：[{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1437"),(locale = "en-US"),(tsmpRtnMsg = "Starting more than 1 module which have same name [{{0}}] in one DC is forbidden"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1431"),(locale = "en-US"),(tsmpRtnMsg = "Authorization code is invalid"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1431"),(locale = "zh-TW"),(tsmpRtnMsg = "授權碼不可用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1432"),(locale = "en-US"),(tsmpRtnMsg = "Inconsistent authorization type"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1432"),(locale = "zh-TW"),(tsmpRtnMsg = "授權類型不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1435"),(locale = "en-US"),(tsmpRtnMsg = "時間範圍只能為T、W、M、D"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1349"),(locale = "zh-TW"),(tsmpRtnMsg = "主機IP:格式錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1349"),(locale = "en-US"),(tsmpRtnMsg = "Host IP:wrong format"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1350"),(locale = "en-US"),(tsmpRtnMsg = "Required field: [{{0}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1350"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 為必填欄位"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1351"),(locale = "en-US"),(tsmpRtnMsg = "[{{0}}] is no more than [{{1}}] characters [{{2}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1332"),(locale = "zh-TW"),(tsmpRtnMsg = "電子郵件帳號:只能為Email格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1332"),(locale = "en-US"),(tsmpRtnMsg = "Email account:Email format only"),(tsmpRtnDesc = ""));       
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1314"),(locale = "en-US"),(tsmpRtnMsg = "Function code: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1314"),(locale = "zh-TW"),(tsmpRtnMsg = "功能代碼:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1316"),(locale = "en-US"),(tsmpRtnMsg = "Language: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1316"),(locale = "zh-TW"),(tsmpRtnMsg = "語系:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1321"),(locale = "en-US"),(tsmpRtnMsg = "Function name English: only enter alphanumeric characters、_、-"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1321"),(locale = "zh-TW"),(tsmpRtnMsg = "功能名稱英文:只能輸入英數、_、- "),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1324"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端代號:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1324"),(locale = "en-US"),(tsmpRtnMsg = "Client code: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1327"),(locale = "zh-TW"),(tsmpRtnMsg = "用戶端名稱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1327"),(locale = "en-US"),(tsmpRtnMsg = "Client name: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1352"),(locale = "en-US"),(tsmpRtnMsg = "String pattern is not matched: [{{0}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1352"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 格式不正確"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1353"),(locale = "en-US"),(tsmpRtnMsg = "[{{0}}] already exists: {{1}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1353"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 已存在: {{1}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1354"),(locale = "en-US"),(tsmpRtnMsg = "[{{0}}] does not exist: {{1}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1354"),(locale = "zh-TW"),(tsmpRtnMsg = "[{{0}}] 不存在: {{1}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1355"),(locale = "en-US"),(tsmpRtnMsg = "[{{0}}] shall not be less than {{1}} {{2}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1360"),(locale = "zh-TW"),(tsmpRtnMsg = "群組不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1360"),(locale = "en-US"),(tsmpRtnMsg = "Group was not found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1362"),(locale = "en-US"),(tsmpRtnMsg = "Function code: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1362"),(locale = "zh-TW"),(tsmpRtnMsg = "功能代碼:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1363"),(locale = "en-US"),(tsmpRtnMsg = "Security Level: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1363"),(locale = "zh-TW"),(tsmpRtnMsg = "安全等級:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1364"),(locale = "en-US"),(tsmpRtnMsg = "Security Level not found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1364"),(locale = "zh-TW"),(tsmpRtnMsg = "安全等級不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1358"),(locale = "en-US"),(tsmpRtnMsg = "Upper limit of 205 Groups per Group List"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1356"),(locale = "en-US"),(tsmpRtnMsg = "[{{0}}] shall not be greater than {{1}} {{2}}"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1365"),(locale = "en-US"),(tsmpRtnMsg = "The SECURITY LEVEL ID of the client does not match the SECURITY LEVEL ID of the group. The group ID is [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1366"),(locale = "zh-TW"),(tsmpRtnMsg = "開始日期與到期日期必須填寫"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1366"),(locale = "en-US"),(tsmpRtnMsg = "Start date and due date must be filled in"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1367"),(locale = "zh-TW"),(tsmpRtnMsg = "到期時間不可小於開始時間"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1367"),(locale = "en-US"),(tsmpRtnMsg = "The expiration time cannot be less than the start time"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1368"),(locale = "zh-TW"),(tsmpRtnMsg = "到期日期不可小於開始日期"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1368"),(locale = "en-US"),(tsmpRtnMsg = "The expiration date cannot be less than the start date"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1369"),(locale = "zh-TW"),(tsmpRtnMsg = "開始時間與到期時間必須填寫"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1369"),(locale = "en-US"),(tsmpRtnMsg = "Start time and expiration time must be filled in"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1372"),(locale = "zh-TW"),(tsmpRtnMsg = "未知類型的Grant Type: [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1372"),(locale = "en-US"),(tsmpRtnMsg = "Grant Type of unknown type: [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1379"),(locale = "en-US"),(tsmpRtnMsg = "This Open API Key has been renewed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1379"),(locale = "zh-TW"),(tsmpRtnMsg = "此 Open API Key 已執行過展期"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1381"),(locale = "en-US"),(tsmpRtnMsg = "Starting time: Incorrect format"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1381"),(locale = "zh-TW"),(tsmpRtnMsg = "開始時間:格式不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1383"),(locale = "en-US"),(tsmpRtnMsg = "End Time: Incorrect format"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1383"),(locale = "zh-TW"),(tsmpRtnMsg = "結束時間:格式不正確"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1384"),(locale = "en-US"),(tsmpRtnMsg = "[{{0}}] is no less than [{{1}}] characters [{{2}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1385"),(locale = "zh-TW"),(tsmpRtnMsg = "模組名稱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1385"),(locale = "en-US"),(tsmpRtnMsg = "Module name: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1397"),(locale = "en-US"),(tsmpRtnMsg = "Authorized verification type: [{{0}}] does not exist"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1397"),(locale = "zh-TW"),(tsmpRtnMsg = "授權核身種類:[{{0}}]不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1403"),(locale = "en-US"),(tsmpRtnMsg = "Failed to remove. This virtual group is being used."),"");
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1414"),(locale = "en-US"),(tsmpRtnMsg = "Failed to remove. This deploy container is activated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1415"),(locale = "zh-TW"),(tsmpRtnMsg = "NodeTaskNotifiers發生錯誤"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1415"),(locale = "en-US"),(tsmpRtnMsg = "Error occurred in NodeTaskNotifiers"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1416"),(locale = "zh-TW"),(tsmpRtnMsg = "查詢日期區間不得超過 {{0}} 天"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1416"),(locale = "en-US"),(tsmpRtnMsg = "Query date range is no more than {{0}} days"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1417"),(locale = "zh-TW"),(tsmpRtnMsg = "該群組有用戶端"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1417"),(locale = "en-US"),(tsmpRtnMsg = "Group has been used by Client"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1418"),(locale = "zh-TW"),(tsmpRtnMsg = "請先匯入外部系統介接規格"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1418"),(locale = "en-US"),(tsmpRtnMsg = "You need to upload an openAPI specification or import from an URL"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1419"),(locale = "en-US"),(tsmpRtnMsg = "Host name already exists"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1419"),(locale = "zh-TW"),(tsmpRtnMsg = "主機名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1420"),(locale = "en-US"),(tsmpRtnMsg = "Host does not exist"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1420"),(locale = "zh-TW"),(tsmpRtnMsg = "主機不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1421"),(locale = "en-US"),(tsmpRtnMsg = "Register Host must be disabled"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1421"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機心跳必須停用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1422"),(locale = "en-US"),(tsmpRtnMsg = "Register Host been refered by Registered API"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1422"),(locale = "zh-TW"),(tsmpRtnMsg = "註冊主機被註冊API參考"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1423"),(locale = "zh-TW"),(tsmpRtnMsg = "主機名稱{{0}}有重複相同資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1423"),(locale = "en-US"),(tsmpRtnMsg = "The host name {{0}} has duplicate data"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1425"),(locale = "zh-TW"),(tsmpRtnMsg = "主機IP{{0}}有重複相同資料"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1425"),(locale = "en-US"),(tsmpRtnMsg = "The host IP{{0}} has duplicate data"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1428"),(locale = "zh-TW"),(tsmpRtnMsg = "告警名稱已存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1428"),(locale = "en-US"),(tsmpRtnMsg = "Alert name duplicated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1429"),(locale = "zh-TW"),(tsmpRtnMsg = "告警設定不存在"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1429"),(locale = "en-US"),(tsmpRtnMsg = "Alert Setting not found"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1430"),(locale = "en-US"),(tsmpRtnMsg = "Unable to get authorization code"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1426"),(locale = "en-US"),(tsmpRtnMsg = "Module name has been created by importing OpenAPI doc."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1424"),(locale = "zh-TW"),(tsmpRtnMsg = "未指定主機位址host"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1424"),(locale = "en-US"),(tsmpRtnMsg = "Host is not specified"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1427"),(locale = "zh-TW"),(tsmpRtnMsg = "例外類型只能為N、W、M、D"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1408"),(locale = "zh-TW"),(tsmpRtnMsg = "新密碼與再次確認密碼不一致"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1408"),(locale = "en-US"),(tsmpRtnMsg = "The new password does not match the reconfirmed password"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1411"),(locale = "zh-TW"),(tsmpRtnMsg = "部署容器已經啟用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1411"),(locale = "en-US"),(tsmpRtnMsg = "Deploy Container is already activated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1412"),(locale = "zh-TW"),(tsmpRtnMsg = "部署容器已經停用"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1412"),(locale = "en-US"),(tsmpRtnMsg = "Deploy Container is already inactivated"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1407"),(locale = "en-US"),(tsmpRtnMsg = "Amount of [{{0}}] is no larger than [{{1}}]: [{{2}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1404"),(locale = "en-US"),(tsmpRtnMsg = "Failed to remove. Groups are linked to this authority: {{0}}"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1405"),(locale = "zh-TW"),(tsmpRtnMsg = "請輸入網址格式"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1405"),(locale = "en-US"),(tsmpRtnMsg = "Please enter URL format"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1406"),(locale = "en-US"),(tsmpRtnMsg = "Amount of [{{0}}] is no less than [{{1}}]: [{{2}}]"),(tsmpRtnDesc = "DP共用訊息"));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1514"),(locale = "en-US"),(tsmpRtnMsg = "You are in the network segment and cannot log in"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1413"),(locale = "en-US"),(tsmpRtnMsg = "Deploy Container is not bound to any nodes"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1280"),(locale = "en-US"),(tsmpRtnMsg = "Contact person mail: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1280"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人信箱:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1278"),(locale = "en-US"),(tsmpRtnMsg = "Contact person name: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1278"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人姓名:必填參數"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1276"),(locale = "en-US"),(tsmpRtnMsg = "Contact phone: Required parameters"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1276"),(locale = "zh-TW"),(tsmpRtnMsg = "聯絡人電話:必填參數"),(tsmpRtnDesc = ""));
	//=============================================
	        
	        
	        
	        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9901"), (locale = "zh-TW"), (tsmpRtnMsg = "系統錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9901"), (locale = "en-US"), (tsmpRtnMsg = "System error"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9906"), (locale = "zh-TW"), (tsmpRtnMsg = "Client 請求數量超過限制"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9906"), (locale = "en-US"), (tsmpRtnMsg = "Client requests exceeds TPS limit"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9912"), (locale = "zh-TW"), (tsmpRtnMsg = "API 已被停用"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9912"), (locale = "en-US"), (tsmpRtnMsg = "API disable"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9926"), (locale = "zh-TW"), (tsmpRtnMsg = "不合法字元"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9926"), (locale = "en-US"), (tsmpRtnMsg = "Invalid Character"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9930"), (locale = "zh-TW"), (tsmpRtnMsg = "有不合法的字串"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9930"), (locale = "en-US"), (tsmpRtnMsg = "Invalid String"), (tsmpRtnDesc = ""));

//	      20220310, Sso/UdpSso, 增加rtn code, Mini Lee
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1517"), (locale = "zh-TW"), (tsmpRtnMsg = "不合法的SSO登入錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1517"), (locale = "en-US"), (tsmpRtnMsg = "Invalid SSO login error"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1518"), (locale = "zh-TW"), (tsmpRtnMsg = "此使用者不能以SSO方式登入"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1518"), (locale = "en-US"), (tsmpRtnMsg = "This user cannot log in with SSO"), (tsmpRtnDesc = ""));

//	      -- 20220810,  filter rtnCode, Zoe Lee
	        createTsmpRtnCode((tsmpRtnCodeColumn = "0125"), (locale = "zh-TW"), (tsmpRtnMsg = "API不存在"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "0125"), (locale = "en-US"), (tsmpRtnMsg = "API not found"), (tsmpRtnDesc = ""));
	        
//	      -- 20221031,  自訂註冊外部API的rtnCode, Tom Chu
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1524"), (locale = "zh-TW"), (tsmpRtnMsg = "proxy path({{0}})和目標URL({{1}})的{p}數量不匹配"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1524"), (locale = "en-US"), (tsmpRtnMsg = "The number of {p} in proxy path ({{0}}) and target URL ({{1}}) do not match"), (tsmpRtnDesc = ""));
	        
//	      -- 20221124,  Kibana錯誤訊息, min
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1525"), (locale = "zh-TW"), (tsmpRtnMsg = "Kibana 連線逾時"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1525"), (locale = "en-US"), (tsmpRtnMsg = "Kibana Connection timed out"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1526"), (locale = "zh-TW"), (tsmpRtnMsg = "Kibana 未經授權"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1526"), (locale = "en-US"), (tsmpRtnMsg = "Kibana Unauthorized"), (tsmpRtnDesc = ""));
	        
//	      -- 20221124, Gatewate LB錯誤訊息, Mini Lee
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1527"), (locale = "zh-TW"), (tsmpRtnMsg = "{{0}} 僅可輸入整數"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1527"), (locale = "en-US"), (tsmpRtnMsg = "{{0}} can only enter integers"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1528"), (locale = "zh-TW"), (tsmpRtnMsg = "機率總數必須是100，但卻是{{0}}"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1528"), (locale = "en-US"), (tsmpRtnMsg = "The total number of chances must be 100, but it is {{0}}"), (tsmpRtnDesc = ""));

//	      -- 20221128,  Kibana錯誤訊息, min
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1529"), (locale = "zh-TW"), (tsmpRtnMsg = "設定Kibana版本錯誤"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1529"), (locale = "en-US"), (tsmpRtnMsg = "Setting Kibana version error"), (tsmpRtnDesc = ""));
	        
//	      -- 20221205,  Composer Swagger API錯誤訊息, min
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1530"), (locale = "zh-TW"), (tsmpRtnMsg = "Composer Server連線失敗"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1530"), (locale = "en-US"), (tsmpRtnMsg = "Composer Server connection failed"), (tsmpRtnDesc = ""));  
//	      -- 20230103,  idp info 錯誤訊息, zoe
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2024"), (locale = "zh-TW"), (tsmpRtnMsg = "IDP資訊已存在"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2024"), (locale = "en-US"), (tsmpRtnMsg = "IDP information already exists"), (tsmpRtnDesc = "")); 
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2025"), (locale = "zh-TW"), (tsmpRtnMsg = "[{{0}}] : 必填"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2025"), (locale = "en-US"), (tsmpRtnMsg = "[{{0}}] : required parameter"), (tsmpRtnDesc = "")); 
		
//		  -- 20230413,  website 錯誤訊息, Kevin Cheng
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1531"), (locale = "zh-TW"), (tsmpRtnMsg = "至少需要一組目標URL"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1531"), (locale = "en-US"), (tsmpRtnMsg = "At least one set of target URLs is required"), (tsmpRtnDesc = ""));
	        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1532"), (locale = "zh-TW"), (tsmpRtnMsg = "網站反向代理資料不存在"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1532"), (locale = "en-US"), (tsmpRtnMsg = "Website reverse proxy data does not exist"), (tsmpRtnDesc = ""));
	        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1533"), (locale = "zh-TW"), (tsmpRtnMsg = "ID Token 不合法"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1533"), (locale = "en-US"), (tsmpRtnMsg = "ID Token is invalid"), (tsmpRtnDesc = ""));

//		   -- 20230509,  Dp 錯誤訊息, Kevin Cheng
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1393"), (locale = "zh-TW"), (tsmpRtnMsg = "Module名稱:必填參數"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1393"), (locale = "en-US"), (tsmpRtnMsg = "Module name: Required parameters"), (tsmpRtnDesc = ""));
	        
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1394"), (locale = "zh-TW"), (tsmpRtnMsg = "Module名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1394"), (locale = "en-US"), (tsmpRtnMsg = "Module name: length limit [{{0}}] characters, you enter [{{1}}] characters"), (tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1395"), (locale = "zh-TW"), (tsmpRtnMsg = "API Key:必填參數"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1395"), (locale = "en-US"), (tsmpRtnMsg = "API Key: Required parameters"), (tsmpRtnDesc = ""));
	        		
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1396"), (locale = "zh-TW"), (tsmpRtnMsg = "API Key:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1396"), (locale = "en-US"), (tsmpRtnMsg = "API Key: length limit [{{0}}] characters, you enter [{{1}}] characters"), (tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1534"), (locale = "zh-TW"), (tsmpRtnMsg = "缺少digiRunner URL"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1534"), (locale = "en-US"), (tsmpRtnMsg = "The digiRunner URL is missing"), (tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1537"), (locale = "zh-TW"), (tsmpRtnMsg = "缺少檔案編號"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1537"), (locale = "en-US"), (tsmpRtnMsg = "Missing file number"), (tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1536"), (locale = "zh-TW"), (tsmpRtnMsg = "缺少User ID"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1536"), (locale = "en-US"), (tsmpRtnMsg = "The User ID is missing"), (tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1535"), (locale = "zh-TW"), (tsmpRtnMsg = "缺少Application ID"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1535"), (locale = "en-US"), (tsmpRtnMsg = "The Application ID is missing"), (tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1539"),(locale = "en-US"),(tsmpRtnMsg = "The DigiLog User duplicates with the Delegate AC User."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1539"),(locale = "zh-TW"),(tsmpRtnMsg = "使用者帳號與Delegate AC User重複"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1540"),(locale = "en-US"),(tsmpRtnMsg = "The Delegate AC User duplicates with the DigiLog User."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1540"),(locale = "zh-TW"),(tsmpRtnMsg = "Delegate AC User與使用者帳號重複"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1541"),(locale = "en-US"),(tsmpRtnMsg = "The token has expired."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1541"),(locale = "zh-TW"),(tsmpRtnMsg = "Token已失效"),(tsmpRtnDesc = ""));

	        
//	        -- 20230612, 安全性檢查的錯誤訊息, Mini Lee	
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9978"), (locale = "zh-TW"), (tsmpRtnMsg = "不合法的 Host Header"), (tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "9978"), (locale = "en-US"), (tsmpRtnMsg = "Invalid Host Header"), (tsmpRtnDesc = ""));
	        
//		   -- 20230918,  Dp 錯誤訊息, Kevin Cheng
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1538"),(locale = "en-US"),(tsmpRtnMsg = "Missing Version ID"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1538"),(locale = "zh-TW"),(tsmpRtnMsg = "缺少Version ID"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1542"),(locale = "en-US"),(tsmpRtnMsg = "The expiration date should be equal to or greater than today."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1542"),(locale = "zh-TW"),(tsmpRtnMsg = "到期日要等於或是大於今天"),(tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1543"),(locale = "en-US"),(tsmpRtnMsg = "The extension can only be extended once."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1543"),(locale = "zh-TW"),(tsmpRtnMsg = "展期只能夠展期一次"),(tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1409"),(locale = "en-US"),(tsmpRtnMsg = "This Open API Key has been disabled and cannot be renewed"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1409"),(locale = "zh-TW"),(tsmpRtnMsg = "此 Open API Key 已停用，無法展期"),(tsmpRtnDesc = ""));

	        createTsmpRtnCode((tsmpRtnCodeColumn = "1544"),(locale = "en-US"),(tsmpRtnMsg = "DP user did not provide an email."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1544"),(locale = "zh-TW"),(tsmpRtnMsg = "DP user 沒有提供 E-mail"),(tsmpRtnDesc = ""));
	        
	        // -- 20240103,  Dp 錯誤訊息, jhmin
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1545"),(locale = "en-US"),(tsmpRtnMsg = "OPEN APIKEY ID missing."),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1545"),(locale = "zh-TW"),(tsmpRtnMsg = "缺少OPEN APIKEY ID"),(tsmpRtnDesc = ""));
	        
	        //20231214 ,For API 批量修改,zoe Lee
	        createTsmpRtnCode((tsmpRtnCodeColumn = "2026"),(locale = "en-US"),(tsmpRtnMsg = "Batch modify failed :   {{0}}"),(tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2026"),(locale = "zh-TW"),(tsmpRtnMsg = "批量更新失敗 :  {{0}}"),(tsmpRtnDesc = ""));
		    
		    // -- 20240131,  Dp 訊息, jhmin
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1546"),(locale = "en-US"),(tsmpRtnMsg = "The extension has been successful, please close this page. Please go to the DP page and email to confirm the new Open API Key: [{{0}}]"),(tsmpRtnDesc = ""));
	        createTsmpRtnCode((tsmpRtnCodeColumn = "1546"),(locale = "zh-TW"),(tsmpRtnMsg = "已展期成功，請關閉此頁面。請至DP頁面與信箱確認，新的Open API Key:[{{0}}]"),(tsmpRtnDesc = ""));

		    //20240124 ,嵌入式頁面維護,zoe Lee
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2027"),(locale = "en-US"),(tsmpRtnMsg = "This directory can no longer add new features."),(tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2027"),(locale = "zh-TW"),(tsmpRtnMsg = "該目錄無法再新增功能"),(tsmpRtnDesc = ""));

		    //20240217 ,用戶端匯出/入,Tom
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1547"),(locale = "en-US"),(tsmpRtnMsg = "[Client Id:{{0}}]The total number of APIs set with API Scope and API Group settings exceeds {{1}}"),(tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1547"),(locale = "zh-TW"),(tsmpRtnMsg = "[用戶端帳號:{{0}}]授權範圍設定的API加授權設定的總數量超過{{1}}"),(tsmpRtnDesc = ""));

		    // 20240423, API 上下架輸入日期資訊, Kevin Cheng 
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2030"),(locale = "en-US"),(tsmpRtnMsg = "The input date must be greater than today"),(tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2030"),(locale = "zh-TW"),(tsmpRtnMsg = "輸入日期必須大於今日"),(tsmpRtnDesc = ""));
		    //20240506, 異動系統預設資料請先解鎖, Webber Luo
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1548"),(locale = "en-US"),(tsmpRtnMsg = "Default data in the transaction system must be unlocked before modification(Setting with DEFAULT_DATA_CHANGE_ENABLED)"),(tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1548"),(locale = "zh-TW"),(tsmpRtnMsg = "異動系統預設資料請先解鎖(Setting的DEFAULT_DATA_CHANGE_ENABLED)"),(tsmpRtnDesc = ""));

		    // 20240521, API 啟用與停用日期不可為今日, Kevin Cheng 
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1549"), (locale = "zh-TW"), (tsmpRtnMsg = "設定啟用停用日期不可為今日"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1549"), (locale = "en-US"), (tsmpRtnMsg = "The enable and disable date cannot be set to today"), (tsmpRtnDesc = ""));
		    // 20240521, API 啟用日期與停用日期不可為同日, Kevin Cheng 
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1550"), (locale = "zh-TW"), (tsmpRtnMsg = "啟用日期與停用日期不可為同日"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1550"), (locale = "en-US"), (tsmpRtnMsg = "The enable date and disable date cannot be the same day"), (tsmpRtnDesc = ""));

		    // 20240524, API 啟用停用上架下架相關錯誤
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1551"), (locale = "zh-TW"), (tsmpRtnMsg = "不可包含已啟用的API"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1551"), (locale = "en-US"), (tsmpRtnMsg = "Cannot include enabled APIs"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1552"), (locale = "zh-TW"), (tsmpRtnMsg = "不可包含已停用的API"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1552"), (locale = "en-US"), (tsmpRtnMsg = "Cannot include disabled APIs"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1553"), (locale = "zh-TW"), (tsmpRtnMsg = "啟用日期需要大於停用日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1553"), (locale = "en-US"), (tsmpRtnMsg = "The enable date needs to be greater than the disable date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1554"), (locale = "zh-TW"), (tsmpRtnMsg = "停用日期需要大於啟用日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1554"), (locale = "en-US"), (tsmpRtnMsg = "The disable date needs to be greater than the enable date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1555"), (locale = "zh-TW"), (tsmpRtnMsg = "必須先移除預定啟用日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1555"), (locale = "en-US"), (tsmpRtnMsg = "Scheduled enable date must be removed first"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1556"), (locale = "zh-TW"), (tsmpRtnMsg = "必須先移除預定停用日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1556"), (locale = "en-US"), (tsmpRtnMsg = "Scheduled disable date must be removed first"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2031"), (locale = "zh-TW"), (tsmpRtnMsg = "不可包含已上架的API"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2031"), (locale = "en-US"), (tsmpRtnMsg = "Cannot include APIs that are already on the shelves"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2032"), (locale = "zh-TW"), (tsmpRtnMsg = "不可包含已下架的API"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2032"), (locale = "en-US"), (tsmpRtnMsg = "Cannot include APIs that have been removed from the shelves"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2033"), (locale = "zh-TW"), (tsmpRtnMsg = "上架日期需要大於下架日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2033"), (locale = "en-US"), (tsmpRtnMsg = "The launch date needs to be greater than the removal date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2034"), (locale = "zh-TW"), (tsmpRtnMsg = "下架日期需要大於上架日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2034"), (locale = "en-US"), (tsmpRtnMsg = "The removal date needs to be greater than the launch date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2035"), (locale = "zh-TW"), (tsmpRtnMsg = "必須先移除預定上架日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2035"), (locale = "en-US"), (tsmpRtnMsg = "Scheduled launch date must be removed first"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2036"), (locale = "zh-TW"), (tsmpRtnMsg = "必須先移除預定下架日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2036"), (locale = "en-US"), (tsmpRtnMsg = "Scheduled removal date must be removed first"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2037"), (locale = "zh-TW"), (tsmpRtnMsg = "上架日期需要小於下架日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2037"), (locale = "en-US"), (tsmpRtnMsg = "The launch date needs to be less than the removal date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "2038"), (locale = "zh-TW"), (tsmpRtnMsg = "下架日期需要小於上架日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "2038"), (locale = "en-US"), (tsmpRtnMsg = "The removal date needs to be less than the launch date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1557"), (locale = "zh-TW"), (tsmpRtnMsg = "啟用日期需要小於停用日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1557"), (locale = "en-US"), (tsmpRtnMsg = "The enable date needs to be less than the disable date"), (tsmpRtnDesc = ""));

		    createTsmpRtnCode((tsmpRtnCodeColumn = "1558"), (locale = "zh-TW"), (tsmpRtnMsg = "停用日期需要小於啟用日期"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1558"), (locale = "en-US"), (tsmpRtnMsg = "The disable date needs to be less than the enable date"), (tsmpRtnDesc = ""));

			// 20240905, 自訂錯誤訊息, Mini Lee
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1559"), (locale = "zh-TW"), (tsmpRtnMsg = "{{0}}"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1559"), (locale = "en-US"), (tsmpRtnMsg = "{{0}}"), (tsmpRtnDesc = ""));

		    
		    // 20250206, 對稱式加密/解密失敗訊息, Mini Lee
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1561"), (locale = "zh-TW"), (tsmpRtnMsg = "對稱式加密失敗：{{0}}"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1561"), (locale = "en-US"), (tsmpRtnMsg = "Symmetric encryption error: {{0}}"), (tsmpRtnDesc = ""));
			
			createTsmpRtnCode((tsmpRtnCodeColumn = "1562"), (locale = "zh-TW"), (tsmpRtnMsg = "對稱式解密失敗：{{0}}"), (tsmpRtnDesc = ""));
		    createTsmpRtnCode((tsmpRtnCodeColumn = "1562"), (locale = "en-US"), (tsmpRtnMsg = "Symmetric decryption error: {{0}}"), (tsmpRtnDesc = ""));
			// 20250116, 刪除失敗，狀態為啟用中, Zoe Lee
			createTsmpRtnCode((tsmpRtnCodeColumn = "2039"), (locale = "zh-TW"), (tsmpRtnMsg = "啟用中，無法刪除"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "2039"), (locale = "en-US"), (tsmpRtnMsg = "Enabled, cannot be deleted."), (tsmpRtnDesc = ""));

		
			// 20250701, 查無簽核關卡設定訊息, Kevin Cheng
			createTsmpRtnCode((tsmpRtnCodeColumn = "1565"), (locale = "zh-TW"), (tsmpRtnMsg = "查無簽核關卡設定，應設定簽核關卡"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1565"), (locale = "en-US"), (tsmpRtnMsg = "No approval setting found, should set approval setting"), (tsmpRtnDesc = ""));
		
			// 20250722, 結束日期不可小於開始日期, Kevin Cheng
			createTsmpRtnCode((tsmpRtnCodeColumn = "1566"), (locale = "zh-TW"), (tsmpRtnMsg = "結束日期不可小於開始日期"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1566"), (locale = "en-US"), (tsmpRtnMsg = "End date cannot be less than start date"), (tsmpRtnDesc = ""));

			// 20250723, 憑證過期, Zoe Lee
			createTsmpRtnCode((tsmpRtnCodeColumn = "1560"), (locale = "zh-TW"), (tsmpRtnMsg = "憑證過期"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1560"), (locale = "en-US"), (tsmpRtnMsg = "The Certificate has expired."), (tsmpRtnDesc = ""));

			//20250902, 忘記密碼, tom
			createTsmpRtnCode((tsmpRtnCodeColumn = "1567"), (locale = "zh-TW"), (tsmpRtnMsg = "{{0}}驗證失敗"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1567"), (locale = "en-US"), (tsmpRtnMsg = "{{0}} verification failed"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1568"), (locale = "zh-TW"), (tsmpRtnMsg = "超過驗證有效時間{{0}}分鐘"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1568"), (locale = "en-US"), (tsmpRtnMsg = "Verification validity period has exceeded {{0}} minutes"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1569"), (locale = "zh-TW"), (tsmpRtnMsg = "已達錯誤{{0}}次上限，請重新發送"), (tsmpRtnDesc = ""));
			createTsmpRtnCode((tsmpRtnCodeColumn = "1569"), (locale = "en-US"), (tsmpRtnMsg = "The error limit of {{0}} has been reached. Please try again"), (tsmpRtnDesc = ""));

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		
		return tsmpRtnCodelist;
	}
	
	
    protected void createTsmpRtnCode(String tsmpRtnCodeColumn, String locale, String tsmpRtnMsg, String tsmpRtnDesc) {
            TsmpRtnCodeVo tsmpRtnCode = new TsmpRtnCodeVo();
            tsmpRtnCode.setTsmpRtnCode(tsmpRtnCodeColumn);
            tsmpRtnCode.setLocale(locale);
            tsmpRtnCode.setTsmpRtnMsg(tsmpRtnMsg);
            tsmpRtnCode.setTsmpRtnDesc(tsmpRtnDesc);
            tsmpRtnCodelist.add(tsmpRtnCode);     
    }

}
