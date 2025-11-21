package uk.gov.netz.api.common.validation.uniqueelements;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE_USE, FIELD})
@Retention(RUNTIME)
@Documented
public @interface UniqueField {

}
