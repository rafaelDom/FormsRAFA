package com.bpa.framework.bpm.bpautils.service;

import java.util.List;
import com.bpa.framework.bpm.bpautils.model.ObjJSON;
import org.json.JSONObject;

public class GeraJSON {

	public String gerarJSON(List<ObjJSON> objJson) {

		// instancia um novo JSONObject
		JSONObject objJSON = new JSONObject();

		// preenche o objeto com os campos: titulo, ano e genero
		for(ObjJSON obj:objJson) {
			objJSON.put(obj.getChave(), obj.getValor());
		}
		
		return objJSON.toString();
	}
}
