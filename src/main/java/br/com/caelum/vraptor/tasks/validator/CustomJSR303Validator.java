package br.com.caelum.vraptor.tasks.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.validator.BeanValidator;
import br.com.caelum.vraptor.validator.Message;
import br.com.caelum.vraptor.validator.ValidationMessage;

public class CustomJSR303Validator implements BeanValidator {

	private static final Logger logger = LoggerFactory.getLogger(CustomJSR303Validator.class);

	private final Validator validator;

	private final MessageInterpolator interpolator;

	public CustomJSR303Validator(Validator validator, MessageInterpolator interpolator) {
		this.validator = validator;
		this.interpolator = interpolator;
		logger.debug("Initializing JSR303 Validator");
	}

	public List<Message> validate(Object bean) {
		if (bean == null) {
			logger.warn("skiping validation, input bean is null.");
			return Collections.emptyList(); // skip if the bean is null
		}

		final Set<ConstraintViolation<Object>> violations = validator.validate(bean);
		logger.debug("there are {} violations at bean {}.", violations.size(),bean);

		Locale locale = Locale.getDefault();

		List<Message> messages = new ArrayList<Message>();
		for (ConstraintViolation<Object> violation : violations) {
			// interpolate the message
			final Context ctx = new Context(violation.getConstraintDescriptor(), violation.getInvalidValue());
			String msg = interpolator.interpolate(violation.getMessageTemplate(), ctx, locale);

			messages.add(new ValidationMessage(msg, violation.getPropertyPath().toString()));
			logger.debug("added message {} to validation of bean {}", msg, violation.getRootBean());
		}

		return messages;
	}

	/**
	 * Create a personalized implementation for
	 * {@link javax.validation.MessageInterpolator.Context}. This class is need
	 * to interpolate the constraint violation message with localized messages.
	 * 
	 * @author Otavio Scherer Garcia
	 */
	class Context implements MessageInterpolator.Context {

		private final ConstraintDescriptor<?> descriptor;
		private final Object validatedValue;

		public Context(ConstraintDescriptor<?> descriptor, Object validatedValue) {
			this.descriptor = descriptor;
			this.validatedValue = validatedValue;
		}

		public ConstraintDescriptor<?> getConstraintDescriptor() {
			return descriptor;
		}

		public Object getValidatedValue() {
			return validatedValue;
		}
	}
}