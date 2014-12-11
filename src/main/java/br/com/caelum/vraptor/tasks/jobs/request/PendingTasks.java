package br.com.caelum.vraptor.tasks.jobs.request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.StereotypeHandler;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

import com.google.common.collect.Maps;

@Component
@ApplicationScoped
public class PendingTasks implements StereotypeHandler {

	private Map<String, Method> entries = Maps.newHashMap();

	public Class<? extends Annotation> stereotype() {
		return Resource.class;
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

	public Iterator<Entry<String, Method>> iterator() {
		return entries.entrySet().iterator();
	}

}
