import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

//Runs when a listener is implemented
class ListenerFunction implements ActionListener	{
	public void actionPerformed(ActionEvent e)	{}
}

public class chatclient implements Runnable {
	public static Socket socket = null;
	public static BufferedReader in = null;
	public static PrintWriter out = null;

	public static boolean WASCONNECTED;
	public final static int NULL = 0;
	public final static int NOTCONNECTED = 1;
	public final static int EXIT = 2;
	public final static int CONNECT = 3;
	public final static int CONNECTED = 4;
	 
	public final static String statusMessages[] =	{
		" Oh Snap! That is not a valid IP/Host or Port!",
		" Enter IP and Port, then click Connect button",
		" You will not see this message",
		" ...Thinking...", 
		" Connected To Chat Server"
	};

	public final static chatclient theConnection = new chatclient();
		public static String chatserverName = "10.1.10.100";
		public static int port = 6667;
		public static int connectionStatus = NOTCONNECTED;
		public static String statusString = statusMessages[connectionStatus];
		public static StringBuffer tbAppend = new StringBuffer("");
		public static StringBuffer tbSend = new StringBuffer("");

		public static JFrame frameChatClient = null;
		public static JTextArea txtChatHistory = null;
		public static JTextField txtChatInput = null;
		public static JPanel statusBar = null;
		public static JLabel statusInfo = null;
		public static JTextField txtIPHostName = null;
		public static JTextField txtPortNum = null;
		public static JButton btnConnect = null;
		public static JButton btnExit = null;

	private static JPanel jpanelBuilder() {
		JPanel panelChat = null;
		ListenerFunction btnListener = null;

		//Create an options panelChat
		JPanel panelSetup = new JPanel(new GridLayout(4, 1));
		//IP address input
		panelChat = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panelChat.add(new JLabel("Server:"));
		txtIPHostName = new JTextField(10); 
		txtIPHostName.setText(chatserverName);
		txtIPHostName.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				txtIPHostName.selectAll();
				//Should be editable only when not connected.
				if (connectionStatus != NOTCONNECTED) {
					changeStatusNTS(NULL, true);
				}	else {
					chatserverName = txtIPHostName.getText();
				}
			}
		});
		panelChat.add(txtIPHostName);
		panelSetup.add(panelChat);

		//Port input
		panelChat = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panelChat.add(new JLabel("Port:"));
		txtPortNum = new JTextField(10); txtPortNum.setEditable(true);
		txtPortNum.setText((new Integer(port)).toString());
		txtPortNum.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				//should be editable only when not connected.
				if (connectionStatus != NOTCONNECTED) {
					changeStatusNTS(NULL, true);
				}	else {
					int temp;
					try {
						temp = Integer.parseInt(txtPortNum.getText());
							port = temp;
					}	catch (NumberFormatException nfe) {
						txtPortNum.setText((new Integer(port)).toString());
						frameChatClient.repaint();
					}
				}
			}
	 	});
		panelChat.add(txtPortNum);
		panelSetup.add(panelChat);

		btnListener = new ListenerFunction() {
			public void actionPerformed(ActionEvent e) {
				if (connectionStatus != NOTCONNECTED) {
					changeStatusNTS(NULL, true);
				}
			}
		};
		ButtonGroup bg = new ButtonGroup();
		panelChat = new JPanel(new GridLayout(1, 2));
		panelSetup.add(panelChat);

		//Connect/Exit buttons
		JPanel btnPane = new JPanel(new GridLayout(1, 1, 2, 1));
		btnListener = new ListenerFunction() {
			public void actionPerformed(ActionEvent e) {
				//Request a connection initiation
				if (e.getActionCommand().equals("connect")) {
					changeStatusNTS(CONNECT, true);
				}
				//Exit
				else {
					changeStatusNTS(EXIT, true);
				}
			}
	 	};
		btnConnect = new JButton("Connect");
		btnConnect.setSize(new Dimension(2,2));
		btnConnect.setMnemonic(KeyEvent.VK_C);
		btnConnect.setActionCommand("connect");
		btnConnect.addActionListener(btnListener);
		btnConnect.setEnabled(true);
		btnExit = new JButton("Exit");
		btnExit.setMnemonic(KeyEvent.VK_X);
		btnExit.setActionCommand("exit");
		btnExit.setSize(new Dimension(2,2));
		btnExit.addActionListener(btnListener);
		btnPane.add(btnConnect);
		btnPane.add(btnExit);
		panelSetup.add(btnPane);

		return panelSetup;
	}
	 
	//Initialize all the GUI components and display the frame
	private static void buildChatClientScreen() {
		//Set up the status bar
		statusInfo = new JLabel();
		statusInfo.setText(statusMessages[NOTCONNECTED]);
		statusBar = new JPanel(new BorderLayout());
		statusBar.add(statusInfo, BorderLayout.CENTER);
		//Set up the options panelChat
		JPanel panelSetup = jpanelBuilder();
		//Set up the chat panelChat
		JPanel chatPane = new JPanel(new BorderLayout());
		txtChatHistory = new JTextArea(10,20);
		txtChatHistory.setLineWrap(true);
		txtChatHistory.setEditable(false);
		Font font = new Font("Arial", Font.PLAIN, 10);
		txtChatHistory.setFont(font);
		JScrollPane txtChatHistoryPane = new JScrollPane(txtChatHistory,
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		txtChatInput = new JTextField();
		txtChatInput.setEnabled(false);
		
		txtChatInput.addActionListener(new ListenerFunction() {
			public void actionPerformed(ActionEvent e) {
			String s = txtChatInput.getText();
				if (!s.equals("")) {
					chatHistoryUpdate(s + "\n");
					txtChatInput.selectAll();
					//Send the string
					sendString(s);
				}
			}
		});

		chatPane.add(txtChatInput, BorderLayout.SOUTH);
		chatPane.add(txtChatHistoryPane, BorderLayout.CENTER);
		//chatPane.setPreferredSize(new Dimension(200, 200));
		chatPane.setPreferredSize(new Dimension(400, 200));	//Make the JPanel wider so you can see stuff.
		//Set up the main panelChat
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.add(statusBar, BorderLayout.SOUTH);
		mainPane.add(panelSetup, BorderLayout.WEST);
		mainPane.add(chatPane, BorderLayout.CENTER);
		//Set up the main frame
		frameChatClient = new JFrame("Osirus chat client");
		frameChatClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameChatClient.setContentPane(mainPane);
		frameChatClient.setSize(frameChatClient.getPreferredSize());
		frameChatClient.setLocation(200, 200);
		frameChatClient.pack();
		frameChatClient.setVisible(true);
	}

	//The thread-safe way to change the GUI components while changing state
	private static void changeStatusTS(int newConnectStatus, boolean noError) {
		//Change state if valid state
		if (newConnectStatus != NULL) {
			connectionStatus = newConnectStatus;
		}
		//If there is no error, display the appropriate status message
		if (noError) {
			statusString = statusMessages[connectionStatus];
		}
		//Otherwise, display error message
		else {
			statusString = statusMessages[NULL];
		}
		//Call the run() routine (Runnable interface) on the error-handling and GUI-update thread
		SwingUtilities.invokeLater(theConnection);
	}

	//The non-thread-safe way to change the GUI components while changing state
	private static void changeStatusNTS(int newConnectStatus, boolean noError) {
		//Change state if valid state
		if (newConnectStatus != NULL) {
			connectionStatus = newConnectStatus;
		}
		//If there is no error, display the appropriate status message
		if (noError) {
			statusString = statusMessages[connectionStatus];
		}
		//Otherwise, display error message
		else {
			statusString = statusMessages[NULL];
		}
		//Call the run() routine (Runnable interface) on the current thread
		theConnection.run();
	}

	//Thread-safe way to append to the chat box
	private static void chatHistoryUpdate(String s) {
		synchronized (tbAppend) {
			tbAppend.append(s);
		}
	}

	//Add text to send-buffer
	private static void sendString(String s) {
		synchronized (tbSend) {
			tbSend.append(s + "\n");
		}
	}

	//Cleanup for disconnect
	private static void cleanUp() {
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}	catch (IOException e) { 
			socket = null;
		}

		try {
			if (in != null) {
				in.close();
				in = null;
			}
		}	catch (IOException e) {
				in = null; 
		}

		if (out != null) {
			out.close();
			out = null;
		}
	}

	//Checks the current state and sets the enables/disables accordingly
	public void run() {
		switch (connectionStatus) {
			case EXIT:
				if(WASCONNECTED)	{
					sendString("exit");
					out.print(tbSend); 
					out.flush();
				}
				cleanUp();
			System.exit(0);

			case CONNECTED:
				txtChatInput.setEnabled(true);
				WASCONNECTED=true;
				break;
			}

			//Make sure that the button/text field states are consistent with the internal states
			txtIPHostName.setText(chatserverName);
			txtPortNum.setText((new Integer(port)).toString());
			statusInfo.setText(statusString);
			txtChatHistory.append(tbAppend.toString());
			tbAppend.setLength(0);

			frameChatClient.repaint();
	}

	//The main procedure
	public static void main(String args[]) {
		String s;
		buildChatClientScreen();

		while (true) {
			try { //Poll every ~10 ms
						Thread.sleep(10);
			}	catch (InterruptedException e) {}
				switch (connectionStatus) {
					case CONNECT:
						try {
							socket = new Socket(chatserverName, port);
							in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							out = new PrintWriter(socket.getOutputStream(), true);
							changeStatusTS(CONNECTED, true);
						}
						//If error, clean up and output an error message
						catch (IOException e) {
							cleanUp();
							changeStatusTS(NOTCONNECTED, false);
						}
						break;

				 	case CONNECTED:
						try {
							//Send data
							if (tbSend.length() != 0) {
								out.print(tbSend); out.flush();
								tbSend.setLength(0);
								changeStatusTS(NULL, true);
							}

							//Receive data
							if (in.ready()) {
								s = in.readLine();
								if ((s != null) &&  (s.length() != 0)) {
									//Check if it is the end of a trasmission
									if (s.equals(EXIT)) {
										changeStatusTS(EXIT, true);
									}
									//Otherwise, receive what text
									else {
										chatHistoryUpdate(s + "\n");
										changeStatusTS(NULL, true);
									}
								}
							}
						}	catch (IOException e) {
								cleanUp();
								changeStatusTS(NOTCONNECTED, false);
						}
						break;
				}
			}
	}
}
