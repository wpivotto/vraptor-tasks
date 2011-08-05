package br.com.caelum.vraptor.tasks.jobs.jpa;

import javax.persistence.EntityManagerFactory;

import org.quartz.Job;
import org.quartz.JobDetail;

import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;

public class JPAJobProvider implements JobProvider {

	private final EntityManagerFactory factory;
	private final TaskValidator validator;

	public JPAJobProvider(EntityManagerFactory factory, TaskValidator validator) {
		this.factory = factory;
		this.validator = validator;
	}

	public Job newJob(JobDetail jobDetail) {
		TransactionalTask task = newTask(jobDetail.getKey().getName());
		return new JPAJob(task, validator, factory.createEntityManager());
	}

	private TransactionalTask newTask(String className) {
		try {
			return (TransactionalTask) Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
