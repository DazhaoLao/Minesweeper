
public class GameState implements java.io.Serializable{
	
	public static final long serialVersionUID = -6472910739036079992L;
	private int requestType;
	private String stateName;
	private GridButton[][] buttons;
	private int mineLeft;
	private int timeLeft;
	private boolean firstClick;
	private int clickCount;
	
	public GameState(int requestType, String stateName, GridButton[][] buttons, int mineLeft, int timeLeft, boolean firstClick, int clickCount) {
		this.requestType = requestType;
		this.stateName = stateName;
		this.buttons = buttons;
		this.mineLeft = mineLeft;
		this.timeLeft = timeLeft;
		this.firstClick = firstClick;
		this.clickCount = clickCount;
	}
	
	public int getType() {
		return this.requestType;
	}
	
	public String getName() {
		return this.stateName;
	}
	
	public GridButton[][] getButtons() {
		return this.buttons;
	}
	
	public int getMineLeft() {
		return this.mineLeft;
	}

	public int getTimeLeft() {
		return this.timeLeft;
	}
	
	public int getClickCount() {
		return this.clickCount;
	}
	public boolean getFirstClick() {
		return this.firstClick;
	}
}
