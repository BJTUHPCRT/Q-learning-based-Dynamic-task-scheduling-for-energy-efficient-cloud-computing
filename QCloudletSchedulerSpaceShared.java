package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class QCloudletSchedulerSpaceShared extends CloudletSchedulerSpaceShared{
	private int vmId;														
	private Vm vm;
	private double t=0;
	
	private VirtualQueueSize virQueueSize = VirtualQueueSize.getInstance();	
	private double aveWaitingTime; 											
	private double PreviousAverageWaitingTime;
	private Queue<ResCloudlet> cloudletWaitingQueue; 						

	private int cloudletWaitingQueueLength; 								
	private int cloudletHasRun;
	
	public QCloudletSchedulerSpaceShared(int vmId, int maxLength) {
		super();
		setAverageWaitingTime(0);
		setPreviousAverageWaitingTime(0);
		cloudletWaitingQueue = new LinkedList<ResCloudlet>();
		setVmId(vmId);
		setCloudletWaitingQueueLength(maxLength);
		setVm(vm);
		cloudletHasRun = 0;
	}
	
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);

		
		// Log.printLine("updateVmProcessing Vm#" + getVmId());
		double timeSpam = currentTime - getPreviousTime(); 
		double capacity = 0.0;
		int cpus = 0;
		
		for (Double mips : mipsShare) { // count the CPUs available to the VMM
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu

		// each machine in the exec list has the same amount of cpu
		for (ResCloudlet rcl : getCloudletExecList()) {
			rcl.updateCloudletFinishedSoFar((long) (capacity * timeSpam
					* rcl.getNumberOfPes() * 1000000));
			
		}

		if (getCloudletExecList().size() == 0 && getCloudletWaitingQueue().size() == 0) {
			setPreviousTime(currentTime);
			return 0.0;
		}
		if (!(getCloudletExecList().size() == 0) ) {
			setT(currentTime);
			for (ResCloudlet rcl : getCloudletExecList()) {
			//	Log.printLine(rcl.getCloudlet().getCloudletId()+" "+currentTime+" "+vmId);
			}
		}

		int finished = 0;
		List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			// finished anyway, rounding issue...
			if (rcl.getRemainingCloudletLength() == 0) {
				toRemove.add(rcl);
				cloudletFinish(rcl);
				finished++;
			}
		}
		getCloudletExecList().removeAll(toRemove);

		//Task waiting queue is not empty, select tasks from the queue to execute
		if (!getCloudletWaitingQueue().isEmpty()) {
			for (int i = 0; i < finished; i++) {
				toRemove.clear();
				for (ResCloudlet rcl : getCloudletWaitingQueue()) {
					if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
						rcl.setCloudletStatus(Cloudlet.INEXEC);
						updateAverageWaitingTime(rcl.getCloudlet().getWaitingTime());  //update task waiting time
	
						virQueueSize.decrement(getVmId());

						for (int k = 0; k < rcl.getNumberOfPes(); k++) {
							rcl.setMachineAndPeId(0, k); 
						}
						getCloudletExecList().add(rcl);
						usedPes += rcl.getNumberOfPes();
						getCloudletWaitingQueue().remove(rcl);

						break;
					}
				}
			}
		}

		//Estimate the time needed to complete the task in progress
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double remainingLength = rcl.getRemainingCloudletLength();
			double estimatedFinishTime = currentTime
					+ (remainingLength / (capacity * rcl.getNumberOfPes()));
			if (estimatedFinishTime - currentTime < 0.1) {
				estimatedFinishTime = currentTime+ 0.1;
			}
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}			
		}

		setPreviousTime(currentTime);
		return nextEvent;
	}

	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes()) {
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			updateAverageWaitingTime(cloudlet.getWaitingTime());
			virQueueSize.decrement(getVmId());

			rcl.setCloudletStatus(Cloudlet.INEXEC);

			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}

			getCloudletExecList().add(rcl);
			usedPes += cloudlet.getNumberOfPes();
		} else {
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			if (addWaitingCloudlet(rcl)) 
				return 0.0;
			else
				return -1.0; 
		}

		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : getCurrentMipsShare()) {
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus;

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = cloudlet.getCloudletLength();
		length += extraSize;
		cloudlet.setCloudletLength(length);
		return cloudlet.getCloudletLength() / capacity;
	}


	public boolean addWaitingCloudlet(ResCloudlet cloudlet) {
	
		if (getCloudletWaitingQueue().size() < getCloudletWaitingQueueLength()) {

			return getCloudletWaitingQueue().offer(cloudlet);

		} else {
			virQueueSize.decrement(getVmId());

			Log.printLine("ERROR:VM #" + cloudlet.getCloudlet().getVmId()

					+ " add Cloudlet #" + cloudlet.getCloudletId()

					+ " FAILDED!! Queue Size :"

					+ getCloudletWaitingQueue().size());

			System.exit(0);

			return false;
		}
	}



	public ResCloudlet removeWaitingCloudlet() {

		return cloudletWaitingQueue.poll();

	}



	private void updateAverageWaitingTime(double newWaitingTime) { 

		if(newWaitingTime < 0) newWaitingTime = 0;
		setAverageWaitingTime((getAverageWaitingTime()
				* cloudletHasRun + newWaitingTime)
				/ (cloudletHasRun + 1));
		cloudletHasRun++;
		setPreviousAverageWaitingTime(getAverageWaitingTime());

	}



	public double getAverageWaitingTime() {

		return aveWaitingTime;

	}



	public void setAverageWaitingTime(double averageWaitingTime) {

		this.aveWaitingTime = averageWaitingTime;

	}


	public void setPreviousAverageWaitingTime(double PreviousAverageWaitingTime){
		this.PreviousAverageWaitingTime=PreviousAverageWaitingTime;
	}
	
	
	public double getPreviousAverageWaitingTime(){
		return PreviousAverageWaitingTime;
	}

	public Queue<ResCloudlet> getCloudletWaitingQueue() {

		return cloudletWaitingQueue;

	}



	public int getCloudletWaitingQueueLength() {

		return cloudletWaitingQueueLength;

	}



	public void setCloudletWaitingQueueLength(int cloudletWaitingQueueLength) {

		this.cloudletWaitingQueueLength = cloudletWaitingQueueLength;

	}



	public int getVm() {

		return vmId;

	}



	public void setVm(Vm vm) {

		this.vmId = vmId;

	}
	
	public int getVmId() {

		return vmId;

	}



	public void setVmId(int vmId) {

		this.vmId = vmId;

	}
	
	public void setT(double t){
		this.t=t;
	}
	
	public double getT(){
		return t;
	}
}
