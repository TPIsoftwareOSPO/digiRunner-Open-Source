package tpi.dgrv4.dpaa.constant;

public enum DpaaAlertDetectorJobCommand {
	UPDATE,
	DELETE
	;

	public static DpaaAlertDetectorJobCommand resolve(String command) {
		for (DpaaAlertDetectorJobCommand cmd : values()) {
			if (cmd.toString().equals(command)) {
				return cmd;
			}
		}
		return null;
	}

}
