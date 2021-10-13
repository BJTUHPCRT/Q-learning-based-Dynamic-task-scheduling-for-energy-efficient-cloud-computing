package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;

public class QDatacenterBroker extends DatacenterBroker {


	private VmCloudletAssigner vmCloudletAssigner;	// policy of task assignment
	private static final int CREATE_CLOUDLETS = 49;
	private static List<Double> delayList;			// List of delay times submitted by each wave of cloud tasks
	private static List<Integer> numLetList;		
	private int currWave;							
	private int numlets;							
	private int numVm;								
	private int vmMips[]; 							
	private double lambda;							
	private double numletWaveInterval;				
	private static int cloudletWaitingQueueLength;	

	public QDatacenterBroker(String name,
			VmCloudletAssigner vmCloudletAssigner, int numlets, int numVm,
			double lambda, double numletWaveInterval,int[] vmMips,
			int cloudletWaitingQueueLength) throws Exception {
		super(name);
		setVmCloudletAssigner(vmCloudletAssigner);
		this.numlets = numlets;
		this.numVm = numVm;
		this.vmMips = vmMips;
		this.lambda = lambda;
		this.numletWaveInterval = numletWaveInterval;
		this.cloudletWaitingQueueLength = cloudletWaitingQueueLength;
		currWave = 0;
	}



	@Override

	public void processOtherEvent(SimEvent ev) {
		switch (ev.getTag()) {
		case CREATE_CLOUDLETS: // The generation and arrival of a new wave of cloud tasks
			int waveId = ((Integer) ev.getData()).intValue(); // Get the current wave number
			System.out.println(CloudSim.clock() + ":" + (waveId + 1)+ " cloudlet start arrive");
			double time=CloudSim.clock();
			
			submitCloudletList(createCloudlets(getId(), numLetList.get(waveId),waveId * 1000 + 1000)); // Generate a wave of cloud tasks
			if(waveId>0){
				for (int s=0;s<numLetList.get(waveId);s++){

					for(Cloudlet c:getCloudletList()){
						c.setArrivetime(time);
					}
				  }
				}
			currWave++;
			if (waveId > 0) {		
				submitCloudlets(); 		
			}
			CloudSim.resumeSimulation();
	//		Log.printLine(getCloudletList().size()+"gggsss");
			for(Cloudlet c:getCloudletList()){
				c.setArrivetime(time);
		//		Log.printLine(time);
			}
			break;
		case QDatacenter.CLOUDLET_SUBMIT_FAILED:
			cloudletSubmitFailed(ev);
			break;
		default:
			Log.printLine(getName() + ": unknown event type");
			break;
		 }
	}



	@Override
	public void startEntity() {	
		Log.printLine(super.getName() + " is starting...");
		try {
			createCloudletWave(numlets, lambda, numletWaveInterval); //Generate a delay list for the start of each wave cloud task
		} catch (Exception e) {
			System.out.println("Error generating cloud task queue!");
			e.printStackTrace();
		}
		setVmList(createVM(getId(), numVm, vmMips, 0)); 

		for (int i = 0; i < delayList.size(); i++) {
			schedule(getId(), delayList.get(i), CREATE_CLOUDLETS, i); 
		}
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);	
	}



	@Override
	protected void submitCloudlets() { 
		// Bind tasks to virtual machines
		Log.printLine("SSSSubmitCloudlet() size: " + getCloudletList().size());

		if(getCloudletList().size()==0) return;
		List<Cloudlet> assignedCloudletList = getVmCloudletAssigner().cloudletAssign(getCloudletList(), getVmList()); //Assign tasks to virtual machines by task distributor

		for (Cloudlet cloudlet : assignedCloudletList) {
			Vm vm;
			if (cloudlet.getVmId() != -1) {
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId()
							+ ": bount VM not available");
					continue;
				}
				 Log.printLine(CloudSim.clock() + ": " + getName()
				 + ": Sending cloudlet " + cloudlet.getCloudletId()
				 + " to VM #" + vm.getId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()),
						CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
				cloudletsSubmitted++;
				getCloudletSubmittedList().add(cloudlet);
			}					
		}
		getCloudletList().clear(); //The task has been submitted to the task dispatcher, either has been allocated or entered the main task queue
	}



	@Override

	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);

		cloudletsSubmitted--;
		List<Cloudlet> assignedCloudletList = getVmCloudletAssigner().cloudletAssign(null, getVmList());
		// Schedule a task from the main queue
		if (assignedCloudletList != null) {
			for (Cloudlet cl : assignedCloudletList) {
				Vm vm;
				if (cl.getVmId() != -1) {
				vm = VmList.getById(getVmsCreatedList(), cl.getVmId());
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": Sending cloudlet " + cl.getCloudletId()
						+ " to VM #" + vm.getId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()),CloudSimTags.CLOUDLET_SUBMIT, cl);
				cloudletsSubmitted++;
				getCloudletSubmittedList().add(cl);
				}else{
					//Log.printLine("assignedCloudletList Assign Error! Cloudlet#"+cl.getCloudletId());
				}
			}
		} else {
			//Log.printLine("CloudletReturn Assign NULL!");
		}		 


		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) {
			if (currWave < delayList.size()) {
				System.out.println((delayList.size() - currWave)+ "wave have not arrived......");
				return;
			}

			Log.printLine(CloudSim.clock() + ": " + getName()+ ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();

		} else { 
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				for (Vm vm : getVmsCreatedList()) {
					Log.printLine("QueueLeft:"+ ((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler())
									.getCloudletWaitingQueue().size());
				}
				if (currWave < delayList.size()) {
					System.out.println("submit" + currWave + "wave tasks......");
					return;

				}

				clearDatacenters();
				createVmsInDatacenter(0);

			}

		}



	}



	protected void cloudletSubmitFailed(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		cloudletsSubmitted--;
		Log.printLine("\nQDatacenterBroker received CLOUDLET "
				+ cloudlet.getCloudletId() + " Failed cloudletList Size:"
				+ getCloudletList().size());
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		cloudletList.add(cloudlet);
		submitCloudletList(cloudletList);
		Log.printLine("After submit cloudletList Size:"+ getCloudletList().size());
		submitCloudlets();
	}

	

	public VmCloudletAssigner getVmCloudletAssigner() {
		return vmCloudletAssigner;
	}



	public void setVmCloudletAssigner(VmCloudletAssigner vmCloudletAssigner) {
		this.vmCloudletAssigner = vmCloudletAssigner;
	}



	private static List<Vm> createVM(int userId, int vms,int[] vmMips, int idShift) {
		LinkedList<Vm> list = new LinkedList<Vm>();
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 1000;// 250;
		long bw = 1000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name
		Vm[] vm = new Vm[vms];

		for (int i = 0; i < vms; i++) {
			vm[i] = new Vm(idShift + i, userId, vmMips[i], pesNumber, ram, bw, size,vmm, new QCloudletSchedulerSpaceShared(idShift + i,
							cloudletWaitingQueueLength));
			list.add(vm[i]);

		}
		return list;

	}


	public static List<Cloudlet> createCloudlets(int userId, int cloudlets,int idShift) { 
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
		long length = 4000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		double deadline=100;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		Cloudlet[] cloudlet = new Cloudlet[cloudlets];
		for (int i = 0; i < cloudlets; i++) {
			length=(long)(2500*Math.random()+500);//[500,3000]
			fileSize=(long)(500*Math.random()+1);//[1,512]
			outputSize=(long)(500*Math.random()+1);//[1,512]
			deadline=(int)(70*Math.random()+50);//[50,120]
			cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber,fileSize, outputSize, utilizationModel, utilizationModel,utilizationModel);
			cloudlet[i].setUserId(userId);
			cloudlet[i].setDeadline(deadline);
			list.add(cloudlet[i]);
		}
		return list;

	}

	public static List<Cloudlet> createCloudlet(int userId, int cloudlets,int idShift) {
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		Cloudlet[] cloudlet = new Cloudlet[cloudlets];
		for (int i = 0; i < cloudlets; i++) {
			cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber,fileSize, outputSize, utilizationModel, utilizationModel,utilizationModel);
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}
		return list;

	}



	public static double f_Poisson(double lambda, int k) {
		double e = 2.7182818284;
		double result;
		result = Math.pow(e, -lambda) * Math.pow(lambda, k);
		for (int i = 1; i <= k; i++) {
			result = result / i;
		}
		return result;

	}



	public static void createCloudletWave(int numlets, double lambda,
			double numletWaveInterval) throws Exception {
		int numLetWave = 100000;
		ArrayList<Integer> numLet = new ArrayList<Integer>();
		int tmpCloudlets = 0;
		double tmp = numlets;
		for (int i = 0; i <= numLetWave; i++) {
			 int x = 0;
		        double y = Math.random(), cdf = f_Poisson(lambda, x);
		        while (cdf < y) {
		            x++;
		            cdf += f_Poisson(lambda, x);
		        }
		        numLet.add(i, x);
		//	numLet.add(i, (int) (tmp * f_Poisson(lambda, i)));
			if (numLet.get(i) <= 0) {
				numLet.set(i, 1);
			}
			if ((tmpCloudlets + numLet.get(i)) > numlets) {
				numLet.set(i, numlets - tmpCloudlets);
				System.out.println("adjust to: numLet[" + i + "]: "+ numLet.get(i) + "\tlambda: " + lambda+ "\tf_Poisson: " + f_Poisson(lambda, i));
				break;

			}

			tmpCloudlets += numLet.get(i);
			System.out.println("numLet[" + i + "]: " + numLet.get(i)+ "\tlambda: " + lambda + "\tf_Poisson: "+ f_Poisson(lambda, i));

		}

		delayList = new LinkedList<Double>();
		numLetList = new LinkedList<Integer>();
		for (int i = 0; i < numLet.size(); i++) {
			delayList.add(i, numletWaveInterval * i);
			numLetList.add(i, numLet.get(i));
		}
	}

	public static List<Integer> getNumLetList() {
		return numLetList;
	}


	public static void setNumLetList(List<Integer> numLetList) {
		QDatacenterBroker.numLetList = numLetList;
	}

}