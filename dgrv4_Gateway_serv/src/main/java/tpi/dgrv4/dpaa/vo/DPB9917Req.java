package tpi.dgrv4.dpaa.vo;

import java.util.ArrayList;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9917Req extends ReqValidator {
	private String refFileCateCode;
	private Long refId;
	private String isTmpfile;
	private String fileName;
	private String tmpfileName;

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

	public String getIsTmpfile() {
		return isTmpfile;
	}

	public void setIsTmpfile(String isTmpfile) {
		this.isTmpfile = isTmpfile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getTmpfileName() {
		return tmpfileName;
	}

	public void setTmpfileName(String tmpfileName) {
		this.tmpfileName = tmpfileName;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		List<BeforeControllerRespItem> constraints =new ArrayList<BeforeControllerRespItem>();
		
		BeforeControllerRespItem bcri = new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("refFileCateCode")
				.isRequired()
				.build();

		BeforeControllerRespItem bcri1 = new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("refId")
				.isRequired()
				.max(Long.MAX_VALUE)
				.build();
		
		BeforeControllerRespItem bcri2 = new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("isTmpfile")
				.pattern("^[Y]")
				.build();
		
		BeforeControllerRespItem bcri3 = new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("fileName")
				.isRequired()
				.maxLength(100)
				.build();
		
		constraints.add(bcri);
		constraints.add(bcri1);
		constraints.add(bcri2);
		constraints.add(bcri3);
		
		return constraints;
	}

}
