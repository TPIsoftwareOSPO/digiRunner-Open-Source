package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_vgroup_group")
@IdClass(value = TsmpVgroupGroupId.class)
public class TsmpVgroupGroup {

	@Id
	@Column(name = "vgroup_id")
	private String vgroupId;

	@Id
	@Column(name = "group_id")
	private String groupId;
	
	@Column(name = "create_time")
	private Date createTime  = DateTimeUtil.now();

	/* constructors */

	public TsmpVgroupGroup() {}

	/* methods */
	
	@Override
	public String toString() {
		return "TsmpVgroupGroup [vgroupId=" + vgroupId + ", groupId=" + groupId + ", createTime=" + createTime + "]";
	}

	/* getters and setters */
	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
