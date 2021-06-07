import javax.swing.JButton;

public class GridButton extends JButton{
	public GridButton(int i, int j) {
		super();
		this.row = i;
		this.col =j;
	}
	public int row;
	public int col;
	public boolean isFlaged = false;
	public boolean isClicked = false;
	public boolean isBoom = false;
	public int boomAround;
	
	public void resetAll() {
		isFlaged = false;
		isClicked = false;
		isBoom = false;
	}
}
