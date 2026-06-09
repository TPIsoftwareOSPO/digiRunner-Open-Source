package tpi.dgrv4.entity.constant;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CodeEnums {

    private CodeEnums() {
    }

    // enumClass -> (code -> enumConstant)
    private static final Map<Class<?>, Map<Object, Enum<?>>> CACHE = new ConcurrentHashMap<>();

    private static <E extends Enum<E> & CodeEnum<C>, C> Map<Object, Enum<?>> index(Class<E> enumClass) {
        return CACHE.computeIfAbsent(enumClass, cls -> Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(
                        e -> ((CodeEnum<?>) e).getCode(),
                        Function.identity(),
                        (a, b) -> {
                            throw new IllegalStateException("Duplicate code in " + enumClass.getName()
                                    + ": " + ((CodeEnum<?>) a).getCode());
                        },
                        LinkedHashMap::new)));
    }

    /** 驗證 code 是否存在於 enum 的 code 列表中 */
    public static <E extends Enum<E> & CodeEnum<C>, C> boolean isValidCode(Class<E> enumClass, C code) {
        if (code == null)
            return false;
        return index(enumClass).containsKey(code);
    }

    /** code -> enum；找不到回 Optional.empty() */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E> & CodeEnum<C>, C> Optional<E> tryFromCode(Class<E> enumClass, C code) {
        if (code == null)
            return Optional.empty();
        Enum<?> e = index(enumClass).get(code);
        return Optional.ofNullable((E) e);
    }

    /** code -> enum；找不到直接丟 IllegalArgumentException（常用於資料不該髒的場景） */
    public static <E extends Enum<E> & CodeEnum<C>, C> E fromCode(Class<E> enumClass, C code) {
        return tryFromCode(enumClass, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown code [" + code + "] for enum " + enumClass.getSimpleName()));
    }
}
