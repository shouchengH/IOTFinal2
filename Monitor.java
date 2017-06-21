package org.eclipse.om2m.test;

import org.eclipse.om2m.commons.resource.*;
import org.eclipse.om2m.commons.rest.*;
import org.eclipse.om2m.core.service.SclService;

import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;   
import javax.xml.parsers.DocumentBuilderFactory;   
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.json.*;

public class Monitor {
    static enum Activity
    {
    	Toilet,			// 大小號
    	Brush_teeth,	// 刷牙
    	Wash_face,		// 洗臉
    	Do_nothing		// 什麼都沒做
    }
    static enum Tag
    {
    	Toilet,			// 馬桶
    	Tooth_brush,	// 牙刷
    	Cleanser,		// 洗面乳
    }
    static enum Move
    {
    	Slow,			// 緩慢    	
    	Back_forward,	// 前後
    	Oval,			// 橢圓
    }
    
    static int[] new_states = new int[]{Activity.Brush_teeth.ordinal(), Activity.Wash_face.ordinal(), Activity.Toilet.ordinal()};
    static int[] new_observations1 = new int[]{};	// 當前state，所觀察的值
    static int[] new_observations2 = new int[]{};
    static double[] new_start_probability = new double[]{0.2, 0.1, 0.7};	//一天刷兩次牙、一次洗臉、七次大小號
    static double[][] new_transititon_probability = new double[][]{		// 由上一個state，推到下一state
            {0.1, 0.3, 0.6},
            {0.3, 0.1, 0.6},
            {0.5, 0.4, 0.1},
    };
    static double[][] new_emission_probability1 = new double[][]{		// tag，observations推測當前state
            {0.8, 0.1, 0.1},
            {0.1, 0.8, 0.1},
            {0.1, 0.1, 0.8},
    };
    static double[][] new_emission_probability2 = new double[][]{		// move
    	{0.7, 0.2, 0.1},       
        {0.2, 0.5, 0.2},
        {0.1, 0.3, 0.6},
    };
    static double[][] transititon_probability = new double[][]{			// CHMM 轉移矩陣
        {0.7, 0.4},
        {0.3, 0.6},
    };
    
	
	static SclService core;
	static String sclId = System.getProperty("org.eclipse.om2m.sclBaseId", "");
	static String nsclId = System.getProperty("org.eclipse.om2m.remoteNsclId", "");
	static String reqEntity = System.getProperty("org.eclipse.om2m.adminRequestingEntity", "");
	static String ipuId = "sample";
	static String actuatorId = "MY_ACTUATOR";
	static String sensorId = "MY_SENSOR";
	static String applicationId = "MY_APPLICATION";
	static boolean actuatorValue = false;
	static int sensorValue = 0;
	JSONObject j;
	int tag = 0,move = 0;
	Vector v1 = new Vector(0);
	Vector v2 = new Vector(0);
	String lastURI = "";

	public Monitor(SclService sclService) {
		core = sclService;
	}

	public void start() {
		System.out.println("Monitor start");
               // Create required resources for the Sensor
		createSensorResources();
               // Listen for the Sensor data
		//listenToSensor();

		createNetworkResources();
               // Create required resources for the Actuator
		createActuatorResources();
               // Listen for the Actuator data
		listenToActuator();
	}

	public void createSensorResources() {
		String targetId, content;
		
		System.out.println("Monitor createSensorResources");
               // Create the MY_SENSOR application
		targetId = sclId + "/applications";
		System.out.println("-------------------------------------------------------------------");
		System.out.println(sclId);
		System.out.println(sclId);
		System.out.println(sclId);
		System.out.println("-------------------------------------------------------------------");
		ResponseConfirm response = core
				.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
						new Application(sensorId, ipuId)));

		if (response.getStatusCode().equals(StatusCode.STATUS_CREATED)) {
                       // Create the "DESCRIPTOR" container
			targetId = sclId + "/applications/" + sensorId + "/containers";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new Container("DESCRIPTOR")));

                        // Create the "DATA" container
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new Container("DATA")));

                        // Create the description contentInstance
			content = Mapper.getSensorDescriptorRep(sclId, sensorId, ipuId);
			targetId = sclId + "/applications/" + sensorId
					+ "/containers/DESCRIPTOR/contentInstances";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new ContentInstance(content.getBytes())));

                        // Create the data contentInstance
			content = Mapper.getSensorDataRep(sensorValue);
			targetId = sclId + "/applications/" + sensorId
					+ "/containers/DATA/contentInstances";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new ContentInstance(content.getBytes())));
		}
	}
	
	public void createNetworkResources() {
		String targetId, content;
		
               // Create the MY_SENSOR application
		targetId = nsclId + "/applications";
		ResponseConfirm response = core
				.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
						new Application(applicationId, ipuId)));

		if (response.getStatusCode().equals(StatusCode.STATUS_CREATED)) {
                       // Create the "DESCRIPTOR" container
			targetId = nsclId + "/applications/" + applicationId + "/containers";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new Container("DESCRIPTOR")));

                        // Create the "DATA" container
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new Container("DATA")));

                        // Create the description contentInstance
			content = Mapper.getSensorDescriptorRep(nsclId, applicationId, ipuId);
			targetId = nsclId + "/applications/" + applicationId
					+ "/containers/DESCRIPTOR/contentInstances";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new ContentInstance(content.getBytes())));

		}
	}

	public void createActuatorResources() {
		String targetId, content;

		System.out.println("Monitor createActuatorResources");
               // Create the "MY_ACTUATOR" application
		targetId = sclId + "/applications";
		ResponseConfirm response = core.doRequest(new RequestIndication(
				"CREATE", targetId, reqEntity, new Application(actuatorId,ipuId)));

		if (response.getStatusCode().equals(StatusCode.STATUS_CREATED)) {
                       // Create the "DESCRIPTOR" container
			targetId = sclId + "/applications/" + actuatorId + "/containers";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new Container("DESCRIPTOR")));

                        // Create the "DATA" container
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
					new Container("DATA")));

                       // Create the description contentInstance
			content = Mapper.getActutaorDescriptorRep(sclId, actuatorId, ipuId);
			targetId = sclId + "/applications/" + actuatorId
					+ "/containers/DESCRIPTOR/contentInstances";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity, 
					content));

                       // Create the data contentInstance
			content = Mapper.getActuatorDataRep(actuatorValue);
			targetId = sclId + "/applications/" + actuatorId
					+ "/containers/DATA/contentInstances";
			core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,	
					content));
		}
	}
	
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}

	public void listenToSensor() {
		new Thread() {
			public void run() {
				while (true) {
					System.out.println("Monitor listenToSensor");
                                       // Simualte a random measurement of the sensor
					sensorValue = 10 + (int) (Math.random() * 100);

                                       // Create a data contentInstance
					String content = Mapper.getSensorDataRep(sensorValue);
					String targetID = sclId + "/applications/" + sensorId
							+ "/containers/DATA/contentInstances";
					core.doRequest(new RequestIndication("CREATE", targetID,reqEntity, 
							content));

                                       // Wait for 2 seconds then loop
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void listenToActuator() {
		new Thread() {
			public void run() {

				boolean memorizedActuatorValue = false;
				while (true) {
					
                                       // If the Actuator state has changed
					if (memorizedActuatorValue != actuatorValue) {
                                               // Memorize the new Actuator state
						memorizedActuatorValue = actuatorValue;

                                               // Create a data contentInstance
						String content = Mapper.getActuatorDataRep(actuatorValue);
						String targetID = sclId + "/applications/" + actuatorId
								+ "/containers/DATA/contentInstances";
						core.doRequest(new RequestIndication("CREATE",targetID, reqEntity, 
								content));
					}
					
					
					//String content = Mapper.getSensorDataRep(sensorValue);
					String testID = sclId + "/applications/" + sensorId
							+ "/containers/DATA/contentInstances/latest";
					String targetID = sclId + "/applications/" + sensorId
							+ "/containers/DATA/contentInstances/latest/content";
					String test = core.doRequest(new RequestIndication("RETRIEVE", testID,reqEntity)).getResourceURI();
					
					String s_all = core.doRequest(new RequestIndication("RETRIEVE", targetID,reqEntity))
							.getRepresentation();
					
					if (lastURI == test){
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
					lastURI = test;
					
					System.out.println("-----------------------------------------------------------------------");
					System.out.println();
					/*try {
						System.out.println(loadXMLFromString(s));
					} catch (Exception e1) {
						System.out.println("Wrong!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/
					System.out.println(test);
					System.out.println();
					System.out.println("-----------------------------------------------------------------------");
					
					
					String s[] = s_all.split(" ");
					String key = "",value = "";
					
					for (int i = 0;i < s.length; i++){
						if (s[i].indexOf("val") == -1)
							continue;
						
						if (s[i].indexOf("\"") != -1){
							System.out.println(s[i]);
							String[] tempArr = s[i].split("\"");
							key = tempArr[0].trim();
							value = tempArr[1].trim();
							break;
						}
						else if (s[i].indexOf("'") != -1){
							System.out.println(s[i]);
							String[] tempArr = s[i].split("'");
							key = tempArr[0].trim();
							value = tempArr[1].trim();
							break;
						}

					}
					
					if (value.length() > 1){
						String s1[] = value.split(",");
						System.out.println(s1[0].trim());
						System.out.println(s1[1].trim());
						
						tag = Integer.parseInt(s1[0].trim());
						move = Integer.parseInt(s1[1].trim());
						
						System.out.println();
						System.out.println(tag);
						/*try {
							System.out.println(loadXMLFromString(s));
						} catch (Exception e1) {
							System.out.println("Wrong!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}*/
						System.out.println();
						System.out.println(move);
						System.out.println();
						v1.add(tag);
						v2.add(move);
						
				        int[] x; 
				        x = new int[v1.size()];
				        for(int i=0;i<v1.size();i++){
				        	x[i] = (int) v1.get(i);
				        }
				        int[] y; 
				        y = new int[v2.size()];
				        for(int i=0;i<v2.size();i++){
				        	y[i] = (int) v2.get(i);
				        }
				        
						/*int[] result1 = Chmm.HMMcompute(x, new_states, new_start_probability, new_transititon_probability, new_emission_probability1);
						int[] result2 = Chmm.HMMcompute(y, new_states, new_start_probability, new_transititon_probability, new_emission_probability2);
						
				        for (int r : result1)
				        {
				            System.out.print(Activity.values()[r] + " ");
				        }
				        System.out.println();
				        for (int r : result2)
				        {
				            System.out.print(Activity.values()[r] + " ");
				        }*/
				        System.out.println("--------------------------------------------------------");
				        System.out.println();
				        int[] result1 = Chmm.HMMcompute(x, new_states, new_start_probability, new_transititon_probability, new_emission_probability1);
				        int[] result2 = Chmm.HMMcompute(y, new_states, new_start_probability, new_transititon_probability, new_emission_probability2);
				        int[] result3 = Chmm.compute(x, y, new_states, new_start_probability, new_transititon_probability, new_emission_probability1, new_emission_probability2);
				        
				        System.out.print("HMM : ");
				        for (int r : result1)
				        {
				        	
				            System.out.print(Activity.values()[r] + " ");
				        }
				        System.out.println();
				        System.out.print("CHMM : ");
				        for (int r : result3)
				        {
				            System.out.print(Activity.values()[r] + " ");
				        }
				        System.out.println();
				        System.out.println("--------------------------------------------------------");
				        
				        
				        String targetId, content;
                        // Create the data contentInstance
						content = Mapper.getResultRep(Activity.values()[result1[result1.length - 1]].toString());
						targetId = nsclId + "/applications/" + applicationId
								+ "/containers/DATA/contentInstances";
						core.doRequest(new RequestIndication("CREATE", targetId, reqEntity,
								new ContentInstance(content.getBytes())));
					}
					else {
						System.out.println();
						System.out.println(value);
						System.out.println();
					}
					
					

                                       // Wait for 2 seconds then loop
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						System.out.println();
						System.out.println("InterruptedException");
						System.out.println();
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}