package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9902Req extends ReqValidator {
	
	private String id;

	private String value;

	private String memo;
	private String encrptionType;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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
				.field("value") //
				.isRequired()
				.build(),new BeforeControllerRespItemBuilderSelector() //
                .buildString(locale) //
                .field("encrptionType") //
                .isRequired()
                .build()
		});
	}

    public String getEncrptionType() {
        return encrptionType;
    }

    public void setEncrptionType(String encrptionType) {
        this.encrptionType = encrptionType;
    }

}
