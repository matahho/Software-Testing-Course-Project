package mizdooni.controller;

import mizdooni.controllers.AuthenticationController;
import mizdooni.exceptions.DuplicatedUsernameEmail;
import mizdooni.exceptions.InvalidEmailFormat;
import mizdooni.exceptions.InvalidUsernameFormat;
import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestAuthenticationController {
    @Mock
    private UserService userService;
    @InjectMocks
    private AuthenticationController authenticationController;

    private Map<String, Object> signupParams = new HashMap<>();
    private Map<String, String> loginParams = new HashMap<>();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        signupParams.put("username", "testUser");
        signupParams.put("password", "testPassword");
        signupParams.put("email", "test@example.com");
        signupParams.put("role", "client");
        Map<String, String> address = new HashMap<>();
        address.put("country", "TestCountry");
        address.put("city", "TestCity");
        signupParams.put("address", address);

        loginParams.put("username", "testUser");
        loginParams.put("password", "testPassword");

    }

    @Test
    public void noUserExists_tryToSignupWithMissingAuthParam_failedToSignup() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        signupParams.remove("email");
        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> authenticationController.signup(signupParams),
                "Auth params are missed so you cannot signup and we expect an exception!"
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());

        verify(userService, never()).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
    }


    @Test
    public void noUserExists_tryToSignupWithInvalidRole_failedToSignupDueToBadType() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        signupParams.put("role", "RandomWrongRole");

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> authenticationController.signup(signupParams),
                "Auth params are wrong it type, so you cannot signup and we expect an exception!"
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());

        verify(userService, never()).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
    }

    @Test
    public void noUserExists_tryToSignupWithCorrectParams_signedUpSuccessfully() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        // Expectations :
        User mockUser = new User("testUser", "testPassword", "test@example.com", new Address("TestCountry", "TestCity", null), User.Role.client);
        doNothing().when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
        when(userService.login("testUser", "testPassword")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);
        // _____

        Response response = authenticationController.signup(signupParams);

        assertEquals("signup successful", response.getMessage());
        assertEquals(mockUser, response.getData());
        verify(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
        verify(userService).login("testUser", "testPassword");

    }

    @Test
    public void userExists_tryToSignupWithSameEmailAddress_failed() throws Exception{
        doThrow(new DuplicatedUsernameEmail()).when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.signup(signupParams));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
    }

    @Test
    public void userExists_tryToLogin_successfullyLoggedIn(){
        when(userService.login(anyString(), anyString())).thenReturn(true);

        Response response = authenticationController.login(loginParams);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("login successful", response.getMessage());
        verify(userService).login(anyString(), anyString());

    }

    @Test
    public void userExists_tryToLoginWithWrongPassword_notAllowed(){
        loginParams.put("password", "InvalidPassword");
        when(userService.login("testUser", "InvalidPassword")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class , ()-> authenticationController.login(loginParams));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("invalid username or password", exception.getMessage());
        verify(userService).login("testUser", "InvalidPassword");

    }

    @Test
    public void userExists_tryToLoginWithNullParam_failedToLogin() {
        loginParams.put("password" , null);
        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.login(loginParams));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
        verify(userService, never()).login(anyString(), anyString());
    }


    @Test
    public void userExists_tryToLoginWithMissingParameter_failedToLogin() {
        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.login(new HashMap<>()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    public void userLoggedIn_tryToGetCurrentUser_currentUserRetrievedCorrectly(){
        User mockUser = new User("testUser", "testPassword", "test@example.com", new Address("TestCountry", "TestCity", null), User.Role.client);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authenticationController.user();
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("current user", response.getMessage());
        assertEquals(mockUser, response.getData());
        verify(userService).getCurrentUser();
    }

    @Test
    public void userNotLoggedIn_tryToGetCurrentUser_UnauthenticatedErrorRaisedProperly(){
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.user());
        assertEquals(HttpStatus.UNAUTHORIZED ,exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
        verify(userService).getCurrentUser();
    }

    @Test
    public void userHaveLoggedIn_tryToLogout_loggedOutCorrectly(){
        when(userService.logout()).thenReturn(true);

        Response response = authenticationController.logout();
        assertEquals("logout successful",response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(userService).logout();
    }
    @Test
    public void userNotLoggedIn_tryToLogout_properErrorRaised(){
        when(userService.logout()).thenReturn(false);

        ResponseException response = assertThrows(ResponseException.class, () -> authenticationController.logout());
        assertEquals("no user logged in",response.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        verify(userService).logout();
    }

}
