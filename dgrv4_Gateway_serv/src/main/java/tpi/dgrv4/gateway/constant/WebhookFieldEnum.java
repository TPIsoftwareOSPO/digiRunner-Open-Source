package tpi.dgrv4.gateway.constant;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Optional;

@Getter
public enum WebhookFieldEnum {
	AUTHORIZATION("authorization"),
	TO("to"),
	NOTIFICATION_DISABLED("notificationDisabled"),
	SUBJECT("subject"),
	RECIPIENTS("recipients"),	
	URL("url"),
	USERNAME("username"),
	ICON_EMOJI("icon_emoji"),
	CHANNEL("channel"),
	AVATAR_URL("avatar_url"),
	PERCENT("percent"),
	;

	private final String code;

    WebhookFieldEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
