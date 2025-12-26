package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

@Getter
@Setter
public class AA0549Req extends ReqValidator{

	private String newMima;
	private String oriMima;

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("newMima")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("oriMima")
				.isRequired()
				.build(),
			});
	}

}