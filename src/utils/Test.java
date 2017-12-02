package utils;

public class Test {

	public static void main(String[] args) {
		
		String myName = "2017-11-30 23:30:00,048 INFO r.MySummariser: [MySummariser]                      +    797 in 00:00:30 =   26.5/s Avg:    37 Min:     0 Max:   185 Err:   400 (50.19%) Active: 3 Started: 3 Finished: 0";
		System.out.println(myName.contains("+"));
	}
}
