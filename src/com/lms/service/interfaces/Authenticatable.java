package com.lms.service.interfaces;

import com.lms.exception.AuthenticationException;

public interface Authenticatable {
    boolean authenticate(String passwordEntered);
    String getUsername();
    String getRoleName();
}
