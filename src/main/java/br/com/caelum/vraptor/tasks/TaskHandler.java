package br.com.caelum.vraptor.tasks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.List;

import org.quartz.Trigger;

import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.StereotypeHandler;
import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.jobs.request.DefaultRequestScopedTask;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@Component
@ApplicationScoped
public class TaskHandler implements StereotypeHandler {

	private final TaskScheduler scheduler;
	private final TaskLinker linker;
	private final TriggerBuilder builder;

	public TaskHandler(TaskScheduler scheduler, TaskLinker linker, TriggerBuilder builder, List<Task> tasks) {
		this.scheduler = scheduler;
		this.linker = linker;
		this.builder = builder;
		for (Task task : tasks) {
			if(task.getClass().isAnnotationPresent(Scheduled.class))
				scheduleTask(task.getClass());
		}
	}

	public Class<? extends Annotation> stereotype() {
		return Resource.class;
	}
	
	public void handle(Class<?> controller) {
		for(Method method : controller.getMethods()) {
			if(isEligible(method)) {
				try {
					Trigger trigger = builder.triggerFor(controller, method);
					String uri = linker.linkTo(controller, method);
					String id = getTaskId(controller, method);
					trigger.getJobDataMap().put("task-uri", uri);
					scheduler.schedule(DefaultRequestScopedTask.class, trigger, id);
				} catch (ParseException e) {
					throw new IllegalStateException(e);
				}	
			}
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
	
	private boolean isEligible(Method m) {
		return Modifier.isPublic(m.getModifiers()) &&
			   !Modifier.isStatic(m.getModifiers()) &&
			   m.isAnnotationPresent(Scheduled.class) && 
			   m.isAnnotationPresent(Post.class);
	}
	
	private String getTaskId(Class<? extends Task> task) {
		Scheduled params = task.getAnnotation(Scheduled.class);
		return !params.id().isEmpty() ? params.id() : task.getSimpleName();
	}
	
	private String getTaskId(Class<?> controller, Method method) {
		Scheduled params = method.getAnnotation(Scheduled.class);
		if (!params.id().isEmpty()) 
			return params.id();
		else
			return controller.getSimpleName() + "." + method.getName();
	}

}
