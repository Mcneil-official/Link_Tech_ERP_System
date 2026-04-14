package erp.link_tech_erp;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import erp.link_tech_erp.integration.GlobalLoginFrame;

@SpringBootApplication
public class LinkTechErpApplication {

	public static void main(String[] args) {
		GlobalLoginFrame.launch();
	}

}
