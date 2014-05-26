package br.com.caelum.vraptor.tasks;

import java.text.ParseException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;

import org.quartz.Trigger;

import br.com.caelum.vraptor.events.VRaptorInitialized;
import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@ApplicationScoped
public class TaskHandler {
	
	public void whenApplicationStarts(@Observes VRaptorInitialized initialized, TaskScheduler scheduler, TriggerBuilder builder, @Any Instance<Task> tasks) {
		for (Task task : tasks) {
			Class<? extends Task> taskClass = task.getClass();
			if(taskClass.isAnnotationPresent(Scheduled.class))
				try {
					Trigger trigger = builder.triggerFor(taskClass);
					String id = getTaskId(taskClass);
					scheduler.schedule(taskClass, trigger, id);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
		}
    }
	
	private String getTaskId(Class<? extends Task> task) {
		Scheduled params = task.getAnnotation(Scheduled.class);
		return !params.id().isEmpty() ? params.id() : task.getSimpleName();
	}

}
