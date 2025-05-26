package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

@FunctionalInterface
public interface ScheduledEnableDisableFunc<TsmpApi> {

	void setTsmpApi(TsmpApi tsmpApi);
}
