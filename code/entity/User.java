// Base user class that other roles will inherit from
package entity;

public abstract class User {
    protected final String hospitalId;
    protected String password;
    protected String name;
    
    public User(String hospitalId, String password, String name) {
        this.hospitalId = hospitalId;
        this.password = password;
        this.name = name;
    }
    
    public boolean validatePassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!validatePassword(oldPassword) || newPassword.length() < 8 || oldPassword.equals(newPassword)) {
            return false;
        }
        this.password = newPassword;
        return true;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}