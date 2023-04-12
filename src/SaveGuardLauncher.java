import java.io.FileNotFoundException;
import java.io.IOException;

public class SaveGuardLauncher {
    public static void main(String[] args) {
        SaveGuard saveGuard = new SaveGuard("TextFiles/saveProfiles.txt", "TextFiles/targetPaths.txt");
        try {
            saveGuard.loadProfiles(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            saveGuard.backupDetectOS(saveGuard.search("ryza1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
