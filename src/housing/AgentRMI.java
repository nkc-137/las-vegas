package housing;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentRMI extends Remote {
    void receiveCycle() throws RemoteException; //tell children that cycle detected
    void removePref(Request request) throws RemoteException; //remove house from pref list
    void receiveOk(Request request) throws RemoteException; //send ok to other nodes
}
