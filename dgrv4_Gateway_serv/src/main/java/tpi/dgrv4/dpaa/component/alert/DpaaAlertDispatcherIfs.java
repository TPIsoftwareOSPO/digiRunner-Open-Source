package tpi.dgrv4.dpaa.component.alert;

import tpi.dgrv4.entity.entity.jpql.TsmpAlert;

public interface DpaaAlertDispatcherIfs {

	/** 在偵測排程中, 加入一個新的告警項目 */
	public void joinAlert(TsmpAlert tsmpAlert);

	/** 從偵測排程中移除一個告警項目 */
	public void separateAlert(Long alertId);

	/** 更新在偵測排程中執行的告警項目 */
	public void updateAlert(TsmpAlert tsmpAlert);

	/** 刷新告警項目, 使項目實際被執行 */
	public void syncAlert();

}
