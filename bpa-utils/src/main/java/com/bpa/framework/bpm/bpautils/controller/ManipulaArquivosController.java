package com.bpa.framework.bpm.bpautils.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jcifs.smb.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bpa.framework.bpm.bpautils.service.ManipulaArquivosService;
import com.bpa.framework.bpm.bpautils.model.KeePassBPA;
import com.bpa.framework.bpm.bpautils.model.ObjJSON;
import com.bpa.framework.bpm.bpautils.service.GetUserPassKeePass;
import com.bpa.framework.bpm.bpautils.service.GeraJSON;

@Controller
@RequestMapping("/api/files")
public class ManipulaArquivosController {

	@Autowired
	private ManipulaArquivosService thisService;

	public static void main(String[] args) throws UnsupportedEncodingException, UnknownHostException {
		InetAddress localHost = InetAddress.getLocalHost();
		System.out.println(localHost.getHostAddress());
		System.out.println(localHost.getHostName());
		System.out.println(localHost.getCanonicalHostName());
	}
	
	@RequestMapping(value = "/createLocalFile", method = RequestMethod.POST)
	@ResponseBody
	public String criarArquivoLocal(
			@RequestParam(value = "attach", required = true) MultipartFile attach,
			@RequestParam(value = "whereToSave", required = false) String whereToSave) 
			throws IOException, UnknownHostException {
		
		Pair<InetAddress, String> whereToSavePair = (whereToSave == null || whereToSave.isEmpty()) 
				? thisService.getHostWhereToSave()
				: new Pair<InetAddress, String>(null, whereToSave);
				
		System.out.println("whereToSave: " + whereToSavePair.getValue1());
		System.out.println("attach: " + attach.getOriginalFilename() + " | " + attach.getContentType());

		File defaultDirectory = new File(whereToSavePair.getValue1());
		if (!defaultDirectory.exists())
			defaultDirectory.mkdirs();
		if (!defaultDirectory.isDirectory())
			throw new IllegalArgumentException("O arquivo nao e um diretorio");
		
		System.out.println("Diretorio padrao: " + defaultDirectory.getAbsolutePath());
		
		File folder = new File(defaultDirectory, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		if (!folder.exists()) {
			folder.mkdirs();
			System.out.println("Diretorio com a data foi criado + ["+folder.getName()+"]");
		}
		
		System.out.println("Pasta do arquivo: " + folder.getAbsolutePath());
		
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hhmmss"));
		String name = URLDecoder.decode(attach.getOriginalFilename(), "UTF-8");
		File attachLocal = new File(folder,  timestamp + "-" + name);
		
		Files.write(Paths.get(attachLocal.getAbsolutePath()), attach.getBytes());
		if (!(attachLocal.isFile() && attachLocal.canRead())) {
			System.err.println("O arquivo nao existe ou nao esta disponivel para leitura");
			throw new IllegalStateException("O arquivo nao existe ou nao esta disponivel para leitura");
		}
		
		System.out.println("Arquivo para salvar: " + attachLocal.getAbsolutePath());
		
		return attachLocal.getAbsolutePath().replace("\\", "/");
	}

	@RequestMapping(value = "/createRedeFile", method = RequestMethod.POST)
	@ResponseBody
	public String criarArquivoRede(
			@RequestParam(value = "attach", required = true) MultipartFile attach,
			@RequestParam(value = "whereToSave", required = true) String whereToSave,
			@RequestParam(value = "pathCofre", required = true) String pathCofre,
			@RequestParam(value = "chavekp", required = true) String chavekp,
			@RequestParam(value = "titlepk", required = true) String titlepk,
			@RequestParam(value = "horaForm", required = false) String horaForm)
			throws IOException, UnknownHostException, Exception {
		
		InetAddress address = InetAddress.getLocalHost();
		Pair<InetAddress, String> whereToSavePair = (whereToSave == null || whereToSave.isEmpty()) 
				? thisService.getHostWhereToSave()
				: new Pair<InetAddress, String>(address, whereToSave);
				
		System.out.println("whereToSave: " + whereToSavePair.getValue1());
		System.out.println("attach: " + attach.getOriginalFilename() + " | " + attach.getContentType());
		System.out.println("chavekp: " + chavekp);
		
		String classDecrypt = "C:\\bpatech\\Cofres\\Decrypt\\BPACryptHelper.class";
		if(!new File(classDecrypt).exists()) {
			throw new IllegalStateException("Falha ao encontrar a classe BPACryptHelper.class em C:\\bpatech\\Cofres\\Decrypt");
		}

		Process p = Runtime.getRuntime().exec("cmd.exe /q /c java -cp C:\\bpatech\\Cofres\\Decrypt BPACryptHelper goback " + chavekp);
		p.waitFor();
		BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String chavekpDecrypt = "";
		chavekpDecrypt = buf.readLine();
		System.out.println("chavekpDecrypt: " + chavekpDecrypt);


		KeePassBPA kp = new KeePassBPA();
		GetUserPassKeePass getUserPassKeePass = new GetUserPassKeePass();
		kp = getUserPassKeePass.getData(pathCofre, chavekpDecrypt, titlepk);

		//File defaultDirectory = new File(whereToSavePair.getValue1());
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", kp.getUsuario(), kp.getSenha());
		SmbFile defaultDirectory = new SmbFile("smb:" + whereToSavePair.getValue1(), auth);

		System.out.println("File (defaultDirectory): " + defaultDirectory);

		if (!defaultDirectory.exists()){
			try {
				System.out.println("Dir nao existe, criando...");
				defaultDirectory.mkdirs();	
			} catch (Exception e) {
				System.out.println("Falha ao criar diretorio: " + defaultDirectory);	
				throw new IllegalStateException(e);
			}
			
		}else{
			System.out.println("Diretorio existe: " + whereToSavePair.getValue1());	
		}

		if (!defaultDirectory.isDirectory())
			throw new IllegalArgumentException("O arquivo nao e um diretorio");
		
		System.out.println("Diretorio padrao: " + defaultDirectory);
		
		SmbFile folder = new SmbFile(defaultDirectory + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), auth);
		//File folder = new File(defaultDirectory, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		if (!folder.exists()) {
			folder.mkdirs();
			System.out.println("Diretorio com a data foi criado + ["+folder.getName()+"]");
		}
		
		System.out.println("Pasta do arquivo: " + folder);
		
		//String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hhmmss"));
		String timestamp = "";
		if(horaForm == null || horaForm.isEmpty()){
			timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hhmmss"));
		}else{
			timestamp = horaForm;
		}
		String name = URLDecoder.decode(attach.getOriginalFilename(), "UTF-8");
		//File attachLocal = new File(folder,  timestamp + "-" + name);
		SmbFile attachLocal = new SmbFile(folder + "/" + timestamp + "-" + name, auth);
		
		System.out.println("attachLocal: " + attachLocal);

		SmbFileOutputStream smbfosAttachLocal = new SmbFileOutputStream(attachLocal);

		smbfosAttachLocal.write(attach.getBytes());
		//Files.write(Paths.get(attachLocal.getCanonicalPath()), attach.getBytes());
		if (!(attachLocal.isFile() && attachLocal.canRead())) {
			System.err.println("O arquivo nao existe ou nao esta disponivel para leitura");
			throw new IllegalStateException("O arquivo nao existe ou nao esta disponivel para leitura");
		}
		
		System.out.println("Arquivo para salvar: " + attachLocal);
		
		return attachLocal.toString().replace("\\", "/");
	}

	@RequestMapping(value = "/createRedeJSONCapturaDeDados", method = RequestMethod.POST)
	@ResponseBody
	public String createRedeJSONCapturaDeDados(
			@RequestParam(value = "whereToSave", required = true) String whereToSave,
			@RequestParam(value = "pathCofre", required = true) String pathCofre,
			@RequestParam(value = "chavekp", required = true) String chavekp,
			@RequestParam(value = "titlepk", required = true) String titlepk,
			@RequestParam(value = "siglaInicioCorreios", required = false) String siglaInicioCorreios,
			@RequestParam(value = "siglaFimCorreios", required = false) String siglaFimCorreios,
			@RequestParam(value = "horaForm", required = false) String horaForm)
			throws IOException, UnknownHostException, Exception {
		
		System.out.println("createRedeJSONCapturaDeDados()");

		if((siglaInicioCorreios == null || siglaInicioCorreios.isEmpty()) || (siglaFimCorreios == null || siglaFimCorreios.isEmpty())){
			System.out.println("A siglaInicioCorreios e/ou siglaFimCorreios nao foram preenchidas!");
			throw new IllegalStateException("A siglaInicioCorreios e/ou siglaFimCorreios nao foram preenchidas!");
		}

		InetAddress address = InetAddress.getLocalHost();
		Pair<InetAddress, String> whereToSavePair = (whereToSave == null || whereToSave.isEmpty()) 
				? thisService.getHostWhereToSave()
				: new Pair<InetAddress, String>(address, whereToSave);
				
		System.out.println("whereToSave: " + whereToSavePair.getValue1());
		System.out.println("chavekp: " + chavekp);
		
		String classDecrypt = "C:\\bpatech\\Cofres\\Decrypt\\BPACryptHelper.class";
		if(!new File(classDecrypt).exists()) {
			throw new IllegalStateException("Falha ao encontrar a classe BPACryptHelper.class em C:\\bpatech\\Cofres\\Decrypt");
		}

		Process p = Runtime.getRuntime().exec("cmd.exe /q /c java -cp C:\\bpatech\\Cofres\\Decrypt BPACryptHelper goback " + chavekp);
		p.waitFor();
		BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String chavekpDecrypt = "";
		chavekpDecrypt = buf.readLine();
		System.out.println("chavekpDecrypt: " + chavekpDecrypt);


		KeePassBPA kp = new KeePassBPA();
		GetUserPassKeePass getUserPassKeePass = new GetUserPassKeePass();
		kp = getUserPassKeePass.getData(pathCofre, chavekpDecrypt, titlepk);
		

		//File defaultDirectory = new File(whereToSavePair.getValue1());
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", kp.getUsuario(), kp.getSenha());
		SmbFile defaultDirectory = new SmbFile("smb:" + whereToSavePair.getValue1(), auth);

		System.out.println("File (defaultDirectory): " + defaultDirectory);

		if (!defaultDirectory.exists()){
			try {
				System.out.println("Dir nao existe, criando...");
				defaultDirectory.mkdirs();	
			} catch (Exception e) {
				System.out.println("Falha ao criar diretorio: " + defaultDirectory);	
				throw new IllegalStateException(e);
			}
			
		}else{
			System.out.println("Diretorio existe: " + whereToSavePair.getValue1());	
		}

		if (!defaultDirectory.isDirectory())
			throw new IllegalArgumentException("O arquivo nao e um diretorio");
		
		System.out.println("Diretorio padrao: " + defaultDirectory);
		
		SmbFile folder = new SmbFile(defaultDirectory + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), auth);
		//File folder = new File(defaultDirectory, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		if (!folder.exists()) {
			folder.mkdirs();
			System.out.println("Diretorio com a data foi criado + ["+folder.getName()+"]");
		}
		
		System.out.println("Pasta do arquivo: " + folder);
		
		List<ObjJSON> listaObjJSON = new ArrayList<ObjJSON>();
		
		ObjJSON objJSON1 = new ObjJSON();
		objJSON1.setChave("siglaInicioCorreios");
		objJSON1.setValor(siglaInicioCorreios);
		listaObjJSON.add(objJSON1);
		
		ObjJSON objJSON2 = new ObjJSON();
		objJSON2.setChave("siglaFimCorreios");
		objJSON2.setValor(siglaFimCorreios);
		listaObjJSON.add(objJSON2);
		
		GeraJSON  geraJSON = new GeraJSON();
		String JSON = "";
		try{
			JSON = geraJSON.gerarJSON(listaObjJSON);
		}catch (Exception e) {
			throw new IllegalStateException("Falha ao montar JSON!" + e);
		}
		
		String nameFileJSON = horaForm + "-siglasCorreios.json";
		System.out.println("nameFileJSON: " + nameFileJSON);
		String strPathFileJSON = "smb:" + whereToSavePair.getValue1() + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" + nameFileJSON;

		SmbFile fileJSON = new SmbFile(strPathFileJSON, auth);
		SmbFileOutputStream opsFileJSON = new SmbFileOutputStream(fileJSON);
		opsFileJSON.write(JSON.getBytes());
		opsFileJSON.close();

		if (!(fileJSON.isFile() && fileJSON.canRead())) {
			System.err.println("O arquivo nao existe ou nao esta disponivel para leitura");
			throw new IllegalStateException("O arquivo nao existe ou nao esta disponivel para leitura");
		}
		
		System.out.println("Arquivo criado e salvo: " + strPathFileJSON);
		
		return strPathFileJSON.toString().replace("\\", "/");
	}
}
