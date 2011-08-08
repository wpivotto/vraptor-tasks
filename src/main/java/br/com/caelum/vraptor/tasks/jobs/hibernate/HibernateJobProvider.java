package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.SessionFactory;
import org.quartz.Job;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;

public class HibernateJobProvider implements JobProvider {

	private final SessionFactory factory;
	private final TaskValidator validator;

	public HibernateJobProvider(SessionFactory factory, TaskValidator validator) {
		this.factory = factory;
		this.validator = validator;
	}

	public Job newJob(Task task) {
		return new HibernateJob((TransactionalTask) task, validator, factory.openSession());
	}

}
