package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_notice_log")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpNoticeLog {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "notice_log_id")
	private Long noticeLogId;

	@Column(name = "notice_src")
	private String noticeSrc;

	@Column(name = "notice_mthd")
	private String noticeMthd;

	@Column(name = "notice_key")
	private String noticeKey;

	@Column(name = "detail_id")
	private Long detailId;

	@Column(name = "last_notice_date_time")
	private Date lastNoticeDateTime = DateTimeUtil.now();

	/* constructors */

	public TsmpNoticeLog() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpNoticeLog [noticeLogId=" + noticeLogId + ", noticeSrc=" + noticeSrc + ", noticeMthd=" + noticeMthd
				+ ", noticeKey=" + noticeKey + ", detailId=" + detailId + ", lastNoticeDateTime=" + lastNoticeDateTime
				+ "]";
	}

	/* getters and setters */

	public Long getNoticeLogId() {
		return noticeLogId;
	}

	public void setNoticeLogId(Long noticeLogId) {
		this.noticeLogId = noticeLogId;
	}

	public String getNoticeSrc() {
		return noticeSrc;
	}

	public void setNoticeSrc(String noticeSrc) {
		this.noticeSrc = noticeSrc;
	}

	public String getNoticeMthd() {
		return noticeMthd;
	}

	public void setNoticeMthd(String noticeMthd) {
		this.noticeMthd = noticeMthd;
	}

	public String getNoticeKey() {
		return noticeKey;
	}

	public void setNoticeKey(String noticeKey) {
		this.noticeKey = noticeKey;
	}

	public Long getDetailId() {
		return detailId;
	}

	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}

	public Date getLastNoticeDateTime() {
		return lastNoticeDateTime;
	}

	public void setLastNoticeDateTime(Date lastNoticeDateTime) {
		this.lastNoticeDateTime = lastNoticeDateTime;
	}

}