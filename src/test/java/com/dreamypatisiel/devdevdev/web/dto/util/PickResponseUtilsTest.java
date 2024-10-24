package com.dreamypatisiel.devdevdev.web.dto.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PickResponseUtilsTest {

    @Test
    @DisplayName("투표한 회원인지 확인한다.")
    void isVotedMember() {
        // given
        Pick mockPick = mock(Pick.class);
        Member mockMember = mock(Member.class);
        PickVote mockPickVote = mock(PickVote.class);

        // when
        when(mockMember.getId()).thenReturn(1L);

        when(mockPick.getPickVotes()).thenReturn(List.of(mockPickVote));
        when(mockPick.getId()).thenReturn(1L);

        when(mockPickVote.getPick()).thenReturn(mockPick);
        when(mockPickVote.getPick().isEqualsId(mockPick.getId())).thenReturn(true);
        when(mockPickVote.isMemberNotNull()).thenReturn(true);
        when(mockPickVote.isDeleted()).thenReturn(false);
        when(mockPickVote.getMember()).thenReturn(mockMember);
        when(mockPickVote.getMember().isEqualsId(mockMember.getId())).thenReturn(true);

        boolean isVotedMember = PickResponseUtils.isVotedMember(mockPick, mockMember);

        // then
        assertThat(isVotedMember).isTrue();
    }

    @Test
    @DisplayName("투표한 익명 회원인지 확인한다.")
    void isVotedAnonymousMember() {
        // given
        Pick mockPick = mock(Pick.class);
        AnonymousMember mockAnonymousMember = mock(AnonymousMember.class);
        PickVote mockPickVote = mock(PickVote.class);

        // when
        when(mockAnonymousMember.getId()).thenReturn(1L);

        when(mockPick.getPickVotes()).thenReturn(List.of(mockPickVote));
        when(mockPick.getId()).thenReturn(1L);

        when(mockPickVote.getPick()).thenReturn(mockPick);
        when(mockPickVote.getPick().isEqualsId(mockPick.getId())).thenReturn(true);
        when(mockPickVote.isAnonymousMemberNotNull()).thenReturn(true);
        when(mockPickVote.isDeleted()).thenReturn(false);
        when(mockPickVote.getAnonymousMember()).thenReturn(mockAnonymousMember);
        when(mockPickVote.getAnonymousMember().isEqualAnonymousMemberId(mockAnonymousMember.getId())).thenReturn(true);

        boolean isVotedAnonymousMember = PickResponseUtils.isVotedAnonymousMember(mockPick, mockAnonymousMember);

        // then
        assertThat(isVotedAnonymousMember).isTrue();
    }

    @Test
    @DisplayName("픽픽픽 선택지를 선택한 회원인지 확인한다.")
    void isPickedPickOptionByMember() {
        // given
        Pick mockPick = mock(Pick.class);
        Member mockMember = mock(Member.class);
        PickVote mockPickVote = mock(PickVote.class);
        PickOption mockPickOption = mock(PickOption.class);

        // when
        when(mockMember.getId()).thenReturn(1L);

        when(mockPick.getPickVotes()).thenReturn(List.of(mockPickVote));
        when(mockPick.getId()).thenReturn(1L);

        when(mockPickOption.getId()).thenReturn(1L);

        when(mockPickVote.getPick()).thenReturn(mockPick);
        when(mockPickVote.getPick().isEqualsId(mockPick.getId())).thenReturn(true);
        when(mockPickVote.isMemberNotNull()).thenReturn(true);
        when(mockPickVote.isDeleted()).thenReturn(false);
        when(mockPickVote.getMember()).thenReturn(mockMember);
        when(mockPickVote.getMember().isEqualsId(mockMember.getId())).thenReturn(true);

        when(mockPickVote.getPickOption()).thenReturn(mockPickOption);
        when(mockPickVote.getPickOption().isEqualsId(mockPickOption.getId())).thenReturn(true);

        boolean isVotedMember = PickResponseUtils.isPickedPickOptionByMember(mockPick, mockPickOption, mockMember);

        // then
        assertThat(isVotedMember).isTrue();
    }

    @Test
    @DisplayName("픽픽픽 선택지를 선택한 익명회원인지 확인한다.")
    void isPickedPickOptionByAnonymousMember() {
        // given
        Pick mockPick = mock(Pick.class);
        AnonymousMember mockAnonymousMember = mock(AnonymousMember.class);
        PickVote mockPickVote = mock(PickVote.class);
        PickOption mockPickOption = mock(PickOption.class);

        // when
        when(mockAnonymousMember.getId()).thenReturn(1L);

        when(mockPick.getPickVotes()).thenReturn(List.of(mockPickVote));
        when(mockPick.getId()).thenReturn(1L);

        when(mockPickOption.getId()).thenReturn(1L);

        when(mockPickVote.getPick()).thenReturn(mockPick);
        when(mockPickVote.getPick().isEqualsId(mockPick.getId())).thenReturn(true);
        when(mockPickVote.isAnonymousMemberNotNull()).thenReturn(true);
        when(mockPickVote.isDeleted()).thenReturn(false);
        when(mockPickVote.getAnonymousMember()).thenReturn(mockAnonymousMember);
        when(mockPickVote.getAnonymousMember().isEqualAnonymousMemberId(mockAnonymousMember.getId())).thenReturn(true);

        when(mockPickVote.getPickOption()).thenReturn(mockPickOption);
        when(mockPickVote.getPickOption().isEqualsId(mockPickOption.getId())).thenReturn(true);

        boolean isVotedMember = PickResponseUtils.isPickedPickOptionByAnonymousMember(mockPick, mockPickOption,
                mockAnonymousMember);

        // then
        assertThat(isVotedMember).isTrue();
    }
}