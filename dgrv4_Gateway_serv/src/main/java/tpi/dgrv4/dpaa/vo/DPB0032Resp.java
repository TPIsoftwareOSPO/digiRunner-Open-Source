package tpi.dgrv4.dpaa.vo;

public class DPB0032Resp {

	/** ID (流水號)	若為空表示create, 反之為 update */
	private Long seqId;

	/** 標題 */
	private String aboutSubject;

	/** 描述 */
	private String aboutDesc;

	public DPB0032Resp() {}

	public Long getSeqId() {
		return seqId;
	}

	public void setSeqId(Long seqId) {
		this.seqId = seqId;
	}

	public String getAboutSubject() {
		return aboutSubject;
	}

	public void setAboutSubject(String aboutSubject) {
		this.aboutSubject = aboutSubject;
	}

	public String getAboutDesc() {
		return aboutDesc;
	}

	public void setAboutDesc(String aboutDesc) {
		this.aboutDesc = aboutDesc;
	}

}
