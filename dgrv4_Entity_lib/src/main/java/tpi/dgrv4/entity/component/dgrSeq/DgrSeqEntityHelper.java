package tpi.dgrv4.entity.component.dgrSeq;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;

public final class DgrSeqEntityHelper {

	private static final Map<Class<?>, Field> dgrSeqFieldCache;

	private static final Map<Class<?>, Method> dgrSeqFieldSettersCache;

	private static final Map<Class<?>, Method> dgrSeqFieldGettersCache;

	private static final Logger logger = LoggerFactory.getLogger(DgrSeqEntityHelper.class);

	static {
		dgrSeqFieldCache = new ConcurrentHashMap<>();
		dgrSeqFieldSettersCache = new ConcurrentHashMap<>();
		dgrSeqFieldGettersCache = new ConcurrentHashMap<>();
	}

	public static RandomLongTypeEnum getStrategy(Class<?> clazz) {
		Field dgrSeqField = dgrSeqFieldCache.get(clazz);

		if (dgrSeqField == null) {
			analyzeEntityType(clazz);
			dgrSeqField = dgrSeqFieldCache.get(clazz);
		}

		if (dgrSeqField == null) {
			return null;
		}

		DgrSeq dgrSeq = dgrSeqField.getAnnotation(DgrSeq.class);
		return dgrSeq.strategy();
	}

	/**
	 * @param obj
	 * @param setAnyway true to set DgrSeq anyway
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static DgrSeqAssignResult assignDgrSeq(Object obj, boolean setAnyway) {
		if (obj == null) {
			throw new NullPointerException("Entity must not be null!");
		}

		Class<?> clazz = obj.getClass();

		if (!(dgrSeqFieldSettersCache.containsKey(clazz) && dgrSeqFieldGettersCache.containsKey(clazz))) {
			analyzeEntityType(clazz);
		}

		DgrSeqAssignResult result = new DgrSeqAssignResult<>(obj);
		
		Long dgrSeq = getExistingDgrSeq(obj, clazz);
		if (!(dgrSeq == null || dgrSeq == -1L)) {
			result.setHasDgrSeqAlready(true);
			result.setDgrSeq(dgrSeq);
		}
		
		if (result.hasDgrSeqAlready() && !setAnyway) return result;

		Method dgrSeqFieldSetter = dgrSeqFieldSettersCache.get(clazz);
		if (dgrSeqFieldSetter == null) {
			throw new NullPointerException("Setter of primary key field is not found.");
		}

		try {
			dgrSeq = genDgrSeq(clazz);
			dgrSeqFieldSetter.invoke(obj, dgrSeq);

			result.setDgrSeq(dgrSeq);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error(String.format("Fail to call '%s': invoke error '%s'", dgrSeqFieldSetter.getName(),
					StackTraceUtil.logStackTrace(e)));
		}
		
		return result;
	}

	/** 分析 Entity Class 中標註 {@link DgrSeq} 的欄位 */
	private static void analyzeEntityType(Class<?> entityType) {
		List<Field> allFields = getHierarchyFields(entityType);
		Optional<Field> opt = allFields.stream() //
			.filter((f) -> f.isAnnotationPresent(DgrSeq.class)).findFirst();
		if (opt.isEmpty()) {
			dgrSeqFieldCache.put(entityType, null);
			dgrSeqFieldSettersCache.put(entityType, null);
			dgrSeqFieldGettersCache.put(entityType, null);
		} else {
			Field field = opt.get();
			field.setAccessible(true);
			// Find setter/getter
			Method setter = null;
			Method getter = null;
			try {
				setter = getSetterByField(entityType, field);
				getter = getGetterByField(entityType, field);
			} catch (NoSuchMethodException | SecurityException e) {
				logger.error(String.format("Fail to find setter/getter of DgrSeq field '%s': No such method '%s'",
						field.getName(), StackTraceUtil.logStackTrace(e)));
			}
			dgrSeqFieldCache.put(entityType, field);
			dgrSeqFieldSettersCache.put(entityType, setter);
			dgrSeqFieldGettersCache.put(entityType, getter);
		}
	}

	private static List<Field> getHierarchyFields(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		Class<?> superClass = clazz.getSuperclass();
		List<Field> superFields = new ArrayList<>(getHierarchyFields(superClass));
		List<Field> slefFields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
		superFields.addAll(slefFields);
		return superFields;
	}

	private static Method getSetterByField(Class<?> clazz, Field field) throws NoSuchMethodException, SecurityException {
		return getMethod(0, clazz, field);
	}

	private static Method getGetterByField(Class<?> clazz, Field field) throws NoSuchMethodException, SecurityException {
		return getMethod(1, clazz, field);
	}

	private static Method getMethod(int type, Class<?> clazz, Field field) throws NoSuchMethodException, SecurityException {
		final String fieldName = field.getName();
		
		Method method = null;
		if (type == 0) {
			final String methodName = "set" //
				+ Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
			method = clazz.getMethod(methodName, Long.class);
		} else {
			final String methodName = "get" //
				+ Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
			method = clazz.getMethod(methodName);
		}
		return method;
	}

	private static Long getExistingDgrSeq(Object obj, Class<?> clazz) {
		Method dgrSeqFieldGetter = dgrSeqFieldGettersCache.get(clazz);
		if (dgrSeqFieldGetter != null) {
			try {
				Long dgrSeq = (Long) dgrSeqFieldGetter.invoke(obj);
				return dgrSeq;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error(String.format("Fail to call '%s': invoke error '%s'", dgrSeqFieldGetter.getName(),
					StackTraceUtil.logStackTrace(e)));
			}
		}
		// If getter is missing, then don't set DgrSeq
		return -1L;
	}

	private static long genDgrSeq(Class<?> clazz) {
		RandomLongTypeEnum strategy = getStrategy(clazz);
		if (strategy != null) {
			switch(strategy) {
				case YYMMDD:
					return RandomSeqLongUtil.getRandomLongByYYMMDD();
				case YYYYMMDD:
					return RandomSeqLongUtil.getRandomLongByYYYYMMDD();
				case YYYYMMDDHH:
					return RandomSeqLongUtil.getRandomLongByYYYYMMDDHH();
				case YYYYMMDDHHMM:
					return RandomSeqLongUtil.getRandomLongByYYYYMMDDHHMM();
				case YYYYMMDDHHMMSS:
					return RandomSeqLongUtil.getRandomLongByYYYYMMDDHHMMSS();
			}
		}
		return Long.MIN_VALUE;
	}

}
