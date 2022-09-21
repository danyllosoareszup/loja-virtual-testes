package br.com.zup.edu.nossalojavirtual.users;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.StringJoiner;

class NewUserRequest {

    @Email
    @NotEmpty
    private String login;


    /**
     * @deprecated frameworks eyes only
     */
    @Deprecated
    NewUserRequest() { }

    NewUserRequest(@Email @NotEmpty String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }


    @Override
    public String toString() {
        return "NewUserRequest{" +
                "login='" + login + '\'' +
                '}';
    }
}
