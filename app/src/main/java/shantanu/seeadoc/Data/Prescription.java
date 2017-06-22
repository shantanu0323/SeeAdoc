package shantanu.seeadoc.Data;

/**
 * Created by SHAAN on 22-06-17.
 */

public class Prescription {
    private String name, profilepic, message, image, uid;

    public Prescription() {
    }

    public Prescription(String name, String profilepic, String message, String image, String uid) {
        this.name = name;
        this.profilepic = profilepic;
        this.message = message;
        this.image = image;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
