
import io.Mongo;
import model.CrowdingFeedback;
import model.UncertainValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.web.client.RestTemplate;

@RunWith(JUnit4.class)
public class RestTest {
	public  CrowdingFeedback fdbk;
	public Mongo dao;

	public RestTemplate restTemplate;
	
	@Before
	public void setUp() {
		dao = new Mongo();
		restTemplate = new RestTemplate();
		
		fdbk = new CrowdingFeedback("gr@gr.gr", dao.retrievePOI("2575586935"), dao.retrievePOI("21828209"), "09:00", 0);
	}
	
	@After
	public void tearDown() {
		fdbk = null;
	}
	
	@Test @Ignore
	public void testSuccess() {
		UncertainValue value = restTemplate.getForObject("https://158.85.245.154:8443/CrowdingModule/crowding_fdbk/"+
				fdbk.getUser()+"/"+
				fdbk.getDeparture().getPlace_id()+"/"+
				fdbk.getArrival().getPlace_id()+"/"+
				fdbk.getDeparture_time()+"/"+
				fdbk.getChoice(), UncertainValue.class);
		
		System.out.println("Value:"+value.getMean());
		
	}
	
	
}