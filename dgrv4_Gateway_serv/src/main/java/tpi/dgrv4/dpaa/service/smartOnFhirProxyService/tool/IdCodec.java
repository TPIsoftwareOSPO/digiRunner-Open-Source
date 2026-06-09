package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.gateway.keeper.TPILogger;

public final class IdCodec {

    private IdCodec() {
    }

    /**
     * 將字串解析為 Long
     * 
     * @param value     要解析的字串
     * @param fieldName 欄位名稱（用於錯誤訊息）
     * @param logger    日誌記錄器（可為 null）
     * @return Result<Long> - 成功、空值或失敗
     */
    public static Result<Long> parseLong(String value, String fieldName, TPILogger logger) {

        try {

            return Result.success(Long.parseLong(value));

        } catch (NumberFormatException e) {
            String msg = "Invalid " + fieldName + " format: '" + value
                    + "'. Expected numeric string representing a valid data.";

            if (logger != null) {
                logger.error(msg);
            }

            return Result.failure(msg, TsmpDpAaRtnCode._1559.toException(msg));
        }
    }

    /**
     * 將字串解析為 Long
     * 
     * @param value 要解析的字串
     * @return Result<Long> - 成功、空值或失敗
     */
    public static Result<Long> parseLong(String value) {

        try {

            return Result.success(Long.parseLong(value));
        } catch (NumberFormatException e) {
            String msg = "Invalid number format: '" + value
                    + "'. Expected numeric string representing a valid data.";
            return Result.failure(msg, new NumberFormatException(msg));
        }
    }

    /**
     * 將 Long 轉換為字串
     * 
     * @param id Long 值（可為 null）
     * @return 字串表示，null 則回傳 null
     */
    public static String toString(Long id) {
        return id != null ? String.valueOf(id) : null;
    }
}
