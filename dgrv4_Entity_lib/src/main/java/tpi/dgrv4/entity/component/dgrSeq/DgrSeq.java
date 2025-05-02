package tpi.dgrv4.entity.component.dgrSeq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DgrSeq {

	RandomLongTypeEnum strategy() default RandomLongTypeEnum.YYYYMMDD;

}
