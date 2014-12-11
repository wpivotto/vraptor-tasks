package br.com.caelum.vraptor.tasks.jobs.request;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.tasks.TaskLinker;
import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@Component
@Intercepts
public class PendingTasksInterceptor implements Interceptor {
	
	private final PendingTasks pendingTasks;
	private final TaskLinker linker;
	private final TriggerBuilder builder;
	private final TaskScheduler scheduler;
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public PendingTasksInterceptor(PendingTasks tasks, TaskLinker linker, TriggerBuilder builder, TaskScheduler scheduler) {
		this.pendingTasks = tasks;
		this.linker = linker;
		this.builder = builder;
		this.scheduler = scheduler;
	}

	@Override
	public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws InterceptionException {
		schedulePendingTasks();
		stack.next(method, resourceInstance);
	}

	@Override
	public boolean accepts(ResourceMethod method) {
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
