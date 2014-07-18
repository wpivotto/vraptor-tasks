package br.com.caelum.vraptor.tasks.jobs.request;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.Accepts;
import br.com.caelum.vraptor.AroundCall;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.interceptor.SimpleInterceptorStack;
import br.com.caelum.vraptor.tasks.TaskLinker;
import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@Intercepts
@RequestScoped
public class PendingTasksInterceptor {
	
	private PendingTasks pendingTasks;
	private TaskLinker linker;
	private TriggerBuilder builder;
	private TaskScheduler scheduler;
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Deprecated // CDI eyes only
	public PendingTasksInterceptor() {}
	
	@Inject
	public PendingTasksInterceptor(PendingTasks tasks, TaskLinker linker, TriggerBuilder builder, TaskScheduler scheduler) {
		this.pendingTasks = tasks;
		this.linker = linker;
		this.builder = builder;
		this.scheduler = scheduler;
	}

	@AroundCall
	public void intercept(SimpleInterceptorStack stack) {
		schedulePendingTasks();
		stack.next();
	}

	@Accepts
    public boolean accepts(ControllerMethod method) {
		return !pendingTasks.isEmpty();
    }
	
	private void schedulePendingTasks() {
		Iterator<Entry<String, Method>> tasks = pendingTasks.iterator();
		while(tasks.hasNext()) {
			Entry<String, Method> task = tasks.next();
			Method method = task.getValue();
			Class<?> controller = method.getDeclaringClass();
			String URI = linker.linkTo(controller, method);
			try {
				Trigger trigger = builder.triggerFor(controller, method);
				trigger.getJobDataMap().put("task-uri", URI);
				scheduler.schedule(DefaultRequestScopedTask.class, trigger, task.getKey());
				tasks.remove();
			} catch (ParseException e) {
				log.error("Can't schedule task", e);
			}
		}
	}

}
