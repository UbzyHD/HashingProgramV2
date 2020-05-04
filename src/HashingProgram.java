import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

//TODO: Add better REGEX matching for MD5 and SHA-256 - current implementation is "\n" which is not appropriate for all types of matching.

public class HashingProgram extends JFrame {
    private static final List<String> FileName_HashList_Array = new ArrayList<>();
    private static final List<String> MD5_HashList_Array = new ArrayList<>();
    private static final List<String> SHA256_HashList_Array = new ArrayList<>();
    private JPanel mainPanel;
    private JTextPane txt_FileName;
    private JTextPane txt_SHA256TextPane;
    private JTextPane txt_MD5TextPane;
    private JButton selectFileButton;
    private JTextField txt_StringSearch;
    private JRadioButton MD5RadioButton;
    private JRadioButton SHA256RadioButton;
    private JButton btn_SearchHashString;
    private JButton importHashListButton;
    private JLabel lbl_StringStatus;

    public HashingProgram(String title) {
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.setPreferredSize(new Dimension(1100, 400));
        this.pack();

        ButtonGroup button_group = new ButtonGroup();
        button_group.add(MD5RadioButton);
        button_group.add(SHA256RadioButton);

        selectFileButton.addActionListener(e -> {
            try {
                selectFileHash();
            } catch (NoSuchAlgorithmException | IOException noSuchAlgorithmException) {
                noSuchAlgorithmException.printStackTrace();
            }
        });

        importHashListButton.addActionListener(e -> {
            if (MD5RadioButton.isSelected()) {
                try {
                    importHashCalculate(MD5_HashList_Array);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (SHA256RadioButton.isSelected()) {
                try {
                    importHashCalculate(SHA256_HashList_Array);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        btn_SearchHashString.addActionListener(e -> SearchHashString());

        txt_StringSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (txt_StringSearch.getText().matches("[a-fA-F0-9]{32}")) {
                    lbl_StringStatus.setText("String Input Status: MD5 String Detected");
                    lbl_StringStatus.setForeground(Color.green);
                } else if (txt_StringSearch.getText().matches("[a-fA-F0-9]{64}")) {
                    lbl_StringStatus.setText("String Input Status: SHA-256 String Detected");
                    lbl_StringStatus.setForeground(Color.green);
                } else {
                    lbl_StringStatus.setText("String Input Status: NO MATCH FOUND");
                    lbl_StringStatus.setForeground(Color.RED);
                }
            }
        });
    }

    public static void main(String[] args) {

        JFrame frame = new HashingProgram("Hashing Program by Mohammed Uabidul Islam - 19085817");

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();  // Get screen-size
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);   // Set variables so
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2); // display is centered

        frame.setLocation(x, y);   // Set window size
        frame.setVisible(true); // Display window on launch

    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public void selectFileHash() throws NoSuchAlgorithmException, IOException {

        StringBuilder FileName_output = new StringBuilder();
        StringBuilder MD5Hash_output = new StringBuilder();
        StringBuilder SHA256Hash_output = new StringBuilder();

        FileName_HashList_Array.clear();
        MD5_HashList_Array.clear();
        SHA256_HashList_Array.clear();

        final FileDialog fileDialog = new FileDialog(this, "FileDialog");
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);
        File[] files = fileDialog.getFiles();

        for (File file : files) {

            // File Name operations
            FileName_HashList_Array.add(file.getName());
            txt_FileName.setText(String.valueOf(FileName_output.append(file.getName()).append("\n")));

            // MD5 operations
            MessageDigest MD5_Digest = MessageDigest.getInstance("MD5");
            String MD5_checksum = getFileChecksum(MD5_Digest, file);
            MD5_HashList_Array.add(MD5_checksum);
            txt_MD5TextPane.setText(String.valueOf(MD5Hash_output.append(MD5_checksum).append("\n")));

            // SHA-256 operations
            MessageDigest SHA256_Digest = MessageDigest.getInstance("SHA-256");
            String SHA256_checksum = getFileChecksum(SHA256_Digest, file);
            SHA256_HashList_Array.add(SHA256_checksum);
            txt_SHA256TextPane.setText((String.valueOf(SHA256Hash_output.append(SHA256_checksum).append("\n"))));

        }

    }

    public void SearchHashString() {
        StringBuilder output = new StringBuilder();

        IntStream.range(0, FileName_HashList_Array.size()).forEach(i -> {
            if (MD5_HashList_Array.get(i).equalsIgnoreCase(txt_StringSearch.getText())) {
                output.append(FileName_HashList_Array.get(i)).append("\n");
            }
            if (SHA256_HashList_Array.get(i).equalsIgnoreCase(txt_StringSearch.getText())) {
                output.append(FileName_HashList_Array.get(i)).append("\n");
            }
        });

        if (txt_StringSearch.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Enter a hash string");
        }
        if (output.toString().equals("")) {
            output.append("No files found.");
        }
        JOptionPane.showMessageDialog(null, "Files found matching hash: \n\n" + output.toString());
    }

    public static void importHashCalculate(List<String> HashListArray) throws IOException {

        StringBuilder output = new StringBuilder();

        FileDialog dialog = new FileDialog((Frame) null, "Select Hash List File to Open");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String Hash_file = dialog.getFile();

        List<String> HashList_Array = new ArrayList<>(Arrays.asList(new String(Files.readAllBytes(Paths.get(Hash_file))).split("\n")));

        IntStream.range(0, HashList_Array.size()).forEach(z -> IntStream.range(0, HashListArray.size()).forEach(i -> {
            if (HashList_Array.get(z).equalsIgnoreCase(HashListArray.get(i))) {
                output.append("File Name: ").append(FileName_HashList_Array.get(i)).append("\n");
            }
        }));

        JOptionPane.showMessageDialog(null, "Found the following file(s):\n" + output);
    }

}