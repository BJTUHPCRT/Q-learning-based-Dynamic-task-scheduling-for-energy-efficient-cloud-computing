# Q-learning-based-Dynamic-task-scheduling-for-energy-efficient-cloud-computing
We open sourced the code of our team’s paper ‘Q-learning based Dynamic task scheduling for energy efficient cloud computing’, so as to facilitate more scholars to understand the implementation details of the QEEC framework in cloud enviroment. 

## Paper link: 
https://www.sciencedirect.com/science/article/abs/pii/S0167739X19313858

## Environmet
 eclipse + java or you can run as a jar file.

## Main code file introduction
1. Use the LuncherMM1.java and LuncherMMS.java files to verfy that the M/M/S model can offer shorter task response time than the M/M/1 model under the same conditions.
2. Run the LuncherQlearning.java for getting QEEC framework results and LuncherBaselinesTest.java for baseline algorithms(fair, random, and greedy) results obtain.
3. VmCloudletAssignerXXX.java contains different algorithms'(mentioned above) implementation.
4. We put these .java files under cloudsim-3.0s/examples/org/cloudbus/cloudsim/examples/ dir.
