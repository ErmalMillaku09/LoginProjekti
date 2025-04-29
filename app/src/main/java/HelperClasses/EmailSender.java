package HelperClasses;

import com.mailersend.sdk.Recipient;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.exceptions.MailerSendException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.*;

public class EmailSender {

    // Map to store OTPs for each email
    private static Map<String, String> otpStorage = new HashMap<>();

    private static final String API_TOKEN = "mlsn.4ac44b88750fc63ffb98266e78064e5bcfebc5c7e043975e3d7df7ac1d8e2a5b";
    private static final String MAILERSEND_API_URL = "https://api.mailersend.com/v1/email";

    // OkHttp client to send the request
    private OkHttpClient client = new OkHttpClient();

    // Method to send the OTP email
    public void sendEmail(String recipientEmail) {
        Email email = new Email();

        // Set the sender's details (use your verified sender email)
        email.setFrom("Login Projekti", "MS_Y86jRT@test-51ndgwvvn25lzqx8.mlsender.net");

        // Add the recipient dynamically (from the parameter)
        email.addRecipient("User", recipientEmail);

        // Generate OTP
        String OTP = generateOTP();

        // Store OTP in memory for the given email
        otpStorage.put(recipientEmail.toLowerCase(), OTP);


        // Set subject and content (plain text)
        email.setSubject("Your OTP for Login Projekti");
        email.setPlain("Your OTP is: " + OTP);

        // Prepare the JSON payload for the email request
        String jsonPayload = "{"
                + "\"from\": {\"email\": \"MS_Y86jRT@test-51ndgwvvn25lzqx8.mlsender.net\", \"name\": \"Login Projekti\"},"
                + "\"to\": [{\"email\": \"" + recipientEmail + "\", \"name\": \"User\"}],"
                + "\"subject\": \"Your OTP for Login Projekti\","
                + "\"text\": \"Your OTP is: " + OTP + "\""
                + "}";

        // Prepare the request body
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        // Create the request with Authorization header
        Request request = new Request.Builder()
                .url(MAILERSEND_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle success
                    System.out.println("Email sent successfully! Message ID: " + response.body().string());
                } else {
                    // Handle failure
                    System.err.println("Email failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Generate 5-digit OTP
    public static String generateOTP() {
        Random random = new Random();
        int otp = 10000 + random.nextInt(90000);  // Generates a 5-digit OTP
        return String.valueOf(otp);
    }

    // Validate OTP entered by the user
    public static boolean validateOTP(String email, String enteredOtp) {
        String normalizedEmail = email.toLowerCase();
        String storedOtp = otpStorage.get(normalizedEmail);
        if (storedOtp != null && storedOtp.equals(enteredOtp)) {
            otpStorage.remove(normalizedEmail);
            return true;
        } else {
            return false;
        }
    }
}
