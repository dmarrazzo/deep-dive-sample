package example;

import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleWorkItemHandler implements WorkItemHandler {
	public final static String WORKITEMHANDLER_NAME = "TEST_WIH";

	public final static String INPUT_PARAM_NAME_WORK = "Work";
	public final static String OUTPUT_PARAM_NAME_RESULT = "Result";

	private Logger log = LoggerFactory.getLogger(getClass());

	private static int count;

	public SimpleWorkItemHandler() {
		System.out.println("TestWorkItemHandler.TestWorkItemHandler()");
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> output = null;
		System.out.println("TestWorkItemHandler.executeWorkItem()");
		log.trace("{} start", WORKITEMHANDLER_NAME);
		
		Map<String, Object> parameters = workItem.getParameters();
		Object countInt = parameters.get("count");
		
		if (countInt != null && countInt instanceof Integer) {
			count = (Integer)countInt;
			log.trace("count parameter: {}", count);
		}
				
		manager.completeWorkItem(workItem.getId(), output);

	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		log.trace("{} abortWIH", WORKITEMHANDLER_NAME);
		manager.abortWorkItem(workItem.getId());
	}

}
