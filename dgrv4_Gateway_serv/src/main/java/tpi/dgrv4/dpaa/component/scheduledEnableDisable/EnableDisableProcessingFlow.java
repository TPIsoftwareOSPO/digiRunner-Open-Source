package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class EnableDisableProcessingFlow {

	private PassDisableDetectApis passDisableDetectApis;
	private PassEnableDetectApis passEnableDetectApis;
	private RemoveAPIsImmediatelyDisable removeAPIsImmediatelyDisable;
	private RemoveAPIsImmediatelyEnalbe removeAPIsImmediatelyEnalbe;
	private RemoveIllegalAPIs1551 removeIllegalAPIs1551;
	private RemoveIllegalAPIs1552 removeIllegalAPIs1552;
	private RemoveIllegalAPIs1553 removeIllegalAPIs1553;
	private RemoveIllegalAPIs1554 removeIllegalAPIs1554;
	private RemoveIllegalAPIs1557 removeIllegalAPIs1557;
	private RemoveIllegalAPIs1558 removeIllegalAPIs1558;

	public ApiPublicFlagHandlerInterface getDisableProcessingFlow() {
		List<ApiPublicFlagHandlerInterface> steps = Arrays.asList( //
				removeAPIsImmediatelyDisable, //
				removeIllegalAPIs1552, //
				removeIllegalAPIs1554, //
				removeIllegalAPIs1558, //
				passDisableDetectApis);

		return getProcessingChain(steps);

	}

	public ApiPublicFlagHandlerInterface getEnableProcessingFlow() {
		List<ApiPublicFlagHandlerInterface> steps = Arrays.asList( //
				removeAPIsImmediatelyEnalbe, //
				removeIllegalAPIs1551, //
				removeIllegalAPIs1553, //
				removeIllegalAPIs1557, //
				passEnableDetectApis);

		return getProcessingChain(steps);
	}

	private ApiPublicFlagHandlerInterface getProcessingChain(List<ApiPublicFlagHandlerInterface> handlers) {
		for (int i = 0; i < handlers.size() - 1; i++) {
			handlers.get(i).setNext(handlers.get(i + 1));
		}
		return handlers.get(0);
	}

	public PassDisableDetectApis getPassDisableDetectApis() {
		return passDisableDetectApis;
	}

	public void setPassDisableDetectApis(PassDisableDetectApis passDisableDetectApis) {
		this.passDisableDetectApis = passDisableDetectApis;
	}

	public PassEnableDetectApis getPassEnableDetectApis() {
		return passEnableDetectApis;
	}

	public void setPassEnableDetectApis(PassEnableDetectApis passEnableDetectApis) {
		this.passEnableDetectApis = passEnableDetectApis;
	}

	public RemoveAPIsImmediatelyDisable getRemoveAPIsImmediatelyDisable() {
		return removeAPIsImmediatelyDisable;
	}

	public void setRemoveAPIsImmediatelyDisable(RemoveAPIsImmediatelyDisable removeAPIsImmediatelyDisable) {
		this.removeAPIsImmediatelyDisable = removeAPIsImmediatelyDisable;
	}

	public RemoveAPIsImmediatelyEnalbe getRemoveAPIsImmediatelyEnalbe() {
		return removeAPIsImmediatelyEnalbe;
	}

	public void setRemoveAPIsImmediatelyEnalbe(RemoveAPIsImmediatelyEnalbe removeAPIsImmediatelyEnalbe) {
		this.removeAPIsImmediatelyEnalbe = removeAPIsImmediatelyEnalbe;
	}

	public RemoveIllegalAPIs1551 getRemoveIllegalAPIs1551() {
		return removeIllegalAPIs1551;
	}

	public void setRemoveIllegalAPIs1551(RemoveIllegalAPIs1551 removeIllegalAPIs1551) {
		this.removeIllegalAPIs1551 = removeIllegalAPIs1551;
	}

	public RemoveIllegalAPIs1552 getRemoveIllegalAPIs1552() {
		return removeIllegalAPIs1552;
	}

	public void setRemoveIllegalAPIs1552(RemoveIllegalAPIs1552 removeIllegalAPIs1552) {
		this.removeIllegalAPIs1552 = removeIllegalAPIs1552;
	}

	public RemoveIllegalAPIs1553 getRemoveIllegalAPIs1553() {
		return removeIllegalAPIs1553;
	}

	public void setRemoveIllegalAPIs1553(RemoveIllegalAPIs1553 removeIllegalAPIs1553) {
		this.removeIllegalAPIs1553 = removeIllegalAPIs1553;
	}

	public RemoveIllegalAPIs1554 getRemoveIllegalAPIs1554() {
		return removeIllegalAPIs1554;
	}

	public void setRemoveIllegalAPIs1554(RemoveIllegalAPIs1554 removeIllegalAPIs1554) {
		this.removeIllegalAPIs1554 = removeIllegalAPIs1554;
	}

	public RemoveIllegalAPIs1557 getRemoveIllegalAPIs1557() {
		return removeIllegalAPIs1557;
	}

	public void setRemoveIllegalAPIs1557(RemoveIllegalAPIs1557 removeIllegalAPIs1557) {
		this.removeIllegalAPIs1557 = removeIllegalAPIs1557;
	}

	public RemoveIllegalAPIs1558 getRemoveIllegalAPIs1558() {
		return removeIllegalAPIs1558;
	}

	public void setRemoveIllegalAPIs1558(RemoveIllegalAPIs1558 removeIllegalAPIs1558) {
		this.removeIllegalAPIs1558 = removeIllegalAPIs1558;
	}

}
