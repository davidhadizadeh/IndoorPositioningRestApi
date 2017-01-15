package de.hadizadeh.positioning.api.rest;

import java.io.IOException;
import java.util.Properties;

/**
 * Handles authentication services for external authentication servers (empty for the prototype)
 */
public class AuthenticationService {
    /**
     * Checks the authentication
     * @param authCredentials authentication credentials
     * @param method http method
     * @return true, if authentication is successful, else it is not successful
     * @throws IOException if the check could not be done
     */
    public boolean authenticate(String authCredentials, String method) throws IOException {
        return true;
//        if("GET".equals(method)) {
//            return true;
//        } else {
//            if (authCredentials != null) {
//                Properties properties = new Properties();
//                properties.load(MainResource.class.getResourceAsStream("config.properties"));
//                String correctAuthToken = (String) properties.get("auth_token");
//                String token = authCredentials.replaceFirst("Bearer ", "");
//                return correctAuthToken.equals(token);
//            }
//            return false;
//        }
    }
}
