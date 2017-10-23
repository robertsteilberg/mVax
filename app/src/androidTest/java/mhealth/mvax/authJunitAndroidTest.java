package mhealth.mvax;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Test;

/**
 * Created by mtribby on 10/23/17.
 */


public class authJunitAndroidTest {
    public static final String TEST_USERNAME = "testusernameMVAX@mvaxtest.com";
    public static final String TEST_PASSWORD = "password";


    @Test
    public void checkAuthWorks(){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(TEST_USERNAME, TEST_PASSWORD);
        auth.signInWithEmailAndPassword(TEST_USERNAME, TEST_PASSWORD);
        Boolean correctUser = auth.getCurrentUser().getEmail().equals(TEST_USERNAME);

        //Delete user to allow for reproducing test
        auth.getCurrentUser().delete();

        assert(correctUser);
    }


}