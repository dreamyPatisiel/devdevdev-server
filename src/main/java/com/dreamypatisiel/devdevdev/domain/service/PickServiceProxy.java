package com.dreamypatisiel.devdevdev.domain.service;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class PickServiceProxy implements PickService {

    private final ApplicationContext applicationContext;

    @Override
    public Slice<PicksResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, Authentication authentication) {
        if(isMember()) {
            return applicationContext.getBean(MemberPickService.class).findPicksMain(pageable, pickId, pickSort, authentication);
        }

        return applicationContext.getBean(GuestPickService.class).findPicksMain(pageable, pickId, pickSort, authentication);
    }

    private boolean isMember() {
        return !AuthenticationMemberUtils.isAnonymous();
    }
}
