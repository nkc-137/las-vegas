package housing;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Agent implements Runnable{
	public static int[] ports;
	public boolean active;
	public int portNum;
	boolean inCycle;
	int countOk = 0;
	int numOks = 1;
	boolean alreadySent = false;
	boolean isRoot = false;
	boolean coinValue = false;
	Agent successor;
	CyclicBarrier barrier;
	int nextPref;
	Thread[] threads;
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

	HashSet<Agent> children;
	
	public Agent(int portNum, ArrayList<Integer> preference, int house) {
		this.active = true;
		this.inCycle = false;
		this.children = new HashSet<Agent>();
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
		return coin;
	}
	
	public void findCycle() {
		while (active == true) {
			
			// Coin flip step:
			this.coinValue = this.flipCoin();
//			if (this.portNum == 0) {
//				this.coinValue = false; // Never become inactive because of randomness
//			}
			boolean succCoin = successor.coinValue;
//			System.out.println("******* From the bigger while loop for " + this.portNum + " coinvalue: " + coinValue + " succcoin: " + succCoin);
			if (this.coinValue == true && succCoin == false) {
				this.active = false;
				System.out.println("SETTING ACTIVE FALSE FOR : " + this.portNum);
			}
			// Explore step
			if (active == true) {
//				System.out.println("Active is true! " + this.portNum);
				if (successor.portNum == 1) { //TODO:remove deterministic
					this.successor.active = false;
				}
				boolean succActive = this.successor.active; // Get active status from successor
				System.out.println("*********This is the map : ");
				for (Integer key: pref.keySet()) {
					System.out.println("****** This is the key: " + key + " vlaue is : " + pref.get(key).portNum);
				}
//				System.out.println("**********From the bigger while loop " + this.portNum + " giving assinged status");
//				for (Agent a:agents) {
//					System.out.println("***ASSIGNED STATUS FOR: " + a.portNum + " VALUE " + a.assigned );
//				}
				while (succActive == false) {
//					System.out.println("Doing the while loop from: " + this.portNum);
					children.add(successor);
					successor.setParent(this);
					this.setSuccessor(this.successor.getSuccessor());
					System.out.println("From- the while loop " + this.portNum + " my succ is : " + this.successor.portNum);
					succActive = this.successor.active;
					if (this.successor == this) {
						break;
					}
					if(this.successor.assigned == true) {
						// I have not found any cycles, I should exit the stage
						System.out.println(" i need to exit the stage "+portNum);
//						this.successor = this;
//						for(Agent a: children) {
//							a.parent = this.parent;
//						}
						this.children.clear();
						active = false;
						this.parent.receiveOk(portNum, 1);
						return;
					}
				}
				System.out.println("*****Going to print actives");
				for (Agent a: agents) {
					System.out.println("Active for agent " + a.portNum + " active val: " + a.active);
				}
				if (this.successor == this) {
					System.out.println("Detected a cycle from: " + this.portNum);
					children.remove(this);
					for (Agent a:this.children) {
						System.out.println("*****>>>MY CHILDREN ARE " + a.portNum+" Me: "+this.portNum);
					}
					this.active = false;
				}
			}
		}

		// Notify step
		if (this.successor == this) {
//			System.out.println("Going to notify from: " + this.portNum);
			this.inCycle = true;
			Request req = new Request("Cycle");
			for (Agent child: this.children) {
				// send cycle to child
				child.receiveCycle();
			}
		}
	}
	
	// only run for root node (env)
	public void envFn() {
		for (Agent a:agents) {
			this.children.add(a);
			a.setParent(this);
		}
		threads = new Thread[3];
		for(int i=0; i<3; i++) {
			threads[i] = new Thread(Agent.agents.get(i));
			threads[i].start();
		}
		
		// Joining threads
		for(int i=0; i<3; i++) {
        	try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}
	
	@Override
	public void run() {
		System.out.println("Run being called from : " + this.portNum);
		if (isRoot == true) {
			this.envFn();
		} else {
			boolean keepRunning = true;
			while(keepRunning) {
				System.out.println("keepRunning is true "+portNum);
				System.out.println("current thred number "+Thread.activeCount());
				this.receiveNextStage();
				try {
					barrier.await(2L, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					System.out.println("i am here");
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
				System.out.println("barrier wait is over");
				this.startStage();
				keepRunning = false;
				try {
					Thread.sleep(15000); //Synchronous system of messaging is assumed
				} catch (InterruptedException e) {
					keepRunning = true;
					e.printStackTrace();
				}
			}	
		}
	}

	public void startStage() {
		System.out.println("Starting the stage from: " + this.portNum);
		this.successor = next;
		//Execute cycle algo
		this.findCycle();
//		System.out.println("Finished exec findcycle for : " + this.portNum);
//		System.out.println("Printing the children values of all:");
//		for (Agent a: Agent.agents) {
//			System.out.println(a.portNum + " " + a.children);
//		}
		if (this.inCycle == true) { //parent
			this.changeInCycle();
		}
//		System.out.println("Done with start stage method :" + this.portNum);
	}
	
	public void changeInCycle() {
//		System.out.println("**********ASSIGNED BECAME TRUE FOR : " + this.portNum);
		this.house = this.nextPref;
		this.assigned = true;
		// Broadcast remove to all
		for (Agent a: agents) {
			a.remove(this.house);
		}
//		System.out.println("These are my children: " + this.children);
		if (this.children.size() == 0) {
			Agent myParent = this.getParent();
			if (myParent.isRoot == false) {
				myParent.receiveOk(this.portNum, this.numOks);
			}
		} 
		else if (countOk == this.children.size() && !alreadySent) {
			alreadySent = true;
			Agent myParent = this.getParent();
			if (myParent.isRoot == false) {
				myParent.receiveOk(this.portNum, this.numOks);
			}
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
		System.out.println("********* I AM IN RECEIVE NEXT STAGE : " + this.portNum + " ASSIENGED IS " + this.assigned);
		if (this.assigned == false) {
			this.active = true;
//			System.out.println("CALLING RECEIVE NEXT STAGE  FOR POCESS " + this.portNum);
			for (int i =0;i<preference.size();i++) {
				int top = preference.get(i);
				if (pref.containsKey(top)) {
					this.next = pref.get(top);
					this.nextPref = top;
					break;
				}
			}
//			if (prefNumber < preference.size()) {
//				int top = preference.get(prefNumber); // top is the next available house choice
//				this.next = pref.get(top);
//				this.nextPref = top;
//				System.out.println("for " + portNum + " top is " + top);
//			}
		}
		System.out.println("executed recieve next stage "+portNum);
		return;
	}

	public void makeSuccessor() {
		prefNumber++;
		int top = preference.get(prefNumber);
		this.next = pref.get(top);
		this.successor = next;
	}
	
	public void receiveOk(int from, int numOk) {
		Agent myParent = this.getParent();
		countOk++;
		numOks += numOk;
		System.out.println("OK RECEIVED BY : " + this.portNum + " FROM " + from);
		if (countOk == this.children.size() && !alreadySent && this.assigned== true) {
			alreadySent = true;
			if (myParent.isRoot == false) {
				myParent.receiveOk(this.portNum, numOks);
			}
		}
		if (isRoot == true) {
			System.out.println("root calling next stage");
			for(int i=0; i<threads.length; i++) {
				threads[i].interrupt();
			}
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
