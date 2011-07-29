package br.com.caelum.vraptor.tasks.jobs;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.quartz.Job;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.jobs.hibernate.HibernateJobProvider;
import br.com.caelum.vraptor.tasks.jobs.jpa.JPAJobProvider;

@Component
@ApplicationScoped
public class JobProviders {
	
	private final Container container;

	public JobProviders(Container container) {
		this.container = container;
	}
	
	public JobProvider getProvider(Class<? extends Job> job){
		
		if(job.equals(br.com.caelum.vraptor.tasks.jobs.hibernate.HibernateJob.class))
			return new HibernateJobProvider(container.instanceFor(SessionFactory.class));
		if(job.equals(br.com.caelum.vraptor.tasks.jobs.jpa.JPAJob.class))
			return new JPAJobProvider(container.instanceFor(EntityManagerFactory.class));
		
		return new DefaultJobProvider();
		
	}

	
	

}
