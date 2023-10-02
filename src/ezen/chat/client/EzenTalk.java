package ezen.chat.client;

public class EzenTalk {

	public static void main(String[] args) {
		ChatFrame chatFrame = new ChatFrame("::: 재밌는 대화 나누세요.. :::");
		chatFrame.setSize(400, 500); //프레임크기
		chatFrame.init(); //컴포넌트 배치
		chatFrame.addEventListener(); //이벤트 처리
		chatFrame.setVisible(true); //창 보이기.
	}

}
