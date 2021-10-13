package org.cloudbus.cloudsim.examples;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;

//import sun.org.mozilla.javascript.internal.ast.Assignment;

public class VmCloudletAssignerReinforcementLearning extends VmCloudletAssigner { //reinforcement learning policy implement

	private VirtualQueueSize vQueueSize = VirtualQueueSize.getInstance();
	private GenExcel genExcel = null;
	private static double gamma;  
	private static double alpha;   
	private static double epsilon; 
	private static Map<String, Map<Integer, Double>> QList = new HashMap<String, Map<Integer, Double>>(); //Q table
	int s=0;
	private static double Utilization=0;
	private static double Utilization1=0;
	private static double Utilization2=0;
	private static double Utilization3=0;
	private static double Utilization4=0;
	private static double Utilization5=0;
	private static double Utilization6=0;
	private static double Utilization7=0;
	private static double Utilization8=0;
	private static double Utilization9=0;
	private static double Utilization10=0;
	private static double Utilization11=0;
	private static double Utilization12=0;
	private static double Utilization13=0;
	private static double Utilization14=0;
	private static double Utilization15=0;
	private static double Utilization16=0;
	private static double Utilization17=0;
	private static double Utilization18=0;
	private static double Utilization19=0;

	long totalworkload=0;
	private final List<HostStateHistoryEntry> stateHistory = new LinkedList<HostStateHistoryEntry>();

	
	
	public VmCloudletAssignerReinforcementLearning( double gamma, double alpha, double epsilon, GenExcel genExcel) {
		this.gamma = gamma;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.genExcel=genExcel;
		this.genExcel.init();
		QList=this.genExcel.init();

	}

	@Override
	public List<Cloudlet> cloudletAssign(List<Cloudlet> cloudletList, List<Vm> vmList) {
		double r=-1;
	
		if (vmList != null || vmList.size() != 0) {
			List<Cloudlet> toAssignCloudletList = getToAssignCloudletList(cloudletList); //Initial startup authorization authorization
			if (toAssignCloudletList.size() < 1) { //There are no tasks waiting to be assigned, return an empty list
				return null;
//				System.exit(0);
			}
	
			List<Map<String, Integer>>[] vmWaitingQueueSizeList =new ArrayList[10000];
			List<Map<String, Integer>>[] tmpSizeList =new ArrayList[10000];
		
//			//Task preprocessing
			
//			for(Cloudlet c:toAssignCloudletList){
//				
//				if(c.getCloudletLength()<2000){
//					c.setSe(3);
//				}else if(c.getCloudletLength()>2000&&c.getCloudletLength()<4000){
//					c.setSe(2);
//				}else{
//					c.setSe(1);
//				}
//				
//				if(c.getWaitingTime()<5){
//					c.setWe(1);
//				}else if(c.getWaitingTime()>5&&c.getWaitingTime()<15){
//					c.setWe(2);
//				}else{
//					c.setWe(3);
//				}
//				
//				if(c.getDeadline()<10){
//					c.setDe(3);
//				}else if(c.getDeadline()>10&&c.getDeadline()<18){
//					c.setDe(2);
//				}else{
//					c.setDe(1);
//				}
//				
//			}
//			
//			//The proportion of the three indicators is 5:3:2
//			for(Cloudlet c:toAssignCloudletList){
//				int rank=(int) (0.5*c.getSe()+0.3*c.getWe()+0.2*c.getDe());
//				c.setRank(rank);
//			}
			
			
			//sort tasks
			Collections.sort(toAssignCloudletList,new CloudletComparator());
			int i=0;
			int k=0;
			int m = vmList.size();	
			int n = toAssignCloudletList.size();	//Number of task lists to be allocated
			int maxCloudletsWaitingLength = vQueueSize.getMaxLength();	//Maximum length of subtask queue			
		    int numFreeVm = m;
			for (int s = 0; s< 10000; s++) {
				vmWaitingQueueSizeList[s] = new ArrayList<Map<String, Integer>>();
				vmWaitingQueueSizeList[s] = initVmWaitingQueueSizeList();//Initialize the virtual subqueue captain list
				// vqueuesize[s]=VirtualQueueSize.getInstance();
			}
		//	List<Map<String, Integer>> tmpSizeList = updateTmpSizeList(-1, numFreeVm, vmWaitingQueueSizeList);//Temporary queue
			for (int j = 0; j < 10000; j++) {
				tmpSizeList[j] = new ArrayList<Map<String, Integer>>();
				tmpSizeList[j] = updateTmpSizeList(-1, numFreeVm, vmWaitingQueueSizeList[j]);
			}
			
            for(Cloudlet c:toAssignCloudletList){
				totalworkload=totalworkload+c.getCloudletLength();
			}
					        							 
			for (i = 0; i < n; i++) { //Assign tasks to suitable virtual machines

				for (int j = 0; j < m; j++) { //Output the length of the waiting queue of all virtual machines
					System.out.print(vmWaitingQueueSizeList[k].get(j).get("size") + " ");
				}
				System.out.println();

				int index = createAction(numFreeVm, vmWaitingQueueSizeList[k]);//Select action: the id  of the selected virtual machine, corresponding to the column number in the Q value table
				int mSize =  tmpSizeList[k].get(index).get("size");
				if (mSize >= maxCloudletsWaitingLength) {// If the selected queue is full, remove this queue and select again
					
					if (numFreeVm >1) {//If the number of idle queues can be reduced to 1 or more, the temporary queue is updated, that is, the full queue is thrown away
						tmpSizeList[k] = updateTmpSizeList(index, numFreeVm--, tmpSizeList[k]);
//						System.out.println(numFreeVm);
						i--;
						continue;
					}
					else { //The waiting queues of all virtual machines are full
						Log.printLine("mSize=50 list(0):" + mSize);
						break;
					}
					
				}				
				int id = tmpSizeList[k].get(index).get("id"); //The id of the selected virtual machine

			if(k==9){    //Partial update steps
				//Since VirtualQueueSize is a singleton mode, only one instance is created
				if(tmpSizeList[k].get(index).get("size")<maxCloudletsWaitingLength){
					tmpSizeList[k].get(index).put("size", ++mSize); //Update the status of the temporary virtual machine waiting queue length list
					for (int j = 0; j < m; j++) {                //Update the status of the virtual machine waiting queue length list
						if (vmWaitingQueueSizeList[k].get(j).get("id") == tmpSizeList[k].get(index).get("id")) {
							vmWaitingQueueSizeList[k].get(j).put("size", mSize);
							index = j;
							break;
						}
					}
					r=updateQList(index, m, vmList, vmWaitingQueueSizeList[k]); //update Q table				
				} else { //The waiting queue of the selected virtual machine is full
					Log.printLine(index + "Index Assign Full Error!! Vm#" + id
							+ " mSize:" + mSize + " vQueueSize:"+" "+k+" "
							+  tmpSizeList[k].get(index).get("id"));
					System.exit(0);
				}
			}
				
				if(k==0){
					if (vQueueSize.increment(id)) { 
						tmpSizeList[k].get(index).put("size", ++mSize); 
						for (int j = 0; j < m; j++) {            
							if (vmWaitingQueueSizeList[k].get(j).get("id") == tmpSizeList[k].get(index).get("id")) {
								vmWaitingQueueSizeList[k].get(j).put("size", mSize);
								index = j;
								break;
							}
						}
						r=updateQList(index, m, vmList, vmWaitingQueueSizeList[k]); 				
						toAssignCloudletList.get(i).setVmId(id); 					
					} else { 
						Log.printLine(index + "Index Assign Full Error!! Vm#" + id
								+ " mSize:" + mSize + " vQueueSize:"
								+ vQueueSize.getQueueSize().get(id));
						System.exit(0);
					}
				}	
	 }
			
			List<Cloudlet> assignedCloudletList = getAssignedCloudletList(i, toAssignCloudletList); //Get the list of successfully assigned tasks

			finishAssign(toAssignCloudletList); 
		
			Log.printLine("Assign Finished! Left:"
					+ getGlobalCloudletWaitingQueue().size() + " Success:"
					+ assignedCloudletList.size());
	 
			return assignedCloudletList;
    
		} else { 
			Log.printLine("VmCloudletAssignerLearning No VM Error!!");
			return null;
		}
	
	}
	
	private int createAction(int numVm, List<Map<String, Integer>> vmWaitingQueueSizeList) { //Generate the action(vm id) of selecting a virtual machine
		int current_action;       
		int x = randomInt(0, numVm); 
		String state_idx = createState_idx(numVm, vmWaitingQueueSizeList); //The row number of the Q value table generated according to the current state of the waiting queue of each virtual machine
		System.out.println(state_idx+" state");
		if (!QList.containsKey(state_idx)) { //If this row does not exist in the Q value table, initialize this row
			initRowOfQList(state_idx, numVm);
		}else{
			System.out.println(state_idx+"already exit");
		}
		
		//generate the action
		if (((double) x / 100) < (1 - epsilon)) { 
			int umax = 0;
			double tmp = -20000.0;
			for (int i = 0; i < numVm; i++) {
				if (tmp < QList.get(state_idx).get(i)) {
					tmp = QList.get(state_idx).get(i);
					umax = i;
				}
			}
			if (tmp == -20000.0) { 
				System.out.println("no exploit......!");
				System.exit(0);
			}
			current_action = umax;
		}
		else{ //explore
			current_action = randomInt(0, numVm - 1); 
		}
		return current_action;
	}
	
	private double updateQList(int action_idx, int numVm, List<Vm> vmList, List<Map<String, Integer>> vmWaitingQueueSizeList) {
		double sample = ((QCloudletSchedulerSpaceShared) vmList   
				.get(action_idx).getCloudletScheduler()).getAverageWaitingTime();
		if (sample == 0.0) {          //If this value is 0, initialize to a large enough number
			sample = 1000000.0;
		}
	
		double reward=0;
		double h=0;double vms=0;
		double s=0;double s1=0;double s2=0;double s3=0;double s4=0;double s5=0;
		double s6=0;double s7=0;double s8=0;double s9=0;double s10=0;double s11=0;
		double s12=0;double s13=0;double s14=0;double s15=0;double s16=0;double s17=0;
		double s18=0;double s19=0;
		double u1=0;double u=0;double u2=0;double u3=0;double u4=0;double u5=0;
		double u6=0;double u7=0;double u8=0;double u9=0;double u10=0;double u11=0;
		double u12=0;double u13=0;double u14=0;double u15=0;double u16=0;double u17=0;
		double u18=0;double u19=0;
		double ts=0;double a=0;int t=0;
		long len=0;long len1=0;long len2=0;long len3=0;long len4=0;long len5=0;
		long len6=0;long len7=0;long len8=0;long len9=0;long len10=0;long len11=0;
		long len12=0;long len13=0;long len14=0;long len15=0;long len16=0;long len17=0;
		long len18=0;long len19=0;
		for(Vm vm:vmList){
			switch(vm.getHost().getId()){
			
			case 0:
			  t=0;
              for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
            	double tt=  (((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT());
            	t++;   
            	len=len+rcl.getCloudletLength();
            	Log.printLine("cloudlet"+rcl.getCloudlet().getCloudletId()+"\t"+tt);
              }
              vm.getHost().setWorkload(len);
              vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
			  if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
				s++;
			  }
			  u++;break;
			  
			case 1:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len1=len+rcl.getCloudletLength();
	             }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s1++;
				}
				u1++;break;
				
			case 2:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){	            	  
	            	t++; 
	            	len2=len+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s2++;
				}
				u2++;break;
				
			case 3:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len3=len3+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s3++;
				}
				u3++;break;
				
			case 4:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len4=len4+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s4++;
				}
				u4++;break;
				
			case 5:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s5++;
				}
				u5++;break;
				
			case 6:
				t=0;
	              for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s6++;
				}
				u6++;break;
				
			case 7:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s7++;
				}
				u7++;break;
				
			case 8:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s8++;
				}
				u8++;break;
				
			case 9:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s9++;
				}
				u9++;break;
				
			case 10:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s10++;
				}
				u10++;break;
				
			case 11:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s11++;
				}
				u11++;break;
				
			case 12:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s12++;
				}
				u12++;break;
				
			case 13:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s13++;
				}
				u13++;break;
				
			case 14:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s14++;
				}
				u14++;break;
				
			case 15:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s15++;
				}
				u15++;break;
				
			case 16:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s16++;
				}
				u16++;break;
				
			case 17:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s17++;
				}
				u17++;break;
				
			case 18:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s18++;
				}
				u18++;break;
				
			case 19:
				t=0;
	            for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){
	            	t++;   
	            	len5=len5+rcl.getCloudletLength();
	              }
	            vm.getHost().setWorkload(len);
	            vm.setCloudnum(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size());
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s19++;
				}
				u19++;break;
			}
		}
		Utilization=s/u;Utilization1=s1/u1;Utilization2=s2/u2;Utilization3=s3/u3;Utilization4=s4/u4;Utilization5=s5/u5;
		Utilization6=s6/u6;Utilization7=s7/u7;Utilization8=s8/u8;Utilization9=s9/u9;Utilization10=s10/u10;Utilization11=s11/u11;
		Utilization12=s12/u12;Utilization13=s13/u13;Utilization14=s14/u14;Utilization15=s15/u15;Utilization16=s16/u16;Utilization17=s17/u17;
		Utilization18=s18/u18;Utilization19=s19/u19;

		for (Vm vm:vmList) {
		    if(vm.getHost().getId()==0){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization,sample);
		      Log.printLine(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT()+"\t"+Utilization);
		    }
		    if(vm.getHost().getId()==1){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization1,sample);
		    }
		    if(vm.getHost().getId()==2){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization2,sample);
		    }
		    if(vm.getHost().getId()==3){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization3,sample);
		    }
		    if(vm.getHost().getId()==4){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization4,sample);
		    }
		    if(vm.getHost().getId()==5){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization5,sample);
		    }
		    if(vm.getHost().getId()==6){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization6,sample);
		    }
		    if(vm.getHost().getId()==7){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization7,sample);
		    }
		    if(vm.getHost().getId()==8){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization8,sample);
		    }
		    if(vm.getHost().getId()==9){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization9,sample);
		    }
		    if(vm.getHost().getId()==10){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization10,sample);
		    }
		    if(vm.getHost().getId()==11){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization11,sample);
		    }
		    if(vm.getHost().getId()==12){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization12,sample);
		    }
		    if(vm.getHost().getId()==13){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization13,sample);
		    }
		    if(vm.getHost().getId()==14){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization14,sample);
		    }
		    if(vm.getHost().getId()==15){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization15,sample);
		    }
		    if(vm.getHost().getId()==16){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization16,sample);
		    }
		    if(vm.getHost().getId()==17){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization17,sample);
		    }
		    if(vm.getHost().getId()==18){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization18,sample);
		    }
		    if(vm.getHost().getId()==19){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization19,sample);
		    }
		    
		}

		if(vmList.get(action_idx).getHost().getStateHistory().size()-2>=0){
			if(vmWaitingQueueSizeList.get(action_idx).get("size")==0&&vmList.get(action_idx).getHost().getStateHistory().get(vmList.get(action_idx).getHost().getStateHistory().size()-2).getUtilization()<=vmList.get(action_idx).getHost().getStateHistory().get(vmList.get(action_idx).getHost().getStateHistory().size()-1).getUtilization()&&vmList.get(action_idx).getHost().getStateHistory().get(vmList.get(action_idx).getHost().getStateHistory().size()-2).getWorkLoad()<=vmList.get(action_idx).getHost().getStateHistory().get(vmList.get(action_idx).getHost().getStateHistory().size()-1).getWorkLoad()){
				                                reward=1;	
			}else{
				if(((QCloudletSchedulerSpaceShared) vmList.get(action_idx).getCloudletScheduler()).getAverageWaitingTime()<=((QCloudletSchedulerSpaceShared) vmList.get(action_idx).getCloudletScheduler()).getPreviousAverageWaitingTime()&&s/u>=0.3&&s1/u1>=0.3&&s2/u2>=0.3&&s3/u3>=0.3&&s4/u4>=0.3&&s5/u5>=0.3&&vmList.get(action_idx).getCloudnum()==0){
					reward=0;
				}else{
					 if(vmWaitingQueueSizeList.get(action_idx).get("size")>=1){
						 reward=-1;
					 }else{
						 
					 }
					
				}
				
			}
		}else{
		
			if(((QCloudletSchedulerSpaceShared) vmList.get(action_idx).getCloudletScheduler()).getAverageWaitingTime()<=((QCloudletSchedulerSpaceShared) vmList.get(action_idx).getCloudletScheduler()).getPreviousAverageWaitingTime()){
				reward=1;
			}else{
				 if(vmWaitingQueueSizeList.get(action_idx).get("size")>=1){
					 reward=-1;
				 }else{
					 if(s/u>=0.3&&s1/u1>=0.3&&s2/u2>=0.3&&s3/u3>=0.3&&s4/u4>=0.3&&s5/u5>=0.3&&
							 s6/u6>=0.3&&s7/u7>=0.3&&s8/u8>=0.3&&s9/u9>=0.3&&s10/u10>=0.3&&s11/u11>=0.3&&
							 s12/u12>=0.3&&s13/u13>=0.3&&s14/u14>=0.3&&s15/u15>=0.3&&s16/u16>=0.3&&s17/u17>=0.3&&
							 s18/u18>=0.3&&s19/u19>=0.3&&
							 vmList.get(action_idx).getCloudnum()==0)
						 reward=1;
				 }		
			}
		
		}
		
	//	 reward =  1/sample; The reward value generated by the reciprocal of the average waiting time of tasks in the selected virtual machine and the resource utilization of the host
		String state_idx = createLastState_idx(action_idx, numVm, vmWaitingQueueSizeList); //The status line number when the current task is not assigned to the virtual machine queue
		String next_state_idx = createState_idx(numVm, vmWaitingQueueSizeList);            //The status line number after the current task is assigned to the virtual machine queue
		
		System.out.println(((QCloudletSchedulerSpaceShared) vmList.get(action_idx).getCloudletScheduler()).getAverageWaitingTime()+" "+reward);
		System.out.println("\n output state_idx\n" + state_idx);
		System.out.println("\n output next_state_idx\n" + next_state_idx);
		
		if (!QList.containsKey(next_state_idx)) { //If the updated row does not exist in the Q value table, initialize it
			initRowOfQList(next_state_idx, numVm);
		}
		double QMaxNextState = -1.0;
		for (int i = 0; i < numVm; i++) { //Get the maximum value of the updated status line
			if (QMaxNextState < QList.get(next_state_idx).get(i)) {
				QMaxNextState = QList.get(next_state_idx).get(i);
			}
		}
		double QValue = QList.get(state_idx).get(action_idx) //Q function
				+ alpha * (reward + gamma * QMaxNextState - QList.get(state_idx).get(action_idx));
		
		QList.get(state_idx).put(action_idx, QValue);
		
		this.genExcel.fillData(QList, state_idx, action_idx, QValue);
		return reward;
	}
	
	private int randomInt(int min, int max) { 
		if (min == max) {
			return min;
		}
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}
	
	private String createLastState_idx(int action_idx, int numVm, 
			List<Map<String, Integer>> vmWaitingQueueSizeList) {
		String state_idx = "";
		for (int i = 0; i < numVm; i++) { 
			if (i == action_idx) { 
				state_idx += "-" + (vmWaitingQueueSizeList.get(i).get("size").intValue() - 1);
			}
			else {
				state_idx += "-" + vmWaitingQueueSizeList.get(i).get("size").intValue();
			}
		}
		return state_idx;
	}
	
	private String createState_idx(int numVm, List<Map<String, Integer>> vmWaitingQueueSizeList) { 
		String state_idx = "";
		for (int i = 0; i < numVm; i++) { 
			state_idx += "-" + vmWaitingQueueSizeList.get(i).get("size").intValue();
		}
		return state_idx;
	}
	
	private void initRowOfQList(String state_idx, int numColumn) { 
		QList.put(state_idx, new HashMap<Integer, Double>());
		for (int i = 0; i < numColumn; i++) {
			QList.get(state_idx).put(i, 0.0);
		}
	}
	
	private List<Map<String, Integer>> updateTmpSizeList(int index, int numFreeVm, //Update the status of the temporary virtual machine waiting queue length list
			List<Map<String, Integer>> originSizeList) {
		List<Map<String, Integer>> tmp = new ArrayList<Map<String, Integer>>();
		for (int j = 0; j < numFreeVm; j++) {
			if (index == -1 || originSizeList.get(j).get("id") != originSizeList.get(index).get("id")) { //Remove the selected virtual machine (virtual machine waiting queue full) from the temporary list
				tmp.add(originSizeList.get(j));                                                         
			}
		}
		return tmp;
	}
	
	private class CloudletComparator implements Comparator<Cloudlet>{

		@Override
		public int compare(Cloudlet o1, Cloudlet o2) {
			// TODO Auto-generated method stub
			return -((int)o1.getRank()-o2.getRank());
		}
		
	}
}
