package tpi.dgrv4.dpaa.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.ExportField;
import tpi.dgrv4.dpaa.constant.ExportTypeEnum;
import tpi.dgrv4.dpaa.record.AA0435SrcUrlAndPercentageRecord;
import tpi.dgrv4.dpaa.record.AA0435SrcUrlListRecord;
import tpi.dgrv4.dpaa.vo.AA0435Req;
import tpi.dgrv4.dpaa.vo.AA0435Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AA0435Service {
    private TPILogger logger = TPILogger.tl;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private TsmpApiDao tsmpApiDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private TsmpApiRegDao tsmpApiRegDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private TsmpOrganizationDao tsmpOrganizationDao;


    public AA0435Resp apiListExport(AA0435Req req, HttpServletResponse response) {
        AA0435Resp resp = new AA0435Resp();

            String typeStr = req.getType();
            if (!StringUtils.hasLength(typeStr)) {
                throw TsmpDpAaRtnCode._1350.throwing("type");
            }
            ExportTypeEnum type;
            try {
                type = ExportTypeEnum.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                throw TsmpDpAaRtnCode._1352.throwing("type");
            }

            if (CollectionUtils.isEmpty(req.getField())) {
                throw TsmpDpAaRtnCode._2009.throwing("1");
            }

            List<ExportField> exportFields = new ArrayList<>();
            for (String fieldName : req.getField()) {
                try {
                    ExportField field = ExportField.valueOf(fieldName);
                    exportFields.add(field);
                } catch (IllegalArgumentException e) {
                    throw TsmpDpAaRtnCode._1559.throwing("error field : " + fieldName);
                }
            }
        try {
            List<TsmpApiReg> apiRegList = getTsmpApiRegDao().findAll();

            if (CollectionUtils.isEmpty(apiRegList))
                throw TsmpDpAaRtnCode._1298.throwing();

            List<Map<String, Object>> data = hadelData(apiRegList, exportFields);


            switch (type) {
                case EXCEL:
                    writeExcelTable(exportFields, data, response);
                    break;
                case CSV:
                    break;
                case JSON:
                    break;
                case null:
                    throw TsmpDpAaRtnCode._1350.throwing("type");
                default:
                    throw TsmpDpAaRtnCode._1443.throwing();

            }

        } catch (
                TsmpDpAaException e) {
            throw e;
        } catch (
                Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            // 1297:執行錯誤
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        return resp;
    }


    private List<Map<String, Object>> hadelData(List<TsmpApiReg> apiRegList, List<ExportField> fields) {

        return apiRegList.stream().map(reg -> {
            TsmpApiId apiId = new TsmpApiId(reg.getApiKey(), reg.getModuleName());
            TsmpApi api = getTsmpApiDao().findById(apiId).orElse(null);

            Map<String, Object> rowData = new LinkedHashMap<>();

            for (ExportField field : fields) {
                Object value = extractFieldValue(field, api, reg);
                rowData.put(field.getCode(), value);
            }

            return rowData;
        }).collect(Collectors.toList());
    }

    private Object extractFieldValue(ExportField field, TsmpApi api, TsmpApiReg reg) {
        if (field == null) return null;

        try {
            switch (field) {
                case API_NAME:
                    return api.getApiName();
                case MODULE_NAME:
                    return api.getModuleName();
                case API_ID:
                    return api.getApiKey();
                case API_STATUS:
                    return api != null && "1".equals(api.getApiStatus()) ? "Enable" : "Disable";
                case API_SRC:
                    return api != null && "R".equals(api.getApiSrc()) ? "Register" : "Composer";
                case API_CACHE:
                    return getCache(api.getApiCacheFlag());
                case HTTP_METHODS:
                    return reg.getMethodOfJson();
                case NO_AUTH:
                    return reg != null && "1".equals(reg.getNoOauth()) ? "Y" : "N";
                case ORG_ID:
                    return getOrgName(api.getOrgId());
                case SRC_URL:
                    return getSrcUrl(reg);
                case API_DESC:
                    return api.getApiDesc();
                case JWT:
                    String reqFlag = jwejws("req", api.getJewFlag());
                    String respFlag = jwejws("resp", api.getJewFlagResp());
                    return Arrays.asList(reqFlag, respFlag);
                case LABEL:
                    return Arrays.asList(api.getLabel1(), api.getLabel2(), api.getLabel3(), api.getLabel4(), api.getLabel5());
                case ENABLE_SCHEDULED_DATE:
                    return api.getEnableScheduledDate();
                case DISABLE_SCHEDULED_DATE:
                    return api.getDisableScheduledDate();
                case CREATE_USER:
                    return api.getCreateUser();
                case UPDATE_USER:
                    return api.getUpdateUser();
                case CREATE_TIME:
                    return api.getCreateTime();
                case UPDATE_TIME:
                    return api.getUpdateTime();
                default:
                    throw TsmpDpAaRtnCode._1290.throwing();
            }
        } catch (Exception e) {
            logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    private String getCache(String apiCacheFlag) {
        return switch (apiCacheFlag) {
            case "1" -> "NONE";
            case "2" -> "AUTO";
            case "3" -> "FIXED";
            default -> throw TsmpDpAaRtnCode._1352.throwing("apiCacheFlag");
        };
    }

    private String getOrgName(String orgId) {
        TsmpOrganization org = getTsmpOrganizationDao().findById(orgId).orElse(null);
        return org != null ? org.getOrgName() : "";
    }

    private String jwejws(String field, String flag) {

        switch (flag) {
            case "0":
                return null;
            case "1":
                return field + ":JWE";
            case "2":
                return field + ":JWS";
            default:
                throw TsmpDpAaRtnCode._1352.throwing(field);


        }

    }

    private List<AA0435SrcUrlListRecord> getSrcUrl(TsmpApiReg reg) {
        List<AA0435SrcUrlListRecord> srcUrlList = new ArrayList<>();
        try {

            // 有分流
            if ("Y".equals(reg.getRedirectByIp())) {
                processIpUrlMapping(reg.getIpForRedirect1(), reg.getIpSrcUrl1()).ifPresent(srcUrlList::add);
                processIpUrlMapping(reg.getIpForRedirect2(), reg.getIpSrcUrl2()).ifPresent(srcUrlList::add);
                processIpUrlMapping(reg.getIpForRedirect3(), reg.getIpSrcUrl3()).ifPresent(srcUrlList::add);
                processIpUrlMapping(reg.getIpForRedirect4(), reg.getIpSrcUrl4()).ifPresent(srcUrlList::add);
                processIpUrlMapping(reg.getIpForRedirect5(), reg.getIpSrcUrl5()).ifPresent(srcUrlList::add);

            } else { // 無分流 將IP設為 ""
                List<AA0435SrcUrlAndPercentageRecord> urlPercentages = b64Decode(reg.getSrcUrl());
                srcUrlList.add(new AA0435SrcUrlListRecord("", urlPercentages));
            }
        } catch (Exception e) {
            throw TsmpDpAaRtnCode._1452.throwing("url decode", reg.getApiKey(), reg.getModuleName(), "url decode error .");
        }
        return srcUrlList;
    }

    private Optional<AA0435SrcUrlListRecord> processIpUrlMapping(String ip, String url) {
        if (StringUtils.hasLength(url)) {
            try {
                List<AA0435SrcUrlAndPercentageRecord> urlPercentages = b64Decode(url);
                return Optional.of(new AA0435SrcUrlListRecord(ip, urlPercentages));
            } catch (Exception e) {
                logger.error(StackTraceUtil.logStackTrace(e));
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private List<AA0435SrcUrlAndPercentageRecord> b64Decode(String url) {
        if (url == null) {
            return Collections.emptyList();
        }

        if (!url.startsWith("b64.")) {
            return List.of(new AA0435SrcUrlAndPercentageRecord("100", url));
        }

        String encodeString = url.substring(4); // 移除 "b64." 前綴
        String[] base64Parts = encodeString.split("\\.");
        List<AA0435SrcUrlAndPercentageRecord> result = new ArrayList<>();

        for (int i = 0; i < base64Parts.length - 1; i += 2) {
            String percentage = base64Parts[i];
            String encodedUrl = base64Parts[i + 1];
            String decodedUrl = new String(Base64Util.base64URLDecode(encodedUrl));
            result.add(new AA0435SrcUrlAndPercentageRecord(percentage, decodedUrl));
        }

        return result;
    }

    private void writeExcelTable(List<ExportField> fields, List<Map<String, Object>> data,
                                 HttpServletResponse response) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String nowDateTime = DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.匯出檔案格式).orElse(null);
            // 設置響應頭
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=APIList_" + nowDateTime + ".xlsx");

            // 創建工作表
            XSSFSheet sheet = workbook.createSheet("API List");

            // 創建表頭樣式
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);

            // 創建表頭
            createHeaderRow(sheet, headerStyle, fields);

            // 填充數據行
            int rowNum = 1;
            for (Map<String, Object> rowData : data) {
                createDataRow(sheet, rowNum++, fields, rowData);
            }

            // 自動調整列寬
            setColumnWidths(sheet);

            // 寫入響應
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
        } catch (IOException e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw e;
        }
    }

    // 創建表頭樣式
    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    // 創建表頭行
    private void createHeaderRow(XSSFSheet sheet, XSSFCellStyle headerStyle, List<ExportField> fields) {
        Row headerRow = sheet.createRow(0);
        int colNum = 0;
        for (ExportField field : fields) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(field.getCode());
            cell.setCellStyle(headerStyle);
        }
    }

    // 創建數據行
    private void createDataRow(XSSFSheet sheet, int rowNum,
                               List<ExportField> fields, Map<String, Object> rowData) {
        Row row = sheet.createRow(rowNum);
        int colNum = 0;
        CellStyle dateStyle = getDateStyle(sheet);
        CellStyle strStyle = getStyleStyle(sheet);


        for (ExportField field : fields) {
            Cell cell = row.createCell(colNum++);
            Object value = rowData.get(field.getCode());
            setCellValue(cell, value, dateStyle, strStyle);
        }
    }

    // 設置單元格值
    private void setCellValue(Cell cell, Object value, CellStyle dateStyle, CellStyle strStyle) {
        if (value == null) {
            cell.setCellStyle(strStyle);
            cell.setCellValue("");
            return;
        }
        if (value instanceof String) {
            cell.setCellStyle(strStyle);
            cell.setCellValue(String.valueOf(value));
        } else if (value instanceof Long timestamp) {
            if (timestamp > 0) {
                cell.setCellValue(new Date(timestamp));
                cell.setCellStyle(dateStyle);
            } else {
                cell.setCellStyle(strStyle);
                cell.setCellValue("");
            }
        } else if (value instanceof Date d) {
            cell.setCellValue(d);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof List) {
            // 處理列表類型的值，例如 SRC_URL 返回的列表
            cell.setCellValue(formatListValue((List<?>) value));
            cell.setCellStyle(strStyle);
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(strStyle);
        }
    }

    private CellStyle getStyleStyle(Sheet sheet) {
        // 創建日期格式
        CellStyle styleStyle = sheet.getWorkbook().createCellStyle();
        styleStyle.setWrapText(true);
        return styleStyle;
    }

    private CellStyle getDateStyle(Sheet sheet) {
        // 創建日期格式
        CreationHelper creationHelper = sheet.getWorkbook().getCreationHelper();
        CellStyle dateCellStyle = sheet.getWorkbook().createCellStyle();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss.SSS"));
        dateCellStyle.setWrapText(true);
        return dateCellStyle;
    }

    // 格式化列表類型的值
    private String formatListValue(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        // 如果是 AA0435SrcUrlListItem 列表
        if (!CollectionUtils.isEmpty(list) && list.get(0) instanceof AA0435SrcUrlListRecord) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(item -> (AA0435SrcUrlListRecord) item)
                    .map(item -> {
                        // 如果有 IP 才顯示 IP 行
                        String ipLine = item.ip() != null && !item.ip().isEmpty() ?
                                "IP: " + item.ip() + "\n" : "";

                        // 處理 URLs 部分
                        String urlsPart = item.srcUrlAndPercentageList().stream()
                                .filter(Objects::nonNull)
                                .map(url -> String.format("    %s%% : %s",
                                        url.percentage(),
                                        url.url()))
                                .collect(Collectors.joining("\n"));

                        return ipLine + urlsPart;
                    })
                    .collect(Collectors.joining("\n\n"));  // 不同 IP 組之間空一行
        }
        // 其他類型的列表
        return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    // 自動調整列寬
    private void setColumnWidths(XSSFSheet sheet) {
        // 獲取表頭行
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return;

        // 設定每字元的寬度（單位：256）
        int charWidth = 256;

        // 設定特定欄位的固定寬度（單位：字元）
        Map<String, Integer> specialColumns = new HashMap<>();
        specialColumns.put("API_NAME", 30);
        specialColumns.put("ORG_ID", 20);
        specialColumns.put("MODULE_NAME", 30);
        specialColumns.put("SRC_URL", 80);
        specialColumns.put("API_DESC", 50);
        specialColumns.put("JWT", 20);
        specialColumns.put("LABEL", 30);
        specialColumns.put("ENABLE_SCHEDULED_DATE", 30);
        specialColumns.put("DISABLE_SCHEDULED_DATE", 30);
        specialColumns.put("UPDATE_TIME", 30);
        specialColumns.put("CREATE_TIME", 30);
        specialColumns.put("HTTP_METHODS", 43);

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String header = cell.getStringCellValue().trim();
            String headerUpper = header.toUpperCase();
            int width;  // 預設寬度

            // 檢查是否為特殊欄位
            if (specialColumns.containsKey(headerUpper)) {
                width = specialColumns.get(headerUpper);
            } else {
                // 計算自動寬度
                int length = header.chars()
                        .map(c -> (c > 255) ? 2 : 1)
                        .sum();
                width = Math.min(Math.max(length + 2, 10), 30);
            }

            // 設定列寬
            sheet.setColumnWidth(i, width * charWidth);
        }


    }


}
