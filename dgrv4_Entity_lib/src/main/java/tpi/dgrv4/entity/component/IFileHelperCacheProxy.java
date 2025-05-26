package tpi.dgrv4.entity.component;

public interface IFileHelperCacheProxy {

	byte[] downloadByPathAndName(String tsmpDpFilePath, String filename);

}
