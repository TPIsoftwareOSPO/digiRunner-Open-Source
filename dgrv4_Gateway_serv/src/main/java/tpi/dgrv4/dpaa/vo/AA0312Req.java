package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0312Req extends ReqValidator {

	/** 來源 URL, 要測試的 URL */
	private String testURL;

	/** Http Method, ex: 'POST', 'GET', 'PUT' */
	private String method;

	/** 請求表頭, List<Map<鍵, 值>> */
	private List<Map<String, String>> headerList;

	/** 請求表身(表單), List<Map<鍵, 值>> */
	private List<Map<String, String>> paramList;

	/** 請求表身(純文字), submit a string of Json or Xml */
	private String bodyText;

	public String getTestURL() {
		return testURL;
	}

	public void setTestURL(String testURL) {
		this.testURL = testURL;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<Map<String, String>> getHeaderList() {
		return headerList;
	}

	public void setHeaderList(List<Map<String, String>> headerList) {
		this.headerList = headerList;
	}

	public List<Map<String, String>> getParamList() {
		return paramList;
	}

	public void setParamList(List<Map<String, String>> paramList) {
		this.paramList = paramList;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("testURL")
				.isRequired()
				.maxLength(255)
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("method")
				.isRequired()
				.maxLength(10)
				.build()
		});
	}

}