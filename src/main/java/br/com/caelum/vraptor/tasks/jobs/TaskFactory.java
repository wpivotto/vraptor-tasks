package br.com.caelum.vraptor.tasks.jobs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.JobDetail;

import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.Task;

@ApplicationScoped
public class TaskFactory {
	
	private Container container;
	
	@Deprecated // CDI eyes only
	public TaskFactory() {}
	
	@Inject
	public TaskFactory(Container container) {
		this.container = container;
	}
	
	public Task newTask(Class<? extends Task> taskClass, JobDetail detail) {
		Task task = newInstance(taskClass);
		return task;
	}
	
	private Task newInstance(Class<? extends Task> taskClass) {
		try {
			if(container.canProvide(taskClass))
				return (Task) container.instanceFor(taskClass);
			else
				return (Task) taskClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
