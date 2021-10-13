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

public class LuncherBaselinesTest {
	private static final int NUM_VM = 20; 
	private static final int NUM_CLOUDLET = 1000; 
	private static final double POISSON_LAMBDA = 10.0; 
	private static final double LETS_WAVE_INTERVAL = 1.0; 
	private static final int MAX_LENGTH_WAITING_QUEUE = 1; 
	

	private static final int VM_MIPS[] = { 100, 150, 200,250,300,350,400,450,500,550,
                                           600,650,700,750,800,850,900,950,1000,1000
	 };


	public static void main(String[] args) {
		Log.printLine("Starting...");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			CloudSim.init(num_user, calendar, trace_flag);
			if (VM_MIPS.length != NUM_VM) {
				System.out.println("virtual machine MIPS number does not match the number of virtual machines, terminate!");
				System.exit(0);
			}
			int numHost = 6;
			int numVm = NUM_VM;
			int vmMips[] = VM_MIPS;
			QDatacenter datacenter0 = createDatacenter("Datacenter_0", numHost,numVm, vmMips);

		// Three baselines algorithms
	
		//	VmCloudletAssigner vmCloudletAssigner = new VmCloudletAssignerRandom();

		//	VmCloudletAssigner vmCloudletAssigner = new VmCloudletAssignerGreedy();

			VmCloudletAssigner vmCloudletAssigner = new VmCloudletAssignerFair();

			int numlets = NUM_CLOUDLET; 
			int numPe = NUM_VM; 
			double lambda = POISSON_LAMBDA;
			double numletWaveInterval = LETS_WAVE_INTERVAL;
			int cloudletWaitingQueueLength = MAX_LENGTH_WAITING_QUEUE; 
			QDatacenterBroker globalBroker = new QDatacenterBroker(
					"QDatacenterBroker", vmCloudletAssigner, numlets, numPe,
					lambda, numletWaveInterval, vmMips,cloudletWaitingQueueLength);

			VirtualQueueSize.init(NUM_VM, cloudletWaitingQueueLength); 
			
			CloudSim.startSimulation();

			List<Cloudlet> newList = new LinkedList<Cloudlet>();

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



	private static QDatacenter createDatacenter(String name, int numHost,int numPe, int vmMips[]) {

		List<Host> hostList = new ArrayList<Host>();

		int maxMips = 0;
		for (int mip : vmMips)
			if (mip > maxMips)
				maxMips = mip;

		int hostId = 0; 
		int ram = 16384;
		long storage = 1000000;
		int bw = 100000;

		int vmtohostnumber=0;

		for (int i = 0; i < numHost; i++) {
			List<Pe> peList = new ArrayList<Pe>();
			if(i<numHost-1){
				for(int j=0;j<3;j++){
					peList.add(new Pe(j,new PeProvisionerSimple(maxMips)));
					vmtohostnumber++;	
				}
				Log.printLine(vmtohostnumber+"pe number");

			    Host h=new Host(hostId, new RamProvisionerSimple(ram),
				   		new BwProvisionerSimple(bw), storage, peList,new VmSchedulerTimeShared(peList));

				hostList.add(h);
				hostId++;
				Log.printLine(hostId+"host"+hostList.get(0).getNumberOfPes()+"mips"+hostList.get(0).getTotalMips());
				if(hostId==2){
					Log.printLine(hostId+"host"+hostList.get(1).getNumberOfPes()+"mips"+hostList.get(1).getTotalMips());
				}
			}else{
				for(int k=0;k<numPe-vmtohostnumber;k++){
					peList.add(new Pe(k,new PeProvisionerSimple(maxMips)));
					
				}
				Log.printLine(numPe-vmtohostnumber+"remaining pe");
			
			    Host h=new Host(hostId, new RamProvisionerSimple(ram),
			   		new BwProvisionerSimple(bw), storage, peList,new VmSchedulerTimeShared(peList));
			//	hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
			  // 		new BwProvisionerSimple(bw), storage, peList,new VmSchedulerTimeShared(peList)));
				hostList.add(h);
				hostId++;
			}
			
			

		}

		Log.printLine(hostList.get(0).getNumberOfPes()+"mips"+hostList.get(0).getTotalMips());
		Log.printLine(hostList.get(1).getNumberOfPes()+"mips"+hostList.get(1).getTotalMips());
		Log.printLine(hostList.get(2).getNumberOfPes()+"mips"+hostList.get(2).getTotalMips());
		Log.printLine(hostList.get(3).getNumberOfPes()+"mips"+hostList.get(3).getTotalMips());
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
				arch, os, vmm, hostList, time_zone, cost, costPerMem,costPerStorage, costPerBw);

		QDatacenter datacenter = null;
		try {
			datacenter = new QDatacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;

	}

	public static void printUtilization(QDatacenter datacenter0){
		List<Host> hosts=datacenter0.getHostList();
		double totalutilization=0;
		double aveutilization=0;
		double aveu=0;
		double totalworkload=0;
		double aveworkload=0;
		int j=0;
		double power=0;double totalpower=0;double avepower=0;double totalpowers=0;
		for(Host host:hosts){
			for(int i=0;i<host.getStateHistory().size();i++){
				totalutilization=host.getStateHistory().get(i).getUtilization()+totalutilization;				
						if(host.getStateHistory().get(i).getUtilization()>0.5){
							power=(-7.79979*host.getStateHistory().get(i).getUtilization()+150.56995)*host.getStateHistory().get(i).getWorkLoad();
						
						}else{
							power=(132.47581*host.getStateHistory().get(i).getUtilization()+8.84754)*host.getStateHistory().get(i).getWorkLoad();
						}
						totalpower=power+totalpower;
			}
			totalpowers=totalpower/host.getStateHistory().size();
			aveutilization=totalutilization/host.getStateHistory().size();
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
		List<Double> finishTimeEachVm = new ArrayList<Double>(); 
		List<Double> totalWaitingTimeEachVm = new ArrayList<Double>(); 
		List<Double> totalUtilizingTimeEachVm = new ArrayList<Double>(); 
		List<Integer> numCloudletsEachVm = new ArrayList<Integer>(); 
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
				totalWaitingTimeEachVm.set(cloudlet.getVmId(),totalWaitingTimeEachVm.get(cloudlet.getVmId())+ cloudlet.getWaitingTime());
				watingtime=watingtime+cloudlet.getExecStartTime()-cloudlet.getArrivetime();
				wwatingtime=wwatingtime+cloudlet.getFinishTime()-cloudlet.getArrivetime();
				if (finishTimeEachVm.get(cloudlet.getVmId()) < cloudlet.getExecStartTime()) { 
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
