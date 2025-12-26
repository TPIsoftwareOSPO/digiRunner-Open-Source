package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "dgr_dashboard_last_data")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class DgrDashboardLastData{

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "dashboard_id")
	private Long dashboardId;

	@Column(name = "dashboard_type")
	private Integer dashboardType;

	@Column(name = "time_type")
	private Integer timeType;

	@Column(name = "str1")
	private String str1;

	@Column(name = "str2")
	private String str2;

	@Column(name = "str3")
	private String str3;

	@Column(name = "num1")
	private Long num1;

	@Column(name = "num2")
	private Long num2;

	@Column(name = "num3")
	private Long num3;
	
	@Column(name = "num4")
	private Long num4;
	
	@Column(name = "sort_num")
	private Integer sortNum = 1;

	/* constructors */
	public DgrDashboardLastData() {}

	@Override
	public String toString() {
		return "DgrDashboardLastData [dashboardId=" + dashboardId + ", dashboardType=" + dashboardType + ", timeType="
				+ timeType + ", str1=" + str1 + ", str2=" + str2 + ", str3=" + str3 + ", num1=" + num1 + ", num2="
				+ num2 + ", num3=" + num3 + ", num4=" + num4 + ", sortNum=" + sortNum + "]";
	}

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	public Integer getDashboardType() {
		return dashboardType;
	}

	public void setDashboardType(Integer dashboardType) {
		this.dashboardType = dashboardType;
	}

	public Integer getTimeType() {
		return timeType;
	}

	public void setTimeType(Integer timeType) {
		this.timeType = timeType;
	}

	public String getStr1() {
		return str1;
	}

	public void setStr1(String str1) {
		this.str1 = str1;
	}

	public String getStr2() {
		return str2;
	}

	public void setStr2(String str2) {
		this.str2 = str2;
	}

	public String getStr3() {
		return str3;
	}

	public void setStr3(String str3) {
		this.str3 = str3;
	}

	public Long getNum1() {
		return num1;
	}

	public void setNum1(Long num1) {
		this.num1 = num1;
	}

	public Long getNum2() {
		return num2;
	}

	public void setNum2(Long num2) {
		this.num2 = num2;
	}

	public Long getNum3() {
		return num3;
	}

	public void setNum3(Long num3) {
		this.num3 = num3;
	}

	public Long getNum4() {
		return num4;
	}

	public void setNum4(Long num4) {
		this.num4 = num4;
	}

	public Integer getSortNum() {
		return sortNum;
	}

	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}

	
}
