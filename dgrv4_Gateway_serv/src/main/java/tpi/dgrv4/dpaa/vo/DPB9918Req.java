package tpi.dgrv4.dpaa.vo;

import java.util.ArrayList;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9918Req extends ReqValidator{
	
	private Long fileId;
	private String refFileCateCode;
	private Long refId;
	private String fileName;
	private String isBlob;
	private String tmpFileName;
	private Long version;

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIsBlob() {
		return isBlob;
	}

	public void setIsBlob(String isBlob) {
		this.isBlob = isBlob;
	}

	public String getTmpFileName() {
		return tmpFileName;
	}

	public void setTmpFileName(String tmpFileName) {
		this.tmpFileName = tmpFileName;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
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
				.field("isBlob")
				.pattern("^[YN]$")
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
