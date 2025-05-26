package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import org.springframework.stereotype.Service;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.LicenseEditionTypeVo;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpFuncVo;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TsmpFuncTableInitializer {
	
	
	public static List<TsmpFuncVo> tsmpFuncList = new LinkedList<>();
	
	public static final String[] ALPHA = new String[] {

			//v4-權限管理
			"AC00","AC0002","AC0006","AC0012","AC0015","AC1002","AC1202","AC0016","AC0017","AC0018","AC0019","AC0020","AC0021",
			//系統功能管理
			"AC01","AC0101","AC0103","AC0104","AC0105",
			//用戶端管理
			"AC02","AC0202","AC0212","AC0222","AC1107","AC1116","AC0226","AC0227","AC0228","AC0229","AC0230","AC0231",
			//API管理
			"AC03","AC0301","AC0311","AC0315","AC0316","AC0318","AC0319",
			//監控管理(AC0706為無建立功能的告警頁面)
			"AC05","AC0501","AC0502","AC0503","AC0508","AC0509","AC0510","AC0702","AC0706",
			//報表管理(Kibana)
			"AC09","AC0901","AC0902","AC0903","AC0904","AC0905","AC0906","AC0907","AC0908","AC0909","AC0910",
			//報表管理(JS)
			//20250407 no execute
			//"AC13","AC1301","AC1302","AC1303","AC1304","AC1305",
			//入口網後台管理
			"NP01","NP0105","NP0113","NP0114","NP0115","NP0116",
			//用戶憑證管理
			"NP02","NP0201","NP0202","NP0203","NP0204","NP0205",
			//各類申請單
			"NP03","NP0301","NP0302","NP0303","NP0304","NP0401","NP0402",
			//系統現況查詢
			"NP05","NP0504","NP0512","NP0513","NP0514","NP0516","NP1201","NP1202",
			//系統設定
			"LB00","LB0001","LB0002","LB0003","LB0004","LB0005","LB0006","LB0007","LB0008","LB0009","LB0010"		
	};	
	public static final String[] ENTERPRISE = new String[] {
			//v4-權限管理
			"AC00","AC0002","AC0006","AC0012","AC0015","AC1002","AC1202","AC0016","AC0017","AC0018","AC0019","AC0020","AC0021",
			//系統功能管理
			"AC01","AC0101","AC0103","AC0104","AC0105",
			//用戶端管理
			"AC02","AC0202","AC0212","AC0222","AC1107","AC1116","AC0226","AC0227","AC0228","AC0229","AC0230","AC0231",
			//API管理
			"AC03","AC0301","AC0311","AC0315","AC0316","AC0318","AC0319",
			//監控管理
			"AC05","AC0501","AC0502","AC0503","AC0508","AC0509","AC0510","AC0702",
			//報表管理(Kibana)
			"AC09","AC0901","AC0902","AC0903","AC0904","AC0905","AC0906","AC0907","AC0908","AC0909","AC0910",
			//用戶憑證管理
			"NP02","NP0201","NP0202","NP0203","NP0204","NP0205",
			//各類申請單
			"NP03","NP0304","NP0401","NP0402",
			//系統現況查詢
			"NP05","NP0504","NP0513","NP0514","NP0516","NP1201","NP1202",
			//系統設定
			"LB00","LB0001","LB0002","LB0003","LB0004","LB0005","LB0006","LB0007","LB0008","LB0009","LB0010"
	};
	public static final String[] ENTERPRISE_LITE = new String[] {
			//v4-權限管理
			"AC00","AC0002","AC0006","AC0012","AC0015","AC1002","AC1202","AC0016","AC0017","AC0018","AC0019","AC0020","AC0021",
			//系統功能管理
			"AC01","AC0101","AC0103","AC0105",
			//用戶端管理
			"AC02","AC0202","AC0212","AC0222","AC1107","AC1116","AC0226","AC0227","AC0228","AC0229","AC0230","AC0231",
			//API管理
			"AC03","AC0301","AC0311","AC0315","AC0316","AC0318","AC0319",
			//監控管理(AC0706為無建立功能的告警頁面)
			"AC05","AC0501","AC0503","AC0509","AC0706",
			//報表管理(JS)
			//20250407 no execute
			//"AC13","AC1301","AC1302","AC1303","AC1304","AC1305",
			//用戶憑證管理
			"NP02","NP0201","NP0202","NP0203","NP0204",
			//各類申請單
			"NP03","NP0304","NP0401","NP0402",
			//系統現況查詢
			"NP05","NP0504","NP0513","NP0514","NP0516","NP1201","NP1202",
			//系統設定
			"LB00","LB0001","LB0002","LB0003","LB0004","LB0005","LB0006","LB0007","LB0008","LB0009","LB0010"
	};
	public static final String[] EXPRESS = new String[] {
			//v4-權限管理
			"AC00","AC0002","AC0006","AC0012","AC0015","AC1002","AC1202","AC0016","AC0017","AC0018","AC0019","AC0020","AC0021",
			//系統功能管理
			"AC01","AC0101","AC0103","AC0105",
			//用戶端管理
			"AC02","AC0202","AC0212","AC0222","AC1107","AC1116","AC0226","AC0227","AC0228","AC0229","AC0230","AC0231",
			//API管理
			"AC03","AC0301","AC0311","AC0316","AC0318","AC0319",
			//監控管理
			"AC05","AC0501","AC0503","AC0509","AC0706",
			//報表管理(JS)
			//20250407 no execute
			//"AC13","AC1301","AC1302","AC1303","AC1304","AC1305",
			//用戶憑證管理
			"NP02","NP0201","NP0202","NP0203","NP0204",
			//各類申請單
			"NP03","NP0304","NP0401","NP0402",
			//系統現況查詢
			"NP05","NP0504","NP0513","NP0514","NP0516","NP1201","NP1202",
			//系統設定
			"LB00","LB0001","LB0002","LB0003","LB0004","LB0005","LB0006","LB0007","LB0008","LB0009","LB0010"
	};
	
	public List<TsmpFuncVo> insertTsmpFunc(LicenseEditionTypeVo licenseEdition, boolean isOsType) {
		try {
			   
	        createTsmpFunc("AC00","AC User Management","","使用者管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC00","AC 權限管理","AC01","使用者管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0002","Users","User Maintenance","使用者功能查詢, 資料更新",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0002","使用者維護","Query User","使用者功能查詢, 資料更新",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0006","My Profile","","個人資料更新",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0006","個人資料維護","Update User Profile","個人資料維護",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0012","Roles","Role Management","角色查詢, 資料更新",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0012","角色維護","Query Role","角色查詢",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0015","Role Mapping","","queryTRoleRoleMapping","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0015","角色清單設定","","queryTRoleRoleMapping","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0016","Delegate AC User","Delegate AC User","delegateACUser","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0016","Delegate AC User","Delegate AC User","delegateACUser","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0017","AC OAuth 2.0 IdP","AC OAuth 2.0 IdP","acOAuth2.0IdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0017","AC OAuth 2.0 IdP","AC OAuth 2.0 IdP","acOAuth2.0IdP","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0018","AC LDAP IdP","AC LDAP IdP","acLdapIdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0018","AC LDAP IdP","AC LDAP IdP","acLdapIdP","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0019","AC MLDAP IdP","AC MLDAP IdP","acMLdapIdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0019","AC MLDAP IdP","AC MLDAP IdP","acMLdapIdP","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0021","AC CUS IdP","AC CUS IdP","acCusIdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0021","AC CUS IdP","AC CUS IdP","acCusIdP","","zh-TW","manager",DateTimeUtil.now());

	    	createTsmpFunc("AC01","Development Mode","","系統功能管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC01","系統功能管理","Function Management","系統功能管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0101","Function Management","","功能維護",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0101","功能維護","Query Function","功能維護",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0103","Rtn Code Management","Rtn Code Management","回傳碼維護","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0103","回傳碼維護","Rtn Code Management","回傳碼維護","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0104","Index Management","Index Management","Index管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0104","Index管理","Index Management","Index管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC02","Client Management","","用戶端管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC02","用戶端管理","Client Management","用戶端管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0202","API Client","Client Management","用戶端查詢, 資料更新",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0202","用戶端維護","Query Client","用戶端查詢",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0212","API Group","Group Management","群組查詢, 資料更新",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0212","群組維護","Query Group","群組查詢",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0222","API Scope","VGroup Management","虛擬群組查詢, 資料更新","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0222","授權範圍維護","queryVGroupList","查詢VGroupList","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC03","API Management","","API管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC03","API管理","API Management","API管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0301","API List","","API列表",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0301","API列表","API LIST","API列表",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0311","API Registry","","API Register",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0311","API註冊","","API Register",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0315","API Composer","","API Composer",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0315","API組合與設計","","API Composer",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0316","API Test","","API測試區",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0316","API測試區","API TEST","API測試區",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0318","API List-Import API","","",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0318","API列表-匯入註冊/組合API","","",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC05","Monitor & Alert","","監控管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC05","監控管理","Monitor","監控管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0501","digiRunner Server","","digiRunner Server監控",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0501","digiRunner監控","digiRunnerMonitor","digiRunner Server監控",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0502","System Resource","","digiRunner儀表板",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0502","digiRunner儀表板","digiRunnerDashboard","digiRunner儀表板",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0503","API Abnormal Report","API Abnormal Report","API Abnormal Report","","en-US","manager",DateTimeUtil.now());
            createTsmpFunc("AC0503","API異常報表","API Abnormal Report","API異常報表","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0508","Elastic Stack","","",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0508","ELK儀表板","","",null,"zh-TW","manager",DateTimeUtil.now());
	    	
	    	if (isOsType == false) {
	    		createTsmpFunc("AC0509","Security Audit Log","Security Audit Log","安全稽核日誌",null,"en-US","manager",DateTimeUtil.now());
	    		createTsmpFunc("AC0509","安全稽核日誌","Security Audit Log","安全稽核日誌",null,"zh-TW","manager",DateTimeUtil.now());
	    	}
	    	
	    	createTsmpFunc("AC0510","API Dashboard","","API儀表板",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0510","API儀表板","APIDashboard","API儀表板",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0702","Alert Settings","Alert Query","Alert查詢",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0702","告警設定","Alert查詢","Alert查詢",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0706","Alert Settings","Alert Query","Alert查詢",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0706","告警設定","Alert查詢","Alert查詢",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC09","Reports","","報表管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC09","報表管理","Reports","報表管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0901","API Calls","","API使用次數統計",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0901","API使用次數統計","API Usage Report","API使用次數統計",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0902","API RESP distribution","","API回應時間分佈",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0902","API回應時間分佈","API RESP distribution","API回應時間分佈",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0903","API Avg. RESP Time","","API平均回應時間",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0903","API平均回應時間","API Avg. RESP Time","API平均回應時間",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0904","API RPM/RPS","","API請求/回覆明細查詢",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0904","API請求/回覆明細查詢","API Text Search","API請求/回覆明細查詢",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0905","API GTW Traffic","","API流量分析",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0905","API GTW 流量分佈","API Traffic Analysis","API流量分析",null,"zh-TW","manager",DateTimeUtil.now());
	        createTsmpFunc("AC0906","Bad Attempts","","Bad Attempt連線報告",null,"en-US","manager",DateTimeUtil.now());
	        createTsmpFunc("AC0906","Bad Attempt連線報告","Bad Attempt Report","Bad Attempt連線報告",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0907","Client Calls","","Clients使用次數統計",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0907","Clients使用次數統計","Clients Usage Report","Clients使用次數統計",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0908","Client - API Calls","","Client-API使用次數統計",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0908","Client-API使用次數統計","Client-API Usage Report","Client-API使用次數統計",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0909","API GTW Integrity","","API GTW 存取統計",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0909","API GTW 存取統計","API GTW Integrity","API GTW 存取統計",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0910","API RESP elapse","API RESP elapse","API 極值時間分析",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0910","API 回應時間極值","API RESP elapse","API 極值時間分析",null,"zh-TW","manager",DateTimeUtil.now());
	    	
	    	createTsmpFunc("AC1002","Organization","",".組織單位查詢","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1002","組織單位維護","",".組織單位查詢","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1107","Authentications","","","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1107","查詢核身方式","","","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1116","Security Level","Security Level Managerment","安全等級維護","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1116","安全等級維護","Security Level Managerment","安全等級維護","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1202","Role & txID","Role txId mapping","角色與交易代碼對應","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1202","角色與交易代碼對應","Role txId mapping","角色與交易代碼對應","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC13","Reports","Reports","報表管理","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC13","報表管理","Reports","報表管理","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1301","API Calls","API Usage Report","API使用次數統計","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1301","API使用次數統計","API Usage Report","API使用次數統計","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1302","API RESP distribution","API RESP distribution","API回應時間分佈","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1302","API回應時間分佈","API RESP distribution","API回應時間分佈","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1303","API Avg. RESP Time","API Avg. RESP Time","API平均回應時間","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1303","API平均回應時間","API Avg. RESP Time","API平均回應時間","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1304","API GTW traffic","API GTW traffic","API GTW流量分佈","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC1304","API GTW流量分佈","API GTW traffic","API GTW流量分佈","","zh-TW","manager",DateTimeUtil.now());
            createTsmpFunc("AC1305","Bad Attempt Report","Bad Attempt Report","Bad Attempt連線報告","","en-US","manager",DateTimeUtil.now());
            createTsmpFunc("AC1305","Bad Attempt連線報告","Bad Attempt Report","Bad Attempt連線報告","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP01","API Portal Management","Web Portal","用於維護入口網",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP01","入口網後台管理","Web Portal","用於維護入口網",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0105","API Theme","API Theme","主題分類維護管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0105","主題分類維護","Theme Category","主題分類維護管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0113","FAQ","FAQ","常見問答維護&編輯",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0113","常見問答維護","常見問答維護","常見問答維護&編輯",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0114","About Us","About us","關於網站維護&編輯",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0114","關於網站維護","關於網站維護","關於網站維護&編輯",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0115","Sitemap","Sitemap","網站地圖維護&編輯",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0115","網站地圖維護","網站地圖維護","網站地圖維護&編輯",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0116","News","Announcement","公告CRUD",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0116","公告維護","Announcement","公告CRUD",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP02","Certificate Management","Client Certificate Authority","用戶憑證管理",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP02","用戶憑證管理","Client Certificate Authority","用戶憑證管理",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0201","JWE Cert. List","JWE Certificate Authority List","JWE加密憑證列表",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0201","JWE加密憑證列表","JWE Certificate Authority List","JWE加密憑證列表",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0202","JWE Cert. Management","JWE Certificate Authority Management","JWE加密憑證維護",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0202","JWE加密憑證維護","JWE Certificate Authority Management","JWE加密憑證維護",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0203","TLS Cert. List","TLS Communication Certificate List","TLS通訊憑證列表","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0203","TLS通訊憑證列表","TLS Communication Certificate List","TLS通訊憑證列表","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0204","TLS Cert. Management","TLS Communication Certificate Management","TLS通訊憑證列表","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0204","TLS通訊憑證維護","TLS Communication Certificate Management","TLS通訊憑證維護","","zh-TW","manager",DateTimeUtil.now());
			createTsmpFunc("NP0205","Client Certificates(mTLS)","Client Certificates(mTLS)","用戶端憑證(mTLS)","","en-US","manager",DateTimeUtil.now());
			createTsmpFunc("NP0205","用戶端憑證(mTLS)","mTLS Client Management","用戶端憑證(mTLS)","","zh-TW","manager",DateTimeUtil.now());
			createTsmpFunc("NP03","Application Forms","Application Form","各類申請單","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP03","各類申請單","Application Form","各類申請單","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0301","API Publish","API Shelves","API上下架、異動",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0301","API上下架","API Shelves","API上下架、異動",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0302","API Client Account","Client Registered Requisition","用戶端註冊","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0302","用戶端註冊","Client Registered Requisition","用戶端註冊","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0303","API Authorization","Client API Authorization Requisition","用戶端授權API","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0303","用戶端授權API","Client API Authorization Requisition","用戶端授權API","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0304","API Key","Apply an API Key","API Key 申請","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0304","API Key 申請","Apply an API Key","API Key 申請","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0401","Applications","Applications","簽核作業",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0401","簽核作業","Applications","簽核作業",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0402","Approval Flow Settings","Sign Off Setting","簽核關卡設定維護",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0402","簽核關卡維護","Sign Off Setting","簽核關卡設定維護",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP05","System Information","System Information","系統現況查詢","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP05","系統現況查詢","System Information","系統現況查詢","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0504","API Key Approval History","API Key Approval History","API Key 現況查詢","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0504","API Key 現況查詢","API Key Approval History","API Key 現況查詢","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0512","Published API","API Shelves Search","查詢入口網API上架現況",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0512","入口網上架現況查詢","API Shelves Search","查詢入口網API上架現況",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0513","Scheduled Tasks","Schedule","排程作業",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0513","排程作業","Schedule","排程作業",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0514","Recurring Tasks","Cycle Schedule Job","週期排程作業","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0514","週期排程作業","Cycle Schedule Job","週期排程作業","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0516","Mail Log","Mail Log","寄件歷程","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP0516","寄件歷程","Mail Log","寄件歷程","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP1201","Swagger Settings","Swagger Settings","Swager開放設定","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("NP1201","Swagger開放設定","Switch On/Off","Swager開放設定","","zh-TW","manager",DateTimeUtil.now());
			createTsmpFunc("NP1202","API Group Status","API Group Status","apiGroupStatus","","en-US","manager",DateTimeUtil.now());
			createTsmpFunc("NP1202","API群組現況查詢","API Group Status","API群組現況查詢","","zh-TW","manager",DateTimeUtil.now());

	    	createTsmpFunc("LB00","系統設定","System Configs","System Configs",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB00","System Configs","System Configs","System Configs",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0001","Setting","Setting","Setting",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0001","Setting","Setting","Setting",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0002","Items","Items","Items",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0002","Items","Items","Items",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0003","Files","Files","Files",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0003","Files","Files","Files",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0004","Customer Setting","Customer Setting","Customer Setting",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0004","Customer Setting","Customer Setting","Customer Setting",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0005","Online Console","Online Console","Online Console",null,"zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0005","Online Console","Online Console","Online Console",null,"en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0226","GTW OAuth 2.0 IdP","GTW OAuth 2.0 IdP","gtwOAuth2IdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0226","GTW OAuth 2.0 IdP","GTW OAuth 2.0 IdP","gtwOAuth2IdP","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0227","GTW LDAP IdP","GTW LDAP IdP","gtwLdapIdp","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0227","GTW LDAP IdP","GTW LDAP IdP","gtwLdapIdp","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0006","WebSocket Proxy Management","WebSocket Proxy Management","WebSocket Proxy Management","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0006","WebSocket Proxy 維護","WebSocket Proxy Management","WebSocket Proxy 維護","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0007","Static Webpage Reverse Proxy","Static Webpage Reverse Proxy","Static Webpage Reverse Proxy","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0007","靜態網頁反向代理","Static Webpage Reverse Proxy","Static Webpage Reverse Proxy","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0228","GTW API IdP","GTW API IdP","gtwApiIdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0228","GTW API IdP","GTW API IdP","gtwApiIdP","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0020","AC API IdP","AC API IdP","acApiIdP","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0020","AC API IdP","AC API IdP","acApiIdP","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0008","RDB Connection","RDB Connection","rdbConnection","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0008","RDB連線","RDB Connection","rdbConnection","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0009","Mail Template Export/Import","Mail Template Export/Import","mailTemplateExportImport","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0009","Mail模板匯出/入","Mail Template Export/Import","mailTemplateExportImport","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0229","GTW JDBC IdP","GTW JDBC IdP","gtwJdbcIdp","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0229","GTW JDBC IdP","GTW JDBC IdP","gtwJdbcIdp","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0319","API Modify Batch","API Modify Batch","apiModifyBatch","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0319","API批量修改","API Modify Batch","apiModifyBatch","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0105","Embedded Function Management","Embedded Function Management","Embedded Function Management","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0105","嵌入頁面維護","Embedded Function Management","Embedded Function Management","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0230","Client Export/Import","Client Export/Import","clientExportImport","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0230","用戶端匯出/入","Client Export/Import","clientExportImport","","zh-TW","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0231","GTW CUS IdP","GTW CUS IdP","gtwCusIdp","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("AC0231","GTW CUS IdP","GTW CUS IdP","gtwCusIdp","","zh-TW","manager",DateTimeUtil.now());
	    	
	    	createTsmpFunc("LB0010","Bot Detection","Bot Detection","botDetection","","en-US","manager",DateTimeUtil.now());
	    	createTsmpFunc("LB0010","Bot Detection","Bot Detection","botDetection","","zh-TW","manager",DateTimeUtil.now());
	    	
	    	//取得版本tsmpFunc清單
	    	setVersionTsmpFunc(licenseEdition);
	    	List<TsmpFuncVo> tsmpFuncVos = new LinkedList<TsmpFuncVo>();	    	
	        tsmpFuncVos = tsmpFuncList.stream().collect(Collectors.toList());
	        tsmpFuncList.clear();
	        return tsmpFuncVos;
	        
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
    }
	
	
	//依照licenseEdition整理出Func清單
	protected void setVersionTsmpFunc(LicenseEditionTypeVo licenseEdition) {
		if (licenseEdition.equals(LicenseEditionTypeVo.Alpha)) {
			tsmpFuncList = getAlpha();
		}else if (licenseEdition.equals(LicenseEditionTypeVo.Enterprise)) {
			tsmpFuncList = getEnterprise();
		}else if (licenseEdition.equals(LicenseEditionTypeVo.Enterprise_Lite)) {
			tsmpFuncList = getEnterpriseLite();
		}else if (licenseEdition.equals(LicenseEditionTypeVo.Express)) {
			tsmpFuncList = getExpress();
		}

	}
	
	private List<TsmpFuncVo> getAlpha() {
		List<TsmpFuncVo> list = tsmpFuncList.stream().filter(this::isInAlphaList).collect(Collectors.toList());
		return list;
	}
	
	private List<TsmpFuncVo> getEnterprise() {
		List<TsmpFuncVo> list = tsmpFuncList.stream().filter(this::isInEnterpriseList).collect(Collectors.toList());
		return list;
	}
	

	private List<TsmpFuncVo> getEnterpriseLite() {
		List<TsmpFuncVo> list = tsmpFuncList.stream().filter(this::isInEnterpriseLiteList).collect(Collectors.toList());
		return list;
	}

	private List<TsmpFuncVo> getExpress() {
		List<TsmpFuncVo> list = tsmpFuncList.stream().filter(this::isInExpressList).collect(Collectors.toList());
		return list;
	}
	
	private boolean isInAlphaList(TsmpFuncVo tsmpFunc) {
		return isInFuncCodeList(ALPHA, tsmpFunc);
	}	
	private boolean isInEnterpriseList(TsmpFuncVo tsmpFunc) {
		return isInFuncCodeList(ENTERPRISE, tsmpFunc);
	}
	private boolean isInEnterpriseLiteList(TsmpFuncVo tsmpFunc) {
		return isInFuncCodeList(ENTERPRISE_LITE, tsmpFunc);
	}
	private boolean isInExpressList(TsmpFuncVo tsmpFunc) {
		return isInFuncCodeList(EXPRESS, tsmpFunc);
	}
	
	private Boolean isInFuncCodeList(String[] funcCodes, TsmpFuncVo tsmpFunc)  {
		String funcCode = tsmpFunc.getFuncCode();
		for(String value : funcCodes) {
			if (value.equals(funcCode)) {
				return true;
			}
		}
		return false;	    
	}
	
    protected void createTsmpFunc(String funcCode, String funcName, String funcNameEn, String funcDesc,
			String funcUrl, String locale, String updateUser, Date updateTime) {
			TsmpFuncVo tsmpFunc = new TsmpFuncVo();
			tsmpFunc.setFuncCode(funcCode);
			tsmpFunc.setFuncName(funcName);
			tsmpFunc.setFuncNameEn(funcNameEn);
			tsmpFunc.setFuncDesc(funcDesc);
			tsmpFunc.setFuncUrl(funcUrl);
			tsmpFunc.setLocale(locale);
			tsmpFunc.setUpdateUser(updateUser);
			tsmpFunc.setUpdateTime(updateTime);
			tsmpFunc.setFuncType("0");
			tsmpFuncList.add(tsmpFunc);
	}
    
}
