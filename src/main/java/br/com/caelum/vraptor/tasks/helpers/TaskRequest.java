package br.com.caelum.vraptor.tasks.helpers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.PrototypeScoped;
import br.com.caelum.vraptor.proxy.MethodInvocation;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.proxy.SuperMethod;
import br.com.caelum.vraptor.resource.HttpMethod;

@Component
@PrototypeScoped
public class TaskRequest {

	private final Router router;
	private final ServletContext context;
	private final Proxifier proxifier;
	private int port = 8080;
	private String protocol = "http";
	private int responseCode;
	
	private static final Logger logger = LoggerFactory.getLogger(TaskRequest.class);

	public TaskRequest(Router router, ServletContext context, Proxifier proxifier) {
		this.router = router;
		this.context = context;
		this.proxifier = proxifier;
	}
	
	public TaskRequest port(int port){
		this.port = port;
		return this;
	}
	
	public TaskRequest protocol(String protocol){
		this.protocol = protocol;
		return this;
	}

	public <T> T access(final Class<T> type) {

		return proxifier.proxify(type, new MethodInvocation<T>() {
			public Object intercept(T proxy, Method method, Object[] args, SuperMethod superMethod) {

				if (!acceptsHttpGet(method)) {
					throw new IllegalArgumentException("Your logic method must accept HTTP GET method if you want to access it");
				}

				String url = router.urlFor(type, method, args);
				String path = getApplicationPath() + url;

				logger.debug("Trying to access {}", path);

				ping(path);
				
				logger.debug("Response Code: {}", getResponseCode());
				
				return null;
			}

		});
	}
	
	private void ping(String url) {

		HttpURLConnection connection = null;

		try {

			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
			this.responseCode = connection.getResponseCode();

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
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
	
	private String getApplicationPath() {
		return  protocol + "://localhost" + (port != 80? ":" + port : "") + context.getContextPath();
	}
	
	public int getResponseCode(){
		return responseCode;
	}
	
	public boolean success(){
		return responseCode == 200;
	}
	
	public boolean failed(){
		return responseCode != 200;
	}


}