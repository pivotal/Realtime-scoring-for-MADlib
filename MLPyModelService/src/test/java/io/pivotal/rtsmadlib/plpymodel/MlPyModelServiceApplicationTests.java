package io.pivotal.rtsmadlib.plpymodel;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.DockerComposeContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=MlPyModelServiceApplication.class)
@ActiveProfiles("test")
class MlPyModelServiceApplicationTests {

	@ClassRule
    public static DockerComposeContainer madlibContainer = 
      new DockerComposeContainer(
        new File("src/test/resources/docker-compose.yml"))
      ;

	@Test
	public void contextLoads() {
	}
	
	
	@Before
	public void before() {
		System.out.println("Here");

    }

	@After
	public void teardown() {
		System.out.println("Here");
	}
}
