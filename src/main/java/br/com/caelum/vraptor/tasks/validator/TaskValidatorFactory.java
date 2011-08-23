package br.com.caelum.vraptor.tasks.validator;

import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.validator.BeanValidator;
import br.com.caelum.vraptor.validator.NullBeanValidator;

@Component
@ApplicationScoped
public class TaskValidatorFactory {

	private final Container container;
	private BeanValidator beanValidator;
	private static final Logger logger = LoggerFactory.getLogger(TaskValidatorFactory.class);
	
	public TaskValidatorFactory(Container container) {
		this.container = container;
	}

	public Validator getInstance() {
		if(beanValidator == null)
			buildBeanValidator();
		return new TaskValidator(beanValidator);
	}
	
	public void buildBeanValidator(){
		if(isClassPresent("javax.validation.Validation")){
			ValidatorFactory factory = container.instanceFor(ValidatorFactory.class);
			beanValidator = new CustomJSR303Validator(factory.getValidator(), factory.getMessageInterpolator());
			logger.debug("Initializing JSR303 Validator");
		}
		else
			beanValidator = new NullBeanValidator();
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
