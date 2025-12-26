package tpi.dgrv4.dpaa.component.alert;

public class DpaaAlertEvent extends DpaaAlertDetectResult {

	private String alertType;

	public DpaaAlertEvent() {}

	public DpaaAlertEvent(DpaaAlertDetectResult dpaaAlertDetectResult) {
		setAlert(dpaaAlertDetectResult.isAlert());
		setEntity(dpaaAlertDetectResult.getEntity());
		setPayload(dpaaAlertDetectResult.getPayload());
	}

	/* [instance] getter/setter */

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	@Override
	public String toString() {
		return "DpaaAlertEvent [alertType=" + alertType + ", isAlert()=" + isAlert() + ", getEntity()=" + getEntity()
				+ ", getPayload()=" + getPayload() + "]";
	}

}