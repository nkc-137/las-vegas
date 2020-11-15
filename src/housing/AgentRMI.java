package housing;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentRMI extends Remote{
//    Response Prepare(Request req) throws RemoteException;
//    Response Accept(Request req) throws RemoteException;
//    Response Decide(Request req) throws RemoteException; 
    Response receive(Request req) throws RemoteException; 
}
