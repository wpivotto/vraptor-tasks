package br.com.caelum.vraptor.tasks;

import java.lang.reflect.Method;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import br.com.caelum.vraptor.proxy.MethodInvocation;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.proxy.SuperMethod;
import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.jobs.request.RequestScopedTask;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@RequestScoped
public class DefaultExecutor implements TaskExecutor {

	@Inject private Scheduler quartz;
	@Inject private TaskScheduler scheduler;
	@Inject private TriggerBuilder builder;
	@Inject private Proxifier proxifier;
	
	
	private JobKey getKey(String taskId){
		return new JobKey(taskId);
	}
	
	public void execute(String taskId) throws SchedulerException {
		quartz.triggerJob(getKey(taskId));
	}
	
	public void pause(String taskId) throws SchedulerException {
		quartz.pauseJob(getKey(taskId));
	}

	public void resume(String taskId) throws SchedulerException {
		quartz.resumeJob(getKey(taskId));
	}

	public void pauseAll() throws SchedulerException {
		quartz.pauseAll();
	}
	
	public void resumeAll() throws SchedulerException {
		quartz.resumeAll();
	}

	public void runOnce(Class<? extends Task> task) {
		Trigger trigger = builder.runOnce();
		scheduler.schedule(task, trigger, task.getSimpleName());
	}

	public <T> T run(final Class<T> controller) {
		return proxifier.proxify(controller, new MethodInvocation<T>() {
			public Object intercept(T proxy, Method method, Object[] args, SuperMethod superMethod) {
				Trigger trigger = builder.triggerFor(method, args);
				scheduler.schedule(RequestScopedTask.class, trigger, builder.randomKey());
				return null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T run(T controller) {
		return (T) run(controller.getClass());
	}
	

}
