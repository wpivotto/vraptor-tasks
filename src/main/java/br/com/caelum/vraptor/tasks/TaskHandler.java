package br.com.caelum.vraptor.tasks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.quartz.Trigger;

import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.StereotypeHandler;
import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

import com.google.common.collect.Maps;

@Component
@ApplicationScoped
public class TaskHandler implements StereotypeHandler {

	private final TaskScheduler scheduler;
	private Map<String, Trigger> triggers = Maps.newHashMap();

	public TaskHandler(TaskScheduler scheduler, List<Task> tasks) {
		this.scheduler = scheduler;
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
					Trigger trigger = TriggerBuilder.triggerFor(controller, method);
					String id = getId(controller, method);
					triggers.put(id, trigger);
				} catch (ParseException e) {
					throw new IllegalStateException(e);
				}	
			}
		}
	}

	public void scheduleTask(Class<? extends Task> task) {
		try {
			Trigger trigger = TriggerBuilder.triggerFor(task);
			String id = getId(task);
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
	
	private String getId(Class<? extends Task> task) {
		Scheduled params = task.getAnnotation(Scheduled.class);
		return !params.id().isEmpty() ? params.id() : task.getSimpleName();
	}
	
	private String getId(Class<?> controller, Method method) {
		Scheduled params = method.getAnnotation(Scheduled.class);
		if (!params.id().isEmpty()) 
			return params.id();
		else
			return controller.getSimpleName() + "." + method.getName();
	}
	
	public Set<Entry<String, Trigger>> requestScopedTasks() {
		return triggers.entrySet();
	}
	
	public void markAsScheduled() {
		triggers.clear();
	}
	
	public boolean hasPendingTasksToSchedule() {
		return !triggers.isEmpty();
	}

}
