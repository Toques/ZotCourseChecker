package toques.view;

import java.io.Serializable;

public class course implements Serializable{
	public String title;
	public String GEs;

	public course(String t, String g) {
		title = t;
		GEs = g;
	}
	public String giveTitle()
	{
		return title;
	}
	public String GEs()
	{
		return GEs;
	}
	
}
