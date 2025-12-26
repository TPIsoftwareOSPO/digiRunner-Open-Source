package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0014Resp {

	/** 名稱 */
	private String name;

	/** 實例分類Id */
	private Long refAppCateId;

	/** 實例分類名稱 */
	private String refAppCateName;

	/** 作品介紹 */
	private String intro;

	/** 作者 */
	private String author;

	/** 狀態=1：啟用，0：停用 （預設啟用） */
	private String dataStatus;

	/** db中使用哪些API:舊資料, <apiUid1,mail>,<apiUid2,travel>... */
	private List<Map<String, String>> orgUseApis;

	/** db中上傳圖檔icon:舊資料, base64(file) */
	private String orgIcon;

	/** db中上傳說明檔案:舊資料, <fileName,fileData>... */
	private List<Map<String, String>> orgIntroFiles;

	/** db中上傳圖檔icon file name:舊資料, fileName */
	private String orgIconFileName;

	public DPB0014Resp() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getRefAppCateId() {
		return refAppCateId;
	}

	public void setRefAppCateId(Long refAppCateId) {
		this.refAppCateId = refAppCateId;
	}

	public String getRefAppCateName() {
		return refAppCateName;
	}

	public void setRefAppCateName(String refAppCateName) {
		this.refAppCateName = refAppCateName;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public List<Map<String, String>> getOrgUseApis() {
		return orgUseApis;
	}

	public void setOrgUseApis(List<Map<String, String>> orgUseApis) {
		this.orgUseApis = orgUseApis;
	}

	public String getOrgIcon() {
		return orgIcon;
	}

	public void setOrgIcon(String orgIcon) {
		this.orgIcon = orgIcon;
	}

	public List<Map<String, String>> getOrgIntroFiles() {
		return orgIntroFiles;
	}

	public void setOrgIntroFiles(List<Map<String, String>> orgIntroFiles) {
		this.orgIntroFiles = orgIntroFiles;
	}

	public String getOrgIconFileName() {
		return orgIconFileName;
	}

	public void setOrgIconFileName(String orgIconFileName) {
		this.orgIconFileName = orgIconFileName;
	}
	
}
