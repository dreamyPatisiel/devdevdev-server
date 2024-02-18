package com.dreamypatisiel.devdevdev.domain.service;

import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;

public interface PickService {
    Slice<PicksResponse> findPicks(Pageable pageable, Long pickId, UserDetails userDetails);
}
