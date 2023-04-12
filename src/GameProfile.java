public class GameProfile {
    private String profile;
    private String displayName;
    private String winPath;
    private String linPath;

    /**
     * Sets all parameters for a game profile.
     * @param profile Profile name.
     * @param displayName A short name to display in the chat.
     * @param winPath Original save data location for Windows.
     * @param linPath Original save data location for Linux.
     */
    public GameProfile(String profile, String displayName, String winPath, String linPath)
    {
        this.profile = profile;
        this.displayName = displayName;
        this.winPath = winPath;
        this.linPath = linPath;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setWinPath(String winPath) {
        this.winPath = winPath;
    }

    public void setLinPath(String linPath) {
        this.linPath = linPath;
    }

    /**
     * @return String return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @return String return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return String return the winPath
     */
    public String getWinPath() {
        return winPath;
    }

    /**
     * @return String return the linPath
     */
    public String getLinPath() {
        return linPath;
    }

    public String getPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("windows")) {
            return winPath;
        } else
            return linPath;
    }

    public String toString() {
        return profile + ", " + displayName + ", " + winPath + ", " + linPath;
    }

}
