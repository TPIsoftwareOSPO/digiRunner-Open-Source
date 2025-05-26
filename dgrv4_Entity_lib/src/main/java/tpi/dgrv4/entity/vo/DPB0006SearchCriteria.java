package tpi.dgrv4.entity.vo;

import java.util.Date;
import java.util.List;

// 包裝參數
public class DPB0006SearchCriteria {

	private Date startDate;

	private Date endDate;

	private List<String> regStatusList;

	private String[] words;

	private Date lastUpdateDateTime;

	private String lastClientId;

	private Integer pageSize;

	public DPB0006SearchCriteria() {}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<String> getRegStatusList() {
		return regStatusList;
	}

	public void setRegStatusList(List<String> regStatusList) {
		this.regStatusList = regStatusList;
	}

	public String[] getWords() {
		return words;
	}

	public void setWords(String[] words) {
		this.words = words;
	}

	public Date getLastUpdateDateTime() {
		return lastUpdateDateTime;
	}

	public void setLastUpdateDateTime(Date lastUpdateDateTime) {
		this.lastUpdateDateTime = lastUpdateDateTime;
	}

	public String getLastClientId() {
		return lastClientId;
	}

	public void setLastClientId(String lastClientId) {
		this.lastClientId = lastClientId;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
}
