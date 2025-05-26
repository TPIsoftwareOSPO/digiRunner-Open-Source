package tpi.dgrv4.gateway.vo;

import java.util.LinkedList;

public class AllClientInfoData {
	
	private LinkedList<ClientKeeper> allClientList = new LinkedList<ClientKeeper>();
	private LinkedList<ComposerInfoData> allComposerList = new LinkedList<ComposerInfoData>();

	public LinkedList<ClientKeeper> getAllClientList() {
		return allClientList;
	}

	public void setAllClientList(LinkedList<ClientKeeper> allClientList) {
		this.allClientList = allClientList;
	}

	public LinkedList<ComposerInfoData> getAllComposerList() {
		return allComposerList;
	}

	public void setAllComposerList(LinkedList<ComposerInfoData> allComposerList) {
		this.allComposerList = allComposerList;
	}

}
