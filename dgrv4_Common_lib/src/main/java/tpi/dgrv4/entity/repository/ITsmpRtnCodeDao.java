package tpi.dgrv4.entity.repository;

import java.util.Optional;

import tpi.dgrv4.entity.entity.ITsmpRtnCode;


public interface ITsmpRtnCodeDao {

	public Optional<ITsmpRtnCode> findByTsmpRtnCodeAndLocale(String tsmpRtnCode, String locale);
	
}
