package tpi.dgrv4.dpaa.constant;

import java.util.ArrayList;
import java.util.List;

public enum TsmpDpItem {

	ENABLE_FLAG("ENABLE_FLAG", ""), //
	ENABLE_FLAG_ENABLE(TsmpDpItem.ENABLE_FLAG.name(), "1", "1"), //
	ENABLE_FLAG_DEACTIVATE(TsmpDpItem.ENABLE_FLAG.name(), "0", "2"), //
	ENABLE_FLAG_ALL(TsmpDpItem.ENABLE_FLAG.name(), "-1"), //
	ENABLE_FLAG_LOCK(TsmpDpItem.ENABLE_FLAG.name(), "2", "3"), //

	DP_PUBLIC_FLAG("DP_PUBLIC_FLAG", ""), //
	DP_PUBLIC_FLAG_E(TsmpDpItem.DP_PUBLIC_FLAG.name(), "E"), //
	DP_PUBLIC_FLAG_D(TsmpDpItem.DP_PUBLIC_FLAG.name(), "D"), //
	DP_PUBLIC_FLAG_ALL(TsmpDpItem.DP_PUBLIC_FLAG.name(), "ALL"), //

	DP_API_REVOKE_TYPE("DP_API_REVOKE_TYPE", ""), //
	REVOKE_LAUNCH(TsmpDpItem.DP_API_REVOKE_TYPE.name(), "revokeLaunch"), //
	REVOKE_REMOVAL(TsmpDpItem.DP_API_REVOKE_TYPE.name(), "revokeRemoval"), //
	REVOKE_ALL(TsmpDpItem.DP_API_REVOKE_TYPE.name(), "revokeAll"),

	DGR_API_REVOKE_TYPE("DGR_API_REVOKE_TYPE", ""), //
	REVOKE_ENABLE(TsmpDpItem.DGR_API_REVOKE_TYPE.name(), "revokeEnable"), //
	REVOKE_DISABLE(TsmpDpItem.DGR_API_REVOKE_TYPE.name(), "revokeDisable"), //
	REVOKE_ENABLE_DISABLE(TsmpDpItem.DGR_API_REVOKE_TYPE.name(), "revokeAll"), //

	SCHED_CATE1("SCHED_CATE1", ""), //
	API_SCHEDULED(TsmpDpItem.SCHED_CATE1.name(), "API_SCHEDULED"), //

	API_ENABLE(TsmpDpItem.API_SCHEDULED.name(), "API_ENABLE"), //
	API_DISABLE(TsmpDpItem.API_SCHEDULED.name(), "API_DISABLE"), //
	API_LAUNCH(TsmpDpItem.API_SCHEDULED.name(), "API_LAUNCH"), //
	API_REMOVAL(TsmpDpItem.API_SCHEDULED.name(), "API_REMOVAL"), //

	SCHED_MSG("SCHED_MSG", ""), //
	API_SCH_WAKE_UP(TsmpDpItem.SCHED_MSG.name(), "API_SCH_WAKE-UP"), //
	FISHING_SCH_EN_API(TsmpDpItem.SCHED_MSG.name(), "FISHING_SCH_EN_API"), //
	CHANGE_EN_STATE(TsmpDpItem.SCHED_MSG.name(), "CHANGE_EN_STATE"), //
	FISHING_SCH_DIS_API(TsmpDpItem.SCHED_MSG.name(), "FISHING_SCH_DIS_API"), //
	CHANGE_DIS_STATE(TsmpDpItem.SCHED_MSG.name(), "CHANGE_DIS_STATE"), //
	FISHING_SCH_LA_API(TsmpDpItem.SCHED_MSG.name(), "FISHING_SCH_LA_API"), //
	CHANGE_LAUNCH_STATE(TsmpDpItem.SCHED_MSG.name(), "CHANGE_LAUNCH_STATE"), //
	FISHING_SCH_RE_API(TsmpDpItem.SCHED_MSG.name(), "FISHING_SCH_RE_API"), //
	CHANGE_REMOVAL_STATE(TsmpDpItem.SCHED_MSG.name(), "CHANGE_REMOVAL_STATE");

	private String itemNo;
	private String subitemNo;
	private String param1;

	TsmpDpItem(String itemNo, String subitemNo, String param1) {
		this.itemNo = itemNo;
		this.subitemNo = subitemNo;
		this.param1 = param1;
	}

	TsmpDpItem(String itemNo, String subitemNo) {
		this.itemNo = itemNo;
		this.subitemNo = subitemNo;
		this.param1 = "";
	}

	public String getItemNo() {
		return itemNo;
	}

	public String getSubitemNo() {
		return subitemNo;
	}

	public String getParam1() {
		return param1;
	}

	/**
	 * 根據主項目編號獲取匹配的列舉值列表。
	 *
	 * @param targetItemNo 目標主項目編號
	 * @return 匹配的列舉值列表
	 */
	public static List<TsmpDpItem> getByItemNo(TsmpDpItem tsmpDpItem) {
		List<TsmpDpItem> matchingItems = new ArrayList<>();
		for (TsmpDpItem item : values()) {
			// 不分大小寫比對主項目編號
			if (tsmpDpItem.getItemNo().equalsIgnoreCase(item.getItemNo())) {
				matchingItems.add(item);
			}
		}
		return matchingItems;
	}

	public static TsmpDpItem getByItemNoAndParam1(TsmpDpItem tsmpDpItem, String param1) {
		List<TsmpDpItem> ls = getByItemNo(tsmpDpItem);

		return ls.stream() //
				.filter(item -> item.getParam1().equals(param1)) //
				.findFirst().orElse(null);
	}
	
	/**
	 * 檢查指定的字符串是否為某主項目編號下的子項目編號。
	 *
	 * @param str          待檢查的字符串
	 * @param targetItemNo 目標主項目編號
	 * @return 如果存在於子項目編號中則返回 true，否則返回 false
	 */
	public static boolean existsSubitemNoInItemNo(TsmpDpItem tsmpDpItem, String subitemNo) {
		List<TsmpDpItem> ls = getByItemNo(tsmpDpItem);
		// 不分大小寫比對子項目編號
		return ls.stream().anyMatch(item -> item.getSubitemNo().equalsIgnoreCase(subitemNo));
	}

	public static boolean existsParam1InItemNo(TsmpDpItem tsmpDpItem, String param1) {
		List<TsmpDpItem> ls = getByItemNo(tsmpDpItem);
		return ls.stream().anyMatch(item -> item.getParam1().equalsIgnoreCase(param1));
	}

	public static boolean isEqualSubitemNo(TsmpDpItem item, String subitemNo) {

		return item.getSubitemNo().equalsIgnoreCase(subitemNo);
	}

	public static boolean isEqualParam1(TsmpDpItem item, String param1) {

		return item.getParam1().equalsIgnoreCase(param1);
	}

}
