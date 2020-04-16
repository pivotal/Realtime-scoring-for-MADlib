package io.pivotal.rtsmadlib.plpymodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.pivotal.rtsmadlib.plpymodel.service.ModelArtifactsImporterService;

@SpringBootApplication
public class MlPyModelServiceApplication implements CommandLineRunner{

	@Autowired
	ModelArtifactsImporterService modelArtifactsImporterService;
	public static void main(String[] args) {
		SpringApplication.run(MlPyModelServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		modelArtifactsImporterService.importArtifacts();	
	}

}
