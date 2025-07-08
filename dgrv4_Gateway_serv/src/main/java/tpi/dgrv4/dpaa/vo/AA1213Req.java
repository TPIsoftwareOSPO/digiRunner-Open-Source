package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

@Getter
@Setter
public class AA1213Req extends ReqValidator{
	private Integer queryItem;
	private Integer abnormalElapsedTime;
	
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("queryItem")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("abnormalElapsedTime")
					.isRequired()
					.max(864000000)
					.min(-864000000)
					.build(),
				
			});
	}
}
