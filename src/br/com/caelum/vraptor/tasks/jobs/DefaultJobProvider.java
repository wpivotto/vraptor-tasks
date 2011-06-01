package br.com.caelum.vraptor.tasks.jobs;

import net.vidageek.mirror.dsl.Mirror;

import org.quartz.Job;
import org.quartz.JobDetail;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;

@Component
@ApplicationScoped
public class DefaultJobProvider implements JobProvider {


	public Job newJob(JobDetail jobDetail) {
		Task task = newTask(jobDetail.getKey().getName());
		return new DefaultJob(task);
	}

	@Override
	public boolean canProvide(Class<? extends Job> job) {
		return job.equals(DefaultJob.class);
	}

	private Task newTask(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return (Task) new Mirror().on(clazz).invoke().constructor().withoutArgs();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
