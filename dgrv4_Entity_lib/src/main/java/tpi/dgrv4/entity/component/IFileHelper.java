package tpi.dgrv4.entity.component;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.entity.entity.TsmpDpFile;

public interface IFileHelper {

	byte[] downloadByPathAndName(String tsmpDpFilePath, String filename) throws SQLException;
	
	byte[] download(TsmpDpFile dpfile) throws SQLException;

	TsmpDpFile upload(String userName, TsmpDpFileType fileType, Long refId//
			, String filename, byte[] content, String isTmpfile) throws SerialException, SQLException;

	static String getTsmpDpFilePath(TsmpDpFileType fileType, Long refId) {
		return Paths.get(fileType.value(), String.valueOf(refId)).toString() + File.separator;
	}
}
