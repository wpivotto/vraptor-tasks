package br.com.caelum.vraptor.tasks.jobs.jpa;

import javax.persistence.EntityManagerFactory;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;

public class JPAJobProvider implements JobProvider {

	private final EntityManagerFactory factory;
	private final TaskValidator validator;

	public JPAJobProvider(EntityManagerFactory factory, TaskValidator validator) {
		this.factory = factory;
		this.validator = validator;
	}

	public Job newJob(Task task) {
		return new JPAJob((TransactionalTask) task, validator, factory.createEntityManager());
	}

}
