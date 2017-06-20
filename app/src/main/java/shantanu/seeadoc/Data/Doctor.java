package shantanu.seeadoc.Data;

/**
 * Created by SHAAN on 16-06-17.
 */

public class Doctor {
    private String profilepic;
    private String gender;
    private String name;
    private String degree;
    private String address;
    private String phone;
    private String specialization;
    private String email;
    private String password;
    private String uid;

    public Doctor() {
    }

    public Doctor(String profilepic, String gender, String name, String degree, String address, String phone, String specialization, String email, String password, String uid) {
        this.profilepic = profilepic;
        this.gender = gender;
        this.name = name;
        this.degree = degree;
        this.address = address;
        this.phone = phone;
        this.specialization = specialization;
        this.email = email;
        this.password = password;
        this.uid = uid;
    }


    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecializtion(String specializtion) {
        this.specialization = specializtion;
    }
}
