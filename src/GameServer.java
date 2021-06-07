
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.swing.*;

public class GameServer extends JFrame implements Runnable {
	
	private Connection conn;
	private PreparedStatement queryStmt;
	private PreparedStatement insertGameStmt;
	private PreparedStatement insertRankStmt;
	private JTextArea stateArea;
	private int clientNo = 0;
	
	public GameServer() {
		
		stateArea = new JTextArea(10,10);
		JScrollPane sp = new JScrollPane(stateArea);
		this.add(sp);
		this.setTitle("GameServer");
		this.setSize(400,200);
		Thread t = new Thread(this);
		t.start();
	}

	
	public void run() {
		// connect to database
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:gameStore.db");
			queryStmt= conn.prepareStatement("Select * from Game WHERE Name = ?");
			insertGameStmt = conn.prepareStatement("INSERT INTO Game VALUES (?, ?)");
			insertRankStmt = conn.prepareStatement("INSERT INTO Rank (Name, Score) VALUES (?, ?)");
			stateArea.append("Database connected at " + new Date() + '\n');
		} catch (SQLException e) {
			System.err.println("Connection error: " + e);
			System.exit(1);
		}
		
		try {		
			// Create a server socket
			ServerSocket serverSocket = new ServerSocket(8000);
			stateArea.append("MultiThreadServer started at " + new Date() + '\n');
			
	        while (true) {
	        	Socket socket = serverSocket.accept();
	        	clientNo++;
	        	stateArea.append("Starting thread for client " + clientNo + " at " + new Date() + '\n');
	            InetAddress inetAddress = socket.getInetAddress();
	            stateArea.append("Client " + clientNo + "'s host name is "
	              + inetAddress.getHostName() + "\n");
	            stateArea.setCaretPosition(stateArea.getText().length());
	            new Thread(new HandleAClient(socket, clientNo)).start();
	        }
		} catch(IOException ex) {
			System.err.println(ex);
		} 
	}
  

	class HandleAClient implements Runnable {
		private Socket socket; // A connected socket
		private int clientNum;
		
		public HandleAClient(Socket socket, int clientNum) {
			this.socket = socket;
			this.clientNum = clientNum;
		}

		/** Run a thread */
		public void run() {
			ObjectInputStream inputFromClient = null;
			ObjectOutputStream outputToClient = null;
			try {
				inputFromClient = new ObjectInputStream(socket.getInputStream());
				outputToClient = new ObjectOutputStream(socket.getOutputStream());
				Object object = inputFromClient.readObject();
				GameState gs = (GameState)object;
				int type = gs.getType();
				if(type == 1) {
					// open
					try {
						PreparedStatement stmt = queryStmt;
						stmt.setString(1, gs.getName());
						ResultSet rset = stmt.executeQuery();
						GameState receivedObject = null;
						if(rset.next()) {
							stateArea.append("Game found!\n");
							byte[] buf = rset.getBytes(2);
							ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
							Object deSerializedObject = objectIn.readObject();
							receivedObject = (GameState)deSerializedObject;
						}else {
							stateArea.append("No such game!\n");
							receivedObject = new GameState(-1, null, null, 0, 0, true, 0);
						}			
						rset.close();
						outputToClient.writeObject(receivedObject);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}else if (type == 2) {
					// save
					try {
						PreparedStatement stmt = insertGameStmt;
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bos);   
						out.writeObject(gs);
						out.flush();
						byte[] yourBytes = bos.toByteArray();

						stmt.setString(1, gs.getName());
						stmt.setBytes(2, yourBytes);
						stmt.execute();
			            stateArea.append("Save successfully!\n");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}else if (type == 3) {
					// rank search
					int[] scores = new int[5];
					String[] names = new String[5];
					try {
						Statement stmt = conn.createStatement();
						String s = "SELECT * FROM Rank ORDER BY Score DESC LIMIT 5";
						ResultSet rset = stmt.executeQuery(s);
						int count = 0;
						for (int i=0; i<5; i++) {
							rset.next();
							Object o = rset.getObject(1);
							names[i] = o.toString();
							scores[i] = (Integer)rset.getObject(2);
						}
						stateArea.append("Asking for ranking list!\n");
						rset.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					RankList ranklist = new RankList(scores, names);
					outputToClient.writeObject(ranklist);
				}else if (type == 4) {
					// rank insert
					try {
						PreparedStatement stmt = insertRankStmt;
						stmt.setString(1, gs.getName());
						stmt.setInt(2, gs.getTimeLeft());
						stmt.execute();
						stateArea.append("Score recorded successfully!\n");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException | ClassNotFoundException ex) {
				ex.printStackTrace();
			} /*finally {
				try {
					inputFromClient.close();
					outputToClient.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}		*/
		}
	}
	
	
	public static void main(String[] args) {
		GameServer mts = new GameServer();
		mts.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mts.setVisible(true);
	}
}