package com.bpa.framework.bpm.bpautils.service;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class RemoveCaracteresEspecias {
	public static String removerCarateresEspeciais(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
}
