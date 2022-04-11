package dependencies;

//Struct
public class Entry {
    public String ip;
    public String username;
    public String password;

    public String coins;
    public String skins;
    
    public boolean loggedIn = false;

    Entry (String ip, String username, String password, String coins, String skins) {
        this.ip = ip;
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.skins = skins;
    }
}