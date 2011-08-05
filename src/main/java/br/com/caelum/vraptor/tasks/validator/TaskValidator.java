package br.com.caelum.vraptor.tasks.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.validator.BeanValidator;
import br.com.caelum.vraptor.validator.Message;
import br.com.caelum.vraptor.validator.Validations;

public class TaskValidator {

    private static final Logger logger = LoggerFactory.getLogger(TaskValidator.class);

	private final List<Message> errors = new ArrayList<Message>();

	private final BeanValidator beanValidator;

    public TaskValidator(BeanValidator beanValidator) {
		this.beanValidator = beanValidator;
    }

    public void checking(Validations validations) {
        addAll(validations.getErrors());
    }

    public void validate(Object object) {
        if (beanValidator == null)
            logger.warn("has no validators registered");
        else 
            addAll(beanValidator.validate(object));
    }

    public void addAll(Collection<? extends Message> messages) {
		for (Message message : messages) {
			add(message);
		}
	}

    public void add(Message message) {
    	this.errors.add(message);
    }

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public List<Message> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	
}