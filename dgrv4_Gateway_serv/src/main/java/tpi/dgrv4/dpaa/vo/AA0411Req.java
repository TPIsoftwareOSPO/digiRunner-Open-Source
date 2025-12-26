package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0411Req extends ReqValidator {
 
	/** 部署容器代碼 TSMP_DC.dc_code */
	private String dcCode;

	/** 啟用此容器的節點名稱 TSMP_NODE.node */
	private List<String> nodeList;

	/** 部署容器備註 TSMP_DC.dc_memo */
	private String dcMemo;
	
	@Override
	public String toString() {
		return "AA0411Req [dcCode=" + dcCode + ", nodeList=" + nodeList + ", dcMemo=" + dcMemo + "]";
	}

	public String getDcCode() {
		return dcCode;
	}

	public void setDcCode(String dcCode) {
		this.dcCode = dcCode;
	}

	public List<String> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<String> nodeList) {
		this.nodeList = nodeList;
	}

	public String getDcMemo() {
		return dcMemo;
	}

	public void setDcMemo(String dcMemo) {
		this.dcMemo = dcMemo;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("dcCode")
					.isRequired()
					.minLength(2)
					.maxLength(30)
					.pattern("^[\\w|\\-|\\.]+$", TsmpDpAaRtnCode._2011.getCode(), null)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("dcMemo")
					.maxLength(300)
					.build()
		});
	}
}
