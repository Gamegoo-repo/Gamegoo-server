package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.dto.member.RiotResponse;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RiotService {
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;
    @Value("${riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_ACCOUNT_API_URL_TEMPLATE = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s";
    private static final String RIOT_SUMMONER_API_URL_TEMPLATE = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s";
    private static final String RIOT_LEAGUE_API_URL_TEMPLATE = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/%s?api_key=%s";

    // Riot GameName으로 DB에 데이터 저장하기
    public void verifyRiot(String game_name, String tag, String email) {
        // emaiL 로 DB에서 member 찾기
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        /* RIOT API */
        // 티어, 랭킹 정보 불러오기
        String puuid = getRiotPuuid(game_name, tag);
        String encryptedSummonerId = getSummonerId(puuid);
        updateMemberWithLeagueInfo(member, game_name, encryptedSummonerId);

        // 최근 사용한 챔피언 3개 찾기
        // 1. riot API에서 최근 매칭 ID 세 개 List에 저장
        // 2. List에 있는 매칭 ID 바탕으로 각 매칭에서 유저가 사용한 캐릭터 불러오기
        // 3. 캐릭터와 유저 데이터 매핑해서 DB에 저장하기
        
        // DB에 저장
        memberRepository.save(member);
    }

    private String getRiotPuuid(String game_name, String tag) {
        // 1. GameName과 Tag로 puuid 생성
        String url = String.format(RIOT_ACCOUNT_API_URL_TEMPLATE, game_name, tag, riotAPIKey);
        RiotResponse.RiotAccountDTO accountResponse = restTemplate.getForObject(url, RiotResponse.RiotAccountDTO.class);

        // API를 불러올 수 없을 경우
        if (accountResponse == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        return accountResponse.getPuuid();
    }

    private String getSummonerId(String puuid) {
        // 2. puuid를 통해 encryptedsummonerid 얻기
        String summonerUrl = String.format(RIOT_SUMMONER_API_URL_TEMPLATE, puuid, riotAPIKey);
        RiotResponse.RiotSummonerDTO summonerResponse = restTemplate.getForObject(summonerUrl, RiotResponse.RiotSummonerDTO.class);

        if (summonerResponse == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        return summonerResponse.getId();
    }

    private void updateMemberWithLeagueInfo(Member member, String game_name, String encryptedSummonerId) {
        // 3. account id로 티어, 랭크, 불러오기
        String leagueUrl = String.format(RIOT_LEAGUE_API_URL_TEMPLATE, encryptedSummonerId, riotAPIKey);
        RiotResponse.RiotLeagueEntryDTO[] leagueEntries = restTemplate.getForObject(leagueUrl, RiotResponse.RiotLeagueEntryDTO[].class);

        if (leagueEntries == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        for (RiotResponse.RiotLeagueEntryDTO entry : leagueEntries) {
            if ("RANKED_SOLO_5x5".equals(entry.getQueueType())) {
                member.setGameuserName(game_name);
                member.setTier(entry.getTier());
                member.setRank(entry.getRank());
                break;
            }
        }
    }

}
