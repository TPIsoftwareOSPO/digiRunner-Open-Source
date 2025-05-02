package tpi.dgrv4.entity.component.fuzzy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;

/**
 * 注意:非 Singleton 類別
 */
public class FuzzyEntityListener {

	private static ITPILogger logger;

	private static Map<Class<?>, Set<Method>> fuzzyFieldSettersCache = new HashMap<>();

	private static Map<Class<?>, List<Field>> fuzzyValuesCache = new HashMap<>();

	private static AtomicBoolean isRefreshing = new AtomicBoolean(false);

	/**
	 * 初始化時機:<br/>Spring 啟動時，初始化標有 @TableGenerator 的 Entity，<br/>
	 * 若該 Entity 同時標有 @EntityListeners ，則會初始化此類別，<br/>
	 * 但多個 Entity 只會觸發一次此類別的建構子。
	 */
	@PostConstruct
	private void init() {
		// 掃描所有 Entity
		isRefreshing.set(true);
		synchronized (isRefreshing) {
			try {
				ClassLoader classLoader = FuzzyEntityListener.class.getClassLoader();
				resolvePackage("tpi.dgrv4.gateway.entity", classLoader);
			} finally {
				isRefreshing.set(false);
				isRefreshing.notifyAll();
			}
		}
	}

	@PrePersist
	@PreUpdate
	public final void fuzzyProcess(Object obj) {
		Class<?> clazz = obj.getClass();

		if (isRefreshing.get()) {
			synchronized (isRefreshing) {
				if (isRefreshing.get()) {
					try {
						isRefreshing.wait();
					} catch (InterruptedException e) {
						this.logger.error(StackTraceUtil.logStackTrace(e));
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		Set<Method> fuzzyFieldSetters = getFuzzyFieldSetters(clazz);
		List<Field> fuzzyValues = getFuzzyValues(clazz);

		if (!fuzzyFieldSetters.isEmpty() && !fuzzyValues.isEmpty()) {
			final String fuzzyData = combineFuzzyData(obj, fuzzyValues);

			for(Method fuzzyFieldSetter : fuzzyFieldSetters) {
				try {
					fuzzyFieldSetter.invoke(obj, fuzzyData);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					this.logger.error(String.format("Fail to call '%s': invoke error '%s'", fuzzyFieldSetter.getName(), StackTraceUtil.logStackTrace(e)));
				}
			}
		}
	}

	private Set<Method> getFuzzyFieldSetters(Class<?> clazz) {
		Set<Method> fuzzyFieldSetters = fuzzyFieldSettersCache.get(clazz);
		if (fuzzyFieldSetters == null) {
			if (isRefreshing.get()) {
				synchronized (isRefreshing) {
					if (isRefreshing.get()) {
						try {
							isRefreshing.wait();
						} catch (InterruptedException e) {
							this.logger.error(StackTraceUtil.logStackTrace(e));
							Thread.currentThread().interrupt();
						}
					}
				}
			}
			isRefreshing.set(true);
			synchronized (isRefreshing) {
				try {
					resolveClass(clazz);
				} finally {
					isRefreshing.set(false);
					isRefreshing.notifyAll();
				}
			}
			
			fuzzyFieldSetters = fuzzyFieldSettersCache.get(clazz);
		}
		return fuzzyFieldSetters;
	}

	private List<Field> getFuzzyValues(Class<?> clazz) {
		List<Field> fuzzyValues = fuzzyValuesCache.get(clazz);
		if (fuzzyValues == null) {
			isRefreshing.set(true);
			synchronized (isRefreshing) {
				try {
					resolveClass(clazz);
				} finally {
					isRefreshing.set(false);
					isRefreshing.notifyAll();
				}
			}

			fuzzyValues = fuzzyValuesCache.get(clazz);
		}
		return fuzzyValues;
	}

	private void resolvePackage(String basePackage, ClassLoader classLoader) {
		ClassPathScanningCandidateComponentProvider provider = //
			    new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

		Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
		Class<?> entityClass;
		for (BeanDefinition component : components) {
			entityClass = ClassUtils.resolveClassName(component.getBeanClassName(), classLoader);
			resolveClass(entityClass);
		}
	}

	private void resolveClass(Class<?> clazz) {
		if (clazz == null) {
			return;
		}

		this.logger.tl.debugDelay2sec("Resolving " + clazz + " for keyword searching...");

		fuzzyFieldSettersCache.remove(clazz);
		fuzzyValuesCache.remove(clazz);
		
		Set<Method> fuzzyFieldSetters = new HashSet<>();
		List<Field> fuzzyValues = new ArrayList<>();
		
		List<Field> allFields = getFieldsWithInherited(clazz);
		for(Field field : allFields) {
			if (field.isAnnotationPresent(FuzzyField.class)) {
				field.setAccessible(true);

				Method fuzzyFieldSetter;
				try {
					fuzzyFieldSetter = findFuzzyFieldSetter(clazz, field);
					fuzzyFieldSetters.add(fuzzyFieldSetter);
				} catch (NoSuchMethodException | SecurityException e) {
					this.logger.error(String.format("Fail to set fuzzy field '%s': No such method '%s'", field.getName(), StackTraceUtil.logStackTrace(e)));
				}
			} else if (field.isAnnotationPresent(Fuzzy.class)) {
				field.setAccessible(true);
				fuzzyValues.add(field);
			}
		}

		sortFuzzyValues(fuzzyValues);

		fuzzyFieldSettersCache.put(clazz, fuzzyFieldSetters);
		fuzzyValuesCache.put(clazz, fuzzyValues);
	}

	private void sortFuzzyValues(List<Field> fuzzyValues) {
		fuzzyValues.sort((field1, field2) -> {
			final int order1 = field1.getAnnotation(Fuzzy.class).order();
			final int order2 = field2.getAnnotation(Fuzzy.class).order();
			return order1 - order2;
		});
	}
	
	private Method findFuzzyFieldSetter(Class<?> clazz, Field field) throws NoSuchMethodException, SecurityException {
		final String fieldName = field.getName();
		final String fuzzySetterName = "set" //
				+ Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
		
		final Method fuzzySetter = clazz.getMethod(fuzzySetterName, String.class);
		return fuzzySetter;
	}

	private String combineFuzzyData(Object obj, List<Field> fuzzyValues) {
		String[] fuzzyDataArray = fuzzyValues.stream() //
		.map((field) -> {
			try {
				return (String) field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				this.logger.error(String.format("Fail to get fuzzy field '%s': %s", field.getName(), StackTraceUtil.logStackTrace(e)));
			}
			return null;
		}) //
		.filter(Objects::nonNull) //
		.toArray(String[]::new);
		
		return String.join("|", fuzzyDataArray);
	}

	private List<Field> getFieldsWithInherited(Class<?> clazz) {
		if (clazz == null) {
	        return Collections.emptyList();
	    }
		Class<?> superClass = clazz.getSuperclass();
		List<Field> superFields = new ArrayList<>(getFieldsWithInherited(superClass));
		List<Field> slefFields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
		superFields.addAll(slefFields);
	    return superFields;
	}

	public static void setLogger(ITPILogger logger) {
		FuzzyEntityListener.logger = logger;
	}
	
	
}
