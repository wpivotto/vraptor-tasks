package br.com.caelum.vraptor.tasks.jobs;

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
	
	public Task newTask(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			if(container.canProvide(clazz))
				return (Task) container.instanceFor(clazz);
			else
				return (Task) clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
