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

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.StereotypeHandler;
import br.com.caelum.vraptor.resource.HttpMethod;
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
		for(Method method : controller.getMethods()){
			if(isEligible(method)){
				try {
					Trigger trigger = TriggerBuilder.triggerFor(controller, method);
					String key = keyFor(method);
					triggers.put(key, trigger);
				} catch (ParseException e) {
					throw new IllegalStateException(e);
				}	
			}
		}
	}

	public void scheduleTask(Class<? extends Task> task) {
		try {
			Trigger trigger = TriggerBuilder.triggerFor(task);
			String key = keyFor(task);
			scheduler.schedule(task, trigger, key);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean isEligible(Method m){
		return Modifier.isPublic(m.getModifiers()) &&
			   !Modifier.isStatic(m.getModifiers()) &&
			   m.isAnnotationPresent(Scheduled.class) && 
			   acceptsHttpGet(m);
	}
	
	private boolean acceptsHttpGet(Method method) {
		if (method.isAnnotationPresent(Get.class)) {
			return true;
		}
		for (HttpMethod httpMethod : HttpMethod.values()) {
			if (method.isAnnotationPresent(httpMethod.getAnnotation())) {
				return false;
			}
		}
		return true;
	}
	
	private String keyFor(Class<? extends Task> task){
		Scheduled params = task.getAnnotation(Scheduled.class);
		return !params.key().isEmpty() ? params.key() : task.getName();
	}
	
	private String keyFor(Method method){
		Scheduled params = method.getAnnotation(Scheduled.class);
		return !params.key().isEmpty() ? params.key() : TriggerBuilder.randomKey();
	}
	
	public Set<Entry<String, Trigger>> requestScopedTasks(){
		return triggers.entrySet();
	}
	
	public void markAsScheduled(){
		triggers.clear();
	}
	
	public boolean hasPendingTasksToSchedule(){
		return !triggers.isEmpty();
	}

}
