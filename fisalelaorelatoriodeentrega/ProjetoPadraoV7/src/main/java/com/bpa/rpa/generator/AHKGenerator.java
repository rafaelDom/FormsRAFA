package com.bpa.rpa.generator;

import com.bpa.framework.bpm.general.GeneralAHKGenerator;

public class AHKGenerator extends GeneralAHKGenerator {
	static String	bpmnFileName	= "fisalelorelatoriodeentregaform";
	static String	processName		= "FISAleloRelatorioDeEntregaForm";

	public static void main(String[] args) {
		new GeneralAHKGenerator().generateTemplate(bpmnFileName, processName);
	}
}
