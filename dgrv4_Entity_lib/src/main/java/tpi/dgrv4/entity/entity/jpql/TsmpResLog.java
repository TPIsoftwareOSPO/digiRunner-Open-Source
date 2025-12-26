package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_res_log")
public class TsmpResLog {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "exe_status")
	private String exeStatus;

	@Column(name = "elapse")
	private Integer elapse;

	@Column(name = "rcode")
	private String rcode;

	@Column(name = "http_status")
	private Integer httpStatus;

	@Column(name = "err_msg")
	private String errMsg;
	
	/* constructors */

	public TsmpResLog() {}

	/* methods */
	@Override
	public String toString() {
		return "TsmpResLog [id=" + id + ", exeStatus=" + exeStatus + ", elapse=" + elapse + ", rcode=" + rcode
				+ ", httpStatus=" + httpStatus + ", errMsg=" + errMsg + "]";
	}

	/* getters and setters */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExeStatus() {
		return exeStatus;
	}

	public void setExeStatus(String exeStatus) {
		this.exeStatus = exeStatus;
	}

	public Integer getElapse() {
		return elapse;
	}

	public void setElapse(Integer elapse) {
		this.elapse = elapse;
	}

	public String getRcode() {
		return rcode;
	}

	public void setRcode(String rcode) {
		this.rcode = rcode;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	
	
}
