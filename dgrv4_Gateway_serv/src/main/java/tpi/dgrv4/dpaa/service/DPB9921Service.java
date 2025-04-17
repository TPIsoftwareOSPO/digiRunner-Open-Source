package tpi.dgrv4.dpaa.service;

import java.io.OutputStream;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9921Req;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9921Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpSettingDao tsmpSettingDao;

	@Autowired
	public DPB9921Service(TsmpSettingDao tsmpSettingDao) {
		super();
		this.tsmpSettingDao = tsmpSettingDao;
	}

	public void exportTsmpSetting(TsmpAuthorization tsmpAuthorization, DPB9921Req req, OutputStream outputStream) {
		exportTsmpSetting(outputStream);
	}

	/**
	 * In-Memory, <br> 
	 * Landing 端匯出資料,由此進入
	 */
	public void exportTsmpSetting(OutputStream outputStream) {
		try (XSSFWorkbook workbook = new XSSFWorkbook(); outputStream;) {

			XSSFSheet sheet = workbook.createSheet();

			// 抬頭
			this.writeHeader(workbook, sheet);

			// 資料
			this.writeData(sheet);

			sheet.setColumnWidth(0, 12800); // 50*256
			sheet.setColumnWidth(1, 20480); // 80*256
			sheet.setColumnWidth(2, 25600); // 100*256

			// 寫入串流
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private void writeHeader(XSSFWorkbook wb, XSSFSheet sheet) {

		// 粗體字
		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		// 置中
		cellStyle.setAlignment(HorizontalAlignment.CENTER);

		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("id");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(1);
		cell.setCellValue("value");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(2);
		cell.setCellValue("memo");
		cell.setCellStyle(cellStyle);
	}

	private void writeData(XSSFSheet sheet) {
		List<TsmpSetting> list = getTsmpSettingDao().findAllByOrderByIdAsc();
		if (CollectionUtils.isEmpty(list)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		int rowIndex = 1;
		for (TsmpSetting vo : list) {
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(0);
			cell.setCellValue(vo.getId());

			cell = row.createCell(1);
			cell.setCellValue(vo.getValue());

			cell = row.createCell(2);
			cell.setCellValue(vo.getMemo());

		}
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

}
