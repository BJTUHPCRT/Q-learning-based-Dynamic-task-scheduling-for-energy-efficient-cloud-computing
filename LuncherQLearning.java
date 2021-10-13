package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class LuncherQLearning {
	private static final int NUM_VM = 20; 
	private static final int NUM_CLOUDLET = 1000; 
	private static final double POISSON_LAMBDA =20.0; 
	private static final double LETS_WAVE_INTERVAL = 1.0; 
	private static final int MAX_LENGTH_WAITING_QUEUE = 1; // cloud task waiting queue maximum length in each virtual machine
	private static final double LEARNING_GAMMA = 0.9; 
	private static final double LEARNING_ALPHA = 0.5; 
	private static final double LEARNING_EPSILON =0.5; 

	private static final int VM_MIPS[] = { 100, 150, 200,250,300,350,400,450,500,550,
            600,650,700,750,800,850,900,950,1000,1000};

	public static void main(String[] args) {
		Log.printLine("Starting...");
		for(int i = 0; i < 5000; i++) {    // training episode 5000
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			CloudSim.init(num_user, calendar, trace_flag);
			if (VM_MIPS.length != NUM_VM) {
				System.out.println("virtual machine MIPS number does not match the number of virtual machines, terminate!");
				System.exit(0);
			}
			int numHost = 4;
			int numVm = NUM_VM;
			int vmMips[] = VM_MIPS;
			QDatacenter datacenter0 = createDatacenter("Datacenter_0", numHost,numVm, vmMips);

			double gamma = LEARNING_GAMMA; 
			double alpha = LEARNING_ALPHA; 
			double epsilon = LEARNING_EPSILON; 

	
			VmCloudletAssigner vmCloudletAssigner = new VmCloudletAssignerReinforcementLearning(gamma, alpha, epsilon, GenExcel.getInstance());

			

	

			int numlets = NUM_CLOUDLET; // Cloudlet number in cloud environment
			int numPe = NUM_VM; // Set the number of cores according to the number of virtual machines
			double lambda = POISSON_LAMBDA;
			double numletWaveInterval = LETS_WAVE_INTERVAL; // Time interval of each wave cloudlet
			int cloudletWaitingQueueLength = MAX_LENGTH_WAITING_QUEUE; 
			QDatacenterBroker globalBroker = new QDatacenterBroker(
					"QDatacenterBroker", vmCloudletAssigner, numlets, numPe,
					lambda, numletWaveInterval, vmMips,cloudletWaitingQueueLength);

		    VirtualQueueSize.init(NUM_VM, cloudletWaitingQueueLength); // Initialize the virtual queue
			
			CloudSim.startSimulation(); 

			GenExcel.getInstance().genExcel();

			

			List<Cloudlet> newList = new LinkedList<Cloudlet>(); // Create a list that records the results of running cloud tasks
			
			newList.addAll(globalBroker.getCloudletReceivedList());

			Log.printLine("Total Cloudlets: " + numlets);
			printCloudletList(newList); 
			
			
			CloudSim.stopSimulation(); 
			printUtilization(datacenter0);
			
			Log.printLine("finished!");
			
		} catch (Exception e) { 
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");

		}
	   }
	}



	private static QDatacenter createDatacenter(String name, int numHost,int numPe, int vmMips[]) {

		List<Host> hostList = new ArrayList<Host>();

		int maxMips = 0;
		for (int mip : vmMips)
			if (mip > maxMips)
				maxMips = mip;

		int hostId = 0; // id of hosts
		int ram = 16384; // host ram
		long storage = 1000000; 
		int bw = 100000; // host bandwidth



		int vmtohostnumber=0;

		for (int i = 0; i < numHost; i++) { // create hosts
			List<Pe> peList = new ArrayList<Pe>();
			if(i<numHost-1){
				for(int j=0;j<3;j++){//  create pe number randomly
					peList.add(new Pe(j,new PeProvisionerSimple(maxMips)));
					vmtohostnumber++;			
				}
				Log.printLine(vmtohostnumber+"pe number");
			
			    Host h=new Host(hostId, new RamProvisionerSimple(ram),
				   		new BwProvisionerSimple(bw), storage, peList,new VmSchedulerTimeShared(peList));
				hostList.add(h);
				hostId++;
				Log.printLine(hostId+"host"+hostList.get(0).getNumberOfPes()+"mips"+hostList.get(i).getTotalMips());
			}else{
				for(int k=0;k<numPe-vmtohostnumber;k++){				
					peList.add(new Pe(k,new PeProvisionerSimple(maxMips)));			
				}
				Log.printLine(numPe-vmtohostnumber+"remianing pe");
			
			    Host h=new Host(hostId, new RamProvisionerSimple(ram),
			   		new BwProvisionerSimple(bw), storage, peList,new VmSchedulerTimeShared(peList));
				hostList.add(h);
				hostId++;
			}
		}
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.1; // the cost of using storage in this
										
		double costPerBw = 0.1; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();



		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				// set datacenter characteristics
				arch, os, vmm, hostList, time_zone, cost, costPerMem,costPerStorage, costPerBw);
		QDatacenter datacenter = null;
		try {
			datacenter = new QDatacenter(name, characteristics, // create datacenter
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;

	}

	// print different host utilization and energy
	public static void printUtilization(QDatacenter datacenter0){
		List<Host> hosts=datacenter0.getHostList();
		double totalutilization=0;//utilization of each host
		double aveutilization=0;// average utilization of each host
		double aveu=0;
		double totalworkload=0;//workload of each host
		double aveworkload=0;//average workload of each host
		int j=0;
		double power=0;double totalpower=0;double avepower=0;double totalpowers=0;
		
		for(Host host:hosts){
			for(int i=0;i<host.getStateHistory().size();i++){
			//	System.out.println("host"+i+"\t"+host.getStateHistory().get(i).getUtilization()+"\t"+host.getStateHistory().size()+"\t"+host.getStateHistory().get(i).getTime());
				totalutilization=host.getStateHistory().get(i).getUtilization()+totalutilization;				
			//	totalworkload=host.getStateHistory().get(i).getWorkLoad()+totalworkload;
				if(host.getStateHistory().get(i).getUtilization()>0.5){
					power=(-7.79979*host.getStateHistory().get(i).getUtilization()+150.56995)*host.getStateHistory().get(i).getWorkLoad();
				
				}else{
					power=(132.47581*host.getStateHistory().get(i).getUtilization()+8.84754)*host.getStateHistory().get(i).getWorkLoad();
				}
				totalpower=power+totalpower;
			}
			totalpowers=totalpower/host.getStateHistory().size();
			aveutilization=totalutilization/host.getStateHistory().size();
		//	aveworkload=totalworkload/host.getStateHistory().size();
			totalutilization=0;
			j++;
		}
		aveu=totalutilization/hosts.size();
	//	avepower=totalpower/host.getStateHistory().size();
		System.out.println("utilization:" + aveutilization);//print each host utilization
		System.out.println("Energy:" + totalpowers);//print each host energy
	}

	private static void printCloudletList(List<Cloudlet> list) { 
		int size = list.size();
		Cloudlet cloudlet;
		List<Double> finishTimeEachVm = new ArrayList<Double>(); // The time when the last cloud task in each virtual machine left the waiting queue
		List<Double> totalWaitingTimeEachVm = new ArrayList<Double>(); // The total waiting time of all cloud tasks in the waiting queue of each virtual machine
		List<Double> totalUtilizingTimeEachVm = new ArrayList<Double>(); // The total time consumed by all cloud tasks in each virtual machine
		List<Integer> numCloudletsEachVm = new ArrayList<Integer>(); // The total number of cloud tasks executed in each virtual machine
		double finishtime=0;
		double watingtime=0;
		double wwatingtime=0;
		
		for (int i = 0; i < NUM_VM; i++) {
			finishTimeEachVm.add(0.0);
			totalWaitingTimeEachVm.add(0.0);
			totalUtilizingTimeEachVm.add(0.0);
			numCloudletsEachVm.add(0);
		}
		
		String indent = "    ";
		int success = 0;
		DecimalFormat dft = new DecimalFormat("###.##");
		
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			// Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				success++;
				 // Calculate the total waiting time in the queue of all cloud tasks in each physical machine
				totalWaitingTimeEachVm.set(cloudlet.getVmId(),totalWaitingTimeEachVm.get(cloudlet.getVmId())+ cloudlet.getWaitingTime());
				watingtime=watingtime+cloudlet.getExecStartTime()-cloudlet.getArrivetime();
				//waiting time
				wwatingtime=wwatingtime+cloudlet.getFinishTime()-cloudlet.getArrivetime();
				if (finishTimeEachVm.get(cloudlet.getVmId()) < cloudlet.getExecStartTime()) { 
					// Get the time when the last cloud task of each physical machine left the waiting queue
					finishTimeEachVm.set(cloudlet.getVmId(),cloudlet.getExecStartTime());	
				}
				if(finishtime<cloudlet.getExecStartTime()){
					finishtime=cloudlet.getExecStartTime();
				}
				
				// Calculate the total elapsed time of all cloud tasks in each physical machine
				totalUtilizingTimeEachVm.set(cloudlet.getVmId(), totalUtilizingTimeEachVm.get(cloudlet.getVmId())+ cloudlet.getFinishTime()
								- cloudlet.getSubmissionTime());
				// Calculate the total number of cloud tasks in each physical machine
				numCloudletsEachVm.set(cloudlet.getVmId(),numCloudletsEachVm.get(cloudlet.getVmId()) + 1);
				
			} else {
			}
		}

		int s=0;
		
		// average length of queue
		//	System.out.println(watingtime*POISSON_LAMBDA/size/1000+"cloudlets/ms");
	
		System.out.println("average waiting time:" + watingtime/size + "s");
		Log.printLine("Number of Success cloudlet : " + success);

		System.out.println("average response time:"+ wwatingtime/size + "s");
	}

}
