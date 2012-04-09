package br.com.caelum.vraptor.tasks.jobs.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DefaultRequestScopedTask implements RequestScopedTask {

	private URL endpoint;
	
	public void execute() {
		
		HttpURLConnection connection = null;

	    try {
			connection = (HttpURLConnection) endpoint.openConnection();
			connection.setAllowUserInteraction(false);
			HttpURLConnection.setFollowRedirects(false);
			connection.connect();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	public void setup(URL endpoint) {
		this.endpoint = endpoint;
	}

}