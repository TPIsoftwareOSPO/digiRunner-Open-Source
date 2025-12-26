package tpi.dgrv4.gateway.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonNodeUtil {

	public static String getNodeAsText(JsonNode node, String fieldName) {
		return getNodeAsValue(node, fieldName, (n) -> {
			return n.asText();
		});
	}

	public static Long getNodeAsLong(JsonNode node, String fieldName) {
		return getNodeAsValue(node, fieldName, (n) -> {
			return n.asLong();
		});
	}

	public static int getNodeAsInt(JsonNode node, String fieldName, int defaultVal) {
		JsonNode n = node.get(fieldName);
		if (n == null || n.isNull()) {
			return defaultVal;
		}
		return n.asInt();
	}

	public static ArrayNode getNodeAsArrayNode(JsonNode node, String fieldName) {
		return getNodeAsValue(node, fieldName, (n) -> {
			return (ArrayNode) n;
		});
	}

	public static <R> R getNodeAsValue(JsonNode node, String fieldName, Function<JsonNode, R> func) {
		if (node == null || node.isNull()) {
			return null;
		}
		JsonNode n = node.get(fieldName);
		if (n == null || n.isNull()) {
			return null;
		}
		return func.apply(n);
	}
	
	/**
	 * 將 JsonNode Array 的值,轉成 ArrayList
	 * ["aaa", "bbb"]
	 */
	public static List<String> convertJsonArrayToList(JsonNode jsonArray) {
		if(jsonArray == null || !jsonArray.isArray()) {
			return null;
		}
		
		List<String> list = new ArrayList<>();
		for (final JsonNode objNode : jsonArray) {
			String text = objNode != null ? objNode.asText() : "";
			list.add(text);
		}
		return list;
	}
	
	/**
	 * 將 JsonNode Array 的值,轉成 String
	 * 
	 * @param jsonArray
	 * @param splitChar 用來分隔每筆資料的文字, ex. " "
	 * @return  "aa bb cc"
	 */
	public static String convertJsonArrayToString(JsonNode jsonArray, String splitChar) {
		if(jsonArray == null || !jsonArray.isArray()) {
			return null;
		}
		
		String str = "";
		for (final JsonNode objNode : jsonArray) {
			String text = objNode != null ? objNode.asText() : "";
			if(StringUtils.hasLength(str)) {
				str += splitChar;
			}
			if(StringUtils.hasLength(text)) {
				str += text;
			}
		}
		return str;
	}
}
