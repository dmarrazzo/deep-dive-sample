package client;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.kie.api.command.Command;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.jaxb.gen.ClaimTaskCommand;
import org.kie.remote.jaxb.gen.CompleteTaskCommand;
import org.kie.remote.jaxb.gen.FindActiveProcessInstancesCommand;
import org.kie.remote.jaxb.gen.GetProcessInstanceCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsPotentialOwnerCommand;
import org.kie.remote.jaxb.gen.GetTaskContentCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.SetProcessInstanceVariablesCommand;
import org.kie.remote.jaxb.gen.SignalEventCommand;
import org.kie.remote.jaxb.gen.StartProcessCommand;
import org.kie.remote.jaxb.gen.StartTaskCommand;
import org.kie.remote.jaxb.gen.util.JaxbStringObjectPair;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.AbstractJaxbHistoryObject;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskContentResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

import simple.bom.Customer;

@SuppressWarnings("rawtypes")
public class BCClientMain {

	private static final String URL = "http://localhost:8080/business-central";
	private static final String user = "donato";
	private static final String password = "donato";
	private static final String deploymentId = "example:simple-project:1.0-SNAPSHOT";

	public static void main(String[] args) {

		try {
			// getTasks();

			// startProcessCmd();

			startProcess2();
			// getTasksCmd();
			// claimAndStartTask(289L);
			// getTaskInputData(289L);
			// completeTask(289L);
			// sendSignal("adunanza", null, 385);
			// listProcesses();
			// setVariable(438);
			// getVariable(438, "adempimento");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Esempio di uso RuntimeEngine
	 */
	private static void getTasks() throws Exception {
		URL instanceURL = new URL(URL + "/rest");

		RuntimeEngine engine = RemoteRuntimeEngineFactory.newRestBuilder().addUrl(instanceURL).addUserName(user)
				.addPassword(password).addDeploymentId(deploymentId).build();

		TaskService taskService = engine.getTaskService();
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner(user, null);
		for (TaskSummary taskSummary : list) {
			System.out.println(taskSummary.getId() + " " + taskSummary.getDescription());
		}
	}

	private static void startProcess2() throws Exception {
		URL instanceURL = new URL(URL);

		RemoteRestRuntimeEngineBuilder engineBuilder = RemoteRuntimeEngineFactory.newRestBuilder().addUrl(instanceURL)
				.addUserName(user).addPassword(password).addDeploymentId(deploymentId);

		// ----------------
		String processId = "simple-project.simple-ht";

		engineBuilder.addExtraJaxbClasses(Customer.class);
		Customer customer = new Customer();
		customer.setName("pippo");
		customer.setPremium(false);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("customer", customer);

		// ------------------
		RuntimeEngine engine = engineBuilder.build();

		engine.getKieSession().startProcess(processId, parameters);
	}

	private static void startProcessCmd() throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		// A sample command to start a process:
		StartProcessCommand startProcessCommand = new StartProcessCommand();
		JaxbStringObjectPairArray params = new JaxbStringObjectPairArray();

		// ---------------- <<< codice specifico da modificare -------------
		String processId = "simple-project.simple-ht";

		Customer customer = new Customer();
		customer.setName("donato");
		customer.setPremium(false);

		params.getItems().add(new JaxbStringObjectPair("customer", customer));

		// ---------------- >>> codice specifico da modificare -------------

		startProcessCommand.setProcessId(processId);
		startProcessCommand.setParameter(params);
		commands.add(startProcessCommand);
		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);
		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static void getTasksCmd() throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		GetTaskAssignedAsPotentialOwnerCommand potentialOwnerCommand = new GetTaskAssignedAsPotentialOwnerCommand();
		potentialOwnerCommand.setUserId("donato");
		commands.add(potentialOwnerCommand);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		for (JaxbCommandResponse<?> jaxbCommandResponse : response) {
			Object result = jaxbCommandResponse.getResult();
			if (List.class.isAssignableFrom(result.getClass())) {
				List<?> list = (List<?>) result;
				for (Object object : list) {
					if (object instanceof JaxbTaskSummary) {
						JaxbTaskSummary ts = (JaxbTaskSummary) object;
						System.out.println(">>> " + ts.getId());
					}
				}
			}
		}

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static void getVariable(long processInstanceId, String variableId) throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		GetProcessInstanceCommand command = new GetProcessInstanceCommand();
		command.setProcessInstanceId(processInstanceId);
		commands.add(command);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static void setVariable(long processInstanceId) throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		SetProcessInstanceVariablesCommand command = new SetProcessInstanceVariablesCommand();
		command.setProcessInstanceId(processInstanceId);
		JaxbStringObjectPairArray variables = new JaxbStringObjectPairArray();
		JaxbStringObjectPair varPair = new JaxbStringObjectPair("test", "test");
		variables.getItems().add(varPair);
		command.setVariables(variables);
		commands.add(command);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static void claimAndStartTask(Long taskId) throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		ClaimTaskCommand command = new ClaimTaskCommand();
		command.setTaskId(taskId);
		command.setUserId(user);

		commands.add(command);

		StartTaskCommand command2 = new StartTaskCommand();
		command2.setTaskId(taskId);
		command2.setUserId(user);

		commands.add(command2);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static Map<String, Object> getTaskInputData(Long taskId) throws Exception {
		// List of commands to be executed:

		List<Command> commands = new ArrayList<Command>();

		GetTaskContentCommand command = new GetTaskContentCommand();
		command.setTaskId(taskId);
		command.setUserId(user);

		commands.add(command);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		JaxbTaskContentResponse taskContent = (JaxbTaskContentResponse) response.get(0);
		java.util.Map<String, Object> map = (Map<String, Object>) taskContent.getResult();

		// ---------------- <<< codice specifico per un task da modificare
		// -------------
		System.out.println(">>>" + map.get("adempimento"));
		// ---------------- >>> codice specifico per un task da modificare
		// -------------

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);

		return map;
	}

	private static void completeTask(Long taskId) throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		CompleteTaskCommand command = new CompleteTaskCommand();
		command.setTaskId(taskId);
		command.setUserId(user);
		JaxbStringObjectPairArray outputData = new JaxbStringObjectPairArray();
		List<JaxbStringObjectPair> items = outputData.getItems();

		// ---------------- <<< codice specifico per un task da modificare
		// -------------
		items.add(new JaxbStringObjectPair("accetta", false));
		items.add(new JaxbStringObjectPair("motivoRichiesta", "test"));

		Map<String, Object> inputData = getTaskInputData(taskId);
		items.add(new JaxbStringObjectPair("adempimento", inputData.get("adempimento")));

		// ---------------- >>>> codice specifico per un task da modificare
		// -------------

		command.setData(outputData);

		commands.add(command);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static void listProcesses() throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		FindActiveProcessInstancesCommand command = new FindActiveProcessInstancesCommand();
		commands.add(command);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);
		if (response.size() == 1) {
			JaxbHistoryLogList historyLogList = (JaxbHistoryLogList) response.get(0);
			List<AbstractJaxbHistoryObject> list = historyLogList.getHistoryLogList();
			for (AbstractJaxbHistoryObject historyObject : list) {
				if (historyObject instanceof JaxbProcessInstanceLog) {
					JaxbProcessInstanceLog processInstanceLog = (JaxbProcessInstanceLog) historyObject;

					System.out.println(">>> " + processInstanceLog.getProcessInstanceId());
				}
			}
		}

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);

	}

	private static void sendSignal(String eventType, Object eventObject, long processInstanceId) throws Exception {
		// List of commands to be executed:
		List<Command> commands = new ArrayList<Command>();

		SignalEventCommand command = new SignalEventCommand();
		command.setEventType(eventType);
		command.setEvent(eventObject);
		command.setProcessInstanceId(processInstanceId);
		commands.add(command);

		List<JaxbCommandResponse<?>> response = executeCommand(deploymentId, commands);

		System.out.printf("Command %s executed.\n", response.toString());
		System.out.println("commands1" + commands);
	}

	private static List<JaxbCommandResponse<?>> executeCommand(String deploymentId, List<Command> commands)
			throws Exception {

		URL address = new URL(URL + "/rest/execute");
		ClientRequest request = createRequest(address);

		request.header(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER, deploymentId);
		JaxbCommandsRequest commandMessage = new JaxbCommandsRequest();
		commandMessage.setCommands(commands);
		commandMessage.setDeploymentId(deploymentId);
		String body = convertJaxbObjectToString(commandMessage);
		request.body(MediaType.APPLICATION_XML, body);
		ClientResponse<String> responseObj = request.post(String.class);
		String strResponse = responseObj.getEntity();
		System.out.println("RESPONSE FROM THE SERVER: \n" + strResponse);
		JaxbCommandsResponse cmdsResp = convertStringToJaxbObject(strResponse);

		return cmdsResp.getResponses();
	}

	private static ClientRequest createRequest(URL address) {
		return new ClientRequestFactory().createRequest(address.toExternalForm()).header("Authorization",
				getAuthHeader());
	}

	private static String getAuthHeader() {
		String auth = user + ":" + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));

		return "Basic " + new String(encodedAuth);
	}

	private static String convertJaxbObjectToString(Object object) throws JAXBException {
		// Add your classes here.

		Class<?>[] classesToBeBound = { JaxbCommandsRequest.class, Customer.class };
		Marshaller marshaller = JAXBContext.newInstance(classesToBeBound).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(object, stringWriter);
		String output = stringWriter.toString();
		System.out.println("REQUEST CONTENT: \n" + output);

		return output;
	}

	private static JaxbCommandsResponse convertStringToJaxbObject(String str) throws JAXBException {
		Unmarshaller unmarshaller = JAXBContext.newInstance(JaxbCommandsResponse.class).createUnmarshaller();

		return (JaxbCommandsResponse) unmarshaller.unmarshal(new StringReader(str));
	}
}
