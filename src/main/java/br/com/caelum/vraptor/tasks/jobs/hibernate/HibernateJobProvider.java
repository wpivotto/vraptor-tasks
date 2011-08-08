package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.SessionFactory;
import org.quartz.Job;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.ioc.PrototypeScoped;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;

@Component
@PrototypeScoped
public class HibernateJobProvider implements JobProvider {

	private final Container container;
	private final TaskValidator validator;

	public HibernateJobProvider(Container container, TaskValidator validator) {
		this.container = container;
		this.validator = validator;
	}

	public Job newJob(Task task) {
		SessionFactory factory = container.instanceFor(SessionFactory.class);
		return new HibernateJob((TransactionalTask) task, validator, factory.openSession());
	}
	
	public boolean canProvide(Class<? extends Job> job) {
		return HibernateJob.class.equals(job);
	}

	public boolean canDecorate(Class<? extends Task> task) {
		return TransactionalTask.class.isAssignableFrom(task);
	}

	public Class<? extends Job> getJobWrapper() {
		return HibernateJob.class;
	}

}
