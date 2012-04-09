package br.com.caelum.vraptor.tasks.helpers;

import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.Lazy;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.view.Results;

/**
 * Avoids all calls from outside the server
 * @author William
 *
 */

@Intercepts
@Lazy
public class RemoteAddressInterceptor implements Interceptor {

	private final Result result;
	private final HttpServletRequest request;

	public RemoteAddressInterceptor(Result result, HttpServletRequest request) {
		this.result = result;
		this.request = request;
	}

	public boolean accepts(ResourceMethod method) {
		return method.containsAnnotation(Scheduled.class);
	}

	public void intercept(InterceptorStack stack, ResourceMethod method, Object instance) throws InterceptionException {

		if("127.0.0.1".equals(request.getRemoteAddr()))
			stack.next(method, instance);
		else
			result.use(Results.status()).notAcceptable();
	}

}