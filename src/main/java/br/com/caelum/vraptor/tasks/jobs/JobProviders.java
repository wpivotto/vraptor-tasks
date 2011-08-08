package br.com.caelum.vraptor.tasks.jobs;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.quartz.Job;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.ioc.PrototypeScoped;
import br.com.caelum.vraptor.tasks.jobs.hibernate.HibernateJob;
import br.com.caelum.vraptor.tasks.jobs.hibernate.HibernateJobProvider;
import br.com.caelum.vraptor.tasks.jobs.jpa.JPAJob;
import br.com.caelum.vraptor.tasks.jobs.jpa.JPAJobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;

@Component
@PrototypeScoped
public class JobProviders {
	
	private final Container container;
	private final TaskValidator validator;

	public JobProviders(Container container, TaskValidator validator) {
		this.container = container;
		this.validator = validator;
	}
	
	public JobProvider getProvider(Class<? extends Job> job){
		
		if(job.equals(HibernateJob.class))
			return new HibernateJobProvider(container.instanceFor(SessionFactory.class), validator);
		if(job.equals(JPAJob.class))
			return new JPAJobProvider(container.instanceFor(EntityManagerFactory.class), validator);
		
		return new DefaultJobProvider();
		
	}	

}
