package com.bpa.framework.bpm.bpautils.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.javatuples.Pair;
import org.springframework.stereotype.Service;

@Service
public class ManipulaArquivosService {
	
	public Pair<InetAddress, String> getHostWhereToSave() throws UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();
		
		StringBuilder mensagemHostError = new StringBuilder("O host local não está na lista de produção/desenvolvimento/homologação\n");
		mensagemHostError.append("Ips cadastrados:\n");
		mensagemHostError.append("Desenvolvimento: 10.111.177.134\n");
		mensagemHostError.append("Homologação: 10.111.177.140\n");
		mensagemHostError.append("Produção: 10.111.177.6|10.111.177.7|10.111.177.8\n");
		
		address.getHostAddress();
		
		switch (address.getHostName()) {
			case "CPTSRVBPMSPD01":
				return new Pair<InetAddress, String>(address, "C:/bpatech/BPM-PastasCompartilhadas/dev/fisextracaomultibeneficios");
			case "CPTSRVBPMSPH01":
				return new Pair<InetAddress, String>(address, "C:/bpatech/BPM-PastasCompartilhadas/hmg/fisextracaomultibeneficios");
			case "BPM":
			case "CPTSRVBPMSP01":
			case "CPTSRVBPMSP02":
				return new Pair<InetAddress, String>(address, "//10.111.177.9/prd/fisextracaomultibeneficios");
			case "MARIOPC":
				return new Pair<InetAddress, String>(address, "C:/bpatech/BPM-PastasCompartilhadas/teste/fisextracaomultibeneficios");
			default:
				throw new UnknownHostException(mensagemHostError.toString());
		}
	}
}
