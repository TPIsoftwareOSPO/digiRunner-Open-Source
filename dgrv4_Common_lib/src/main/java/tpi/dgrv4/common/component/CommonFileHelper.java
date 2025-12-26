package tpi.dgrv4.common.component;

import java.io.File;
import java.util.regex.Matcher;

import org.springframework.stereotype.Component;

@Component
public class CommonFileHelper {
	
	/**
	 * 替換分隔符為系統預設
	 * @param path
	 * @param isTruncateLeadingSeperator
	 * @return
	 */
	public static final String filterPath(String path, boolean isTruncateLeadingSeperator) {
		if (path != null) {
			path = path.replaceAll("(\\\\+|/+)", Matcher.quoteReplacement(File.separator));
			if (isTruncateLeadingSeperator) {
				path = path.replaceFirst("^" + Matcher.quoteReplacement(File.separator), "");
			}
		}
		return path;
	}
	
}
