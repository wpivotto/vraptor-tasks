package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.SessionFactory;
import org.quartz.Job;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.tasks.validator.TaskValidatorFactory;

@Component
@ApplicationScoped
public class HibernateJobProvider implements JobProvider {

	private final Container container;
	private final TaskValidatorFactory validatorFactory;

	public HibernateJobProvider(Container container, TaskValidatorFactory validatorFactory) {
		this.container = container;
		this.validatorFactory = validatorFactory;
	}

	public Job newJob(Task task, Scheduled options) {
		SessionFactory factory = container.instanceFor(SessionFactory.class);
		TaskLogic logic = new TaskLogic((TransactionalTask) task, validatorFactory.getInstance(), factory.openSession());
		
		if(options == null)
			return new ConcurrentHibernateJob(logic);
		
		return options.concurrent() ? new ConcurrentHibernateJob(logic) : new StatefulHibernateJob(logic);
	}
	
	public boolean canProvide(Class<? extends Job> job) {
		return ConcurrentHibernateJob.class.equals(job) || StatefulHibernateJob.class.equals(job);
	}

	public boolean canDecorate(Class<? extends Task> task) {
		return TransactionalTask.class.isAssignableFrom(task);
	}

	public Class<? extends Job> getJobWrapper(Scheduled options) {
		if(options == null)
			return ConcurrentHibernateJob.class;
		
		return options.concurrent() ? ConcurrentHibernateJob.class : StatefulHibernateJob.class;
	}

}
