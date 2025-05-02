package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.ArrayList;
import java.util.List;

import tpi.dgrv4.entity.entity.TsmpApi;

public class ApiListAndAlgorithm {

	private List<TsmpApi> ls = new ArrayList<>();

	public void addTsmpApi(TsmpApi api) {
		if (api != null) {
			ls.add(api);
		}
	}

	public List<TsmpApi> getLs() {
		return ls;
	}
}
