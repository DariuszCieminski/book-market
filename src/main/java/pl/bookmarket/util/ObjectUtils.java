package pl.bookmarket.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Provides useful utility methods for various object-related work.
 */
public final class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * Checks value of each field of provided object against specified condition and returns array with field names,
     * which meet the condition.
     *
     * @param object         the target object. Cannot be <i>null</i>.
     * @param valueCondition a {@link Predicate} testing the value of each field of the object against specified condition.
     * @return {@link String} array containing all object field names meeting given condition or an empty array
     * if no matching fields were found.
     */
    public static String[] getFieldNamesByValueCondition(Object object, Predicate<Object> valueCondition) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);
        return Arrays.stream(beanWrapper.getPropertyDescriptors())
                     .map(PropertyDescriptor::getName)
                     .filter(propertyName -> valueCondition.test(beanWrapper.getPropertyValue(propertyName)))
                     .toArray(String[]::new);
    }
}