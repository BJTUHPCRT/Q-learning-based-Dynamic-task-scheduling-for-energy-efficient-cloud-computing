package org.cloudbus.cloudsim.examples;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
public class VmCloudletAssignerFair extends VmCloudletAssigner { 


@Override
public List<Cloudlet> cloudletAssign(List<Cloudlet> cloudletList,
		List<Vm> vmList) {
	if (vmList != null || vmList.size() != 0) {
		List<Cloudlet> toAssignCloudletList = getToAssignCloudletList(cloudletList); 
		if (toAssignCloudletList.size() < 1) { 
			return null;
			// System.exit(0);
		}

		int m = vmList.size();
		int n = toAssignCloudletList.size(); 
		int maxCloudletsWaitingLength = vQueueSize.getMaxLength(); 
		List<Map<String, Integer>> vmWaitingQueueSizeList = initVmWaitingQueueSizeList();
		
		long totalworkload=0;
		for(Cloudlet c:toAssignCloudletList){
			totalworkload=totalworkload+c.getCloudletLength();
		}

		int i;
		for (i = 0; i < n; i++) {
			int index = 0;
			int mSize = maxCloudletsWaitingLength + 1;
			for (int j = 0; j < m; j++) {
				if (mSize > vmWaitingQueueSizeList.get(j).get("size")) {
					mSize = vmWaitingQueueSizeList.get(j).get("size");
					index = j;
				}
			}

			for (int j = 0; j < m; j++) { 
				System.out.print(vmWaitingQueueSizeList.get(j).get("size")
						+ " ");
			}
			System.out.println();

			if (mSize >= maxCloudletsWaitingLength) {
				Log.printLine("mSize=50 list(0):" + mSize);
				break;
			}

			int id = vmWaitingQueueSizeList.get(index).get("id"); 
			if (vQueueSize.increment(id)) { 
				vmWaitingQueueSizeList.get(index).put("size", ++mSize);
				toAssignCloudletList.get(i).setVmId(id); 

			} else { 
				Log.printLine(index + "Index Assign Full Error!! Vm#" + id
						+ " mSize:" + mSize + " vQueueSize:"
						+ vQueueSize.getQueueSize().get(id));
				System.exit(0);
			}
		}
	
		double s=0;double s1=0;double s2=0;double s3=0; double s4=0; double s5=0; double u1=0;double u=0;
		double u2=0;double u3=0;double u4=0;double u5=0;double Utilization=0; double Utilization1=0;
		 double Utilization2=0;		double Utilization3=0;   double Utilization4=0; double Utilization5=0; long len=0;long len1=0;
		 long len2=0;long len3=0;double w=0;long len4=0;long len5=0;
		 
		for(Vm vm:vmList){
			switch(vm.getHost().getId()){
			
			case 0:
				for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){	            	
		            	len=len+rcl.getCloudletLength();}
		        vm.getHost().setWorkload(len);
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s++;
				}
				u++;break;
				
			case 1:
				 for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){        	
					 len1=len1+rcl.getCloudletLength();}
		        vm.getHost().setWorkload(len1);		              
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s1++;
				}
				u1++;break;
				
			case 2:
				for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){		            	
					 len2=len2+rcl.getCloudletLength();}
		        vm.getHost().setWorkload(len2);		              
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s2++;
				}
				u2++;break;
				
			case 3:
				 for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){	            	
		        len3=len3+rcl.getCloudletLength();}
		        vm.getHost().setWorkload(len3);		              
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s3++;
				}
				u3++;break;
				
			case 4:			
				 for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){		            	
		            	len4=len4+rcl.getCloudletLength();}
		        vm.getHost().setWorkload(len4);
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s4++;
				}
				u4++;break;
				
			case 5:
				
				for(ResCloudlet rcl: ((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList()){		            	
		            	len5=len5+rcl.getCloudletLength();}
		         vm.getHost().setWorkload(len5);
				if(((CloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getCloudletExecList().size()!=0){
					s5++;
				}
				u5++;break;				
			}
		}
		Utilization=s/u;Utilization1=s1/u1;Utilization2=s2/u2;Utilization3=s3/u3;Utilization4=s4/u4;Utilization5=s5/u5;
		
		w=len*1.0/totalworkload;
		Log.printLine("length"+totalworkload+"\t"+len+"\t"+w);
		for (Vm vm:vmList) {
		    if(vm.getHost().getId()==0){
		    	
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization,((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getAverageWaitingTime());
		      Log.printLine(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT()+"\t"+Utilization);
		    }
		    if(vm.getHost().getId()==1){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization1,((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getAverageWaitingTime());
		    }
		    if(vm.getHost().getId()==2){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization2,((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getAverageWaitingTime());
		    }
		    if(vm.getHost().getId()==3){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization3,((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getAverageWaitingTime());
		    }
		    if(vm.getHost().getId()==4){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization4,((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getAverageWaitingTime());
		    }
		    if(vm.getHost().getId()==5){
		    	vm.getHost().addStateHistoryEntry(((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getT(), Utilization5,((QCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).getAverageWaitingTime());
		    }
		}

		
		List<Cloudlet> assignedCloudletList = getAssignedCloudletList(i,
				toAssignCloudletList); 
		finishAssign(toAssignCloudletList); 
		Log.printLine("Assign Finished! Left:"
				+ getGlobalCloudletWaitingQueue().size() + " Success:"
				+ assignedCloudletList.size());
		return assignedCloudletList;
	} else {

		Log.printLine("VmCloudletAssignerFair No VM Error!!");

		return null;

	}
}



}
