package com.utc2.cinema.controller;

import com.utc2.cinema.dao.AccountDao;
import com.utc2.cinema.dao.InventoryDao;
import com.utc2.cinema.model.entity.*;
import com.utc2.cinema.service.AccountService;
import com.utc2.cinema.utils.PasswordUtils;
import com.utc2.cinema.utils.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {

    @FXML
    private Button loginButton;
    @FXML
    private PasswordField passWord;
    @FXML
    private Label toRegisterButton;
    @FXML
    private TextField userName;
    @FXML
    private TextField passWordText;
    @FXML
    private CheckBox showPass;

    private void showMainMenu() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("/FXML/MainMenu.fxml"));
            Pane root = fxmlLoader.load();
            Scene scene = new Scene(root, 1160, 800);
            Stage stage = new Stage();
            stage.setTitle("Cinema Manager");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showManagerMenu() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("/FXML/MainManager.fxml"));
            MainManagerController a = new MainManagerController();
            fxmlLoader.setController(a);
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root, 1160, 800);
            Stage stage = new Stage();
            stage.setTitle("Cinema Manager");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            stage.setOnCloseRequest(event -> {
                System.out.println("🔄 Shutting down...");
                System.exit(0); // Tắt toàn bộ JVM - kill tất cả threads
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int loginFail = 0;String partEmail = "";
    @FXML
    public void onClickLoginButton(ActionEvent event) {
        try {
            if(!userName.getText().equals(partEmail))
            {
                partEmail = userName.getText();
                loginFail = 0;
            }
            String passw = passWord.isVisible() ? passWord.getText() : passWordText.getText();
            System.out.println(partEmail);
            if (userName.getText() == "" || passw == "")
                CustomAlert.showError("", "Có lỗi", "Vui lòng điền đầy đủ");
            else {
                String pass = passWord.isVisible() ? passWord.getText() : passWordText.getText();
                if (PasswordUtils.checkPassword(pass, AccountService.getPassword(userName.getText()))) {
                    Account findAccount = AccountService.findAccount(userName.getText());
                    if (findAccount != null) {
                        UserSession.createUserSession(findAccount.getId(), findAccount.getEmail(), findAccount.getPassword(), findAccount.getAccountStatus(), findAccount.getRoleId());
                        userName.clear();
                        passWord.clear();
                        passWordText.clear();
                        CustomAlert.showInfo("", "Thành công", "Đăng nhập thành công");
                        Stage loginWin = (Stage) userName.getScene().getWindow();
                        loginWin.close();
                        if (UserSession.getInstance().getRoleId() == 1)
                            showManagerMenu();
                        else if (UserSession.getInstance().getRoleId() == 3) {
                            showMainMenu();
                        }
                    }
                } else {
                    if(userName.getText().equals(partEmail))++loginFail;
                    if(loginFail == 3)
                    {
                        loginFail=0;
                        boolean checkInput = CustomAlert.showConfirmation("", "Bạn đã nhập sai 3 lần!", "Bạn có muốn reset mật khẩu không?");
                        if (checkInput == true) {
                            boolean checkOTP = false;
                            String otp = CreateOTP.generateOTP(6);
                            String emailCheck = userName.getText();
                            int failCount = 0;
                            EmailOTP.sendEmail(emailCheck,"Mã xác thực reset mật khẩu", otp);

                            while (true) {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle("Nhập mã OTP");
                                dialog.setHeaderText("OTP đã được gửi đến: " + emailCheck);
                                dialog.setContentText("Nhập 6 chữ số:");

                                Optional<String> result = CustomOTPDialog.show(emailCheck, "OTP đã được gửi đến:");

                                if (!result.isPresent()) {
                                    CustomAlert.showError("Đã hủy", "Xác thực OTP bị hủy", "Reset mật khẩu thất bại!");
                                    break;
                                }

                                String userInputOTP = result.get().trim();

                                if (userInputOTP.isEmpty()) {
                                    CustomAlert.showError("Lỗi", "Không được để trống OTP", "Vui lòng nhập lại.");
                                    continue;
                                }

                                if (!userInputOTP.matches("\\d{6}")) {
                                    CustomAlert.showError("Lỗi", "OTP phải bao gồm đúng 6 chữ số", "Vui lòng nhập lại.");
                                    continue;
                                }

                                if (!userInputOTP.equals(otp)) {
                                    CustomAlert.showError("Sai OTP", "Mã OTP không đúng", "Vui lòng nhập lại.");
                                    if(++failCount == 3)
                                    {
                                        failCount = 0;
                                        CustomAlert.showError("Đã hủy", "Sai 3 lần, OTP thất bại", "Đăng ký thất bại!");
                                        break;
                                    }
                                    continue;
                                }
                                checkOTP = true;
                                break;
                            }
                            if(checkOTP == true) {
                                String newPassword = CreateRandomPassword.generateRandomPassword(10);
                                EmailOTP.sendEmail(userName.getText(),"Mật khẩu mới",newPassword);
                                AccountDao.updatePasswordByEmail(userName.getText(),newPassword);
                                CustomAlert.showInfo("Thành công", "Reset mật khẩu thành công", "Một mật khẩu mới đã được gửi đến " + userName.getText());
                            }
                        }
                    }

                    else {
                        CustomAlert.showError("", "Có lỗi xảy ra", "Sai mật khẩu!");
                    }
                    passWord.setText("");
                }
            }
        } catch (Exception e) {
            CustomAlert.showError("", "Có lỗi xảy ra", "Không tìm thấy tài khoản đăng nhập!");
        }
    }

    @FXML
    void onCloseLogin(MouseEvent e) {
        System.out.println("🔄 Shutting down...");
        System.exit(0);
    }

    @FXML
    void onMinLogin(MouseEvent e) {
        Stage stage = (Stage) userName.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    void onMouseClickEmail(MouseEvent   event) {

    }

    @FXML
    void onMouseClickedPassword(MouseEvent   event) {

    }
    @FXML
    void onClickShowPassLogin(ActionEvent event) {
        if(showPass.isSelected() == true)
        {
            passWord.setVisible(false);
            passWordText.setVisible(true);
            passWordText.setText(passWord.getText());
        }
        else
        {
            passWord.setVisible(true);
            passWordText.setVisible(false);
            passWord.setText(passWordText.getText());
        }
    }
    @FXML
    public void onClickToRegister(MouseEvent event)
    {
        if(loginPane.isVisible()) {
            loginPane.setVisible(false);
            registerPane.setVisible(true);
        }
    }
    @FXML
    private Label backButton;

    @FXML
    private Label labelError;

    @FXML
    private TextField emailRegister;

    @FXML
    private TextField passConfirmPf;

    @FXML
    private PasswordField passConfirmTf;

    @FXML
    private TextField passwordPf;

    @FXML
    private PasswordField passwordTf;

    @FXML
    private Button registerButton;

    @FXML
    private CheckBox showPassCF;
    @FXML
    private Pane loginPane;
    @FXML
    private Pane registerPane;

    @FXML
    void OnClickPassConfirmPf(MouseEvent event) {
        if(labelError.getText() != "")
            labelError.setText("");
    }

    @FXML
    void OnClickPassConfirmTf(MouseEvent event) {
        if(labelError.getText() != "")
            labelError.setText("");
    }

    @FXML
    void OnClickPassPf(MouseEvent event) {
        if(labelError.getText() != "")
            labelError.setText("");
    }

    @FXML
    void OnClickPassTf(MouseEvent event) {
        if(labelError.getText() != "")
            labelError.setText("");
    }

    @FXML
    void onClickBackButton(MouseEvent event)
    {
        if(registerPane.isVisible()) {
            registerPane.setVisible(false);
            loginPane.setVisible(true);
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void onClickRegisterButton(ActionEvent event) {
        String email = emailRegister.getText().trim();
        String password = passwordTf.isVisible() ? passwordTf.getText() : passwordPf.getText();
        String confirmPass = passConfirmTf.isVisible() ? passConfirmTf.getText() : passConfirmPf.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            CustomAlert.showError("", "Có lỗi xảy ra", "Vui lòng điền đầy đủ!");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            CustomAlert.showError("", "Có lỗi xảy ra", "Email không đúng định dạng (thuan@gmail.com)!");
            return;
        }
        if (!PasswordUtils.isValidPassword(password)) {
            CustomAlert.showError("", "Có lỗi xảy ra",
                    "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
            return;
        }
        if (!password.equals(confirmPass)) {
            CustomAlert.showError("", "Có lỗi xảy ra", "Mật khẩu không trùng khớp!");
            return;
        }
        if (AccountService.checkEmail(email)) {
            CustomAlert.showError("", "Có lỗi xảy ra", "Email này đã tồn tại!");
            return;
        }
        boolean checkOTP = false;
        String otp = CreateOTP.generateOTP(6);
        String emailCheck = emailRegister.getText();

        try {
            EmailOTP.sendEmail(emailCheck,"Mã xác thực đăng ký", otp);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi email OTP", e);
        }
        int failCount = 0;
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nhập mã OTP");
            dialog.setHeaderText("OTP đã được gửi đến: " + emailCheck);
            dialog.setContentText("Nhập 6 chữ số:");

            Optional<String> result = CustomOTPDialog.show(emailCheck, "OTP đã được gửi đến:");

            if (!result.isPresent()) {
                CustomAlert.showError("Đã hủy", "Xác thực OTP bị hủy", "Đăng ký thất bại!");
                break;
            }

            String userInputOTP = result.get().trim();

            if (userInputOTP.isEmpty()) {
                CustomAlert.showError("Lỗi", "Không được để trống OTP", "Vui lòng nhập lại.");
                continue;
            }

            if (!userInputOTP.matches("\\d{6}")) {
                CustomAlert.showError("Lỗi", "OTP phải bao gồm đúng 6 chữ số", "Vui lòng nhập lại.");
                continue;
            }

            if (!userInputOTP.equals(otp)) {
                CustomAlert.showError("Sai OTP", "Mã OTP không đúng", "Vui lòng nhập lại.");
                if(++failCount == 3)
                {
                    failCount=0;
                    CustomAlert.showError("Đã hủy", "Sai 3 lần, OTP thất bại", "Đăng ký thất bại!");
                    break;
                }
                continue;
            }

            CustomAlert.showInfo("Thành công", "Xác thực OTP thành công", "Bạn có thể tiếp tục đăng ký.");
            checkOTP = true;
            break;
        }
        if(checkOTP == true)
        {
            Account accRegis = new Account(0, email, PasswordUtils.hashPassword(password), "OFFLINE", 3);
            emailRegister.clear();
            passwordTf.clear();
            passwordPf.clear();
            passConfirmTf.clear();
            passConfirmPf.clear();
            if (AccountService.registerAccount(accRegis))
            {
                Account b = AccountService.getDataByEmail(accRegis.getEmail());
                Inventory a = new Inventory(b.getId(),0,0,0, 0);
                InventoryDao.insertInventory(a);
                boolean check = CustomAlert.showConfirmation("", "Hoàn tất", "Đăng ký thành công, bạn có muốn đăng nhập không?");
                if (check == true) {
                    try {
                        Account findAccount = AccountService.findAccount(accRegis.getEmail());
                        UserSession.createUserSession(findAccount.getId(), findAccount.getEmail(), findAccount.getPassword(), findAccount.getAccountStatus(), findAccount.getRoleId());

                        Stage loginWin = (Stage) emailRegister.getScene().getWindow();
                        loginWin.close();

                        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("/FXML/MainMenu.fxml"));
                        Pane root = fxmlLoader.load();
                        Scene scene = new Scene(root, 1160, 800);
                        Stage stage = new Stage();
                        stage.setTitle("Cinema Manager");
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.show();
                        stage.setOnCloseRequest(e -> {
                            System.out.println("🔄 Shutting down...");
                            System.exit(0); // Tắt toàn bộ JVM - kill tất cả threads
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                CustomAlert.showError("", "Có lỗi xảy ra", "Đăng ký thất bại!");
            }
        }
    }


    @FXML
    void onClickShowPass(ActionEvent event) {
        boolean show = showPassCF.isSelected();
        handlePassword(show);
    }
    public void handlePassword(boolean show) {
        if (show) {
            passwordPf.setText(passwordTf.getText());
            passwordTf.setVisible(false);
            passwordPf.setVisible(true);
            passConfirmPf.setText(passConfirmTf.getText());
            passConfirmTf.setVisible(false);
            passConfirmPf.setVisible(true);
        } else
        {
            passwordTf.setText(passwordPf.getText());
            passwordPf.setVisible(false);
            passwordTf.setVisible(true);
            passConfirmTf.setText(passConfirmPf.getText());
            passConfirmPf.setVisible(false);
            passConfirmTf.setVisible(true);
        }
    }

    @FXML
    void onMouseClickedEmail(MouseEvent event) {
        if(labelError.getText() != "")
            labelError.setText("");
    }

}
