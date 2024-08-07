package com.dreamypatisiel.devdevdev.domain.service.response.util;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;

public class PickResponseUtils {

    public static boolean isVotedMember(Pick pick, Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPick().isEqualsId(pick.getId()) && pickVote.isMemberNotNull())
                .anyMatch(pickVote -> pickVote.getMember().isEqualId(member.getId()));
    }

    public static boolean isVotedAnonymousMember(Pick pick, AnonymousMember anonymousMember) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPick().isEqualsId(pick.getId()) && pickVote.isAnonymousMemberNotNull())
                .anyMatch(pickVote -> pickVote.getAnonymousMember().isEqualAnonymousMember(anonymousMember.getId()));
    }

    public static boolean isPickedPickOptionByMember(Pick pick, PickOption pickOption,
                                                     Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPickOption().isEqualsId(pickOption.getId())
                        && pickVote.isMemberNotNull())
                .anyMatch(pickVote -> pickVote.getMember().isEqualId(member.getId()));
    }

    public static boolean isPickedPickOptionByAnonymousMember(Pick pick, PickOption pickOption,
                                                              AnonymousMember anonymousMember) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPickOption().isEqualsId(pickOption.getId())
                        && pickVote.isAnonymousMemberNotNull())
                .anyMatch(pickVote -> pickVote.getAnonymousMember().isEqualAnonymousMember(anonymousMember.getId()));
    }

    public static String sliceAndMaskEmail(String email) {
        String id = email.replaceAll("@.*", ""); // @ 이후 문자열 제거
        return id.replaceAll("(?<=.{3}).", "*"); // 앞 3자리만 제외하고 전부 *로 마스킹
    }
}
