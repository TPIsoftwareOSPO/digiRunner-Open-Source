package tpi.dgrv4.dpaa.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0298Req;
import tpi.dgrv4.dpaa.vo.DPB0298Resp;
import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DPB0298Service {


    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookNotifyDao dgrWebhookNotifyDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(AccessLevel.PROTECTED)
    private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;


    public DPB0298Resp exportWebhook(DPB0298Req req, HttpServletResponse response) {
        DPB0298Resp resp = new DPB0298Resp();
        try {

            //find all wbData
            List<DgrWebhookNotify> notifies = getDgrWebhookNotifyDao().findAll();
            // if no data, throw.
            if (CollectionUtils.isEmpty(notifies)) {
                throw TsmpDpAaRtnCode._1298.throwing();
            }
            // map data by webhook_notify_id and use notify_name to be key
            Map<String, List<DgrWebhookNotifyField>> fieldsMap = getFieldMap(notifies);

            XSSFWorkbook workbook = new XSSFWorkbook();
            OutputStream outputStream = response.getOutputStream();
            XSSFSheet sheet = workbook.createSheet("WEBHOOK_NOTIFY");
            String[] sheet1Header = {
                    "NOTIFY_NAME",
                    "NOTIFY_TYPE",
                    "ENABLE",
                    "MESSAGE",
                    "PAYLOAD_FLAG"
            };
            writeHeader(workbook, sheet, sheet1Header);
            writeNotifySheet(sheet, notifies);

            XSSFSheet sheet2 = workbook.createSheet("WEBHOOK_NOTIFY_FIELD");
            String[] sheet2Header = {
                    "NOTIFY_NAME",
                    "FIELD_KEY",
                    "FIELD_VALUE",
                    "FIELD_TYPE",
                    "MAPPING_URL"
            };
            writeHeader(workbook, sheet2, sheet2Header);
            writeFieldSheet(sheet2, fieldsMap);


            doExport(response);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return resp;
    }

    private Map<String, List<DgrWebhookNotifyField>> getFieldMap(List<DgrWebhookNotify> notifies) {
        return notifies.stream()
                .collect(Collectors.toMap(
                        DgrWebhookNotify::getNotifyName,
                        n -> getDgrWebhookNotifyFieldDao().findByWebhookNotifyId(n.getWebhookNotifyId())
                ));
    }


    private void writeNotifySheet(XSSFSheet sheet, List<DgrWebhookNotify> notifyList) {
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        //置中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        //邊框
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        int rowNum = 1;

        for (DgrWebhookNotify notify : notifyList) {
            Row row = sheet.createRow(rowNum++);
            createNotifyRow(row, notify,  cellStyle);
        }
        sheet.setColumnWidth(0, 20 * 256); // 20*256
        sheet.setColumnWidth(1, 20 * 256); // 20*256
        sheet.setColumnWidth(2, 20 * 256); // 50*256
        sheet.setColumnWidth(3, 40 * 256);
        sheet.setColumnWidth(4, 20 * 256);

    }


    private void createNotifyRow(Row row, DgrWebhookNotify notify, CellStyle cellStyle) {
        int colNum = 0;

        CellStyle textCellStyle = row.getSheet().getWorkbook().createCellStyle();
        textCellStyle.cloneStyleFrom(cellStyle);
        textCellStyle.setDataFormat(row.getSheet().getWorkbook()
                .createDataFormat()
                .getFormat("@"));
        // 通知基本資訊
        createCell(row, colNum++, notify.getNotifyName(), textCellStyle);
        createCell(row, colNum++, notify.getNotifyType(), cellStyle);
        createCell(row, colNum++, notify.getEnable(), cellStyle);
        createCell(row, colNum++, notify.getMessage(), textCellStyle);
        createCell(row, colNum, notify.getPayloadFlag(), cellStyle);


    }

    private void createCell(Row row, int column, Object value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        if (value != null) {
            switch (value) {
                case String s -> cell.setCellValue(s);
                case Number number -> cell.setCellValue(number.intValue());
                default -> cell.setCellValue(value.toString());
            }
        }
        cell.setCellStyle(cellStyle);
    }

    private void writeFieldSheet(XSSFSheet sheet, Map<String, List<DgrWebhookNotifyField>> fieldsMap) {
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        //置中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        //邊框
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        int rowNum = 1;


        for (Map.Entry<String, List<DgrWebhookNotifyField>> entry : fieldsMap.entrySet()) {
            String notifyName = entry.getKey();
            List<DgrWebhookNotifyField> fields = entry.getValue();

            // 合併儲存格的起始行
            int firstRow = rowNum;

            // 寫入所有API映射
            for (DgrWebhookNotifyField field : fields) {
                Row row = sheet.createRow(rowNum++);
                createFieldsRow(row, notifyName, field, cellStyle);
            }


            int lastRow = rowNum - 1;
            if (lastRow > firstRow) {
                sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, 0, 0));
            }

        }
        sheet.setColumnWidth(0, 20 * 256); // 20*256
        sheet.setColumnWidth(1, 20 * 256); // 20*256
        sheet.setColumnWidth(2, 20 * 256); // 50*256
        sheet.setColumnWidth(3, 20 * 256);
        sheet.setColumnWidth(4, 40 * 256);

    }

    private void createFieldsRow(Row row, String notifyName, DgrWebhookNotifyField field, CellStyle cellStyle) {
        int colNum = 0;
        CellStyle textCellStyle = row.getSheet().getWorkbook().createCellStyle();
        textCellStyle.cloneStyleFrom(cellStyle);
        textCellStyle.setDataFormat(row.getSheet().getWorkbook()
                .createDataFormat()
                .getFormat("@"));

        createCell(row, colNum++, notifyName, textCellStyle);
        createCell(row, colNum++, field.getFieldKey(), textCellStyle);
        createCell(row, colNum++, field.getFieldValue(), textCellStyle);
        createCell(row, colNum++, field.getFieldType(), cellStyle);
        createCell(row, colNum, field.getMappingUrl(), textCellStyle);
    }


    private void writeHeader(XSSFWorkbook wb, XSSFSheet sheet, String[] headers) {

        // 粗體字
        XSSFCellStyle cellStyle = wb.createCellStyle();

        XSSFFont font = wb.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        // 置中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        Row row = sheet.createRow(0);

        int columnIndex = 0;
        for (String h : headers) {
            Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(h);
            cell.setCellStyle(cellStyle);
        }
    }

    private void doExport(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String nowDateTime = DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.匯出檔案格式).orElse(null);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Webhook_" + nowDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

    }
}
