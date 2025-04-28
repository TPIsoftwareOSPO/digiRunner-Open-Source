package tpi.dgrv4.dpaa.service;

import java.sql.PreparedStatement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface ISqlExecutor {
	String execUpdate(PreparedStatement preparedStatement, ObjectMapper objectMapper) throws JsonProcessingException;
}
