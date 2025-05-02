package tpi.dgrv4.gateway.component.job;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.util.StringUtils;
@SuppressWarnings("serial")
public abstract class Job implements Serializable {

	private final String id = UUID.randomUUID().toString();

	private AtomicBoolean isDone = new AtomicBoolean(false);

	// 用來與佇列中其他的Job比對是否為同一群組
	private final String groupId;

	/**
	 * 紀錄被丟入佇列的時間
	 */
	private long timestamp;

	public Job() {
		String temp = this.getClass().getCanonicalName();
		if(!StringUtils.hasLength(temp)) {
			temp = this.getClass().getName();
		}
		groupId = temp;
	}

	public Job(String groupId) {
		this.groupId = groupId;
	}

	// 工作要執行的內容
	public abstract void run(JobHelperImpl jobHelper, JobManager jobManager);

	public void setIsDone() {
		this.isDone.set(true);
	}

	public boolean isDone() {
		return this.isDone.get();
	}

	public String getId() {
		return this.id;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * 設為現在時間
	 */
	public void setTimestamp() {
		this.timestamp = new Date().getTime();
	}

	@Override
	public String toString() {
		return "Job [id=" + id + ", groupId=" + groupId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Job other = (Job) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
