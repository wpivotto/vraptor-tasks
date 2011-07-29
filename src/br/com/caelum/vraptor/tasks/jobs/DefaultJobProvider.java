package br.com.caelum.vraptor.tasks.jobs;

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

	private Task newTask(String className) {
		try {
			return (Task) Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
