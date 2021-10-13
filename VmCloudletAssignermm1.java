package org.cloudbus.cloudsim.examples;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;


public class VmCloudletAssignermm1 extends VmCloudletAssigner{

	int index=(int)(5*Math.random());
	@Override
	public List<Cloudlet> cloudletAssign(List<Cloudlet> cloudletList,
			List<Vm> vmList) {
		if (vmList != null || vmList.size() != 0) {
			List<Cloudlet> toAssignCloudletList = getToAssignCloudletList(cloudletList);
			if (toAssignCloudletList.size() < 1) { 
				return null;
//				System.exit(0);
			}

			//task preprocess
			for(Cloudlet c:toAssignCloudletList){
				
				if(c.getCloudletLength()<2000){
					c.setSe(3);
				}else if(c.getCloudletLength()>200 && c.getCloudletLength()<4000){
					c.setSe(2);
				}else{
					c.setSe(1);
				}
				
				if(c.getWaitingTime()<5){
					c.setWe(1);
				}else if(c.getWaitingTime()>10&&c.getWaitingTime()<15){
					c.setWe(2);
				}else{
					c.setWe(3);
				}
				
				if(c.getDeadline()<6){
					c.setDe(3);
				}else if(c.getDeadline()>6&&c.getDeadline()<20){
					c.setDe(2);
				}else{
					c.setDe(1);
				}
				
			}
			
			
			for(Cloudlet c:toAssignCloudletList){
				int rank=(int) (0.5*c.getSe()+0.3*c.getWe()+0.2*c.getDe());
				c.setRank(rank);
			}
			

			Collections.sort(toAssignCloudletList,new CloudletComparator());
			int m = vmList.size();	
			int n = toAssignCloudletList.size();	
			int maxCloudletsWaitingLength = vQueueSize.getMaxLength();	
			List<Map<String, Integer>> vmWaitingQueueSizeList = initVmWaitingQueueSizeList();


			int i;
			for (i = 0; i < n; i++) {	   
				index=(int)(5*Math.random());
				int mSize = vmWaitingQueueSizeList.get(index).get("size");
				if (mSize >= maxCloudletsWaitingLength) {

					for (int j = 0, tmp = maxCloudletsWaitingLength + 1; j < m; j++) {
						if (tmp > vmWaitingQueueSizeList.get(j).get("size")) {
							tmp = vmWaitingQueueSizeList.get(j).get("size");
							index = j;
						}
					}

					mSize = vmWaitingQueueSizeList.get(index).get("size");
					if (mSize >= maxCloudletsWaitingLength) {
						//Log.printLine("mSize=50 list(0):" + mSize);
						break;
					}

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
				index = (index++)%m;
				Log.printLine(index+"hahaxuniji");
			}

			List<Cloudlet> assignedCloudletList = getAssignedCloudletList(i, toAssignCloudletList);

			finishAssign(toAssignCloudletList); 

			Log.printLine("Assign Finished! Left:"
					+ getGlobalCloudletWaitingQueue().size() + " Success:"
					+ assignedCloudletList.size());
			
			return assignedCloudletList;

		} else { 
			Log.printLine("VmCloudletAssignerRandom No VM Error!!");
			return null;
		}
	}


	private class CloudletComparator implements Comparator<Cloudlet>{

		@Override
		public int compare(Cloudlet o1, Cloudlet o2) {
			// TODO Auto-generated method stub
			return -((int)o1.getRank()-o2.getRank());
		}
		
	}
	
}
