package housing;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Agent implements AgentRMI, Runnable{
	int numAgents = 3;
	int[] ports;
	String[] peers;
	AgentRMI stub;
	Registry registry;
	
	public boolean active;
	public int portNum;
	boolean inCycle;
	int countOk = 0;
	int numOks = 1; //remove
	boolean alreadySent = false;
	boolean isRoot = false;
	boolean coinValue = false;
	Agent successor;
	CyclicBarrier barrier;
	int nextPref;
	Thread[] threads;

	public void setBarrier(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	static ArrayList<Agent> agents = new ArrayList<Agent>(); //remove
	
	Agent next;
	boolean assigned;
	int house;
	HashMap<Integer, Agent> pref;
	ArrayList<Integer> preference = new ArrayList<Integer>();
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
	
	public Agent(int portNum, ArrayList<Integer> preference, int house, String[] peers, int[] ports) {
		this.active = true;
		this.inCycle = false;
		this.children = new HashSet<Agent>();
		this.portNum = portNum;
		this.preference = preference;
		this.house = house;
		this.ports = ports;
		this.peers = peers;
		this.pref = new HashMap<Integer, Agent>();
		
		try {
			System.setProperty("java.rmi.server.hostname", this.peers[portNum]);
			registry = LocateRegistry.createRegistry(this.ports[portNum]);
			stub = (AgentRMI) UnicastRemoteObject.exportObject(this, this.ports[portNum]);
			registry.rebind("Agent", stub);
		} 
		catch (RemoteException e) {
			e.printStackTrace();
		} 
	}
	
	public void setPref(HashMap<Integer, Agent> map) {
		for (Integer k : map.keySet()) {
			this.pref.put(k, map.get(k));
		}
	}
	
	public boolean flipCoin() {
		double randNum = Math.random();
		boolean coin = false;
		if (randNum >= 0.5) {
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
//				if (successor.portNum == 1) { //TODO:remove deterministic
//					this.successor.active = false;
//				}
				boolean succActive = this.successor.active; // Get active status from successor
				System.out.println("*********This is the map : ");
				for (Integer key: pref.keySet()) {
					System.out.println("****** This is the key: " + key + " vlaue is : " + pref.get(key).portNum);
				}
				while (succActive == false) {
					children.add(successor);
					successor.setParent(this);
					this.setSuccessor(this.successor.getSuccessor());
//					System.out.println("From- the while loop " + this.portNum + " my succ is : " + this.successor.portNum);
					succActive = this.successor.active;
					if (this.successor == this) {
						break;
					}
					if(this.successor.assigned == true) {
						// I have not found any cycles, I should exit the stage
						System.out.println(" i need to exit the stage "+portNum);
						this.children.clear();
						active = false;
						Request req = new Request(this.portNum, -1, 1);
						this.Call("receiveOk", req, this.parent.portNum); //TODO: parent
//						this.parent.receiveOk(portNum, 1);
						return;
					}
				}
				System.out.println("*****Going to print actives");
				for (Agent a: agents) {
					System.out.println("Active for agent " + a.portNum + " active val: " + a.active);
				}
				if (this.successor == this) {
					System.out.println("Detected a cycle from: " + this.portNum);
					children.remove(this); // remove itself from the set of children
					for (Agent a:this.children) {
						System.out.println("*****>>>MY CHILDREN ARE " + a.portNum+" Me: "+this.portNum);
					}
					this.active = false;
				}
			}
		}

		// Notify step
		if (this.successor == this) {
			this.inCycle = true;
			for (Agent child: this.children) {
				// send cycle to child
				this.Call("receiveCycle", null, child.portNum);
//				child.receiveCycle();
			}
		}
	}
	
	// only run for root node (env)
	public void envFn() {
		for (Agent a:agents) {
			this.children.add(a);
			a.setParent(this);
		}
		threads = new Thread[numAgents];
		System.out.println("from root, length of agents is "+Agent.agents.size());
		for(int i=0; i<numAgents; i++) {
			threads[i] = new Thread(Agent.agents.get(i));
			threads[i].start();
		}
		
		// Joining threads
		for(int i=0; i<numAgents; i++) {
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
		if (this.inCycle == true) { //parent
			this.changeInCycle();
		}
	}
	
	public void changeInCycle() {
		this.house = this.nextPref;
		this.assigned = true;
		// Broadcast removePref to all
		for (Agent a: agents) {
			Request req = new Request(this.portNum, this.house, -1);
			this.Call("removePref", req, a.portNum);
		}
		if (this.children.size() == 0) {
			Agent myParent = this.getParent();
			if (myParent.isRoot == false) {
				Request req = new Request(this.portNum, -1, this.numOks);
				this.Call("receiveOk", req, this.parent.portNum); //TODO: parent
//				myParent.receiveOk(this.portNum, this.numOks);
			}
		} 
		else if (countOk == this.children.size() && !alreadySent) {
			alreadySent = true;
			Agent myParent = this.getParent();
			if (myParent.isRoot == false) {
				Request req = new Request(this.portNum, -1, this.numOks);
				this.Call("receiveOk", req, this.parent.portNum); //TODO: parent
//				myParent.receiveOk(this.portNum, this.numOks);
			}
		}
	}
	
	@Override
	public void receiveCycle() {
		this.inCycle = true;
		this.changeInCycle();
		for (Agent c: this.children) {
			this.Call("receiveCycle", null, c.portNum);
		}
		return;
	}
	
	public void receiveNextStage() {
		System.out.println("********* I AM IN RECEIVE NEXT STAGE : " + this.portNum + " ASSIENGED IS " + this.assigned);
		if (this.assigned == false) {
			this.active = true;
			for (int i =0;i<preference.size();i++) {
				int top = preference.get(i);
				if (pref.containsKey(top)) {
					this.next = pref.get(top);
					this.nextPref = top;
					break;
				}
			}
		}
		System.out.println("executed recieve next stage "+portNum);
		return;
	}
	
	@Override
	public void receiveOk(Request req) { //int from, int numOk
		int from = req.portNum;
		int req_numOk = req.numOk;
		Agent myParent = this.getParent();
		countOk++;
		numOks += req_numOk;
		System.out.println("OK RECEIVED BY : " + this.portNum + " FROM " + from);
		if (countOk == this.children.size() && !alreadySent && this.assigned== true) {
			alreadySent = true;
			if (myParent.isRoot == false) {
				req.portNum = this.portNum;
				req.numOk = numOks;
				this.Call("receiveOk", req, myParent.portNum); //TODO: get the parent id, chnage to ports[id]
//				myParent.receiveOk(this.portNum, numOks);
			}
		}
		if (isRoot == true) {
			System.out.println("root calling next stage");
			for(int i=0; i<threads.length; i++) {
				threads[i].interrupt();
			}
		}
	}
	
	@Override
	public void removePref(Request request) {
		int house = request.house;
		this.pref.remove(house);
	}
	
	public Response Call(String rmi, Request req, int id){
        Response callReply = null;
        AgentRMI stub;
        try {
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(AgentRMI) registry.lookup("Agent");
            if(rmi.equals("receiveCycle")) {
                stub.receiveCycle();
            }
            else if(rmi.equals("removePref")) {
                stub.removePref(req);
            }
            else if(rmi.equals("receiveOk")) {
                stub.receiveOk(req);
            }
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
        	System.out.println("- - - --- - -caught exception in call "+e);
            return null;
        }
        return callReply;
    }
	
	public void Kill(){
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

}
