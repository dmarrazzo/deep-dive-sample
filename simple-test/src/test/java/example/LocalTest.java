package example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;

public class LocalTest extends JbpmJUnitBaseTestCase {
	KieSession ksession;
	private TaskService taskService;
	private RuntimeEngine engine;

	public LocalTest() {
		super(true, true);
	}

	@Before
	public void before() {
		manager = createRuntimeManager("simple-ht.bpmn2");
		engine = getRuntimeEngine(null);

		ksession = engine.getKieSession();
		taskService = engine.getTaskService();

		WorkItemHandler handler = new org.jbpm.process.workitem.rest.RESTWorkItemHandler(
				this.getClass().getClassLoader());

		ksession.getWorkItemManager().registerWorkItemHandler("Rest", handler);
	}

	@After
	public void after() {
		ksession.dispose();
	}

	@Test
	public void test() {

		// -------------
		Customer customer = new Customer("sasa", true);

		// -------------
		Map<String, Object> variablesMap = new HashMap<String, Object>();

		variablesMap.put("customer", customer);

		WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
				.startProcess("simple-project.simple-ht", variablesMap);

		// User Task
		assertNodeTriggered(processInstance.getId(), "user");

		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("donato", "*");
		customer.setName("pluto");
		TaskSummary task = list.get(0);
		taskService.start(task.getId(), "donato");
		taskService.complete(task.getId(), "donato", variablesMap);

		// REST
		assertNodeTriggered(processInstance.getId(), "REST");


		// COMPLETED
		assertProcessInstanceCompleted(processInstance.getId());

	}

}
