package com.example.socketdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Start extends AppCompatActivity {

    private EditText inputUsername, inputPassword,inputIP;
    private ProgressDialog progressDialog;
    private Feedback feedback;
    private Button loginButton;
    private CheckBox checkBox;
    private SessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Bundle bundle = getIntent().getExtras();  //这里好像啥数据都没有得到啊
        if (bundle != null) {
            // Retrieve the parcelable
            Feedback feedback = bundle.getParcelable("feedback");
            // Get the from the object
            String userName = feedback.getName();
            TextView display = (TextView)findViewById(R.id.display);
            display.setVisibility(View.VISIBLE);
            String prompt = userName.substring(0, 1).toUpperCase() + userName.substring(1) + " " + "your account has been created.";
            display.setText(prompt);
        }

        inputUsername = (EditText)findViewById(R.id.ed_username);
        inputPassword = (EditText)findViewById(R.id.ed_pswd);
        inputIP = (EditText)findViewById(R.id.ed_ip);
        loginButton = (Button)findViewById(R.id.btn_login);
        checkBox = (CheckBox)findViewById(R.id.cli_antologin);

        /**
         * Prepare the dialog to display when the login button is pressed
         */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());  //取得的是当前app所使用的application，这在AndroidManifest中唯一指定

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void OnRegister(View view)
    {
        Intent intent = new Intent(getApplicationContext(),Register.class);
        startActivity(intent);
    }

    public void OnLogin(View view) {
        String email = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Check for empty data in the form
        if (!email.isEmpty() && !password.isEmpty()) {

            // Avoid multiple clicks on the button
            loginButton.setClickable(false);

            //Todo : ensure the user has Internet connection

            // Display the progress Dialog
            progressDialog.setMessage("Logging in ...");
            if (!progressDialog.isShowing())
                progressDialog.show();

            //Todo: need to check weather the user has Internet before attempting checking the data
            // Start fetching the data from the Internet
            new OnlineCredentialValidation().execute(email,password);

        } else {
            // Prompt user to enter credentials
            Toast.makeText(getApplicationContext(),
                    "Enter your credentials.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    class OnlineCredentialValidation extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) { //这个string是个数组 储存了账号密码
            feedback = new Feedback();

            String response = null;
            OutputStreamWriter request = null;
            int parsingFeedback = feedback.FAIL;


            // Variables
            final String BASE_URL = new Config().getLoginUrl();
            final String EMAIL = "email";
            final String PASSWORD = "password";
            final String PARAMS = EMAIL + "=" + strings[0] + "&" + PASSWORD + "=" + strings[1];
            Log.d("TAG","Email and Pass - "+EMAIL + "=" + strings[0] + "&" + PASSWORD + "=" + strings[1]);

            URL url = null;
            HttpURLConnection connection = null;
            try {
                url = new URL(BASE_URL);
                connection = (HttpURLConnection) url.openConnection();
                //Set the request method to POST
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                // Timeout for reading InputStream arbitrarily set to 3000ms.
                connection.setReadTimeout(15000);
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connection.setConnectTimeout(15000);

                // Output the stream to the server
                request = new OutputStreamWriter(connection.getOutputStream());
                request.write(PARAMS);
                request.flush();
                request.close();

                // Get the inputStream using the same connection
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                response = readStream(inputStream, 500);
                inputStream.close();

                // Parsing the response
                parsingFeedback = parsingResponse(response);


            } catch (MalformedURLException e) {
                Log.e("TAG", "URL - " + e);
                feedback.setError_message(e.toString());
                return feedback.FAIL;
            } catch (IOException e) {
                Log.e("TAG", "openConnection() - " + e);
                feedback.setError_message(e.toString());
                return feedback.FAIL;
            } finally {
                if (connection != null) // Make sure the connection is not null before disconnecting
                    connection.disconnect();
                Log.d("TAG", "Response " + response);

                return parsingFeedback;  //返回的似乎是服务器对应的数据也不太对 这是一个int会传个下面那个函数
            }
        }




        @Override
        protected void onPostExecute(Integer mFeedback) {
            super.onPostExecute(mFeedback);
            if (progressDialog.isShowing()) progressDialog.dismiss();

            if (mFeedback == feedback.SUCCESS) {
                // Update the session
                if(checkBox.isChecked()){ session.setAutoLogin(true);}
                session.setLogin(true);
                session.setLoginIP(inputIP.getText().toString().trim());
                // Move the user to MainActivity and pass in the User name which was form the server
                Intent intent = new Intent(getApplication(), MainActivity.class);  //登陆成功 转跳
                intent.putExtra("feedback", feedback);
                startActivity(intent);
            } else {
                // Allow the user to click the button
                loginButton.setClickable(true);
                Toast.makeText(getApplication(), feedback.getError_message(), Toast.LENGTH_SHORT).show();
            }

        }

        /**
         * Converts the contents of an InputStream to a String.
         */
        String readStream(InputStream stream, int maxReadSize)
                throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] rawBuffer = new char[maxReadSize];
            int readSize;
            StringBuffer buffer = new StringBuffer();
            while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
                if (readSize > maxReadSize) {
                    readSize = maxReadSize;
                }
                buffer.append(rawBuffer, 0, readSize);
                maxReadSize -= readSize;
            }

            Log.d("TAG", buffer.toString());
            return buffer.toString();
        }
    }

    public int parsingResponse(String response) {

        try {
            JSONObject jObj = new JSONObject(response);
            /**
             * If the registration on the server was successful the return should be
             * {"error":false}
             * Else, an object for error message is added
             * Example: {"error":true,"error_msg":"Invalid email format."}
             * Success of the registration can be checked based on the
             * object error, where true refers to the existence of an error
             */
            boolean error = jObj.getBoolean("error");

            if (!error) {
                //No error, return from the server was {"error":false}
                JSONObject user = jObj.getJSONObject("user");
                String email = user.getString("email");
                feedback.setName(email);
                return feedback.SUCCESS;
            } else {
                // The return contains error messages
                String errorMsg = jObj.getString("error_msg");
                Log.d("TAG", "errorMsg : " + errorMsg);
                feedback.setError_message(errorMsg);
                return feedback.FAIL;
            }
        } catch (JSONException e) {
            feedback.setError_message(e.toString());
            return feedback.FAIL;
        }

    }



    /*public void OnLogin(View view)
    {
        session = new SessionManager(this);
        session.setLogin(true);
        Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);
    }*/

}
