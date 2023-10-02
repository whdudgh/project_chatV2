package ezen.chat.client;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import ezen.chat.protocol.MessageType;

public class ChatFrame extends Frame {

	Label nicknameL;
	TextField nicknameTF, inputTF;
	Button loginB, sendB;
	TextArea messageTA, nicknameList;

	Panel northP, southP;

	ChatClient chatClient;
	String nickName;

	Choice choice;

	public ChatFrame() {
		this("No-Title");
	}

	public ChatFrame(String title) {
		super(title);
		// 복합연관
		nicknameL = new Label("닉네임");
		nicknameTF = new TextField();
		inputTF = new TextField();
		loginB = new Button("연  결");
//		loginB.setBackground(new Color(255, 255, 255));
//		loginB.setBackground(Color.BLUE);
//		loginB.setForeground(Color.WHITE);
//		loginB.setFont(new Font("궁서", Font.BOLD, 35));
		sendB = new Button("보내기");
		messageTA = new TextArea();
		nicknameList = new TextArea(10, 10);
		northP = new Panel(new BorderLayout(5, 5));
		southP = new Panel(new BorderLayout(5, 5));
		choice = new Choice();
		addAllChat();
	}

//	컴포넌트 배치
	public void init() {
//		Frame의 디폴트레이아웃매니저 : BorderLayout
		northP.add(nicknameL, BorderLayout.WEST);
		northP.add(nicknameTF, BorderLayout.CENTER);
		northP.add(loginB, BorderLayout.EAST);

		southP.add(inputTF, BorderLayout.CENTER);
		southP.add(sendB, BorderLayout.EAST);
		southP.add(choice, BorderLayout.WEST);

		add(northP, BorderLayout.NORTH);
		add(messageTA, BorderLayout.CENTER);
		add(nicknameList, BorderLayout.EAST);
		add(southP, BorderLayout.SOUTH);
	}

	private void connect() {
		nickName = nicknameTF.getText();
		if (!Validator.hasText(nickName)) {
			JOptionPane.showMessageDialog(this, "닉네임을 입력하세요");
			return;
		}
		chatClient = new ChatClient(this);
		try {
			chatClient.connectServer();
			setEnable(false);
//			chatClient.sendMessage("CONNECT|" + nickName);
			chatClient.sendMessage(MessageType.CONNECT + "|" + nickName);
			chatClient.receiveMessage();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "ChatServer를 연결할 수 없습니다..");
		}
	}

	private void setEnable(boolean enable) {
		nicknameTF.setEnabled(enable);
	}

	public void appendMessage(String message) {
		messageTA.append(message + "\n");
	}

	public void appendUserList(String nickName) {
		nicknameList.append(nickName + "\n");
	}

	// 닉네임 텍스트 에이리어 빈칸으로 초기화메서드
	public void clearList() {
		nicknameList.setText("");
	}

	// 초이스의 항목 전체 삭제
	public void resetChoice() {
		choice.removeAll();
	}

	// 초이스에 전체 채팅 항목 추가
	public void addAllChat() {
		choice.add("전  체");
	}

	// 해당 이름 초이스에 추가
	public void appendChoice(String name) {
		choice.add(name);
	}

	// 여기서 메세지 형식을 지정함(타입 | 닉네임 | 메세지) 서버쪽에서 분류하기 쉽게함.
	private void sendChatMessage() {
		String message = inputTF.getText();
		if (Validator.hasText(message)) {
			try {
				chatClient.sendMessage(MessageType.CHAT_MESSAGE + "|" + nickName + "|" + message);
				inputTF.setText("");
			} catch (IOException e) {
			}
		}
	}

	// DM형식 메세지 보내기
	private void sendDmChat(String receiver) {
		String message = inputTF.getText();
		if (Validator.hasText(message)) {
			try {
				chatClient.sendMessage(MessageType.DM_MESSAGE + "|" + nickName + "|" + receiver + "|" + message);
				inputTF.setText("");
			} catch (IOException e) {

			}
		}
	}
	
	/*
	 * 접속 종료시 서버에 보낼 메세지 형식 
	 */
	private void disConnect() {
		try {
			chatClient.sendMessage(MessageType.DIS_CONNECT + "|" + nickName);
			exit();
		} catch (IOException e) {

		}

	}
	
	/*
	 * 연결된 모든 스트림을 닫아주는 종료처리 메소드.  
	 */
	private void exit() {
		setVisible(false);
		dispose();
		System.exit(0);
	}

	public void addEventListener() {
		// 종료 처리
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disConnect();
			}
		});

		// 연결 처리
		loginB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});

		nicknameTF.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});

		inputTF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (choice.getSelectedItem().equals("전  체")) {
					sendChatMessage();
				}
				sendDmChat(choice.getSelectedItem());
			}
		});

		sendB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (choice.getSelectedItem().equals("전  체")) {
					sendChatMessage();
				}
				sendDmChat(choice.getSelectedItem());
			}
		});

	}

}
