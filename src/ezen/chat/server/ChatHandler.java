package ezen.chat.server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.css.CSSCharsetRule;

import ezen.chat.protocol.MessageType;

public class ChatHandler extends Thread {

	private Socket socket;
	private DataInput in;
	private DataOutput out;

	private ChatServer chatServer;
	private String nickName;
	private boolean running;
	
	//클라이언트가 접속시 생성되는 생성자
	public ChatHandler(Socket socket, ChatServer chatServer) throws IOException {
		//클라이언트와 연결된 새로운 서버측 소켓값을받음.
		this.socket = socket;
		this.chatServer = chatServer;
		//해당 서버측 소켓으로 클라이언트와 연결된 입출력스트림 생성.
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		//접속하면 프로세스 동작하게끔 러닝 true초기화
		running = true;
	}

	public ChatServer getChatServer() {
		return chatServer;
	}

	public String getNickName() {
		return nickName;
	}

	public Socket getSocket() {
		return socket;
	}

	public DataInput getIn() {
		return in;
	}

	public DataOutput getOut() {
		return out;
	}

	public void process() throws IOException {

		while (running) {
			// 파싱되지않은 클라이언트 메시지(구분안됨.)
			String clientMessage = in.readUTF();

			System.out.println("[디버깅] : " + clientMessage);
			// 메세지 파싱
			String[] tokens = clientMessage.split("\\|");

			MessageType messageType = MessageType.valueOf(tokens[0]);

			// 클라이언트 전송 메시지 종류에 따른 처리
			switch (messageType) {
				//연결메시지
				case CONNECT:
					nickName = tokens[1];
					setName(nickName);
					chatServer.addChatClient(this);
					//그대로받아서 그대로보냄 화면에 보여지는건 클라이언트가 처리함.
					chatServer.sendMessageAll(clientMessage);
					//현재 연결된 모든 클라이언트 닉네임 목록 전송
					sendList();
					break;
					
				//다중채팅메시지	
				case CHAT_MESSAGE:
					chatServer.sendMessageAll(clientMessage);
					break;
					
				//연결 해제 메시지
				case DIS_CONNECT:
					chatServer.removeChatClient(this);
					chatServer.sendMessageAll(clientMessage);
					running = false;
					//빠져나갈때도 전체 닉네임목록 보여줌.(삭제된 후의 목록)
					sendList();
					break;
					
				//1:1 채팅메시지
				case DM_MESSAGE:
					String sender = tokens[1];
					String receiver = tokens[2];
					chatServer.sendMessageDm(clientMessage, sender, receiver);
					break;
			}
		}
		close();
	}

	// 자기 자신에게 메시지 출력
	public void sendMessage(String message) throws IOException {
		out.writeUTF(message);
	}
	
	// 클라이언트에게 보내는 닉네임 리스트
	private void sendList() throws IOException {
		Collection<ChatHandler> lists = chatServer.getClients();
		chatServer.getAllName(lists);
		chatServer.sendMessageAll(MessageType.CONNECT_LIST+"|"+ nickName +"|"+chatServer.getAllName(lists));
	}
	
	//프로세스 종료 메서드
	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		try {
			process();
			System.out.println("["+getName()+"]님이 연결을 종료하였습니다...");
		} catch (IOException e) {
			System.err.println("에코 처리 중 예기치 않은 오류가 발생하였습니다.");
		}
	}
}
