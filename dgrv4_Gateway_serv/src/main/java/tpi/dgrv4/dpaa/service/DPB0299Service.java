package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0299Resp;
import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.repository.DgrWebhookApiMapDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.poi.ss.usermodel.CellType.*;

@Service
public class DPB0299Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookNotifyDao dgrWebhookNotifyDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;


    @Transactional
    public DPB0299Resp importWebhook(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {
        DPB0299Resp resp = new DPB0299Resp();
        try {
            if (mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
                throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
            }
            String username = tsmpAuthorization.getUserName();
            checkParam(mFile.getOriginalFilename());
            importWebhook(mFile.getInputStream(), username);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return resp;
    }

    protected void importWebhook(InputStream inputStream, String username) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() != 2) {
                throw TsmpDpAaRtnCode._1291.throwing();
            }

            // Process first sheet (WEBHOOK_NOTIFY)
            Sheet notifySheet = workbook.getSheetAt(0);
            checkSheet1Header(notifySheet.getRow(0));
            // Process second sheet (WEBHOOK_NOTIFY_FIELD)
            Sheet fieldSheet = workbook.getSheetAt(1);
            checkSheet2Header(fieldSheet.getRow(0));
            // Process notify sheet
            Map<String, Long> notifyMap = processNotifySheet(notifySheet, username);

            // Process field sheet
            processFieldSheet(fieldSheet, notifyMap, username);
        }
    }

    protected void checkParam(String fileName) {
        int fileNameIndex = fileName.lastIndexOf(".");
        if (fileNameIndex == -1) {
            throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
        }

        String fileExtension = fileName.substring(fileNameIndex + 1);
        if (!"xlsx".equalsIgnoreCase(fileExtension)) {
            throw TsmpDpAaRtnCode._1443.throwing();
        }
    }

    private Map<String, Long> processNotifySheet(Sheet sheet, String username) {
        // 用於暫存 Notify 數據
        DgrWebhookNotify currentNotify = null;
        Map<String, Long> notifyMap = new HashMap<>();

        // 1. 先收集所有數據到 Map 中
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;


            String notifyName = getStringValue(row.getCell(0));
            if (StringUtils.hasText(notifyName)) {
                currentNotify = getDgrWebhookNotifyDao()
                        .findFirstByNotifyName(notifyName)
                        .orElseGet(DgrWebhookNotify::new);

                currentNotify.setNotifyName(notifyName);
                currentNotify.setNotifyType(getStringValue(row.getCell(1)));
                currentNotify.setEnable(getStringValue(row.getCell(2)));
                currentNotify.setMessage(getStringValue(row.getCell(3)));
                currentNotify.setPayloadFlag(getStringValue(row.getCell(4)));

                if (currentNotify.getWebhookNotifyId() == null) {
                    currentNotify.setCreateUser(username);
                    currentNotify.setCreateDateTime(DateTimeUtil.now());
                } else {
                    currentNotify.setUpdateUser(username);
                    currentNotify.setUpdateDateTime(DateTimeUtil.now());
                }
                currentNotify = getDgrWebhookNotifyDao().save(currentNotify);
                notifyMap.put(currentNotify.getNotifyName(),currentNotify.getWebhookNotifyId());
            }

        }

        return notifyMap;
    }

    protected void checkSheet1Header(Row row) {
        String[] sheet1Header = {
                "NOTIFY_NAME",
                "NOTIFY_TYPE",
                "ENABLE",
                "MESSAGE",
                "PAYLOAD_FLAG"
        };
        checkHeader(row, sheet1Header);
    }

    protected void checkSheet2Header(Row row) {
        String[] sheet2Header = {
                "NOTIFY_NAME",
                "FIELD_KEY",
                "FIELD_VALUE",
                "FIELD_TYPE",
                "MAPPING_URL"
        };
        checkHeader(row, sheet2Header);
    }

    private void checkHeader(Row row, String[] header) {
        if (row == null) {
            throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
        }
        int actualColumnCount = row.getLastCellNum(); // This returns the count, not the index
        if (actualColumnCount < header.length) {
            throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
        }

        for (int i = 0; i < header.length; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String cellValue = (cell != null && cell.getCellType() == STRING) ? cell.getStringCellValue() : "";

            if (!header[i].equals(cellValue)) {
                throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");


            }
        }
    }

    private void processFieldSheet(Sheet sheet, Map<String, Long> notifyMap, String username) {
        // Delete all existing fields for the notifies we're importing
        List<Long> notifyIds = new ArrayList<>(notifyMap.values());
        checkSheet2Header(sheet.getRow(0));
        getDgrWebhookNotifyFieldDao().deleteByWebhookNotifyIdIn(notifyIds);

        // Process each row in the field sheet
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String notifyName = getStringValue(row.getCell(0));

            // 如果 notifyName 為空，但其他欄位有值，則使用上一行的 notifyName
            if (!StringUtils.hasText(notifyName)) {
                // 檢查其他欄位是否至少有一個有值
                boolean hasOtherValues = false;
                for (int j = 1; j <= 4; j++) {
                    if (StringUtils.hasText(getStringValue(row.getCell(j)))) {
                        hasOtherValues = true;
                        break;
                    }
                }
                if (hasOtherValues && i > 1) {
                    // 使用上一行的 notifyName
                    Row prevRow = sheet.getRow(i - 1);
                    if (prevRow != null) {
                        notifyName = getStringValue(prevRow.getCell(0));
                    }
                }
                // 如果還是沒有 notifyName，則跳過這一行
                if (!StringUtils.hasText(notifyName)) {
                    continue;
                }
            }
            if (!notifyMap.containsKey(notifyName)) {
                TPILogger.tl.warn(String.format("Skipping row %d: notifyName '%s' not found in notifyMap", i, notifyName));
                continue;
            }
            long notifyId = notifyMap.get(notifyName);
            DgrWebhookNotifyField field = new DgrWebhookNotifyField();
            field.setWebhookNotifyId(notifyId);
            field.setFieldKey(getStringValue(row.getCell(1)));
            field.setFieldValue(getStringValue(row.getCell(2)));
            field.setFieldType(getStringValue(row.getCell(3)));
            field.setMappingUrl(getStringValue(row.getCell(4)));
            field.setCreateDateTime(DateTimeUtil.now());
            field.setCreateUser(username);

            getDgrWebhookNotifyFieldDao().save(field);
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null || cell.getCellType().equals(BLANK)) {
            return null;
        }
        // 強制將所有單元格視為字符串類型讀取
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell).trim();

        // 如果是空字符串，返回""
        return value.isEmpty() ? "" : value;

    }

}