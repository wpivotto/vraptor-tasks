package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.SessionFactory;
import org.quartz.Job;
import org.quartz.JobDetail;

import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;

public class HibernateJobProvider implements JobProvider {

	private final SessionFactory factory;
	private final TaskValidator validator;

	public HibernateJobProvider(SessionFactory factory, TaskValidator validator) {
		this.factory = factory;
		this.validator = validator;
	}

	public Job newJob(JobDetail jobDetail) {
		TransactionalTask task = newTask(jobDetail.getKey().getName());
		return new HibernateJob(task, validator, factory.openSession());
	}

	private TransactionalTask newTask(String className) {
		try {
			return (TransactionalTask) Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
