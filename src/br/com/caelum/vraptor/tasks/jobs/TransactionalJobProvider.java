package br.com.caelum.vraptor.tasks.jobs;

import net.vidageek.mirror.dsl.Mirror;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.quartz.Job;
import org.quartz.JobDetail;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.TransactionalTask;

@Component
@ApplicationScoped
public class TransactionalJobProvider implements JobProvider {

	private final SessionFactory factory;

	public TransactionalJobProvider(SessionFactory factory) {
		this.factory = factory;
	}

	public Job newJob(JobDetail jobDetail) {
		Session session = factory.openSession();
		TransactionalTask task = newTask(jobDetail.getKey().getName());
		return new TransactionalJob(task, session);
	}

	@Override
	public boolean canProvide(Class<? extends Job> job) {
		return job.equals(TransactionalJob.class);
	}

	private TransactionalTask newTask(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return (TransactionalTask) new Mirror().on(clazz).invoke().constructor().withoutArgs();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
