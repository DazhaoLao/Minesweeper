
public class RankList implements java.io.Serializable{
	private int[] scores = new int[5];
	private String[] names = new String[5];
	public RankList(int[] scores, String[] names) {
		this.scores = scores;
		this.names = names;
	}
	
	public int[] getScores() {
		return this.scores;
	}
	
	public String[] getNames() {
		return this.names;
	}
}
