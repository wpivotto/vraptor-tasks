package br.com.caelum.vraptor.tasks.jobs.jpa;

import javax.persistence.EntityManagerFactory;

import org.quartz.Job;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidatorFactory;

@Component
@ApplicationScoped
public class JPAJobProvider implements JobProvider {

	private final Container container;
	private final TaskValidatorFactory validatorFactory;

	public JPAJobProvider(Container container, TaskValidatorFactory validatorFactory) {
		this.container = container;
		this.validatorFactory = validatorFactory;
	}

	public Job newJob(Task task) {
		EntityManagerFactory factory = container.instanceFor(EntityManagerFactory.class);
		return new JPAJob((TransactionalTask) task, validatorFactory.getInstance(), factory.createEntityManager());
	}

	public boolean canProvide(Class<? extends Job> job) {
		return JPAJob.class.equals(job);
	}
	
	public boolean canDecorate(Class<? extends Task> task) {
		return TransactionalTask.class.isAssignableFrom(task);
	}

	public Class<? extends Job> getJobWrapper() {
		return JPAJob.class;
	}
	
}
