package com.genesistech.njangiApi.controller;

import com.genesistech.njangiApi.Enum.ERole;
import com.genesistech.njangiApi.Enum.Subscription;
import com.genesistech.njangiApi.exception.TokenRefreshException;
import com.genesistech.njangiApi.model.ErrorResponse;
import com.genesistech.njangiApi.model.RefreshToken;
import com.genesistech.njangiApi.model.Role;
import com.genesistech.njangiApi.model.User;
import com.genesistech.njangiApi.payload.request.LoginRequest;
import com.genesistech.njangiApi.payload.request.SignupRequest;
import com.genesistech.njangiApi.payload.request.TokenRefreshRequest;
import com.genesistech.njangiApi.payload.response.JwtResponse;
import com.genesistech.njangiApi.payload.response.MessageResponse;
import com.genesistech.njangiApi.payload.response.TokenRefreshResponse;
import com.genesistech.njangiApi.repo.RoleRepo;
import com.genesistech.njangiApi.security.jwt.JwtUtils;
import com.genesistech.njangiApi.security.services.RefreshTokenService;
import com.genesistech.njangiApi.security.services.UserDetailsImpl;
import com.genesistech.njangiApi.service.EmailServiceImpl;
import com.genesistech.njangiApi.service.interfaces.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.genesistech.njangiApi.helper.PasswordValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = "/api/v1/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)

// Auth Controller Class
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepo roleRepo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    RefreshTokenService refreshTokenService;

    /**
     * Read operation
     * @param loginRequest
     * @return user details if authentication is successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @NonNull LoginRequest loginRequest) {

        if (!emailService.isValidEmail(loginRequest.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid email",
                    "Email is not a valid email address")
                    , HttpStatus.BAD_REQUEST);
        }

        if (!userService.existsByEmail(loginRequest.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid email",
                    "Email does not exist, or is not registered")
                    , HttpStatus.BAD_REQUEST);
        }

        if (!userService.getByEmail(loginRequest.getEmail()).isVerify()) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Not Verified",
                    "Email is not verified, Please check your email, verify by clicking the link and try again!")
                    , HttpStatus.UNAUTHORIZED);
        }

        if (!encoder.matches(loginRequest.getPassword(),
                userService.getByEmail(loginRequest.getEmail()).getPassword()))
        {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Wrong password",
                    "Password credentials do not match")
                    , HttpStatus.UNAUTHORIZED);
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(userDetails.getId(), jwt, "Bearer", refreshToken.getToken(),
                userDetails.getEmail(), refreshToken.getExpiryDate(), roles));
    }

    /**
     * Request operation
     * @param signUpRequest
     * @return registered user
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @NonNull SignupRequest signUpRequest) {

        if (!emailService.isValidEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid email",
                    "Email is not a valid email address")
                    , HttpStatus.BAD_REQUEST);
        }

        boolean isValidPassword = PasswordValidator.isValid(signUpRequest.getPassword());
        if (!PasswordValidator.isValid(signUpRequest.getPassword())) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Password criteria are not met",
                    "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one symbol, and one number.")
                    , HttpStatus.BAD_REQUEST);
        }

        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Email already exist",
                    "Email is already in use, or already exist")
                    , HttpStatus.BAD_REQUEST);
        }

        // Create new user's account
        User user = new User(null,
                null,
                false,
                null,
                null,
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getEmail(),
                null,
                null,
                null,
                null,
                false,
                Subscription.Free,
                null
                );

        Optional<Role> optionalRole = roleRepo.findByName(ERole.ROLE_USER);
        Role role = optionalRole.orElseGet(() -> new Role(null, ERole.ROLE_USER));
        roleRepo.save(role);

        user.setRoles(Collections.singleton(role));
        userService.SaveUser(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        // TODO Change link later on
        String confirmationLink = "http://localhost:8080/api/v1/auth/verify?token=" + refreshToken.getToken();
        emailService.sendEmail(user.getEmail(), confirmationLink);

        return ResponseEntity.ok(new MessageResponse("Registered successfully!"));
    }

    /**
     * Request operation
     * @param request
     * @return new refresh token for user authentication
     */
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@RequestBody @NonNull TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromEmail(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken, "Bearer"));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    /**
     * Request operation
     * @return logs out user
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

    /**
     * Read operation
     * @param authentication
     * @return user logged in status i.e. True/False
     */
    @GetMapping("/isuserloggedin")
    public ResponseEntity<?>  isUserLoggedIn(Authentication authentication) {
        if (authentication == null)
        {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Not Authorized",
                    "You are logged out or token expired!")
                    , HttpStatus.UNAUTHORIZED);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Not Authorized",
                    "You are not logged in!")
                    , HttpStatus.UNAUTHORIZED);
        }
        String username = authentication.getName();
        return ResponseEntity.ok(new MessageResponse("Hello, " + username));
    }

    /**
     * Request operation
     * @param token
     * @return link for user to verify before login into app
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        AtomicBoolean isConfirmed = new AtomicBoolean(false);
        String htmlText;
        refreshTokenService.findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    user.setVerify(true);
                    userService.SaveUser(user);
                    isConfirmed.set(true);
                    refreshTokenService.deleteByUserId(user.getId());
                    return ResponseEntity.ok();
                })
                .orElseThrow(() -> new TokenRefreshException(token, "Invalid token"));

        htmlText = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body>\n" +
                "    <div class=\"contain\">\n" +
                "      <div class=\"congrats\">\n" +
                "        <h1>Congrat<span class=\"hide\">ulation</span>s !</h1>\n" +
                "        <div class=\"done\">\n" +
                "          <svg\n" +
                "            version=\"1.1\"\n" +
                "            id=\"tick\"\n" +
                "            xmlns=\"http://www.w3.org/2000/svg\"\n" +
                "            xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "            x=\"0px\"\n" +
                "            y=\"0px\"\n" +
                "            viewBox=\"0 0 37 37\"\n" +
                "            style=\"enable-background: new 0 0 37 37\"\n" +
                "            xml:space=\"preserve\"\n" +
                "          >\n" +
                "            <path\n" +
                "              class=\"circ path\"\n" +
                "              style=\"\n" +
                "                fill: #0cdcc7;\n" +
                "                stroke: #07a796;\n" +
                "                stroke-width: 3;\n" +
                "                stroke-linejoin: round;\n" +
                "                stroke-miterlimit: 10;\n" +
                "              \"\n" +
                "              d=\"\n" +
                "\tM30.5,6.5L30.5,6.5c6.6,6.6,6.6,17.4,0,24l0,0c-6.6,6.6-17.4,6.6-24,0l0,0c-6.6-6.6-6.6-17.4,0-24l0,0C13.1-0.2,23.9-0.2,30.5,6.5z\"\n" +
                "            />\n" +
                "            <polyline\n" +
                "              class=\"tick path\"\n" +
                "              style=\"\n" +
                "                fill: none;\n" +
                "                stroke: #fff;\n" +
                "                stroke-width: 3;\n" +
                "                stroke-linejoin: round;\n" +
                "                stroke-miterlimit: 10;\n" +
                "              \"\n" +
                "              points=\"\n" +
                "\t11.6,20 15.9,24.2 26.4,13.8 \"\n" +
                "            />\n" +
                "          </svg>\n" +
                "        </div>\n" +
                "        <div class=\"text\">\n" +
                "          <p>\n" +
                "            You have successfully verified your account with us.\n" +
                "            <br />You can close this tab and login back into your account\n" +
                "            <br />\n" +
                "          </p>\n" +
                "          <p>Thanks for being a verified member</p>\n" +
                "        </div>\n" +
                "        <p class=\"regards\">The EchtTune Community!</p>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </body>\n" +
                "</html>\n" +
                "\n" +
                "<style>\n" +
                "  body {\n" +
                "    width: 100%;\n" +
                "    height: 100%;\n" +
                "  }\n" +
                "  .contain {\n" +
                "    position: absolute;\n" +
                "    top: 0;\n" +
                "    left: 0;\n" +
                "    width: 100%;\n" +
                "    height: 100%;\n" +
                "    background: linear-gradient(90deg, #189086, #2f8198);\n" +
                "    background-image: linear-gradient(to bottom right, #02b3e4, #02ccba);\n" +
                "  }\n" +
                "\n" +
                "  .done {\n" +
                "    width: 100px;\n" +
                "    height: 100px;\n" +
                "    position: relative;\n" +
                "    left: 0;\n" +
                "    right: 0;\n" +
                "    top: -20px;\n" +
                "    margin: auto;\n" +
                "  }\n" +
                "  .contain h1 {\n" +
                "    font-family: \"Julius Sans One\", sans-serif;\n" +
                "    font-size: 1.4em;\n" +
                "    color: #02b3e4;\n" +
                "  }\n" +
                "\n" +
                "  .congrats {\n" +
                "    position: relative;\n" +
                "    left: 50%;\n" +
                "    top: 50%;\n" +
                "    max-width: 800px;\n" +
                "    transform: translate(-50%, -50%);\n" +
                "    width: 80%;\n" +
                "    min-height: 300px;\n" +
                "    max-height: 900px;\n" +
                "    border: 2px solid white;\n" +
                "    border-radius: 5px;\n" +
                "    box-shadow: 12px 15px 20px 0 rgba(46, 61, 73, 0.3);\n" +
                "    background-image: linear-gradient(to bottom right, #02b3e4, #02ccba);\n" +
                "    background: #fff;\n" +
                "    text-align: center;\n" +
                "    font-size: 2em;\n" +
                "    color: #189086;\n" +
                "  }\n" +
                "\n" +
                "  .text {\n" +
                "    position: relative;\n" +
                "    font-weight: normal;\n" +
                "    left: 0;\n" +
                "    right: 0;\n" +
                "    margin: auto;\n" +
                "    width: 80%;\n" +
                "    max-width: 800px;\n" +
                "\n" +
                "    font-family: \"Lato\", sans-serif;\n" +
                "    font-size: 0.6em;\n" +
                "  }\n" +
                "\n" +
                "  .circ {\n" +
                "    opacity: 0;\n" +
                "    stroke-dasharray: 130;\n" +
                "    stroke-dashoffset: 130;\n" +
                "    -webkit-transition: all 1s;\n" +
                "    -moz-transition: all 1s;\n" +
                "    -ms-transition: all 1s;\n" +
                "    -o-transition: all 1s;\n" +
                "    transition: all 1s;\n" +
                "  }\n" +
                "  .tick {\n" +
                "    stroke-dasharray: 50;\n" +
                "    stroke-dashoffset: 50;\n" +
                "    -webkit-transition: stroke-dashoffset 1s 0.5s ease-out;\n" +
                "    -moz-transition: stroke-dashoffset 1s 0.5s ease-out;\n" +
                "    -ms-transition: stroke-dashoffset 1s 0.5s ease-out;\n" +
                "    -o-transition: stroke-dashoffset 1s 0.5s ease-out;\n" +
                "    transition: stroke-dashoffset 1s 0.5s ease-out;\n" +
                "  }\n" +
                "  .drawn svg .path {\n" +
                "    opacity: 1;\n" +
                "    stroke-dashoffset: 0;\n" +
                "  }\n" +
                "\n" +
                "  .regards {\n" +
                "    font-size: 0.7em;\n" +
                "  }\n" +
                "\n" +
                "  @media (max-width: 600px) {\n" +
                "    .congrats h1 {\n" +
                "      font-size: 1.2em;\n" +
                "    }\n" +
                "\n" +
                "    .done {\n" +
                "      top: -10px;\n" +
                "      width: 80px;\n" +
                "      height: 80px;\n" +
                "    }\n" +
                "    .text {\n" +
                "      font-size: 0.5em;\n" +
                "    }\n" +
                "    .regards {\n" +
                "      font-size: 0.6em;\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  @media (max-width: 500px) {\n" +
                "    .congrats h1 {\n" +
                "      font-size: 1em;\n" +
                "    }\n" +
                "\n" +
                "    .done {\n" +
                "      top: -10px;\n" +
                "      width: 70px;\n" +
                "      height: 70px;\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  @media (max-width: 410px) {\n" +
                "    .congrats h1 {\n" +
                "      font-size: 1em;\n" +
                "    }\n" +
                "\n" +
                "    .congrats .hide {\n" +
                "      display: none;\n" +
                "    }\n" +
                "\n" +
                "    .congrats {\n" +
                "      width: 100%;\n" +
                "    }\n" +
                "\n" +
                "    .done {\n" +
                "      top: -10px;\n" +
                "      width: 50px;\n" +
                "      height: 50px;\n" +
                "    }\n" +
                "    .regards {\n" +
                "      font-size: 0.55em;\n" +
                "    }\n" +
                "  }\n" +
                "</style>\n" +
                "\n" +
                "<script src=\"https://code.jquery.com/jquery-3.6.0.min.js\"></script>\n" +
                "<script>\n" +
                "  $(window).on(\"load\", function () {\n" +
                "    setTimeout(function () {\n" +
                "      $(\".done\").addClass(\"drawn\");\n" +
                "    }, 500);\n" +
                "  });\n" +
                "</script>\n";

        if (isConfirmed.get()){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(htmlText, headers, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid url",
                    "The requested resource does is not allowed")
                    , HttpStatus.BAD_REQUEST);
        }
    }
}