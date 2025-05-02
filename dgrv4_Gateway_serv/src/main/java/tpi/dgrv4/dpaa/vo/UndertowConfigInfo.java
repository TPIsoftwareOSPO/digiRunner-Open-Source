package tpi.dgrv4.dpaa.vo;

import org.springframework.stereotype.Component;

@Component
public class UndertowConfigInfo {
    private static String undertowInfo = "";

    public static void setUndertowInfo(String info) {
        undertowInfo = info;
    }

    public static String getUndertowInfo() {
        return undertowInfo;
    }
}
