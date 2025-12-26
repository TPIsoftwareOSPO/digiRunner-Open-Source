package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_report_data")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpReportData{

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "id")
	private Long id;

	@Column(name = "report_type")
	private Integer reportType;

	@Column(name = "date_time_range_type")
	private Integer dateTimeRangeType;

	@Column(name = "last_row_date_time")
	private Date lastRowDateTime;

	@Column(name = "statistics_status")
	private String statisticsStatus;

	@Column(name = "string_group1")
	private String stringGroup1;

	@Column(name = "string_group2")
	private String stringGroup2;

	@Column(name = "string_group3")
	private String stringGroup3;

	@Column(name = "int_value1")
	private Long intValue1;

	@Column(name = "int_value2")
	private Long intValue2;

	@Column(name = "int_value3")
	private Long intValue3;
	
	@Column(name = "orgid")
	private String orgid;
	
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	/* constructors */

	public TsmpReportData() {}

	@Override
	public String toString() {
		return "TsmpReportData [id=" + id + ", reportType=" + reportType + ", dateTimeRangeType=" + dateTimeRangeType
				+ ", lastRowDateTime=" + lastRowDateTime + ", statisticsStatus=" + statisticsStatus + ", stringGroup1="
				+ stringGroup1 + ", stringGroup2=" + stringGroup2 + ", stringGroup3=" + stringGroup3 + ", intValue1="
				+ intValue1 + ", intValue2=" + intValue2 + ", intValue3=" + intValue3 + ", orgid=" + orgid
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	/* getters and setters */
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getReportType() {
		return reportType;
	}

	public void setReportType(Integer reportType) {
		this.reportType = reportType;
	}

	public Integer getDateTimeRangeType() {
		return dateTimeRangeType;
	}

	public void setDateTimeRangeType(Integer dateTimeRangeType) {
		this.dateTimeRangeType = dateTimeRangeType;
	}

	public Date getLastRowDateTime() {
		return lastRowDateTime;
	}

	public void setLastRowDateTime(Date lastRowDateTime) {
		this.lastRowDateTime = lastRowDateTime;
	}

	public String getStatisticsStatus() {
		return statisticsStatus;
	}

	public void setStatisticsStatus(String statisticsStatus) {
		this.statisticsStatus = statisticsStatus;
	}

	public String getStringGroup1() {
		return stringGroup1;
	}

	public void setStringGroup1(String stringGroup1) {
		this.stringGroup1 = stringGroup1;
	}

	public String getStringGroup2() {
		return stringGroup2;
	}

	public void setStringGroup2(String stringGroup2) {
		this.stringGroup2 = stringGroup2;
	}

	public String getStringGroup3() {
		return stringGroup3;
	}

	public void setStringGroup3(String stringGroup3) {
		this.stringGroup3 = stringGroup3;
	}

	public Long getIntValue1() {
		return intValue1;
	}

	public void setIntValue1(Long intValue1) {
		this.intValue1 = intValue1;
	}

	public Long getIntValue2() {
		return intValue2;
	}

	public void setIntValue2(Long intValue2) {
		this.intValue2 = intValue2;
	}

	public Long getIntValue3() {
		return intValue3;
	}

	public void setIntValue3(Long intValue3) {
		this.intValue3 = intValue3;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getOrgid() {
		return orgid;
	}

	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}

	
}
