package housing;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Agent implements Runnable{
	public static int[] ports;
	public boolean active;
	public int portNum;
	boolean inCycle;
	boolean coinValue = false;
	Agent successor;
	CyclicBarrier barrier;
	int nextPref;
	public CyclicBarrier getBarrier() {
		return barrier;
	}

	public void setBarrier(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	static ArrayList<Agent> agents = new ArrayList<Agent>();
	
	Agent next;
	boolean assigned;
	int house;
	static HashMap<Integer, Agent> pref = new HashMap<Integer, Agent>();
	ArrayList<Integer> preference = new ArrayList<Integer>();
	int prefNumber = -1;
	Agent parent;
	
	public Agent getParent() {
		return parent;
	}

	public void setParent(Agent parent) {
		this.parent = parent;
	}

	public Agent getSuccessor() {
		return successor;
	}

	public void setSuccessor(Agent successor) {
		this.successor = successor;
	}

	ArrayList<Agent> children;
	
	public Agent(int portNum, ArrayList<Integer> preference, int house) {
		this.active = true;
		this.inCycle = false;
		this.children = new ArrayList<Agent>();
		this.portNum = portNum;
		this.preference = preference;
		this.house = house;
	}
	
	public boolean flipCoin() {
		double randNum = Math.random();
		boolean coin = false;
		if (randNum >=0.5) {
			coin = true;
		}
//		System.out.println("From random " + randNum);
		return coin;
	}
	
	
	public void findCycle() {
//		System.out.println("Started cycle from: " + this.portNum);

		while (active == true) {
			
			// Coin flip step:
			this.coinValue = this.flipCoin();
			if (this.portNum == 0) {
				this.coinValue = false; // Never become inactive because of randomness
			}
			boolean succCoin = successor.coinValue;
			if (this.coinValue == true && succCoin == false) {
				this.active = false;
				System.out.println("SETTING ACTIVE FALSE FOR : " + this.portNum);
//				System.out.println("coin value: " + this.coinValue + " succCoin: " + succCoin);
			}
			// Explore step
			if (active == true) {
//				System.out.println("Active is true! " + this.portNum);
				if (successor.portNum == 1) {
					this.successor.active = false;
				}
				boolean succActive = this.successor.active; // Get active status from successor
				while (succActive == false) {
					System.out.println("Doing the while loop from: " + this.portNum);
					children.add(successor);
					successor.setParent(this);
					this.setSuccessor(this.successor.getSuccessor());
					System.out.println("Printing from while loop " + this.successor.portNum);
					succActive = this.successor.active;
				}
				if (this.successor == this) {
					System.out.println("Detected a cycle from: " + this.portNum);
					this.active = false;
				}
			}
		}

		// Notify step
		if (this.successor == this) {
//			System.out.println("Going to notify from: " + this.portNum);
			this.inCycle = true; // Extra line to make itself incycle true
			Request req = new Request("Cycle");
			for (Agent child: this.children) {
				// send cycle to child
				child.receiveCycle();
			}
		}
	}
	
	@Override
	public void run() {
//		System.out.println("Run being called from : " + this.portNum);
		this.receiveNextStage();
		Thread.yield();
		try {
			barrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
		this.startStage();
//		try {
//			barrier.await(5L, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (BrokenBarrierException e) {
//			e.printStackTrace();
//		} catch (TimeoutException e) {
//			e.printStackTrace();
//		}
//		System.out.println("after startStage in "+this.portNum);
	}
	
	public void startStage() {
		System.out.println("Starting the stage from: " + this.portNum);
		this.successor = next;
		//Execute cycle algo
		this.findCycle();
		System.out.println("Finished exec findcycle for : " + this.portNum);
//		synchronized (this) {
//			System.out.println("Printing the inCycle values of all: " + this.portNum);
//			for (Agent a: Agent.agents) {
//				System.out.println(a.portNum + " " + a.inCycle);
//			}
//		}
//		System.out.println("Printing the children values of all:");
//		for (Agent a: Agent.agents) {
//			System.out.println(a.portNum + " " + a.children);
//		}
//		synchronized (this) {
//			System.out.println("Printing the parent values of all: " + this.portNum + " " + this);
//			for (Agent a: Agent.agents) {
//				System.out.println(a.portNum + "-- " + a.parent);
//			}
//		}
		if (this.inCycle == true) {
			this.changeInCycle();
		}
		System.out.println("Done with start stage method :" + this.portNum);
	}
	
	public void changeInCycle() {
		System.out.println("Assigned became true for: " + this.portNum);
		this.house = this.nextPref;
		this.assigned = true;
		// Broadcast remove to all
		for (Agent a: agents) {
			a.remove(this.house);
		}
		System.out.println("These are my children: " + this.children);
		if (this.children.size() == 0) {
			Agent myParent = this.getParent();
			myParent.receiveOk();
		}
	}
	
	public void receiveCycle() {
		this.inCycle = true;
		this.changeInCycle();
		for (Agent c: this.children) {
			c.receiveCycle();
		}
	}
	
	public void receiveNextStage() {
		if (this.assigned == false) {
			this.active = true;
			System.out.println("CALLING RECE NEXT STAGE  FOR POCESS " + this.portNum);
			prefNumber++; // TODO for variable number of preferences
			int top = preference.get(prefNumber); // top is the next available house choice
			this.next = pref.get(top);
			this.nextPref = top;
			System.out.println("for " + portNum + " top is " + top);
		}
	}
	
	public void makeSuccessor() {
		prefNumber++;
		int top = preference.get(prefNumber);
		this.next = pref.get(top);
		this.successor = next;
	}
	
	public void receiveOk() {
		Agent myParent = this.getParent();
		System.out.println("OK received by : " + this.portNum);
		if (myParent == null) { // Im the root
//			for (Agent a: agents) {
//				a.receiveNextStage();
//			}
		} else {
			myParent.receiveOk();
		}
	}
	
	
	public Response Call(String rmi, Request req, Agent child){
        Response callReply = null;

        AgentRMI stub;
        try{
        	int portNum = child.portNum;
            Registry registry=LocateRegistry.getRegistry(portNum);
            stub=(AgentRMI) registry.lookup("Agent");
            if(rmi.equals("Cycle"))
                callReply = stub.receive(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
	
	// Dummy function instead of messaging (Call)
	public Response sendMsg(String rmi, Request req, Agent agent) {
		Response callReply = null;
		if (rmi.equals("Cycle")) {
			callReply = agent.receiveMsg(req);
		} else if (rmi.equals("ok")) {
			callReply = agent.receiveMsg(req);
		}
		else if (rmi.equals("Remove")){
		}
		return callReply;
	}
	
	public void remove(int house) {
		pref.remove(house);
	}
	
	// Dummy function instead of messaging (receive)
	public Response receiveMsg(Request req) {
		Response response = null;
		this.inCycle = true;
		for (Agent child: this.children) {
			this.sendMsg("Cycle", req, child);
		}
		return response;
	}

	public Response receive(Request req) {
		Response response = null;
		this.inCycle = true;
		for (Agent child: this.children) {
			this.Call("Cycle", req, child);
		}
		return response;
	}

}