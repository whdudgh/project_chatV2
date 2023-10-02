package ezen.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ChatServer {

	private static final int SERVER_PORT = 7777;
	private ServerSocket serverSocket;
	private boolean running;
	//접속한클라이언트를 관리할 콜렉션 선언
	private Map<String, ChatHandler> clients;
	String[] namelist;

	/** ChatServer 구동 */
	public void startup() throws IOException {
		serverSocket = new ServerSocket(SERVER_PORT);
		System.out.println("========= [ChatServer(" + SERVER_PORT + ")] Start =========");
		running = true;
		clients = new Hashtable<String, ChatHandler>();
		try {
			while (running) {
				System.out.println("[ChatServer(" + SERVER_PORT + ")] ChatClient Connect Listenning ..");
				Socket socket = serverSocket.accept();
				System.out.println("[ChatCleint(" + socket.getInetAddress().getHostAddress() + ")] 연결해옴...");

				// 데이터 송수신 스레드 생성 및 실행
				ChatHandler chatHandler = new ChatHandler(socket, this);
				chatHandler.start();
			}
		} catch (IOException e) {
			System.err.println("[ChatServer(" + SERVER_PORT + ")] 실행 중 아래와 같은 오류가 발생하였습니다.");
			System.err.println("오류 내용 :  " + e.getMessage());
		}
	}

	/** 접속한 클라이언트를 콜렉션에 추가 */
	public void addChatClient(ChatHandler chatHandler) {
		clients.put(chatHandler.getName(), chatHandler);
		System.err.println("[현재 채팅에 참여중인 클라이언트 수] : " + clients.size());
	}

	/** 접속한 클라이언트를 콜렉션에 제거 */
	public void removeChatClient(ChatHandler chatHandler) {
		clients.remove(chatHandler.getName());
		System.err.println("[현재 채팅에 참여중인 클라이언트 수] : " + clients.size());
	}

	/** 접속한 모든 클라이언트에게 메시지 전송 
	 * @throws IOException */
	public void sendMessageAll(String message) throws IOException {
		Collection<ChatHandler> list = clients.values();
		for (ChatHandler chatHandler : list) {
			chatHandler.sendMessage(message);
		}
		
	}
	
	/*
	 * DM메세지 보내기 기능 게임의 귓속말과 같이 보낸이가 자신이 보낸 메세지까지 같이보이게끔 두명에게 동시에 보내기로함.
	 */
	public void sendMessageDm(String message, String sender, String receiver) throws IOException {
		Collection<ChatHandler> list = clients.values();
		for (ChatHandler chatHandler : list) {
			if (chatHandler.getName().equals(sender) || chatHandler.getName().equals(receiver)) {
			chatHandler.sendMessage(message);
			} 
		}
		return;	
	}

	/*
	 * 클라이언트의 정보를 얻기위해 클라이언트 자체목록을 보내줌.
	 */
	public Collection<ChatHandler> getClients(){
		return clients.values();
	}
	
	/*
	 * 스트링빌더를 이용한 문자열의 수정 후 문자열로 리턴
	 */
	public String getAllName(Collection<ChatHandler> clients) {
		StringBuilder builder = new StringBuilder();
		for(ChatHandler chatHandler : clients) {
			builder.append(chatHandler.getNickName()).append(",");
		}
		return builder.toString();
	}
	

	/** ChatServer 종료 */
	public void shutdown() {
		try {
			serverSocket.close();
			System.err.println("[ChatServer(" + SERVER_PORT + ")] 종료됨...");
		} catch (IOException e) {
		}
	}

}
