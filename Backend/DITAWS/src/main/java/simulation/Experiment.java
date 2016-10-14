package simulation;

import io.RESTController;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import model.GridCrowding;
import model.PlanRequest;
import model.Visit;
import model.VisitPlan;
import model.VisitPlanAlternatives;
import util.TimeUtils;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class Experiment {

	/***
	 * Priority Queue<Event>
	 * 	--> Plan Requests (user_id, time, pois, user_type)
	 * 	--> Departure from POI
	 * 
	 * List<UserRequest>
	 * 	--> from file (dep POI, time, POIs)
	 *  --> user_type: [crowd_related, greedy, shortest]
	 * 
	 * List<Plan>
	 * 
	 */

	private static RESTController controller;

	private static List<UserRequest> usersRequests;

	//private static Map<String, VisitPlan> usersPlans;

	private static PriorityQueue<Event> events = new PriorityQueue<>();

	//private final Random random;

	private static int replans;
	
	private static String base_dir;



	public Experiment() {
		//usersPlans = new HashMap<>();
		//random = new Random(1234567890);
	}

	public static void main (String args[]) throws Exception {
		//Date d = new Date();
		CSVWriter writer = new CSVWriter(new FileWriter(new File("originated_plans")), ',');


		Experiment exp = new Experiment();
		controller = new RESTController();
		controller.setPath(args[0]);
		controller.init();
		usersRequests = exp.loadUsers(args[0]);
		base_dir = args[0];

		//events.add(new Update("-1", 0));
		// iterate on file entries
		for (UserRequest req : usersRequests) {

			events.add(new Newplan(req.getId(), req.getDeparture_time()));
		}

		//events.add(new Update("-1", events.peek().getTime()-1l));
		events.add(new CrowdDump("-2", events.peek().getTime()-1l));

		while (!events.isEmpty()) {
			Event event = events.poll();
			processEvent(event,writer);
		}

		writer.close();
		System.out.println("number of total replans:"+replans);
		System.exit(1);
	}

	public List<UserRequest> loadUsers(String baseDir) {
		List<UserRequest> output = new ArrayList<>();
		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"users_requests"), ' ', '@', 0)) {

			/***
			 * line[0] = departure
			 * line[1] = departure time
			 * line[2] = list of activities
			 * line[3] = user id
			 * line[4] = user type
			 * line[5] = user crowding pref
			 */

			String [] line;
			while (null != (line = csvReader.readNext())) {
				String [] pois = line[2].split(",");

				List<String> pois_list = new ArrayList<>();

				Collections.addAll(pois_list, pois);

				output.add(new UserRequest(line[3], Integer.parseInt(line[4]), Double.parseDouble(line[5]), line[0], Integer.parseInt(line[1]), pois_list));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	static boolean update = true; 
	private static void processEvent(Event event, CSVWriter writer) {
		UserRequest request = null;
		VisitPlanAlternatives plans = null;
		VisitPlan plan = null;

//		if (event instanceof Update) {
//			Update update_req = (Update) event;
//			controller.askUpdate();
//			if (!events.isEmpty()) {
//				events.add(new Update("-1", update_req.getTime()+60l));
//			}
//		} else 
		if(event instanceof CrowdDump) {  
			CrowdDump crowddump_req = (CrowdDump) event;
			dump(crowddump_req.getTime(), controller.askCrowdDump());
			update = !update;
			if (update)
				controller.askUpdate();
			if (!events.isEmpty()) {
				events.add(new CrowdDump("-2", crowddump_req.getTime()+30l));
			}
		} else {
			if (event instanceof Newplan) {
				Newplan newplan_req = (Newplan) event;
				request = usersRequests.get(Integer.parseInt(newplan_req.getId()));
				System.out.println("user:"+request.getId());
				plans = controller.getNewVisitPlan(new PlanRequest(
						request.getId(),
						request.getCrowd_preference(),
						controller.getActivity(request.getDeparture()),
						controller.getActivity(request.getDeparture()),
						TimeUtils.getStringTime(request.getDeparture_time()),
						request.getPois()));

				if (plans.getCrowd_related().getTo_visit().isEmpty()) {
					System.out.println("Plan w crowd failed, skip what follows");
					plans = null;
				} else {
					//System.out.println("G-user:" + plans.getGreedy().getUser());
					//System.out.println("S-user:" + plans.getShortest().getUser());
					//System.out.println("CR-user:" + plans.getCrowd_related().getUser());
					//System.out.println("user type:"+request.getType());
					controller.acceptVisitPlanWithType(request.getType(), plans);
				}
			} else if (event instanceof Replan) {
				replans++;
				Replan replan_req = (Replan) event;
				request = usersRequests.get(Integer.parseInt(replan_req.getId()));
				plans = controller.addVisitedAndReplanWithType(request.getType(), new Visit(
						replan_req.getId(),
						controller.getActivity(replan_req.getVisit()),
						TimeUtils.getMillis(TimeUtils.getStringTime(((int)replan_req.getTime())))));
			}

			if (null != plans) {
				if (request.getType() == 0) {
					plan = plans.getCrowd_related();
				} else if (request.getType() == 1) {
					plan = plans.getShortest();
				} else {
					plan = plans.getGreedy();
				}

				if (event instanceof Newplan) {
					//usersPlans.put(request.getId(), plan);
					writer.writeNext(new String[]{
							plan.getUser(), 
							Integer.toString(request.getType(),10),
							Double.toString(plans.getCrowd_preference()),
							"CR:"+plans.getCrowd_related().toString(),
							"G:"+plans.getGreedy().toString(),
							"S:"+plans.getShortest()}
							);
					//					System.out.println(Arrays.toString(new String[]{
					//							plan.getUser(), 
					//							Integer.toString(request.getType(),10),
					//							Double.toString(plans.getCrowd_preference()),
					//							"CR:"+plans.getCrowd_related().toString(),
					//							"G:"+plans.getGreedy().toString(),
					//							"S:"+plans.getShortest()}));
				}
				
				
				int next_dep_time;
				String visit;
				if (plan.getTo_visit().isEmpty()) {
					next_dep_time = (int) (TimeUtils.getMillis(plan.getArrival_time()) / 1000 / 60);
					visit = plan.getArrival().getPlace_id();
				} else {
					System.out.println("next_dep_time:" + (TimeUtils.getMillis(plan.getTo_visit().get(0).getDeparture_time()) / 1000 / 60));
					next_dep_time = (int) (TimeUtils.getMillis(plan.getTo_visit().get(0).getDeparture_time()) / 1000 / 60);
					visit = plan.getTo_visit().get(0).getVisit().getPlace_id();
				}

				events.add(new Replan(
						request.getId(),
						next_dep_time,
						visit));
			}

		}
	}

	private static void dump(long time, List<GridCrowding> crowdings) {
		try {
			CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(base_dir+"crowd_dump/"+time)), ' ', '@');
			for (GridCrowding cell : crowdings) {
				csvWriter.writeNext(new String[] {
						cell.getCell(),
						Arrays.toString(cell.getCrowdings().toArray())
				});
			}
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
