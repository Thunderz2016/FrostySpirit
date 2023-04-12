import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

public class SaveGuard extends FrostyQueue {
    public static String channel = Config.channel;
    private ArrayList<GameProfile> profiles = new ArrayList<GameProfile>();
    private Map<String, String> backupPaths = new HashMap<String, String>();
    private String profileFile;
    private String targetDirFile;
    private String targetDir;
    private String os;
    private Boolean hasBackupDir;

    public SaveGuard() {

    }

    public SaveGuard(String profileFile, String targetDirFile) {
        this.profileFile = profileFile;
        this.targetDirFile = targetDirFile;
        this.os = System.getProperty("os.name");
    }
    
    public void loadProfiles(Boolean manual) throws FileNotFoundException {
        Scanner readPro = new Scanner(new File(profileFile));

        profiles.clear();
        while(readPro.hasNextLine()) {
            String[] tokens = readPro.nextLine().split("\\|");
            GameProfile profile = new GameProfile(tokens[0], tokens[1], tokens[2], tokens[3]);
            profiles.add(profile);
        }

        System.out.println("[SaveGuard] " + profiles.size() + " game profile(s) have been successfully loaded.");
        if(manual)
            sendMessage(Config.channel, "[SaveGuard] " + profiles.size() + " game profile(s) have been successfully loaded.");

        Scanner readTar = new Scanner(new File(targetDirFile));
        backupPaths.clear();
        while(readTar.hasNextLine()) {
            String cur[] = readTar.nextLine().split("\\|");
            backupPaths.put(cur[0].toLowerCase(), cur[1].toLowerCase());
        }

        if(backupPaths.containsKey(os.toLowerCase())) {
            this.targetDir = backupPaths.get(os.toLowerCase());
            System.out.println("[SaveGuard] The backup directory is set to " + targetDir);
            this.hasBackupDir = true;
        } else {
            System.out.println("[SaveGuard] Unable to find a specified backup directory for " + os + ". Please check the config file.");
            sendMessage(Config.channel, "[SaveGuard] Unable to find a specified backup directory for " + os + ". Please check the config file.");
            this.hasBackupDir = false;
        }
    }

    public GameProfile search(String profileName) {
        Boolean found = false;
        for(int i = 0; i < profiles.size(); i++) {
            GameProfile current = profiles.get(i);
            String currentProfileName = current.getProfile();
            if(currentProfileName.equalsIgnoreCase(profileName.toLowerCase())) {
                found = true;
                return current;
            }
        }

        if(!found) {
            sendMessage(Config.channel, "[SaveGuard] Unable to find profile [" + profileName + "].");
            System.out.println("[SaveGuard] Unable to find profile [" + profileName + "].");
        }
        
        return null;
    }

    public void backupDetectOS(GameProfile profile) throws IOException {
        if(!hasBackupDir) {
            System.out.println("[SaveGuard] Failed. No specified backup directory.");
        }

        if(os.toLowerCase().contains("windows") && profile != null) {
            String srcDir = profile.getWinPath();
            if(srcDir.isEmpty() || srcDir.equals("0")){
                System.out.println("[SaveGuard] Failed. Profile [" + profile.getDisplayName() + "] does not have a specified save path for Windows.");
                sendMessage(Config.channel, "[SaveGuard] Failed. Profile [" + profile.getDisplayName() + "] does not have a specified save path for Windows.");
            } else {
                backup(srcDir, profile.getProfile(), profile.getDisplayName());
            }

        } else if(os.contains("nux") || os.contains("nix") || os.contains("aix") && profile != null) {
            String srcDir = profile.getLinPath();
            if(srcDir.isEmpty() || srcDir.equals("0")) {
                System.out.println("[SaveGuard] Failed. Profile [" + profile.getDisplayName() + "] does not have a specified save path for Linux/Unix.");
                sendMessage(Config.channel, "[SaveGuard] Failed. Profile [" + profile.getDisplayName() + "] does not have a specified save path for Linux/Unix.");
            } else {
                backup(srcDir, profile.getProfile(), profile.getDisplayName());
            }
        } else if(profile != null) {
            System.out.println("[SaveGuard] Your current host OS is " + os + ". \"SaveGuard -g\" only supports Windows and Linux/Unix.");
            sendMessage(Config.channel, "[SaveGuard] Current host OS is " + os + ". \"SaveGuard -g\" only supports Windows and Linux/Unix.");
        }
    }

    public void backup(String srcDir, String profileName, String profileDisplayName) throws IOException {
        //LocalDate date = LocalDate.now();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String dateString = simpleDateFormat.format(new Date());
        String tarDirSuffix = File.separator + profileName + File.separator + dateString;

        File origin = new File(srcDir);
        File destination = new File(targetDir + tarDirSuffix);

        try {
            if(origin.exists() && origin.isDirectory())
                FileUtils.copyDirectory(origin, destination);
            else {
                System.out.println("[SaveGuard] [" + profileDisplayName + "] Failed. " + srcDir + " does not exist or is not a directory.");
            }
            
            File lastBackupDirDefFile = new File(targetDir + File.separator + profileName + File.separator + "lastBackupDir.dat");
            Writer recordBackupDir;

            if(lastBackupDirDefFile.exists()) {
                recordBackupDir = new FileWriter(lastBackupDirDefFile, false);    
                recordBackupDir.write(destination.getAbsolutePath());
            } else {
                recordBackupDir = new FileWriter(lastBackupDirDefFile);
                recordBackupDir.write(destination.getAbsolutePath());
            }

            System.out.println("[SaveGuard] [" + profileDisplayName + "] Save data has been successfully backed up to " + destination.getAbsolutePath() + ".");
            sendMessage(channel, "[SaveGuard] [" + profileDisplayName + "] Save data has been successfully backed up to " + destination.getAbsolutePath() + ".");
            recordBackupDir.close();

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(Config.channel, "[SaveGuard] Failed. " + e + " occurred while copying files because Thunderz is a dumbass.");
        }
    }

    public void simpleCopy(String original, String destination) {
        try {
            File srcDir = new File(original);
            File destDir = new File(destination);
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            System.out.println("[SaveGuard] IOException occurred while copying files.");
            e.printStackTrace();
        }
    }

    public void restoreLastBackup(String profileName) throws FileNotFoundException {
        GameProfile profile = search(profileName);
        Scanner readLastBackupDef = new Scanner(new File(profile.getWinPath() + File.separator + "lastBackupDir.dat"));
        String backupDir = readLastBackupDef.nextLine();

        try {
            FileUtils.copyDirectory(new File(backupDir), new File(profile.getPath()));
        } catch (IOException e) {
            sendMessage(Config.channel, "[SaveGuard] Failed. " + e + " occurred while restoring the last backup.");
            e.printStackTrace();
        }
    }

    public void runSaveGuard(String arg1, String arg2) throws IOException {
        switch(arg1) {
            case "-g": backupDetectOS(search(arg2));
            break;
            case "-r": restoreLastBackup(arg2);
            break;
            //case "-s": simpleCopy(arg2, arg3);
            //break;
            case "-l": loadProfiles(true);
            break;
        }
    }
}
