package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB9919Req {
	private List<DPB9919Item> fileList;
	private String newIsTmpfile ;

	public String getNewIsTmpfile() {
		return newIsTmpfile;
	}

	public void setNewIsTmpfile(String newIsTmpfile) {
		this.newIsTmpfile = newIsTmpfile;
	}

	public List<DPB9919Item> getFileList() {
		return fileList;
	}

	public void setFileList(List<DPB9919Item> fileList) {
		this.fileList = fileList;
	}

}
