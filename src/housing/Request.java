package housing;

import java.io.Serializable;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int house;
	int portNum;
	int numOk;
	
	public Request(int portNum, int house, int numOk) {
		this.house = house;
		this.portNum = portNum;
		this.numOk = numOk;
	}
}
