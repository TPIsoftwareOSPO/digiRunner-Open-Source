package tpi.dgrv4.dpaa.component.req;

import java.util.List;

import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 入口網簽核作業(查詢)
 * 
 * @author Kim
 *
 */
public interface DpReqQueryIfs<T> {

	/** 申請單資料 */
	public DpReqQueryResp<T> doQuery(Long reqOrdermId, String locale);

	/** 寄發簽核通知 */
	public List<TsmpMailEvent> getTsmpMailEvents(TsmpAuthorization auth, Long reqOrdermId, String locale);

}
