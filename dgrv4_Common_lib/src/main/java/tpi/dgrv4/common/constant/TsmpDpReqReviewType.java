package tpi.dgrv4.common.constant;

import java.util.Optional;

/** 簽核類別代碼 (tsmp_dp_items.item_no = 'REVIEW_TYPE') */
public class TsmpDpReqReviewType {

	public final static String ITEM_NO = "REVIEW_TYPE";
	
	public final static ItemContainer API_APPLICATION = //
			new ItemContainer("API_APPLICATION", "用戶API申請", 60, new String[]{"AP", null, null, null, null});
	
	public final static ApiOnOff API_ON_OFF = //
			new ApiOnOff("API_ON_OFF", "API上下架管理", 61, null);

	public final static ItemContainer CLIENT_REG = //
			new ItemContainer("CLIENT_REG", "用戶註冊", 65, new String[]{"CR", null, null, null, null});
	
	// 此為開發範例，非正式簽核類別
	public final static ItemContainer THINKPOWER_ARTICLE = //
			new ItemContainer("THINKPOWER_ARTICLE", "昕力大學文章", 65, new String[]{"TA", null, null, null, null});
	
	public final static OpenApiKey OPEN_API_KEY = //
			new OpenApiKey("OPEN_API_KEY", "Open API Key 管理", 67, null);

	/** 簽核子類別代碼 (tsmp_dp_items.item_no = 'API_ON_OFF') */
	public static class ApiOnOff extends ItemContainer {

		public final String ITEM_NO = "API_ON_OFF";

		public final ItemContainer API_ON = //
				new ItemContainer("API_ON", "API上架", 62, new String[]{"ON", null, null, null, null});

		public final ItemContainer API_OFF = //
				new ItemContainer("API_OFF", "API下架", 63, new String[]{"OF", null, null, null, null});

		public final ItemContainer API_ON_UPDATE = //
				new ItemContainer("API_ON_UPDATE", "API異動", 64, new String[]{"UP", null, null, null, null});
 
		public ApiOnOff(String value, String text, Integer sort, String[] params) {
			super(value, text, sort, params);
		}

	}
	
	/** 簽核子類別代碼 (tsmp_dp_items.item_no = 'OPEN_API_KEY') */
	public static class OpenApiKey extends ItemContainer {
		
		public final String ITEM_NO = "OPEN_API_KEY";
		
		public final ItemContainer OPEN_API_KEY_APPLICA = //
				new ItemContainer("OPEN_API_KEY_APPLICA", "Open API Key 申請", 700, new String[]{"KA", null, null, null, null});
		
		public final ItemContainer OPEN_API_KEY_UPDATE = //
				new ItemContainer("OPEN_API_KEY_UPDATE", "Open API Key 異動", 701, new String[]{"KU", null, null, null, null});
		
		public final ItemContainer OPEN_API_KEY_REVOKE = //
				new ItemContainer("OPEN_API_KEY_REVOKE", "Open API Key 撤銷", 702, new String[]{"KR", null, null, null, null});
		
		public OpenApiKey(String value, String text, Integer sort, String[] params) {
			super(value, text, sort, params);
		}
	}
	
	public static class ItemContainer {

		private final String value;

		private final String text;

		private final Integer sort;

		private final String[] params;

		public ItemContainer(String value, String text, Integer sort, String[] params) {
			this.value = value;
			this.text = text;
			this.sort = sort;
			this.params = params;
		}

		public String value() {
			return this.value;
		}

		public String text() {
			return this.text;
		}

		public Integer sort() {
			return this.sort;
		}

		public String[] params() {
			return this.params;
		}

		/**
		 * @param index 如果傳入1, 則會取得 params[] 的第0個元素
		 * @return
		 */
		public Optional<String> param(int index) {
			index--;	// 為了對應 TSMP_DP_ITEMS.PARAM 從 1 開始
			try {
				return Optional.ofNullable(params()[index]);
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		public boolean isValueEquals(String input) {
			return value().equals(input);
		}

		public boolean isParamEquals(String input, int index) {
			Optional<String> opt = param(index);
			if (opt.isPresent()) {
				return opt.get().equals(input);
			} else if (input == null) {
				return true;
			}
			return false;
		}
	}

}
