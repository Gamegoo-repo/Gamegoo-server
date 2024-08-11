package com.gamegoo.service.socket;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.SocketHandler;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SocketService {

    private final RestTemplate restTemplate;

    @Value("${socket.server.url}")
    private String SOCKET_SERVER_URL;

    /**
     * SOCKET서버로 해당 member의 socket을 chatroom에 join시키는 API 전송
     *
     * @param memberId
     * @param chatroomUuid
     */
    public void joinSocketToChatroom(Long memberId, String chatroomUuid) {

        String url = SOCKET_SERVER_URL + "/socket/room/join";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("memberId", memberId);
        requestBody.put("chatroomUuid", chatroomUuid);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody,
                String.class);

            log.info("response of joinSocketToChatroom: {}", response.getStatusCode().toString());
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                log.error("joinSocketToChatroom API call FAIL: {}", response.getBody());
                throw new SocketHandler(ErrorStatus.SOCKET_API_RESPONSE_ERROR);
            } else {
                log.info("joinSocketToChatroom API call SUCCESS: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error occurred while notifyChatroomEntered method", e);
            throw new SocketHandler(ErrorStatus.SOCKET_API_RESPONSE_ERROR);
        }
    }
}
