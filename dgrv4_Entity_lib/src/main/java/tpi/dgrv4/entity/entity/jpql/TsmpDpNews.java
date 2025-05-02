package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_news")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpNews {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "news_id")
	private Long newsId;

	@Column(name = "new_title")
	private String newTitle;

	@Column(name = "new_content")
	private String newContent;

	@Column(name = "status")
	private String status = "1";

	@Column(name = "org_id")
	private String orgId;

	@Column(name = "post_date_time")
	private Date postDateTime;

	@Column(name = "ref_type_subitem_no")
	private String refTypeSubitemNo;

	//--- extends BasicFields 
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

	public TsmpDpNews() {
	}


	@Override
	public String toString() {
		Optional<String> postDateDate = DateTimeUtil.dateTimeToString(postDateTime, DateTimeFormatEnum.西元年月日時分秒_2);
		String postDateTimeStr = null;
		String createDateTimeStr = null;

		if (postDateDate.isPresent()) {
			postDateTimeStr = postDateDate.get();
		}

		Optional<String> createDateDate = DateTimeUtil.dateTimeToString(createDateTime, DateTimeFormatEnum.西元年月日時分秒_2);
		if (createDateDate.isPresent()) {
			createDateTimeStr = createDateDate.get();
		}
		
		return "TsmpDpNews [newsId=" + newsId + ", newTitle=" + newTitle + ", newContent=" + newContent + ", status="
				+ status + ", orgId=" + orgId + ", postDateTime=" + postDateTimeStr + ", refTypeSubitemNo="
				+ refTypeSubitemNo + ", createDateTime=" + createDateTimeStr + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]\n";
	}
	
	/* getters and setters */

	public Long getNewsId() {
		return newsId;
	}

	public void setNewsId(Long newsId) {
		this.newsId = newsId;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public Date getPostDateTime() {
		return postDateTime;
	}

	public void setPostDateTime(Date postDateTime) {
		this.postDateTime = postDateTime;
	}

	public String getRefTypeSubitemNo() {
		return refTypeSubitemNo;
	}

	public void setRefTypeSubitemNo(String refTypeSubitemNo) {
		this.refTypeSubitemNo = refTypeSubitemNo;
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


}
