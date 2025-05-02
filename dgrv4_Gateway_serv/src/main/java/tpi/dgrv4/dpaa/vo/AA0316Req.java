package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0316Req extends ReqValidator {

	/** API來源, R：註冊，C：組合 */
	private String apiSrc;

	/** 模組建立來源, 1=WSDL, 2=OAS2.0, 3=OAS3.0 */
	private String moduleSrc;

	/** 暫存的介接規格文件檔名, 由使用者執行步驟1(呼叫 DPB0082)取得暫存檔名後填入此欄位 */
	private String tempFileName;

	/** 模組名稱 */
	private String moduleName;

	/** 模組版本 */
	private String moduleVersion;

	/** 註冊主機(ID), TSMP_API_REG.reghost_id，若傳入空字串則後端轉為 null */
	private String regHostId;

	/** API註冊清單 */
	private List<AA0316Item> regApiList;
	
	private Integer type;
	
	private String targetUrl;

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiSrc")
				.isRequired()
				.pattern("^[R|C]$")
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("moduleSrc")
				.isRequired()
				.pattern("^[1|2|3]$")
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("moduleVersion")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("regHostId")
				.maxLength(10)
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildCollection(locale)
				.field("regApiList")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("targetUrl")
				.isRequired()
				.pattern("[^\\{\\}]+")
				.build()
		});
	}

	public String getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(String apiSrc) {
		this.apiSrc = apiSrc;
	}

	public String getModuleSrc() {
		return moduleSrc;
	}

	public void setModuleSrc(String moduleSrc) {
		this.moduleSrc = moduleSrc;
	}

	public String getTempFileName() {
		return tempFileName;
	}

	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getRegHostId() {
		return regHostId;
	}

	public void setRegHostId(String regHostId) {
		this.regHostId = regHostId;
	}

	public List<AA0316Item> getRegApiList() {
		return regApiList;
	}

	public void setRegApiList(List<AA0316Item> regApiList) {
		this.regApiList = regApiList;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	
	

}