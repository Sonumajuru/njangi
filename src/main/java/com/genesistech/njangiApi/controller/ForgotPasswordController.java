package com.genesistech.njangiApi.controller;

import com.genesistech.njangiApi.exception.TokenRefreshException;
import com.genesistech.njangiApi.model.ErrorResponse;
import com.genesistech.njangiApi.model.RefreshToken;
import com.genesistech.njangiApi.model.User;
import com.genesistech.njangiApi.payload.request.ForgotPasswordRequest;
import com.genesistech.njangiApi.payload.response.MessageResponse;
import com.genesistech.njangiApi.security.services.RefreshTokenService;
import com.genesistech.njangiApi.service.EmailServiceImpl;
import com.genesistech.njangiApi.service.interfaces.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import com.genesistech.njangiApi.helper.PasswordValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping(
        value = "/api/v1/forget",
        produces = MediaType.APPLICATION_JSON_VALUE
)

public class ForgotPasswordController {

    @Autowired
    UserService userService;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    PasswordEncoder encoder;

    /**
     * Request operation
     * @param forgotPasswordRequest
     * @return link to reset password
     */
    @PostMapping("/password")
    public ResponseEntity<?> forgotPassword(@RequestBody @NonNull ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();
        User user = userService.getByEmail(email);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found",
                    "User with email " + email + " does not exist")
                    , HttpStatus.NOT_FOUND);
        }

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        // TODO Change link later on
        String resetPasswordLink = "http://localhost:8080/api/v1/forget/reset?token=" + refreshToken.getToken();
        emailService.sendResetPasswordEmail(email, resetPasswordLink);

        return ResponseEntity.ok().body(new MessageResponse("Reset password link sent!"));
    }

    /**
     * Request operation
     * @param token
     * @return new updated password
     */
    @GetMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token) {
        AtomicBoolean isConfirmed = new AtomicBoolean(false);
        String htmlText;
        refreshTokenService.findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    isConfirmed.set(true);
                    return ResponseEntity.ok();
                })
                .orElseThrow(() -> new TokenRefreshException(token, "Invalid token"));

        // TODO Change link later on
        htmlText = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\" />\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />\n" +
                "    <title>Sign Up/In to SITENAME</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "    <link\n" +
                "      rel=\"stylesheet\"\n" +
                "      href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\"\n" +
                "    />\n" +
                "  </head>\n" +
                "  <style>\n" +
                "    .mainDiv {\n" +
                "      display: flex;\n" +
                "      min-height: 100%;\n" +
                "      align-items: center;\n" +
                "      justify-content: center;\n" +
                "      background-color: #f9f9f9;\n" +
                "      font-family: \"Open Sans\", sans-serif;\n" +
                "    }\n" +
                "    .cardStyle {\n" +
                "      width: 500px;\n" +
                "      border-color: white;\n" +
                "      background: #fff;\n" +
                "      padding: 36px 0;\n" +
                "      border-radius: 4px;\n" +
                "      margin: 30px 0;\n" +
                "      box-shadow: 0px 0 2px 0 rgba(0, 0, 0, 0.25);\n" +
                "    }\n" +
                "    #signupLogo {\n" +
                "      max-height: 100px;\n" +
                "      margin: auto;\n" +
                "      display: flex;\n" +
                "      flex-direction: column;\n" +
                "    }\n" +
                "    .formTitle {\n" +
                "      font-weight: 600;\n" +
                "      margin-top: 20px;\n" +
                "      color: #2f2d3b;\n" +
                "      text-align: center;\n" +
                "    }\n" +
                "    .inputLabel {\n" +
                "      font-size: 12px;\n" +
                "      color: #555;\n" +
                "      margin-bottom: 6px;\n" +
                "      margin-top: 24px;\n" +
                "    }\n" +
                "    .inputDiv {\n" +
                "      width: 70%;\n" +
                "      display: flex;\n" +
                "      flex-direction: column;\n" +
                "      margin: auto;\n" +
                "    }\n" +
                "    input {\n" +
                "      height: 40px;\n" +
                "      font-size: 16px;\n" +
                "      border-radius: 4px;\n" +
                "      border: none;\n" +
                "      border: solid 1px #ccc;\n" +
                "      padding: 0 11px;\n" +
                "    }\n" +
                "    input:disabled {\n" +
                "      cursor: not-allowed;\n" +
                "      border: solid 1px #eee;\n" +
                "    }\n" +
                "    .buttonWrapper {\n" +
                "      margin-top: 40px;\n" +
                "    }\n" +
                "    .submitButton {\n" +
                "      width: 70%;\n" +
                "      height: 40px;\n" +
                "      margin: auto;\n" +
                "      display: block;\n" +
                "      color: #fff;\n" +
                "      background-color: #065492;\n" +
                "      border-color: #065492;\n" +
                "      text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.12);\n" +
                "      box-shadow: 0 2px 0 rgba(0, 0, 0, 0.035);\n" +
                "      border-radius: 4px;\n" +
                "      font-size: 14px;\n" +
                "      cursor: pointer;\n" +
                "    }\n" +
                "    .submitButton:disabled,\n" +
                "    button[disabled] {\n" +
                "      border: 1px solid #cccccc;\n" +
                "      background-color: #cccccc;\n" +
                "      color: #666666;\n" +
                "    }\n" +
                "\n" +
                "    #loader {\n" +
                "      position: absolute;\n" +
                "      z-index: 1;\n" +
                "      margin: -2px 0 0 10px;\n" +
                "      border: 4px solid #f3f3f3;\n" +
                "      border-radius: 50%;\n" +
                "      border-top: 4px solid #666666;\n" +
                "      width: 14px;\n" +
                "      height: 14px;\n" +
                "      -webkit-animation: spin 2s linear infinite;\n" +
                "      animation: spin 2s linear infinite;\n" +
                "    }\n" +
                "\n" +
                "    @keyframes spin {\n" +
                "      0% {\n" +
                "        transform: rotate(0deg);\n" +
                "      }\n" +
                "      100% {\n" +
                "        transform: rotate(360deg);\n" +
                "      }\n" +
                "    }\n" +
                "  </style>\n" +
                "\n" +
                "  <body>\n" +
                "    <div class=\"mainDiv\">\n" +
                "  <div class=\"cardStyle\">\n" +
                "    <form method=\"post\" action=\"/api/v1/forget/reset-password\" name=\"signupForm\" id=\"signupForm\">\n" +
                "      \n" +
                "      <img src=\"\" id=\"signupLogo\"/>\n" +
                "      \n" +
                "      <h2 class=\"formTitle\">\n" +
                "        Choose a new password\n" +
                "      </h2>\n" +
                "      \n" +
                "    <div class=\"inputDiv\">\n" +
                "      <label class=\"inputLabel\" for=\"password\">New Password</label>\n" +
                "      <input type=\"password\" id=\"password\" name=\"password\" required>\n" +
                "    </div>\n" +
                "      \n" +
                "    <div class=\"inputDiv\">\n" +
                "      <label class=\"inputLabel\" for=\"confirmPassword\">Confirm Password</label>\n" +
                "      <input type=\"password\" id=\"confirmPassword\" name=\"confirmPassword\">\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"inputDiv\">\n" +
                "      <input type=\"hidden\" id=\"token\" name=\"token\">\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"buttonWrapper\">\n" +
                "      <button type=\"submit\" id=\"submitButton\" class=\"submitButton pure-button pure-button-primary\">\n" +
                "        <span>Continue</span>\n" +
                "        <span id=\"loader\"></span>\n" +
                "      </button>\n" +
                "    </div>\n" +
                "      \n" +
                "  </form>\n" +
                "  </div>\n" +
                "</div>\n" +
                "    </div>\n" +
                "  </body>\n" +
                "</html>\n" +
                "\n" +
                "<script>\n" +
                "\n" +
                "document.addEventListener('DOMContentLoaded', () => {\n" +
                "  const params = new URLSearchParams(window.location.search);\n" +
                "  const token = params.get('token');\n" +
                "  const tokenField = document.getElementById('token');\n" +
                "  tokenField.value = token;\n" +
                "});\n" +
                "document.getElementById('signupLogo').src = \"https://s3-us-west-2.amazonaws.com/shipsy-public-assets/shipsy/SHIPSY_LOGO_BIRD_BLUE.png\";\n" +
                "</script>";

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

    /**
     * Read operation
     * @param token
     * @param password
     * @param confirmPassword
     * @return returns saved new password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") @NonNull String password,
                                @RequestParam("confirmPassword") String confirmPassword) {
        AtomicBoolean isConfirmed = new AtomicBoolean(false);
        String htmlText;
        if (!password.equals(confirmPassword)) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Password match",
                    "Password do not match, try again!")
                    , HttpStatus.BAD_REQUEST);
        }

        if (!PasswordValidator.isValid(password)) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Password criteria are not met",
                    "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one symbol, and one number.")
                    , HttpStatus.BAD_REQUEST);
        }
        refreshTokenService.findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    user.setPassword(encoder.encode(password));
                    userService.SaveUser(user);
                    isConfirmed.set(true);
                    refreshTokenService.deleteByUserId(user.getId());
                    return ResponseEntity.ok();
                })
                .orElseThrow(() -> new TokenRefreshException(token, "Invalid token"));

        htmlText = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Congratulations!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Password Reset Successful!</h1>\n" +
                "    <p>Your password has been reset.</p>\n" +
                "    <p>You can close this tab and login to your account.</p>\n" +
                "  </body>\n" +
                "</html>";

        String centeredText = "<center>" + htmlText + "</center>";

        if (isConfirmed.get()){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(centeredText, headers, HttpStatus.OK);
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