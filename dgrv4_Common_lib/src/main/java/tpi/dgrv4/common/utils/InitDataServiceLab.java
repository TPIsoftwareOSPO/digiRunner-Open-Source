package tpi.dgrv4.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class InitDataServiceLab {
	List<Map<String,String>> dataList = new ArrayList<>();
	
	public List<Map<String,String>> getTsmpSetting(){
		
		List<Map<String,String>> dataList = new ArrayList<>();
		String id;
		String value;
		String memo;
		
		String[][] arrData = {{id = "SERVICE_MAIL_ENABLE", value = "true", memo = "主要smtp server設定"}
							 ,{"SERVICE_MAIL_HOST", "smtp.gmail.com", "主要smtp server設定"}
							 ,{(id = "SERVICE_MAIL_PORT"), (value = "587"), (memo = "主要smtp server設定")}};

		for(int i=0; i <arrData.length ;i++) {
			 Map<String,String> dataMap = new HashMap<>();
			 dataMap.put("id", arrData[i][0]);
			 dataMap.put("value", arrData[i][1]);
			 dataMap.put("memo", arrData[i][2]);
			 
			 dataList.add(dataMap);
		}
		
		
		return dataList;
	}
	
	public List<Map<String,String>> getTsmpDpItems(){
		
		String itemId;
        String itemNo;
        String itemName;
        String subitemNo;
        String subitemName;
        String sortBy;
        String locale;
        String isDefault;
        String param1;
        String param2;
        String param3;
        String param4;
        String param5;

		
		createTsmpDpItems(itemId = "1",itemNo = "MEMBER_REG_FLAG",itemName = "前台會員註冊開關",subitemNo = "DISABLE",subitemName = "停用", sortBy = "10",locale = "zh-TW", isDefault = "V", param1 = null, param2 = null, param3 = null, param4 = null, param5 = null);
		createTsmpDpItems((itemId = "1"),(itemNo = "MEMBER_REG_FLAG"),(itemName = "Front desk member registration switch"),(subitemNo = "DISABLE"),(subitemName = "Deactivate"),(sortBy = "10"),(locale = "en-us"),(isDefault = null),(param1 = null),(param2 = null),(param3 = null),(param4 = null),(param5 = null));
		
		return dataList;
	}
	
	private void createTsmpDpItems(String itemId, String itemNo, String itemName, String subitemNo, String subitemName
			, String sortBy, String locale ,String isDefault, String param1, String param2, String param3, String param4, String param5){
		
		 Map<String,String> dataMap = new HashMap<>();
		 dataMap.put("itemId", itemId);
		 dataMap.put("itemNo", itemNo);
		 dataMap.put("itemName", itemName);
		 dataMap.put("subitemNo", subitemNo);
		 dataMap.put("subitemName", subitemName);
		 dataMap.put("sortBy", sortBy);
		 dataMap.put("locale", locale);
		 dataMap.put("isDefault", isDefault);
		 dataMap.put("param1", param1);
		 dataMap.put("param2", param2);
		 dataMap.put("param3", param3);
		 dataMap.put("param4", param4);
		 dataMap.put("param5", param5);
		 
		 dataList.add(dataMap);
	}

}
