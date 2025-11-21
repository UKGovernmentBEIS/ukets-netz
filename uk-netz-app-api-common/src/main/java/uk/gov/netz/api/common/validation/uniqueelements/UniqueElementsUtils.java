package uk.gov.netz.api.common.validation.uniqueelements;

import org.springframework.data.util.Pair;
import org.springframework.util.ObjectUtils;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class UniqueElementsUtils {

    public static <T> boolean contains(List<T> list, T o) throws IllegalAccessException {

        for (T element : list) {
            UniqueElementsUtilsEqualResult result = equal(element,o);
            if (result.getResult()) {
                return true;
            }
        }

        return false;
    }

    public static UniqueElementsUtilsValidateSetResult validateSet(Set<?> elements) throws IllegalAccessException, NoSuchAlgorithmException {

        List<Pair<Integer,Integer>> violatedFields = new ArrayList<>();

        if (ObjectUtils.isEmpty(elements)) {
            return UniqueElementsUtilsValidateSetResult.builder().result(true).violatedIndices(violatedFields).build();
        }

        if (!elements.iterator().hasNext()) {
            return UniqueElementsUtilsValidateSetResult.builder().result(true).violatedIndices(violatedFields).build();
        }

        HashMap<String, Integer> occurrences = new HashMap<>();

        for (Object element : elements.toArray()) {
            String identifier = getUniqueFieldsIdentifier(element);

            if (ObjectUtils.isEmpty(occurrences.get(identifier))) {
                occurrences.put(identifier, 1);
            } else {
                occurrences.put(identifier, occurrences.get(identifier) + 1);
            }
        }

        Boolean result = occurrences.values().stream().allMatch(v -> v == 1);

        return UniqueElementsUtilsValidateSetResult.builder().result(result).build();
    }

    public static UniqueElementsUtilsEqualResult equal(Object o1, Object o2) throws IllegalAccessException {
        return equal(o1, o2, new ArrayList<>());
    }

    private static UniqueElementsUtilsEqualResult equal(Object o1, Object o2, List<String> violatedFields)
            throws IllegalAccessException {

        boolean result = true;

        if (ObjectUtils.isEmpty(o1)) {
            if (!ObjectUtils.isEmpty(o2)) {
                return UniqueElementsUtilsEqualResult.builder().result(false).violatedFields(violatedFields).build();
            }
        }

        if (ObjectUtils.isEmpty(o2)) {
            if (!ObjectUtils.isEmpty(o1)) {
                return UniqueElementsUtilsEqualResult.builder().result(false).violatedFields(violatedFields).build();
            }
        }

        Class<?> classz = o1.getClass();

        if (!o2.getClass().equals(classz)) {
            result = false;
        }

        List<Field> uniqueFields = getUniqueFields(classz);

        for (Field field : uniqueFields) {
            field.setAccessible(true);
            Object value1 = field.get(o1);
            Object value2 = field.get(o2);

            if (ObjectUtils.isEmpty(value1)) {
                if (!ObjectUtils.isEmpty(value2)) {
                    result = false;
                    violatedFields.add(field.getName());
                }
            } else {
                Class<?> nestedClass = field.getType();
                List<Field> nestedFields = getUniqueFields(nestedClass);
                if (!nestedFields.isEmpty()) {
                    boolean nestedEqualResult = equal(value1, value2,violatedFields).getResult();
                    if (!nestedEqualResult) {
                        result =  false;
                    }
                } else {
                    if (!value1.equals(value2)) {
                        result = false;
                        violatedFields.add(field.getName());
                    }
                }
            }
        }

        return UniqueElementsUtilsEqualResult.builder().result(result).violatedFields(violatedFields).build();
    }

    private static String getUniqueFieldsIdentifier(Object element) throws IllegalAccessException {

        if (element == null) {
            return "";
        }

        Class<?> clazz = element.getClass();
        List<Field> uniqueFields = getUniqueFields(clazz);

        StringBuilder identifierBuilder = new StringBuilder();

        if (uniqueFields.isEmpty()) {
            identifierBuilder.append(element);
        }

        for (Field field : uniqueFields) {
            field.setAccessible(true);
            Object value = field.get(element);

            identifierBuilder.append(field.getName()).append(getUniqueFieldsIdentifier(value));
        }


        return  identifierBuilder.toString();
    }

    private static List<Field> getUniqueFields(Class<?> classz) {

        Class<?> currentClass = classz;

        List<Field> fields = new ArrayList<>(Arrays.stream(currentClass.getDeclaredFields())
                .filter(field -> Arrays.stream(field.getAnnotations())
                        .anyMatch(annotation -> annotation instanceof UniqueField))
                .toList());

        while (!ObjectUtils.isEmpty(currentClass.getSuperclass())) {
            currentClass = currentClass.getSuperclass();
            fields.addAll(Arrays.stream(currentClass.getDeclaredFields())
                    .filter(field -> Arrays.stream(field.getAnnotations())
                            .anyMatch(annotation -> annotation instanceof UniqueField))
                    .toList());
        }

        return fields;
    }

}
