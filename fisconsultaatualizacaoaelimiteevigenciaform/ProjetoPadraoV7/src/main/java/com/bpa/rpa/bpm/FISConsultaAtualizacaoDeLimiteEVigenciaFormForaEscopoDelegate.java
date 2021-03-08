package com.bpa.rpa.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.bpa.framework.bpm.general.RPAProcess;

public class FISConsultaAtualizacaoDeLimiteEVigenciaFormForaEscopoDelegate extends RPAProcess implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
    
		executeRPAProcess(
				execution,
				this.getClass().getName(),
				false,
				null);
		
	}
  
}