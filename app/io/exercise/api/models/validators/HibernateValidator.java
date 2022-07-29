package io.exercise.api.models.validators;

import io.jsonwebtoken.lang.Strings;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by agonlohaj on 31 Aug, 2020
 */
public class HibernateValidator {

	public static <T> String validate(T t) {
		Set<ConstraintViolation<T>> errors = HibernateValidator.apply(t);
		if (errors.size() == 0) {
			return "";
		}
		return HibernateValidator.formatErrors(errors);
	}

	private static <T> Set<ConstraintViolation<T>> apply(T t) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		return validator.validate(t);
	}

	private static <T> String formatErrors (Set<ConstraintViolation<T>> errors) {
		return errors.stream()
				.map((error) -> String.format("%s %s", Strings.capitalize(error.getPropertyPath().toString()), error.getMessage()))
				.collect(Collectors.joining(", "));
	}
}
