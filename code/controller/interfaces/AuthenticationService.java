package controller.interfaces;

import entity.*;

public interface AuthenticationService {
    User login(String hospitalId, String password);
    boolean changePassword(User user, String oldPassword, String newPassword);
}