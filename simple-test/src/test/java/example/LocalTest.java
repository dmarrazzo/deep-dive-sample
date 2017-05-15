package example;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;

import simple.bom.Customer;

public class LocalTest extends JbpmJUnitBaseTestCase {
	private static final String SIMPLE_HT = "simple-project.simple-ht";
	KieSession ksession;
	private TaskService taskService;
	private RuntimeEngine engine;
	private KieRuntimeLogger logger;
	private AuditService auditService;

	public LocalTest() {
		super(true, true);
	}

	@Before
	public void before() {
		manager = createRuntimeManager("simple-ht.bpmn2");
		engine = getRuntimeEngine(null);

		ksession = engine.getKieSession();
		taskService = engine.getTaskService();
		auditService = engine.getAuditService();

		//--- Register WorkItemHandler ---
		WorkItemHandler handler = new org.jbpm.process.workitem.rest.RESTWorkItemHandler(
				this.getClass().getClassLoader());

		ksession.getWorkItemManager().registerWorkItemHandler("Rest", handler);
		
		//--- Logger ---
		logger = KieServices.Factory.get().getLoggers().newThreadedFileLogger(ksession, "mylogfile", 1000);
	}

	@After
	public void after() {
		ksession.dispose();
		logger.close();
	}

	@Test
	public void test() {

		// -------------
		Customer customer = new Customer("sasa", true);

		// -------------
		Map<String, Object> variablesMap = new HashMap<String, Object>();

		variablesMap.put("customer", customer);

		WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
				.startProcess(SIMPLE_HT, variablesMap);

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
	
	@Test
	public void testListOfProc() {
		// -------------
		Customer customer = new Customer("sasa", true);

		// -------------
		Map<String, Object> variablesMap = new HashMap<String, Object>();

		variablesMap.put("customer", customer);

		WorkflowProcessInstance pi = (WorkflowProcessInstance) ksession
				.startProcess(SIMPLE_HT, variablesMap);

		pi = (WorkflowProcessInstance) ksession
				.startProcess(SIMPLE_HT, variablesMap);

		List<? extends ProcessInstanceLog> instances = auditService.findProcessInstances(SIMPLE_HT);
		for (ProcessInstanceLog processInstance : instances) {
			System.out.println(">>> " + processInstance.getProcessInstanceId());
		}
	}

}
