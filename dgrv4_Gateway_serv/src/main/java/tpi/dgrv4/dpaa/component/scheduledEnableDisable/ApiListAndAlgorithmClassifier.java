package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpApi;

public class ApiListAndAlgorithmClassifier {

	private Map<String, List<TsmpApi>> apiListMap = new HashMap<>();
	private ScheduledEnableDisableFunc<TsmpApi> algorithm;

	public ScheduledEnableDisableFunc<TsmpApi> getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(ScheduledEnableDisableFunc<TsmpApi> algorithm) {
		this.algorithm = algorithm;
	}

	public Map<String, List<TsmpApi>> getApiListMap() {
		return apiListMap;
	}

}
