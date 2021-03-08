package com.bpa.framework.bpm.bpautils.service;

import java.util.List;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.KeePassFile;
import com.bpa.framework.bpm.bpautils.model.KeePassBPA;

public class GetUserPassKeePass {

	public KeePassBPA getData(String path, String chavekp, String titlepk) throws Exception {
		
		List<Entry> entries;
		
		try {
			KeePassFile database = KeePassDatabase.getInstance(path).openDatabase(chavekp);
			entries = database.getEntries();
		} catch (Exception e) {
			throw new Exception("A chave para abrir o cofre esta invalida!");
		}

		KeePassBPA kp = new KeePassBPA();

		if (entries.isEmpty()) {
			throw new Exception("Nao foram cadastros Entry no cofre!");
		}

		for (Entry entry : entries) {
			if (entry.getTitle().equals(titlepk)) {
				kp.setUsuario(entry.getUsername());
				kp.setSenha(entry.getPassword());
				break;
			}
		}

		if (kp.getSenha() == null || kp.getUsuario() == null) {
			throw new Exception("Usuario e/ou senha do cofre nao foram preenchidos para esse titulo!");
		}

		return kp;
	}
}
