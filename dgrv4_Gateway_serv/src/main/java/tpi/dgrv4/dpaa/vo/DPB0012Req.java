package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0012Req {

	/** 名稱 */
	private String name;

	/** 實例分類Id */
	private Long refAppCateId;

	/** 使用哪些API:apiUid.apiUid.....etc */
	private List<String> useApis;

	/** 上傳圖檔icon:file to base64, <fileName,fileData> */
	private Map<String,String> icon;

	/** 作品介紹 */
	private String intro;

	/** 作者 */
	private String author;

	/** 上傳說明檔案:file to base64, <fileName,fileData> */
	private List<Map<String,String>> introFiles;

	/** 狀態=1：啟用，0：停用 (預設啟用)*/
	private String dataStatus;

	public DPB0012Req() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRefAppCateId(Long refAppCateId) {
		this.refAppCateId = refAppCateId;
	}

	public List<String> getUseApis() {
		return useApis;
	}

	public void setUseApis(List<String> useApis) {
		this.useApis = useApis;
	}

	public Map<String, String> getIcon() {
		return icon;
	}
	
	public Long getRefAppCateId() {
		return refAppCateId;
	}

	public void setIcon(Map<String, String> icon) {
		this.icon = icon;
	}

	public String getIntro() {
		return intro;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<Map<String, String>> getIntroFiles() {
		return introFiles;
	}

	public void setIntroFiles(List<Map<String, String>> introFiles) {
		this.introFiles = introFiles;
	}
	
	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
}
