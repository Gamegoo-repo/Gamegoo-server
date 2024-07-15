package com.gamegoo.dto.member;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RiotResponse {

    @Getter
    @Setter
    // Riot API 응답을 위한 DTO
    public static class RiotAccountDTO {
        private String puuid;
        private String gameName;
        private String tagLine;
    }

    @Getter
    @Setter
    // Riot Summoner API 응답을 위한 DTO
    public static class RiotSummonerDTO {
        private String id;
        private String accountId;
        private String puuid;
        private int profileIconId;
        private long revisionDate;
        private int summonerLevel;
    }

    @Getter
    @Setter
    public static class RiotLeagueEntryDTO {
        private String leagueId;
        private String queueType;
        private String tier;
        private String rank;
        private String summonerId;
        private int leaguePoints;
        private int wins;
        private int losses;
        private boolean veteran;
        private boolean inactive;
        private boolean freshBlood;
        private boolean hotStreak;
    }

    @Data
    @Getter
    @Setter
    public static class MatchDTO {
        private InfoDTO info;
    }

    @Data
    @Getter
    @Setter
    public static class InfoDTO {
        private List<ParticipantDTO> participants;
    }

    @Data
    @Getter
    @Setter
    public static class ParticipantDTO {
        private String riotIdGameName;
        private int championId;
    }
}
