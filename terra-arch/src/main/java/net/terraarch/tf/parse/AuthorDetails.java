package net.terraarch.tf.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AuthorDetails {

	public final String name;
	public final String email;
	public final int count;
	public final long window;
	public final long last;
	
	public AuthorDetails(String name, String email, int count, long window, long last) {
		this.name = name;
		this.email = email;
		this.count = count;
		this.window = window;
		this.last = last;		
	}

	public String toString() {
		Date d = new Date(last);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");		
		return "\""+name+"\",\""+email+"\","+(window/(1000L*60L*60L*24L))+","+formatter.format(d);		
	}
	
}
