package housing;

import java.io.Serializable;

public class Request implements Serializable{
	String msg;
	public Request(String msg) {
		this.msg = msg;
	}
}
