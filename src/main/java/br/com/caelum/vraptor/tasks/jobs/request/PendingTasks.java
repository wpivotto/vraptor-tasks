package br.com.caelum.vraptor.tasks.jobs.request;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

import com.google.common.collect.Maps;

@ApplicationScoped
public class PendingTasks implements Extension {

	private Map<String, Method> entries = Maps.newHashMap();
	
	public void scheduleJob(@Observes ProcessAnnotatedType<?> pat) {
		AnnotatedType<?> t = pat.getAnnotatedType();
		if (t.isAnnotationPresent(Controller.class)) {
			handle(t.getJavaClass());
		}
	}
	
	public void handle(Class<?> controller) {
		for(Method method : controller.getDeclaredMethods()) {
			if(isEligible(method)) {
				String id = getTaskId(controller, method);
				entries.put(id, method);
			}
		}
	}
	
	private boolean isEligible(Method m) {
		return Modifier.isPublic(m.getModifiers()) &&
			   !Modifier.isStatic(m.getModifiers()) &&
			   m.isAnnotationPresent(Scheduled.class) && 
			   m.isAnnotationPresent(Post.class);
	}
	
	private String getTaskId(Class<?> controller, Method method) {
		Scheduled params = method.getAnnotation(Scheduled.class);
		if (!params.id().isEmpty()) 
			return params.id();
		else
			return controller.getSimpleName() + "." + method.getName();
	}

	public Map<String, Method> all() {
		return entries;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public void remove(String key) {
		entries.remove(key);
	}

}
