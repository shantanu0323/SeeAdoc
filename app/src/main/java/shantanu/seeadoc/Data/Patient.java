package shantanu.seeadoc.Data;

/**
 * Created by SHAAN on 16-06-17.
 */

public class Patient {
    private String profilePic;
    private String gender;
    private String name;
    private String age;
    private String disease;
    private String phone;
    private String bloodGroup;
    private String email;
    private String password;
    private String uid;

    public Patient(String profilePic, String gender, String name, String age, String disease, String phone, String bloodGroup, String email, String password) {
        this.profilePic = profilePic;
        this.gender = gender;
        this.name = name;
        this.age = age;
        this.disease = disease;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.email = email;
        this.password = password;
        this.uid = "default";
    }

    public String getDisease() {
        return disease;
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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDiseases() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
}
