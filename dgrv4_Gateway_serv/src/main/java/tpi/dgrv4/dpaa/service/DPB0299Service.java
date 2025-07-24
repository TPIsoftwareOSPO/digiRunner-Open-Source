package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.STRING;

@Service
public class DPB0299Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookNotifyDao dgrWebhookNotifyDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookApiMapDao dgrWebhookApiMapDao;

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

            // Process first sheet (WEBHOOK_NOTIFY_AND_MAP)
            Sheet notifySheet = workbook.getSheetAt(0);

            // Process second sheet (WEBHOOK_NOTIFY_FIELD)
            Sheet fieldSheet = workbook.getSheetAt(1);

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
        Map<DgrWebhookNotify, List<DgrWebhookApiMap>> notifyDataMap = new HashMap<>();
        // 用於記錄合併區域
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        String currentMergedNotifyName = null;
        DgrWebhookNotify currentNotify = null;
        Map<String, Long> notifyMap = new HashMap<>();

        // 1. 先收集所有數據到 Map 中
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // 檢查當前行是否在合併區域中
            int rowIndex = i;
            boolean isMerged = mergedRegions.stream()
                    .anyMatch(region -> region.isInRange(rowIndex, 0));

            String notifyName = getStringValue(row.getCell(0));
            if (StringUtils.hasText(notifyName) || !isMerged) {
                currentMergedNotifyName = StringUtils.hasText(notifyName) ? notifyName : null;

                if (currentMergedNotifyName != null) {
                    currentNotify = getDgrWebhookNotifyDao()
                            .findFirstByNotifyName(currentMergedNotifyName)
                            .orElseGet(DgrWebhookNotify::new);

                    currentNotify.setNotifyName(currentMergedNotifyName);
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

                    notifyDataMap.putIfAbsent(currentNotify, new ArrayList<>());
                }
            }

            if (currentNotify != null && currentMergedNotifyName != null) {
                String apiKey = getStringValue(row.getCell(5));
                String moduleName = getStringValue(row.getCell(6));

                if (StringUtils.hasText(apiKey) && StringUtils.hasText(moduleName)) {
                    DgrWebhookApiMap apiMap = new DgrWebhookApiMap();
                    apiMap.setApiKey(apiKey);
                    apiMap.setModuleName(moduleName);
                    apiMap.setCreateUser(username);
                    apiMap.setCreateDateTime(DateTimeUtil.now());

                    // 添加 API 映射到對應的 Notify
                    notifyDataMap.get(currentNotify).add(apiMap);
                }
            }
        }

        for (Map.Entry<DgrWebhookNotify, List<DgrWebhookApiMap>> entry : notifyDataMap.entrySet()) {
            DgrWebhookNotify notify = entry.getKey();
            List<DgrWebhookApiMap> apiMaps = entry.getValue();

            DgrWebhookNotify savedNotify = getDgrWebhookNotifyDao().save(notify);
            notifyMap.put(savedNotify.getNotifyName(), savedNotify.getWebhookNotifyId());

            if (savedNotify.getWebhookNotifyId() != null) {
                getDgrWebhookApiMapDao().deleteByWebhookNotifyId(savedNotify.getWebhookNotifyId());
            }

            for (DgrWebhookApiMap apiMap : apiMaps) {
                apiMap.setWebhookNotifyId(savedNotify.getWebhookNotifyId());
                getDgrWebhookApiMapDao().save(apiMap);
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
                "PAYLOAD_FLAG",
                "API_KEY",
                "MODULE_NAME"
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
            throw TsmpDpAaRtnCode._1559.throwing("Header row is missing");
        }
        int actualColumnCount = row.getLastCellNum(); // This returns the count, not the index
        if (actualColumnCount < header.length) {
            throw TsmpDpAaRtnCode._1559.throwing(
                    String.format("Invalid header format. Expected %d columns but found %d. Please check the file template.",
                            header.length,
                            actualColumnCount)
            );
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
            if (!StringUtils.hasText(notifyName) || !notifyMap.containsKey(notifyName)) {
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
        if (cell.getCellType().equals(STRING))
            return cell.getStringCellValue().trim();
        else
            return String.valueOf(cell.getNumericCellValue());

    }

}