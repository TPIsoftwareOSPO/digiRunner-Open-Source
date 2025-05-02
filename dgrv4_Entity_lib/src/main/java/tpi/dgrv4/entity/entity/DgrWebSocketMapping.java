package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;
import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "dgr_web_socket_mapping")
@EntityListeners(FuzzyEntityListener.class)
public class DgrWebSocketMapping extends BasicFields implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "ws_mapping_id")
	private Long wsMappingId;

	@Fuzzy
	@Column(name = "site_name")
	private String siteName;

	@Fuzzy
	@Column(name = "target_ws")
	private String targetWs;

	@Column(name = "memo")
	private String memo;

	@Column(name = "auth")
	private String auth = "N";

	@Override
	public Long getPrimaryKey() {
		return wsMappingId;
	}

	@Override
	public String toString() {
		return "DgrWebSocketMapping [wsMappingId=" + wsMappingId + ", siteName=" + siteName + ", targetWs=" + targetWs + ", memo=" + memo + "]";
	}

	public Long getWsMappingId() {
		return wsMappingId;
	}

	public void setWsMappingId(Long wsMappingId) {
		this.wsMappingId = wsMappingId;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getTargetWs() {
		return targetWs;
	}

	public void setTargetWs(String targetWs) {
		this.targetWs = targetWs;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

}
