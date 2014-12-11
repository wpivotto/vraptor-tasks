package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.JobDetail;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.Task;

@Component
@ApplicationScoped
public class TaskFactory {
	
	private final Container container;
	
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
