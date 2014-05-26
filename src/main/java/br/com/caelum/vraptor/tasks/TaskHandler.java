package br.com.caelum.vraptor.tasks;

import java.text.ParseException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@ApplicationScoped
public class TaskHandler {

	private final TaskScheduler scheduler;
	private final TriggerBuilder builder;

	@Inject
	public TaskHandler(TaskScheduler scheduler, TriggerBuilder builder, List<Task> tasks) {
		this.scheduler = scheduler;
		this.builder = builder;
		for (Task task : tasks) {
			if(task.getClass().isAnnotationPresent(Scheduled.class))
				scheduleTask(task.getClass());
		}
	}
	
	private void scheduleTask(Class<? extends Task> task) {
		try {
			Trigger trigger = builder.triggerFor(task);
			String id = getTaskId(task);
			scheduler.schedule(task, trigger, id);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getTaskId(Class<? extends Task> task) {
		Scheduled params = task.getAnnotation(Scheduled.class);
		return !params.id().isEmpty() ? params.id() : task.getSimpleName();
	}

}
