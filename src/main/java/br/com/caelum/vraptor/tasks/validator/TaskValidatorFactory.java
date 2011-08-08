package br.com.caelum.vraptor.tasks.validator;

import javax.validation.MessageInterpolator;
import javax.validation.Validator;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.ComponentFactory;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.validator.BeanValidator;
import br.com.caelum.vraptor.validator.NullBeanValidator;

@Component
@ApplicationScoped
public class TaskValidatorFactory implements ComponentFactory<TaskValidator> {

	private final Container container;
	
	public TaskValidatorFactory(Container container) {
		this.container = container;
	}

	public TaskValidator getInstance() {
		BeanValidator validator = null;
		if(isClassPresent("javax.validation.Validation"))
			validator = new CustomJSR303Validator(container.instanceFor(Validator.class), container.instanceFor(MessageInterpolator.class));
		else
			validator = new NullBeanValidator();
		return new TaskValidator(validator);
	}
	
	private boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
