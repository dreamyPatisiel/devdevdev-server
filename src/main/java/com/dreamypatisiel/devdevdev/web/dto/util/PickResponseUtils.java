package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;

public class PickResponseUtils {

    public static boolean isVotedMember(Pick pick, Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPick().isEqualsId(pick.getId())
                        && pickVote.isMemberNotNull())
                .filter(pickVote -> !pickVote.isDeleted())
                .anyMatch(pickVote -> !pickVote.isDeleted()
                        && pickVote.getMember().isEqualsId(member.getId()));
    }

    public static boolean isVotedAnonymousMember(Pick pick, AnonymousMember anonymousMember) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPick().isEqualsId(pick.getId())
                        && pickVote.isAnonymousMemberNotNull())
                .anyMatch(pickVote -> !pickVote.isDeleted()
                        && pickVote.getAnonymousMember().isEqualAnonymousMemberId(anonymousMember.getId()));
    }

    public static boolean isPickedPickOptionByMember(Pick pick, PickOption pickOption,
                                                     Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPickOption().isEqualsId(pickOption.getId())
                        && pickVote.isMemberNotNull())
                .anyMatch(pickVote -> !pickVote.isDeleted()
                        && pickVote.getMember().isEqualsId(member.getId()));
    }

    public static boolean isPickedPickOptionByAnonymousMember(Pick pick, PickOption pickOption,
                                                              AnonymousMember anonymousMember) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPickOption().isEqualsId(pickOption.getId())
                        && pickVote.isAnonymousMemberNotNull())
                .anyMatch(pickVote -> !pickVote.isDeleted()
                        && pickVote.getAnonymousMember().isEqualAnonymousMemberId(anonymousMember.getId()));
    }
}
