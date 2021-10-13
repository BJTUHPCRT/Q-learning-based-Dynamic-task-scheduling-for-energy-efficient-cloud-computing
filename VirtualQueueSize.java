package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.List;

public class VirtualQueueSize {
	private static VirtualQueueSize instance = new VirtualQueueSize();
	private static List<Integer> queueSize;
	private static int maxLength;


    public VirtualQueueSize() {
			super();
    }

    public static VirtualQueueSize getInstance() {
        if (instance == null)
			instance = new VirtualQueueSize();
		return instance;
	}

	public static void init(int numVm, int length) {

		queueSize = new ArrayList<Integer>();
		for (int i = 0; i < numVm; i++) {
			queueSize.add(0);
		}
		setMaxLength(length);
	}

	public static boolean increment(int id) {

		//Log.printLine("Increment Before Id#" + id +" "+ getQueueSize().get(id));
		if (getQueueSize().get(id) >= getMaxLength())
			return false;
		getQueueSize().set(id, getQueueSize().get(id) + 1);

		//Log.printLine("Increment After Id#" + id +" "+ getQueueSize().get(id));
		return true;

	}



	public static boolean decrement(int id){

		//Log.printLine("Decrement Before Id#" + id +" "+ getQueueSize().get(id));
		if (getQueueSize().get(id) <= 0)
			return false;
			//System.exit(0);

		getQueueSize().set(id, getQueueSize().get(id) - 1);
		//Log.printLine("Decrement After Id#" + id +" "+ getQueueSize().get(id));
		return true;

	}

	

	public static List<Integer> getQueueSize() {
		return queueSize;
	}



	public static int getMaxLength() {
		return maxLength;

	}



	public static void setMaxLength(int maxLength) {
		VirtualQueueSize.maxLength = maxLength;

	}
}
