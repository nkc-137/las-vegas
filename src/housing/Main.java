package housing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {
	public static void main(String[] args) {
		int numsAgents = 3;
		String host = "127.0.0.1";
        String[] peers = new String[numsAgents+1];
        int[] ports = new int[numsAgents+1];
        for(int i=0; i<numsAgents+1; i++){
            ports[i] = 1700+i;
            peers[i] = host;
        }
		CyclicBarrier barrier = new CyclicBarrier(numsAgents+1);
        ArrayList<Agent> agentList = new ArrayList<Agent> ();
        Agent root;
        root = new Agent(numsAgents, null, -1, peers, ports);
        root.isRoot = true;
        thirdExample(barrier, agentList, peers, ports);
        
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
        System.out.println("THIS IS THE FINAL ANSWER");
        for (Agent a: agentList) {
        	System.out.println("Agent " + a.portNum + " house : " + a.house);
        }
        
        for (Agent a: agentList) {
			if(a != null){
				a.Kill();
			}	
		}
        root.Kill();
        return;
	}
	
	static void thirdExample(CyclicBarrier barrier, ArrayList<Agent> agentList, String[] peers, int[] ports) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 1, peers, ports);
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 2, peers, ports);
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), 3, peers, ports);
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
		HashMap<Integer, Agent> pref = new HashMap<Integer, Agent>();
        for(Agent a: agentList) {
        	pref.put(a.house, a);
        }
        for(Agent a: agentList) {
        	a.setPref(pref);
        }
	}
	
	static void secondExample(CyclicBarrier barrier, ArrayList<Agent> agentList, String[] peers, int[] ports) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 1, peers, ports);
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 3, peers, ports);
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), 2, peers, ports);
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
        HashMap<Integer, Agent> pref = new HashMap<Integer, Agent>();
        for(Agent a: agentList) {
        	pref.put(a.house, a);
        }
        for(Agent a: agentList) {
        	a.setPref(pref);
        }
        // ans: {1,2,3}
	}
	
	static void firstExample(CyclicBarrier barrier, ArrayList<Agent> agentList, String[] peers, int[] ports) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(1, 2, 3)), 3, peers, ports); //3
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 1, 3)), 1, peers, ports); //1
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 1, 2)), 2, peers, ports); //2
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
        HashMap<Integer, Agent> pref = new HashMap<Integer, Agent>();
        for(Agent a: agentList) {
        	pref.put(a.house, a);
        }
        for(Agent a: agentList) {
        	a.setPref(pref);
        }
        // ans: {}1,2,3
	}
	
	static void fourthExample(CyclicBarrier barrier, ArrayList<Agent> agentList, String[] peers, int[] ports) {
		Agent agent1 = new Agent(0, new ArrayList<Integer> (Arrays.asList(1)), 1, peers, ports);
        Agent agent2 = new Agent(1, new ArrayList<Integer> (Arrays.asList(2, 3)), 4, peers, ports);
        Agent agent3 = new Agent(2, new ArrayList<Integer> (Arrays.asList(3, 2)), 2, peers, ports);
        Agent agent4 = new Agent(3, new ArrayList<Integer> (Arrays.asList(4, 1)), 3, peers, ports);
        agent1.setBarrier(barrier);
        agent2.setBarrier(barrier);
        agent3.setBarrier(barrier);
        agent4.setBarrier(barrier);
        agentList.add(agent1);
        agentList.add(agent2);
        agentList.add(agent3);
        agentList.add(agent4);
        HashMap<Integer, Agent> pref = new HashMap<Integer, Agent>();
        for(Agent a: agentList) {
        	pref.put(a.house, a);
        }
        for(Agent a: agentList) {
        	a.setPref(pref);
        }
        // ans: {1,2,3,4}
	}
}
