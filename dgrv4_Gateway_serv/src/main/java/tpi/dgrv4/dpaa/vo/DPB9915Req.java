package tpi.dgrv4.dpaa.vo;

import java.util.ArrayList;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;


public class DPB9915Req extends ReqValidator{
	
	private Long fileId;
	private String startDate;
	private String endDate;
	private String refFileCateCode;
	private Long refId;
	private String keyword;
	private String isTmpfile;

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getRefFileCateCode() {
		return refFileCateCode;
	}

	public void setRefFileCateCode(String refFileCateCode) {
		this.refFileCateCode = refFileCateCode;
	}

	public Long getRefId() {
		return refId;
	}

	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getIsTmpfile() {
		return isTmpfile;
	}

	public void setIsTmpfile(String isTmpfile) {
		this.isTmpfile = isTmpfile;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		List<BeforeControllerRespItem> constraints =new ArrayList<BeforeControllerRespItem>();
		
		BeforeControllerRespItem bcri = new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("startDate")
				.isRequired()
				.build();

		BeforeControllerRespItem bcri1 = new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("endDate")
				.isRequired()
				.build();
		
		BeforeControllerRespItem bcri2 = new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("refId")
				.max(Long.MAX_VALUE)
				.build();
		
		constraints.add(bcri);
		constraints.add(bcri1);
		constraints.add(bcri2);
		
		return constraints;
	}
}
