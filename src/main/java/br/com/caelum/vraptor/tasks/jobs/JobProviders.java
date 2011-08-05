package br.com.caelum.vraptor.tasks.jobs;

import javax.persistence.EntityManagerFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Validator;

import org.hibernate.SessionFactory;
import org.quartz.Job;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.jobs.hibernate.HibernateJob;
import br.com.caelum.vraptor.tasks.jobs.hibernate.HibernateJobProvider;
import br.com.caelum.vraptor.tasks.jobs.jpa.JPAJob;
import br.com.caelum.vraptor.tasks.jobs.jpa.JPAJobProvider;
import br.com.caelum.vraptor.tasks.validator.CustomJSR303Validator;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;
import br.com.caelum.vraptor.validator.BeanValidator;
import br.com.caelum.vraptor.validator.NullBeanValidator;

@Component
@ApplicationScoped
public class JobProviders {
	
	private final Container container;

	public JobProviders(Container container) {
		this.container = container;
	}
	
	public JobProvider getProvider(Class<? extends Job> job){
		
		if(job.equals(HibernateJob.class))
			return new HibernateJobProvider(container.instanceFor(SessionFactory.class), getValidator());
		if(job.equals(JPAJob.class))
			return new JPAJobProvider(container.instanceFor(EntityManagerFactory.class), getValidator());
		
		return new DefaultJobProvider();
		
	}
	
	private TaskValidator getValidator(){
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
