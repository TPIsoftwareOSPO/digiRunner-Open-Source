package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9903Req extends ReqValidator {

	private String id;

	private String oldVal;

	private String newVal;

	private String memo;
    private String encrptionType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOldVal() {
		return oldVal;
	}

	public void setOldVal(String oldVal) {
		this.oldVal = oldVal;
	}

	public String getNewVal() {
		return newVal;
	}

	public void setNewVal(String newVal) {
		this.newVal = newVal;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

    public String getEncrptionType() {
        return encrptionType;
    }

    public void setEncrptionType(String encrptionType) {
        this.encrptionType = encrptionType;
    }
    
    @Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale) //
				.field("id") //
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale) //
				.field("newVal") //
				.isRequired()
				.build()
		});
	}
}
