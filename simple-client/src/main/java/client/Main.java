package client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simple.bom.Customer;

public class Main {
	private static final String GET_ALL_PROCESS_INSTANCES_WITH_VARIABLES = "getAllProcessInstancesWithVariables2";

	final static Logger log = LoggerFactory.getLogger(Main.class);

	private static final String URL = "http://localhost:8081/kie-server/services/rest/server";
	private static final String user = "donato";
	private static final String password = "donato";
	private static final String CONTAINER = "example:simple-project:1.0-SNAPSHOT";

	public static void main(String[] args) {
		Main clientApp = new Main();
		long start = System.currentTimeMillis();
		clientApp.advandedQuery();

		long end = System.currentTimeMillis();
		System.out.println("elapsed time: " + (end - start));
	}

	public void launchProcess() {
		try {

			KieServicesClient client = getClient();

			ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);

			// ---------------------------
			Customer customer = new Customer("myfriend", true);

			Map<String, Object> variables = new HashMap<>();
			variables.put("customer", customer);

			// ---------------------------
			String processId = "simple-project.simple-ht";
			processClient.startProcess(CONTAINER, processId, variables);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void advandedQuery() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			QueryDefinition queryDefinition = new QueryDefinition();
			queryDefinition.setName(GET_ALL_PROCESS_INSTANCES_WITH_VARIABLES);
			queryDefinition.setExpression("select pil.*, v.variableId, v.value " + "from ProcessInstanceLog pil "
					+ "INNER JOIN (select vil.processInstanceId ,vil.variableId, MAX(vil.ID) maxvilid  FROM VariableInstanceLog vil "
					+ "GROUP BY vil.processInstanceId, vil.variableId ORDER BY vil.processInstanceId)  x "
					+ "ON (v.variableId = x.variableId  AND v.id = x.maxvilid ) " 
					+ "INNER JOIN VariableInstanceLog v "
					+ "ON (v.processInstanceId = pil.processInstanceId)");
			queryDefinition.setSource("java:jboss/datasources/ExampleDS");
			queryDefinition.setTarget("CUSTOM");
			queryClient.replaceQuery(queryDefinition);

			QueryFilterSpec spec = new QueryFilterSpecBuilder()
					.equalsTo("variableId", "name")
					.equalsTo("value", "myfriend")
					.get();

			List<ProcessInstance> listProcessInstance = queryClient.query(GET_ALL_PROCESS_INSTANCES_WITH_VARIABLES,
					"ProcessInstances",spec, 0, 10, ProcessInstance.class);

			for (ProcessInstance processInstance : listProcessInstance) {
				System.out.println(">>>" + processInstance.getId());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private KieServicesClient getClient() {
		KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(URL, user, password);
		// Marshalling
		Set<Class<?>> extraClasses = new HashSet<Class<?>>();
		extraClasses.add(Customer.class);
		config.addExtraClasses(extraClasses);
		config.setMarshallingFormat(MarshallingFormat.JSON);
		Map<String, String> headers = null;
		config.setHeaders(headers);
		KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

		return client;
	}

}
