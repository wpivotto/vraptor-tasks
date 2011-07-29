package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.SessionFactory;
import org.quartz.Job;
import org.quartz.JobDetail;

import br.com.caelum.vraptor.tasks.jobs.JobProvider;

public class HibernateJobProvider implements JobProvider {

	private final SessionFactory factory;

	public HibernateJobProvider(SessionFactory factory) {
		this.factory = factory;
	}

	public Job newJob(JobDetail jobDetail) {
		TransactionalTask task = newTask(jobDetail.getKey().getName());
		return new HibernateJob(task, factory.openSession());
	}

	public boolean canProvide(Class<? extends Job> job) {
		return job.equals(HibernateJob.class);
	}

	private TransactionalTask newTask(String className) {
		try {
			return (TransactionalTask) Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
