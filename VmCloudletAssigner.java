package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public abstract class VmCloudletAssigner {



	protected VirtualQueueSize vQueueSize = VirtualQueueSize.getInstance(); // Global variable of virtual subqueue length
	protected static Queue<Cloudlet> globalCloudletWaitingQueue = new LinkedList<Cloudlet>(); //Main task queue



	public abstract List<Cloudlet> cloudletAssign(List<Cloudlet> cloudletList, List<Vm> vmList);//Cloud task assign policy

	

	public static Queue<Cloudlet> getGlobalCloudletWaitingQueue() { //Get the global task waiting queue
		return globalCloudletWaitingQueue;

	}



	public static void setGlobalCloudletWaitingQueue( //Set the global task waiting queue
			Queue<Cloudlet> globalCloudletWaitingQueue) {
		VmCloudletAssigner.globalCloudletWaitingQueue = globalCloudletWaitingQueue;
	}



	protected List<Cloudlet> getToAssignCloudletList(List<Cloudlet> cloudletList) { //Generate a cloud task list that waiting to be assigned
		List<Cloudlet> toAssignCloudletList = new ArrayList<Cloudlet>(); //List of cloud tasks waiting to be configured
		if (cloudletList != null) {
			System.out.println("assign cloudletList tasks " + cloudletList.size());
			if (getGlobalCloudletWaitingQueue().size() != 0) { //The global task waiting queue is not empty

				for (int i = 0; i < getGlobalCloudletWaitingQueue().size(); i++) //Assign the tasks in the global cloud task waiting queue to the cloud task list
					toAssignCloudletList.add(getGlobalCloudletWaitingQueue().poll());                                        							      
			    }

			toAssignCloudletList.addAll(cloudletList);
		} else {

			if (getGlobalCloudletWaitingQueue().size() != 0) {
				toAssignCloudletList.add(getGlobalCloudletWaitingQueue().poll());						
			} else {
				return toAssignCloudletList;
			}

		}
		return toAssignCloudletList;
	}

	

	protected List<Map<String, Integer>> initVmWaitingQueueSizeList() { //Initialize the virtual machine waiting queue length list
		List<Integer> virQueueSize = vQueueSize.getQueueSize();
		List<Map<String, Integer>> vmWaitingQueueSizeList = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> queueSize;
		for (int i = 0; i < virQueueSize.size(); i++) {
			queueSize = new HashMap<String, Integer>();
			queueSize.put("id", i);
			queueSize.put("size", virQueueSize.get(i));
			vmWaitingQueueSizeList.add(queueSize);
		}
		return vmWaitingQueueSizeList;
	}

	

	protected List<Cloudlet> getAssignedCloudletList(int success, List<Cloudlet> toAssignCloudletList) { // Generate successfully assigned task list
		List<Cloudlet> assignedCloudletList = new ArrayList<Cloudlet>();//Successfully assigned task list
		for (int j = 0; j < success; j++)//The first success tasks were successfully assigned
			assignedCloudletList.add(toAssignCloudletList.get(j));
		toAssignCloudletList.removeAll(assignedCloudletList);// Delete successfully assigned task
		return assignedCloudletList;
	}

	

	protected void finishAssign(List<Cloudlet> toAssignCloudletList){ //task assignment end
		for (int j = 0; j < toAssignCloudletList.size(); j++) {	//Unsuccessfully assigned tasks are returned to the main task queue
			getGlobalCloudletWaitingQueue().offer(toAssignCloudletList.get(j));
		}
	}

}
