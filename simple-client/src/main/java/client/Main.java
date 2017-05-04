package client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simple.bom.Customer;


public class Main {
	final static Logger log =  LoggerFactory.getLogger(Main.class);
	
	private static final String URL = "http://localhost:8080/kie-server/services/rest/server";
	private static final String user = "donato";
	private static final String password = "donato";
	private static final String CONTAINER = "example:simple-project:1.0-SNAPSHOT";

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		launchProcess();
		
		long end = System.currentTimeMillis();
		System.out.println("elapsed time: "+ (end-start));
	}

	public static void launchProcess() {
		try {
			
			KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(URL, user, password);
			// Marshalling
			Set<Class<?>> extraClasses = new HashSet<Class<?>>();	
			extraClasses.add(Customer.class);
			config.addExtraClasses(extraClasses);
			config.setMarshallingFormat(MarshallingFormat.JSON);
			Map<String, String> headers = null;
			config.setHeaders(headers);
			
			KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
			ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);

			
			//---------------------------
			Customer customer = new Customer("myfriend", true);
			
			Map<String, Object> variables = new HashMap<>();
			variables.put("customer", customer);
			
			//---------------------------
			String processId = "simple-project.simple-ht";			
			processClient.startProcess(CONTAINER, processId, variables);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
