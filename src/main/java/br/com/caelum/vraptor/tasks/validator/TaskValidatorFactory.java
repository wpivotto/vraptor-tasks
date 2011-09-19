package br.com.caelum.vraptor.tasks.validator;

import javax.validation.ValidatorFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.validator.BeanValidator;
import br.com.caelum.vraptor.validator.NullBeanValidator;
import br.com.caelum.vraptor.validator.ValidatorFactoryCreator;

@Component
@ApplicationScoped
public class TaskValidatorFactory {

	public Validator getInstance() {
		return new TaskValidator(buildBeanValidator());
	}
	
	public BeanValidator buildBeanValidator(){
		if(isClassPresent("javax.validation.Validation")){
			ValidatorFactory factory = new ValidatorFactoryCreator().getInstance();
			return new CustomJSR303Validator(factory.getValidator(), factory.getMessageInterpolator());
		}
		else
			return new NullBeanValidator();
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
