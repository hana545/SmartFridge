package hr.riteh.sl.smartfridge.FirebaseDatabase;

public class Store {
    public String name, userID;
    public double lat, lng;

    public Store(){

    }

    public Store(String name, String userID, double lat, double lng){
        this.name = name;
        this.userID = userID;
        this.lat = lat;
        this.lng = lng;

    }

}
