# Q-learning-based-Dynamic-task-scheduling-for-energy-efficient-cloud-computing
We propose a Q-learning based task scheduling framework for energy-efficient cloud computing (QEEC).
QEEC has two phases. In the first phase a centralized task dispatcher is used to implement the M/M/S queueing model, by which the arriving user requests are assigned to each server in a cloud. In the second phase a Q-learning based scheduler on each server first prioritizes all the requests by task laxity and task life time, then uses a continuously-updating policy to assign tasks to virtual machines, applying incentives to reward the assignments that can minimize task response time and maximize each server’s CPU utilization. 

We open sourced the code of our team’s paper ‘Q-learning based Dynamic task scheduling for energy efficient cloud computing, so as to facilitate more scholars to understand the implementation details of the QEEC framework in CloudSim cloud enviroment. 

## Citation: 
Our paper has been pulished in Future Generation Computer System and you can cite as follow:
@article{DingFZKYZ20,
  author    = {Ding Ding and
               Xiaocong Fan and
               Yihuan Zhao and
               Kaixuan Kang and
               Qian Yin and
               Jing Zeng},
  title     = {Q-learning based dynamic task scheduling for energy-efficient cloud
               computing},
  journal   = {Future Generation Computer System},
  volume    = {108},
  pages     = {361--371},
  year      = {2020},
  url       = {https://doi.org/10.1016/j.future.2020.02.018},
  doi       = {10.1016/j.future.2020.02.018},
}

## Environment
 eclipse + java or you can run as a jar file.

## Main code file introduction
1. Use the LuncherMM1.java and LuncherMMS.java files to verfy that the M/M/S model can offer shorter task response time than the M/M/1 model under the same conditions.
2. Run the LuncherQlearning.java for getting QEEC framework results and LuncherBaselinesTest.java for baseline algorithms(fair, random, and greedy) results obtain.
3. VmCloudletAssignerXXX.java contains different algorithms'(mentioned above) implementation.
4. We put these .java files under cloudsim-3.0s/examples/org/cloudbus/cloudsim/examples/ dir.
