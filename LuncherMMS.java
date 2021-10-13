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

public class LuncherMMS {
	private static final int NUM_VM =6; 
	private static final int NUM_CLOUDLET = 1000;
	private static final double POISSON_LAMBDA = 20;
	private static final double LETS_WAVE_INTERVAL = 1.0; 
	private static final int MAX_LENGTH_WAITING_QUEUE = 1; 
	

	private static final int VM_MIPS[] = { 1000, 1000, 1000, 1000, 1000, 1000
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

	
		

	    	VmCloudletAssigner vmCloudletAssigner = new VmCloudletAssignermms();

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
		int bw = 10000;

		for (int i = 0; i < numHost; i++) {
			List<Pe> peList = new ArrayList<Pe>();
			peList.add(new Pe(0,new PeProvisionerSimple(vmMips[i])));
			hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw), storage, peList,new VmSchedulerTimeShared(peList)));
			hostId++;
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

				wwatingtime=wwatingtime+cloudlet.getFinishTime()-cloudlet.getArrivetime();

				watingtime=watingtime+cloudlet.getExecStartTime()-cloudlet.getArrivetime();
				if (finishTimeEachVm.get(cloudlet.getVmId()) < cloudlet.getExecStartTime()) { 
					finishTimeEachVm.set(cloudlet.getVmId(),cloudlet.getExecStartTime());
					
				}
				if(finishtime<cloudlet.getExecStartTime()){
					finishtime=cloudlet.getExecStartTime();
				}

				totalUtilizingTimeEachVm.set(cloudlet.getVmId(), totalUtilizingTimeEachVm.get(cloudlet.getVmId())+ cloudlet.getFinishTime()
								- cloudlet.getSubmissionTime());

				numCloudletsEachVm.set(cloudlet.getVmId(),numCloudletsEachVm.get(cloudlet.getVmId()) + 1);
				
			} else {

			}

		}

		int s=0;

		double queue=watingtime/finishtime/NUM_VM;
		double queues=wwatingtime/finishtime/NUM_VM;

		System.out.println(queue+"cloudlets/ms");
		
		System.out.println(queues+"cloudlets/ms");

		System.out.println("average waiting time:" + watingtime/size + "s");
		Log.printLine("Number of Success cloudlet : " + success);

		System.out.println("average response time:"+ wwatingtime/size + "s");
	}


}
