package com.github.devlucasjava.apilucabank.service;

import com.github.devlucasjava.apilucabank.dto.mapper.UsersMapper;
import com.github.devlucasjava.apilucabank.dto.response.UsersResponse;
import com.github.devlucasjava.apilucabank.exception.ResourceNotFoundException;
import com.github.devlucasjava.apilucabank.model.Users;
import com.github.devlucasjava.apilucabank.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    public UsersResponse getAuthenticatedUser(Users user) {
        Users foundUser = usersRepository.findByEmailOrPassport(user.getEmail())
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found: {}", user.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        log.debug("Authenticated user retrieved: {}", foundUser.getEmail());
        return usersMapper.toUsersResponse(foundUser);
    }
}