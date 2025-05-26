package tpi.dgrv4.dpaa.component.req;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.gateway.util.InnerInvokeParam;

/**
 * 入口網簽核作業
 * 
 * @author Kim
 */
public interface DpReqServiceIfs {

	/** 儲存 */
	public <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> R saveDraft(Q req, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException;

	/** 刪除草稿 */
	public void deleteDraft(final Long reqOrdermId) throws TsmpDpAaException;

	/** 更新 */
	public <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> R update(Q req, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException;

	/** 是否可更新 */
	public boolean isUpdatable(Long reqOrdermId);

	/** 送審 */
	public <R extends DpReqServiceResp> R submit(DpReqServiceUpdateReq req, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException;

	/** 是否可送審 */
	public boolean isSubmittable(Long reqOrdermId);

	/** 重送 */
	public <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> R resubmit(Q req, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException;

	/** 是否可重送 */
	public boolean isResubmittable(Long reqOrdermId);

	/** 簽核: 同意/不同意/退回/結案 */
	public <R extends DpReqServiceResp> R doSign(DpReqServiceSignReq q, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException;

	/** 是否可 同意/不同意/退回
	 * <b>注意</b>: 此方法並未檢查操作者是否有資格審核此單
	 * */
	public boolean isSignable(Long reqOrdermId);

	/** 是否可結案<br>
	 * <b>注意</b>: 此方法並未檢查審核者是否有資格將此單結案
	 * */
	public boolean isEndable(Long reqOrdermId);
	
	/** 刪除過期 */
	public void deleteExpired(TsmpDpReqOrderm tsmpDpReqOrderm) throws TsmpDpAaException;

}