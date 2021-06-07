import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;


public class Minesweeper extends JFrame {
	String host = "localhost";
	private JPanel showPanel;
	private JPanel gridPanel;	
	private JLabel minelbl;
	private JLabel timelbl;
	private Timer time;
	
	private int mineLeft = 40;
	private int timeLeft = 1000;
	private boolean firstClick = true;
	private int clickCount = 0;
	private boolean isSaved = false;
	
	private static final int FRAME_WIDTH = 275;
	private static final int FRAME_HEIGHT = 362;
	private int ICON_NUM = 13;
	private int MINE_NUM = 40;
	private int ROW = 16;
	private int COL = 16;
	private int GRID_NUM = ROW*COL;
	private ImageIcon[] icons = new ImageIcon[ICON_NUM];
	private Dimension size;	
	private GridButton[][] buttons = new GridButton[16][16];

	
	public Minesweeper() {
		for(int i=0;i<ICON_NUM;i++) {
			icons[i] = new ImageIcon("minesweepertiles/" + i + ".png");
		}
		size = new Dimension(icons[10].getIconWidth(), icons[10].getIconHeight());
		createMenus();
		createPanel();
		this.add(showPanel,BorderLayout.NORTH);
		this.add(gridPanel,BorderLayout.CENTER);
	}
	
	private void createPanel() {

		// Time and mines remained
		showPanel = new JPanel();
		showPanel.setLayout(new GridLayout(1,2));
		
		JPanel timePanel = new JPanel();
		timePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		timelbl = new JLabel("Time: " + timeLeft);		
		timePanel.add(timelbl);
		showPanel.add(timePanel);
		
		JPanel minePanel = new JPanel();
		minePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		minelbl = new JLabel("Mines: 40");
		minePanel.add(minelbl);
		showPanel.add(minePanel);
		
		time = new Timer(1000,new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeLeft--;
				timelbl.setText("Time: " + timeLeft);
				if(timeLeft == 0) {
					gameLost();
				}
			}
		});
		
		// mines grid
		gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(16,16));
		initButtons();
	}
	
	
	private void createMenus() {
		
		JMenuBar menuBar = new JMenuBar();     
	    setJMenuBar(menuBar);
	    JMenu menu = new JMenu("File");
	    
	    // new
	    class NewActionListener implements ActionListener {
	    	public void actionPerformed(ActionEvent event)
	         {
	    		restart();
	         }
	    }
	    
	    JMenuItem newItem = new JMenuItem("New"); 
	    newItem.setAccelerator(KeyStroke.getKeyStroke('N', ActionEvent.CTRL_MASK));
	    newItem.setMnemonic('N');
	    newItem.addActionListener(new NewActionListener());
	    
	    // open
	    class OpenActionListener implements ActionListener {
	    	public void actionPerformed(ActionEvent event)
	         {
	    		String name = null;
	    		while(true) {
	    			name = JOptionPane.showInputDialog(gridPanel,"Please enter the name of the game saved:");
		    		if(name==null) {
		    			return;
		    		}else if(name.isBlank()) {
		    			JOptionPane.showMessageDialog(gridPanel,"Field is empty!");
		    		}else {
		    			try {
			    	        Socket socket = new Socket(host, 8000);
			    	        ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
			    	        ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
			    	        GameState s = new GameState(1, name, null, 0, 0, false, 0);
			    	        toServer.writeObject(s);						
			    	        Object object = fromServer.readObject();
			    	        GameState gs = (GameState)object;
			    	        if(gs.getType() == -1) {
			    	        	JOptionPane.showMessageDialog(gridPanel,"Game not found!");
			    	        }else {
			    	        	time.stop();
			    	        	GridButton[][] tempButtons = gs.getButtons();
			    	    		for(int i=0; i<ROW; i++) {
			    	    			for(int j=0; j<COL; j++) {
			    	    				buttons[i][j].isFlaged = tempButtons[i][j].isFlaged;
			    	    				buttons[i][j].isClicked = tempButtons[i][j].isClicked;
			    	    				buttons[i][j].isBoom = tempButtons[i][j].isBoom;
			    	    				buttons[i][j].boomAround = tempButtons[i][j].boomAround;
			    	    				if(tempButtons[i][j].isClicked) {
			    	    					buttons[i][j].setEnabled(false);
			    	    					buttons[i][j].setDisabledIcon(icons[buttons[i][j].boomAround]);
			    	    				}else if (tempButtons[i][j].isFlaged) {
			    	    					buttons[i][j].setEnabled(true);
											buttons[i][j].setIcon(icons[11]);
										}else {
											buttons[i][j].setEnabled(true);
											buttons[i][j].setIcon(icons[10]);
										}
			    	    				
			    	    			}
			    	    		}
			    	    		mineLeft = gs.getMineLeft();
			    	    		timeLeft = gs.getTimeLeft();
			    	    		firstClick = true;
			    	    		clickCount = gs.getClickCount();
			    	    		timelbl.setText("Time: " + timeLeft);
			    	    		minelbl.setText("Mine: "+ mineLeft);
			    	        	toServer.close();
				    	        fromServer.close();
				    	        socket.close();
				    	        isSaved = false;
				    	        return;
			    	        }
		    	        } catch (IOException | ClassNotFoundException ex) {
			    	        ex.printStackTrace();
		    	        }
					}	
	    		}
	         }
	    }
	    
	    JMenuItem openItem = new JMenuItem("Open"); 
	    openItem.setAccelerator(KeyStroke.getKeyStroke('O', ActionEvent.CTRL_MASK));
	    openItem.setMnemonic('O');
	    openItem.addActionListener(new OpenActionListener());
	    
	    // save
	    class SaveActionListener implements ActionListener {
	    	public void actionPerformed(ActionEvent event)
	         {
	    		String name = null;
	    		while(true) {
	    			name = JOptionPane.showInputDialog(gridPanel,"Please enter a name of the game:");
		    		if(name==null) {
		    			return;
		    		}else if(name.isBlank()) {
		    			JOptionPane.showMessageDialog(gridPanel,"Field is empty!");
		    		}else {
						break;
					}	
	    		}
	    		try {
	    	        Socket socket = new Socket(host, 8000);
	    	        ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
	    	        GameState s = new GameState(2, name, buttons, mineLeft, timeLeft, firstClick, clickCount);
	    	        toServer.writeObject(s);
	    	        toServer.close();
	    	        socket.close();
    	        } catch (IOException ex) {
	    	        ex.printStackTrace();
    	        }
	         }
	    }
	    
	    JMenuItem saveItem = new JMenuItem("Save"); 
	    saveItem.setAccelerator(KeyStroke.getKeyStroke('S', ActionEvent.CTRL_MASK));
	    saveItem.setMnemonic('S');
	    saveItem.addActionListener(new SaveActionListener());
	    
	    // rank
	    class RankActionListener implements ActionListener {
	    	public void actionPerformed(ActionEvent event)
	         {   		
	    		try {
	    	        Socket socket = new Socket(host, 8000);
	    	        ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
	    	        ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());     
	    	        GameState s = new GameState(3, null, null, 0, 0, false, 0);
	    	        toServer.writeObject(s);
	    	        Object object = fromServer.readObject();
	    	        RankList rl = (RankList)object;
	    	        toServer.close();
	    	        fromServer.close();
	    	        socket.close();
	    	        String[] names = rl.getNames();
	    	        int[] scores = rl.getScores();
	    	        JPanel rankPanel = new JPanel();
	    	        rankPanel.setLayout(new GridLayout(6,2));
	    	        JLabel scorelbl = new JLabel("Scores");
	    	        rankPanel.add(scorelbl);
	    	        JLabel namelbl = new JLabel("Names");
	    	        rankPanel.add(namelbl);
	    	        for(int i=0; i<5; i++) {
		    	        scorelbl = new JLabel(String.valueOf(scores[i]));
		    	        rankPanel.add(scorelbl);
		    	        namelbl = new JLabel(names[i]);
		    	        rankPanel.add(namelbl);
	    	        }
	    	        JOptionPane.showMessageDialog(gridPanel, rankPanel, "Ranking",JOptionPane.PLAIN_MESSAGE);
    	        } catch (IOException | ClassNotFoundException ex) {
	    	        ex.printStackTrace();
    	        }
	         }
	    }
	    
	    JMenuItem rankItem = new JMenuItem("Ranking"); 
	    rankItem.setAccelerator(KeyStroke.getKeyStroke('R', ActionEvent.CTRL_MASK));
	    rankItem.setMnemonic('R');
	    rankItem.addActionListener(new RankActionListener());
	    
	    // exit
	    class ExitActionListener implements ActionListener {
	    	public void actionPerformed(ActionEvent event)
	         {
	            System.exit(0);
	         }
	    }
	    JMenuItem exitItem = new JMenuItem("Exit");
	    exitItem.setAccelerator(KeyStroke.getKeyStroke('X', ActionEvent.CTRL_MASK));
	    exitItem.setMnemonic('X');
	    exitItem.addActionListener(new ExitActionListener());
	    
	    menu.add(newItem);
	    menu.add(openItem);
	    menu.add(saveItem);
	    menu.add(rankItem);
	    menu.add(exitItem);
	    menuBar.add(menu);
		
	}
	
	
	private void initButtons() {
		
		class GridListenner extends MouseAdapter{
			public void mouseClicked(MouseEvent e) {
				GridButton clicked = (GridButton) e.getSource();
				if(firstClick) {
					time.start();
				}
				if(!clicked.isEnabled())
					return;
				if(e.getButton() == e.BUTTON1) {
					if(!clicked.isFlaged) {
						clicked.setEnabled(false);
						int boomAround = clicked.boomAround;
						clicked.isClicked = true;
						clicked.setDisabledIcon(icons[boomAround]);
						if(boomAround == 0) {
							exploreBlank(clicked.row, clicked.col);
							clickCount++;
						} else if(boomAround == 9) {
							gameLost();
						}
						else {
							clickCount++;
						}
						//System.out.println(clickCount);
						if(clickCount == (GRID_NUM - MINE_NUM)) {
						//if(clickCount >= 5) {
							time.stop();
							saveScore();
							restart();
						}
					}
				} else if(e.getButton() == e.BUTTON3) {
					if(!clicked.isFlaged) {
						if(mineLeft>0) {
							clicked.setIcon(icons[11]);
							clicked.isFlaged = true;
							mineLeft--;
							minelbl.setText("Mine: "+ mineLeft);
						}else {
							minelbl.setText("No marks left");
						}					
					}else {
						clicked.setIcon(icons[10]);
						clicked.isFlaged = false;
						mineLeft++;
						minelbl.setText("Mine: "+ mineLeft);
					}
				}
			}
	    }
		
		for(int i=0; i<ROW; i++) {
			for(int j=0; j<COL; j++) {
				GridButton temp = new GridButton(i,j);
				temp.setIcon(icons[10]);
				temp.setFocusable(false);
				temp.setPreferredSize(size);
				temp.setMinimumSize(size);
				temp.setMaximumSize(size);
				temp.setSize(size);
				temp.setBorderPainted(false);
				temp.addMouseListener(new GridListenner());
				gridPanel.add(temp);
				buttons[i][j] = temp;
			}
		}
		
		randomBomb();
		fillNumber();
	}
	
	
	private void randomBomb() {
        for (int i = 0; i < MINE_NUM; i++) {
            int rRow = (int) (Math.random() * ROW);
            int rCol = (int) (Math.random() * COL);
            if(buttons[rRow][rCol].isBoom) {
            	i--;
            }
            buttons[rRow][rCol].isBoom = true;
        }
    }
	
	
	private void fillNumber() {
		for (int  i = 0; i < ROW; i++) {
			for (int j = 0; j < COL; j++) {
				if (buttons[i][j].isBoom) {
					buttons[i][j].boomAround = 9;
					continue;
				}
				int bombCount = 0;
				for (int rowOffset=-1; rowOffset<2; rowOffset++) {
					int row = i + rowOffset;
					if(row < 0 || row > (ROW-1))
						continue;
					for (int colOffset=-1; colOffset<2; colOffset++) {
						int col = j + colOffset;
						if(col < 0 || col > (ROW-1))
							continue;
						if(buttons[row][col].isBoom)
							bombCount++;
					}
				}
				buttons[i][j].boomAround = bombCount;
			}
		}
	}
	
	
	private void exploreBlank(int row, int col) {
		for (int rowOffset=-1; rowOffset<2; rowOffset++) {
			int i = row + rowOffset;
			if(i < 0 || i > (ROW-1))
				continue;
			for (int colOffset=-1; colOffset<2; colOffset++) {
				int j = col + colOffset;
				if(j < 0 || j > (ROW-1))
					continue;
				if(!buttons[i][j].isClicked && !buttons[i][j].isFlaged) {
					buttons[i][j].setEnabled(false);
					int boomAround = buttons[i][j].boomAround;
					buttons[i][j].isClicked = true;
					clickCount++;
					buttons[i][j].setDisabledIcon(icons[boomAround]);
					if(boomAround == 0)
						exploreBlank(i, j);
				}
				
			}
		}
	}

	
	private void gameLost() {
		time.stop();
		
		for(int i=0; i<ROW; i++) {
			for(int j=0; j<COL; j++) {
				if(buttons[i][j].isFlaged) {
					if(!buttons[i][j].isBoom)
						buttons[i][j].setIcon(icons[12]);
					continue;
				}
				if(buttons[i][j].isBoom) {
					buttons[i][j].setEnabled(false);
					buttons[i][j].setDisabledIcon(icons[9]);
				}
			}
		}
		JOptionPane.showMessageDialog(gridPanel,"Game Lost !");
		restart();
	}
	
	
	private void saveScore() {
		if(isSaved)
			return;
		isSaved = true;
		String name = null;
		while(true) {
			name = JOptionPane.showInputDialog(gridPanel,"Game Won !\n\nYour score is "
					+ timeLeft +" !\n\nPlease enter your name:","",JOptionPane.PLAIN_MESSAGE);
    		if(name==null) {
    			return;
    		}else if(name.isBlank()) {
    			JOptionPane.showMessageDialog(gridPanel,"Field is empty!");
    		}else {
				break;
			}	
		}
		try {
	        Socket socket = new Socket(host, 8000);
	        ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
	        GameState s = new GameState(4, name, null, 0, timeLeft, true, 0);
	        toServer.writeObject(s);
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        toServer.close();
	        socket.close();
        } catch (IOException ex) {
	        ex.printStackTrace();
        }
	}
	
	private void restart() {
		time.stop();
		for(int i=0; i<ROW; i++) {
			for(int j=0; j<COL; j++) {
				buttons[i][j].setEnabled(true);
				buttons[i][j].setIcon(icons[10]);
				buttons[i][j].resetAll();
			}
		}
		randomBomb();
		fillNumber();
		mineLeft = 40;
		timeLeft = 1000;
		firstClick = true;
		clickCount = 0;
		timelbl.setText("Time: " + timeLeft);
		minelbl.setText("Mine: "+ mineLeft);
		isSaved = false;
	 }
		 
	
	public static void main(String[] args) {
		JFrame frame = new Minesweeper();
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setTitle("Minesweeper");
		frame.setResizable(false);
	}
	
}
