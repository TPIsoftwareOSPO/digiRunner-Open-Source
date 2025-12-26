package tpi.dgrv4.dpaa.component.req;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;

/**
 * 純vo, 用來放簽核流程中方便使用的變數
 * 
 * @author Kim
 *
 */
public class DpReqServiceSignVo<R extends DpReqServiceResp> {

	/** 申請單主檔 */
	private TsmpDpReqOrderm m;

	/**
	 * 本次請求所要簽核的關卡, 在簽核前就會寫入此欄位<br>
	 * ex: 此申請單目前是第一關待審, 則本次請求簽核時, currentS = 第一關
	 */
	private TsmpDpReqOrders currentS;

	/**
	 * 本次請求簽核後的下一關卡, 如果沒有下一關則為 null, 在簽核後才會寫入此欄位<br>
	 * ex: 此申請單目前是第一關待審, 則本次簽核後, nextChkPoint = 第二關<br>
	 * ex: 此申請單目前是第一關退回, 則本次簽核後, nextChkPoint = null
	 */
	private TsmpDpReqOrders nextChkPoint;

	private R resp;

	/** for tsmp_dp_appt_job.identif_data */
	private String indentifData = new String();

	public DpReqServiceSignVo() {
	}

	public TsmpDpReqOrderm getM() {
		return m;
	}

	public void setM(TsmpDpReqOrderm m) {
		this.m = m;
	}

	public TsmpDpReqOrders getCurrentS() {
		return currentS;
	}

	public void setCurrentS(TsmpDpReqOrders currentS) {
		this.currentS = currentS;
	}

	public TsmpDpReqOrders getNextChkPoint() {
		return nextChkPoint;
	}

	public void setNextChkPoint(TsmpDpReqOrders nextChkPoint) {
		this.nextChkPoint = nextChkPoint;
	}

	public R getResp() {
		return resp;
	}

	public void setResp(R resp) {
		this.resp = resp;
	}

	public String getIndentifData() {
		return indentifData;
	}

	public void setIndentifData(String indentifData) {
		this.indentifData = indentifData;
	}

}
