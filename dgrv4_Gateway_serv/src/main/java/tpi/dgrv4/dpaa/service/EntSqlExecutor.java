package tpi.dgrv4.enterprise;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.dpaa.service.ISqlExecutor;

public class EntSqlExecutor implements ISqlExecutor{

	@Override
	public String execUpdate(PreparedStatement preparedStatement, ObjectMapper objectMapper) throws JsonProcessingException {
    	int updatedRows = 0; //實作內容請自行斟酌
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("updatedRows", updatedRows);
        return objectMapper.writeValueAsString(dataMap);
	}

}
