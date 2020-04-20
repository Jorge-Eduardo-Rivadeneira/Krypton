package krypton_package;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML private RadioButton encryptRDB;
    @FXML private RadioButton decryptRDB;
    @FXML private Button btnStart;
    @FXML private Label lblPath;
    @FXML private PasswordField txtPassword;
    private String folder_location;
    private String rename_file;
    private String encrypt_file;
    private String password;
    byte[] salt = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
            0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
            0x30, 0x30, (byte)0x9d };
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ToggleGroup group = new ToggleGroup();
        encryptRDB.setToggleGroup(group);
        decryptRDB.setToggleGroup(group);
        btnStart.setText("Encrypt");
    }

    public void ExitButtonClick(MouseEvent mouseEvent){
        Platform.exit();
        System.exit(0);
    }

    public void StartButtonClick(MouseEvent mouseEvent)  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("k[r]!PTØn - Open Source Cryptographic Tool");
        alert.setHeaderText(null);

        if (lblPath.getText().isBlank() && txtPassword.getText().isBlank()){
            alert.setContentText("Must select a folder, and define a password");
            alert.showAndWait();

        }else if (lblPath.getText().isBlank()){
            alert.setContentText("Must select a folder");
            alert.showAndWait();
        }else if (txtPassword.getText().isBlank()) {
            alert.setContentText("Must define a password");
            alert.showAndWait();
        }else{
            File folder = new File(folder_location);
            File[] listOfFiles = folder.listFiles(file -> !file.isHidden());
            password=txtPassword.getText();

            Alert alertII = new Alert(Alert.AlertType.INFORMATION);
            alertII.setTitle("k[r]!PTØn - Open Source Cryptographic Tool");
            alertII.setHeaderText(null);

                if (encryptRDB.isSelected()) {

                    for (File file : listOfFiles) {
                        rename_file = folder_location + "/" + sha256(getFileName(file)) + getFileExtension(file);
                        file.renameTo(new File(rename_file));
                        encrypt(rename_file);
                    }

                    alertII.setContentText("The files from "+ folder_location + " are now encrypted");
                    alertII.showAndWait().ifPresent(rs -> {
                        if (rs == ButtonType.OK) {
                            lblPath.setText("");
                            txtPassword.setText("");
                        }
                    });


                }else {
                    for (File file : listOfFiles) {
                        try {
                            encrypt_file=file.getAbsolutePath();
                            decrypt(encrypt_file);

                        } catch (Exception e) {
                            alert.setContentText("Wrong Password");
                            alert.showAndWait();
                            txtPassword.setText("");
                        }
                    }

                    alertII.setContentText("The files from "+ folder_location + " are now decrypted");
                    alertII.showAndWait().ifPresent(rs -> {
                        if (rs == ButtonType.OK) {
                            lblPath.setText("");
                            txtPassword.setText("");
                            encryptRDB.selectedProperty().set(true);
                            btnStart.setText("Encrypt");
                        }
                    });

                }
            }
    }




    public void SearchFolderClick(MouseEvent mouseEvent) {
        try{
            Node node = (Node) mouseEvent.getSource();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(node.getScene().getWindow());
            folder_location=selectedDirectory.getAbsolutePath();
            if(selectedDirectory == null){
                lblPath.setText("");
            }else{
                lblPath.setText(folder_location);
            }
        }
        catch (Exception e){
            lblPath.setText("");
        }

    }

    public void SelectEncrypt(MouseEvent mouseEvent){
        btnStart.setText("Encrypt");
    }

    public void SelectDecrypt(MouseEvent mouseEvent){
        btnStart.setText("Decrypt");
    }

    private static String getFileExtension(File file) {
        String extension = "";
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }
        return extension;

    }

    private static String getFileName(File file) {
        String filename = "";
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                filename = name.substring(0,name.lastIndexOf("."));

            }
        } catch (Exception e) {
            filename = "";
        }
        return filename;

    }

    private static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private void encrypt(String file_path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = f.generateSecret(spec).getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        byte[] ivBytes = new byte[16];
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] inputBytes = Files.readAllBytes(Paths.get(file_path));
        byte[] outputBytes = cipher.doFinal(inputBytes);
        FileOutputStream fileOutputStream = new FileOutputStream(file_path+".enc");
        fileOutputStream.write(outputBytes);
        fileOutputStream.close();
    }
    private void decrypt(String file_path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

           PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
           SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
           byte[] key = f.generateSecret(spec).getEncoded();
           SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
           byte[] ivBytes = new byte[16];
           IvParameterSpec iv = new IvParameterSpec(ivBytes);
           Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

           cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
           byte[] inputBytes = Files.readAllBytes(Paths.get(file_path));
           byte[] outputBytes = cipher.doFinal(inputBytes);
           FileOutputStream fileOutputStream = new FileOutputStream(file_path.substring(0, file_path.length() - 4));
           fileOutputStream.write(outputBytes);
           fileOutputStream.close();

    }

}
