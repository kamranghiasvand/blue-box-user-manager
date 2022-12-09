package com.bluebox.user;

import com.bluebox.service.user.UserEntity;

class UserBuilder {
    private final UserEntity user = new UserEntity();

    public UserEntity build() {
        return user;
    }

    public UserBuilder email(String email) {
        user.setEmail(email);
        return this;
    }

    public UserBuilder password(String pass) {
        user.setPassword(pass);
        return this;
    }

    public UserBuilder firstName(String name) {
        user.setFirstName(name);
        return this;
    }

    public UserBuilder lastName(String lastName) {
        user.setLastName(lastName);
        return this;
    }
}
