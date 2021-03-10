package com.bpa.rpa.bpm;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;

import com.bpa.framework.bpm.general.RPAProcess;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListaTarefasDeUsuarios extends RPAProcess implements JavaDelegate {
	
	private static final String diagram = "fisalelorelatoriodeentregaform.bpmn";
	
	public void execute(DelegateExecution execution) throws Exception {
		variaveisNoDiagrama(execution, diagram);
	}

	private void variaveisNoDiagrama(DelegateExecution execution, String diagram) throws JsonProcessingException {
		
		InputStream in = new ListaTarefasDeUsuarios()
				.getClass().getClassLoader()
				.getResourceAsStream(diagram);
		
		BpmnModelInstance modelInstance = Bpmn.readModelFromStream(in);
		
		Set<Task> tasks = new HashSet<Task>(
				modelInstance.getModelElementsByType(UserTask.class));
		
		List<String> taskIds = tasks.stream()
				.map(task -> task.getId())
				.collect(Collectors.toList());
		
		taskIds.remove("AnaliseManual");
		taskIds.remove("analisemanual");
		taskIds.remove("Analise_Manual");
		taskIds.remove("analise_manual");
		
		ObjectMapper mapper = new ObjectMapper();
		String customTaskIds = mapper
				.writeValueAsString(taskIds)
				.replaceAll("\"", "");
		
		setVariableValue("userTasks", customTaskIds, execution);
		setVariableValue("autoCompletar", "true", execution);
		setVariableValue("taskRetornoPadrao", "pesquisa_item", execution);
		setVariableValue("taskRetentativa", "taskRetentativa", execution);
		setVariableValue("noRetentativa", "0", execution);
	}
	
	private void setVariableValue(String var, String value, DelegateExecution execution) {
		if (!execution.hasVariable(var))
			execution.setVariable(var, value);
		else if (String.valueOf(execution.getVariable(var)).isEmpty())
			execution.setVariable(var, value);
	}
}