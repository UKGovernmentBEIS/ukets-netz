package uk.gov.netz.api.common.validation.uniqueelements;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UniqueElementsUtilsTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void uniqueElementsUtilsValidateSet_classWithFieldsThatHaveUniqueFields_validateRecursively() throws IllegalAccessException, NoSuchAlgorithmException {

        UniqueFieldModel uf1 = UniqueFieldModel.builder().model("model1").manufacturer("man1").location("loc1").city("c1").build();
        UniqueFieldModel uf2 = UniqueFieldModel.builder().model("model2").manufacturer("man2").location("loc1").city("c1").build();

        UniqueFieldTestingClass uft1 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf1)
                .subtype("subtype1")
                .build();

        UniqueFieldTestingClass uft2 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf2)
                .subtype("subtype2")
                .build();

        UniqueElementsUtilsValidateSetResult result = UniqueElementsUtils.validateSet(Set.of(uft1, uft2));

        assertThat(result.getResult()).isFalse();
    }


    @Test
    void uniqueElementsUtilsEqual_objectsAreEqual_returnTrueAndNoViolatedFields() throws IllegalAccessException {
        UniqueFieldModel uf1 = UniqueFieldModel.builder().model("model1").manufacturer("man1").build();

        UniqueFieldTestingClass uft1 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf1)
                .subtype("subtype1")
                .build();

        UniqueFieldTestingClass uft2 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf1)
                .subtype("subtype2")
                .build();

        UniqueElementsUtilsEqualResult result = UniqueElementsUtils.equal(uft1,uft2);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getViolatedFields()).isEmpty();
    }

    @Test
    void uniqueElementsUtilsEqual_objectsAreNotEqual_returnFalseAndViolatedFields() throws IllegalAccessException {
        UniqueFieldModel uf1 = UniqueFieldModel.builder().model("model1").manufacturer("man1").location("loc1").build();
        UniqueFieldModel uf2 = UniqueFieldModel.builder().model("model2").manufacturer("man2").location("loc2").build();

        UniqueFieldTestingClass uft1 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf1)
                .subtype("subtype1")
                .build();

        UniqueFieldTestingClass uft2 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf2)
                .subtype("subtype2")
                .build();

        UniqueElementsUtilsEqualResult result = UniqueElementsUtils.equal(uft1,uft2);

        assertThat(result.getResult()).isFalse();
        assertThat(result.getViolatedFields()).containsExactly("location");
    }

    @Test
    void uniqueElementsValidator_withNullInput_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, ()-> validator.validate(null));
    }

    @Test
    void uniqueElementsValidator_withNullIterable_shouldReturnNoViolation() {
       UniqueElementsTestingClass ue = UniqueElementsTestingClass.builder().uniqueElementsSet(null).build();
       Set<ConstraintViolation<UniqueElementsTestingClass>> violations = validator.validate(ue);
       assertThat(violations.stream().filter(v->
            v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().equals("UniqueElements")
        ).toList()).hasSize(0);
    }

    @Test
    void uniqueElementsValidator_withEmptyIterable_shouldReturnNoViolation() {
       UniqueElementsTestingClass ue = UniqueElementsTestingClass.builder().uniqueElementsSet(Set.of()).build();
       Set<ConstraintViolation<UniqueElementsTestingClass>> violations = validator.validate(ue);
       assertThat(violations.stream().filter(v->
            v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().equals("UniqueElements")
        ).toList()).hasSize(0);
    }

    @Test
    public void uniqueElementsValidator_noViolations_shouldReturnNoViolation() {
        UniqueFieldModel uf1 = UniqueFieldModel.builder().model("model1").manufacturer("man1").location("loc1").build();
        UniqueFieldModel uf2 = UniqueFieldModel.builder().model("model2").manufacturer("man2").location("loc2").build();

        UniqueFieldTestingClass uft1 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf1)
                .subtype("subtype1")
                .build();

        UniqueFieldTestingClass uft2 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf2)
                .subtype("subtype2")
                .build();

        UniqueElementsTestingClass ue = UniqueElementsTestingClass
                .builder()
                .uniqueElementsSet(Set.of(uft1,uft2))
                .build();

        Set<ConstraintViolation<UniqueElementsTestingClass>> violations = validator.validate(ue);
        assertThat(violations.stream().filter(v->
            v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().equals("UniqueElements")
        ).toList()).hasSize(0);
    }

    @Test
    public void uniqueElementsValidator_withViolations_shouldReturnViolation() {

        UniqueFieldModel uf = UniqueFieldModel.builder().model("model1").manufacturer("man1").build();


        UniqueFieldTestingClass uft1 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf)
                .subtype("subtype1")
                .build();

        UniqueFieldTestingClass uft2 = UniqueFieldTestingClass
                .builder()
                .uniqueField(uf)
                .subtype("subtype2")
                .build();

        UniqueElementsTestingClass ue = UniqueElementsTestingClass
                .builder()
                .uniqueElementsSet(Set.of(uft1,uft2))
                .build();

        Set<ConstraintViolation<UniqueElementsTestingClass>> violations = validator.validate(ue);
        assertThat(violations.stream().filter(v->
            v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().equals("UniqueElements")
        ).toList()).hasSize(1);
    }
}
