package br.com.caelum.vraptor.tasks.scheduler;

import java.lang.reflect.Method;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.Trigger;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.tasks.TaskHandler;
import br.com.caelum.vraptor.tasks.jobs.request.DefaultRequestScopedTask;

@Intercepts
public class SchedulerInterceptor implements Interceptor {

	private final TaskHandler handler;
	private final TaskScheduler scheduler;
	private final Router router;
	private final Configuration cfg;
	
	public SchedulerInterceptor(TaskHandler handler, TaskScheduler scheduler, Router router, Configuration cfg) {
		this.handler = handler;
		this.scheduler = scheduler;
		this.router = router;
		this.cfg = cfg;
	}

	@Override
	public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws InterceptionException {
		try {
			for(Map.Entry<String, Trigger> entry : handler.requestScopedTasks()){
				Trigger trigger = entry.getValue();
				buildURI(trigger);
				scheduler.schedule(DefaultRequestScopedTask.class, trigger, entry.getKey());
			}
			handler.markAsScheduled();
		} catch (Exception e) {
			throw new InterceptionException(e);
		}
		stack.next(method, resourceInstance);
	}

	@Override
	public boolean accepts(ResourceMethod method) {
		return handler.hasPendingTasksToSchedule();
	}
	
	private void buildURI(Trigger trigger) throws Exception {
		JobDataMap map = trigger.getJobDataMap();
		Class<?> controller = Class.forName((String) map.get("controller"));
		Method method = controller.getMethod((String) map.get("method"), new Class[]{});
		String URI = uriFor(controller, method);
		trigger.getJobDataMap().put("task-uri", URI);
	}
	
	private String uriFor(Class<?> controller, Method method) {
		String URI = router.urlFor(controller, method, new Object[0]);
		if (URI.startsWith("/")) {
			return cfg.getApplicationPath() + URI;
		}
		return URI;
	}

}
