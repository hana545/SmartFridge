package hr.riteh.sl.smartfridge.SendNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAptQ_Kjc:APA91bGpVCVUxXLQWywWs3c0niGVJcvyHm0S591IDJ8MAd5UOMbGq_IWdgfksZFYlTOV6KnP8aQ-zmn9cdVMbyLA8E2Pt6ot9bQrp1kK7kXft8Rgpii0ncfGXYnxN54SyOL2l3xS8qbZ" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}