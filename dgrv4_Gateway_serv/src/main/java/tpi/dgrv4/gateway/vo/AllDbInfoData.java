package tpi.dgrv4.gateway.vo;

import java.io.Serializable;
import java.util.LinkedList;

public class AllDbInfoData implements Serializable{
	private LinkedList<DbInfo> allDbInfoList = new LinkedList<DbInfo>();

	/**
	 * @return the allDbInfoList
	 */
	public LinkedList<DbInfo> getAllDbInfoList() {
		return allDbInfoList;
	}

	/**
	 * @param allDbInfoList the allDbInfoList to set
	 */
	public void setAllDbInfoList(LinkedList<DbInfo> allDbInfoList) {
		this.allDbInfoList = allDbInfoList;
	}
}
