package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.util.RiotUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
@RequiredArgsConstructor
public class RiotService {
    private final RiotUtil riotUtil;

    /**
     * Riot 계정 유무 확인
     *
     * @param gameName
     * @param tag
     * @return
     */
    @Transactional
    public String verifyRiot(String gameName, String tag) {
        String riotPuuid = riotUtil.getRiotPuuid(gameName, tag);

        if (riotPuuid == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        return riotPuuid;
    }

}
