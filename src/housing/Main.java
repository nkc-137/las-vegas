package housing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {
	public static void main(String[] args) {
		int numsAgents = 4;
		CyclicBarrier barrier = new CyclicBarrier(numsAgents+1);
        ArrayList<Agent> agentList = new  ArrayList<Agent> ();
        Agent root;
        root = new Agent(50, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), -1);
        root.isRoot = true;
        fourthExample(barrier, agentList);
        for (Agent a: agentList) {
        	Agent.pref.put(a.house, a);
        }
        Agent.agents = agentList;
        
        Thread rootThread = new Thread(root);
        rootThread.start();
        
        try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e1) {
			e1.printStackTrace();
		}
        
        try {
			rootThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        for (Agent a: agentList) {
        	System.out.println("Agent " + a.portNum + " house : " + a.house);
        }
	}
	
	static void thirdExample(CyclicBarrier barrier, ArrayList<Agent> agentList) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 1);
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 2);
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), 3);
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
	}
	
	static void secondExample(CyclicBarrier barrier, ArrayList<Agent> agentList) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 1);
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 3);
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), 2);
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
        // ans: {1,2,3}
	}
	
	static void firstExample(CyclicBarrier barrier, ArrayList<Agent> agentList) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(1, 2, 3)), 3); //3
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 1); //1
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), 2); //2
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
        // ans: {}1,2,3
	}
	
	static void fourthExample(CyclicBarrier barrier, ArrayList<Agent> agentList) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(1)), 1);
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 3)), 4);
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 2)), 2);
        Agent agent4 = new Agent(3, new ArrayList<Integer> (Arrays.asList(4, 1)), 3);
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agent4.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
        agentList.add(agent4);
        // ans: {1,2,3,4}
	}
}
