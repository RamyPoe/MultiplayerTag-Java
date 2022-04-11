import java.util.Arrays;

class Profile {
  int x, y;
  int w, h;
  
  boolean loggedIn = false;
  private PFont font;
  
  //Currently equipped items
  int[] equipped = {1, 0, 100, 101, 102}; // [0] = skin, [1] = title, [2][3][4] = chat items
  
  //For animation
  int goalCoins; 
  int increment = 0;
  
  int coins;
  String username;
  String title;
  private String inventory; //Hold all information to be parsed
  private int[] allInv = new int[150]; //Unorganized list of all cosmetics, will be parsed later
  
  PImage coinsImg;
  
  final int hookLength = 40; //For drawing
  final color titleColor = color(52);

  
  Profile () {
    coins = 0;
    title = "";
    username = "";

    w = 500;
    h = 110;
    x = 1280-w-15;
    y = 15;
  }
  
  void updateTitle(String title) {
    this.title = title;
  }
  
  void updateCoins(int goal) {
    goalCoins = goal;
    increment = (goalCoins - coins)/240;
    
    //To guarantee at least some movement
    if (goal > coins) {
      increment++;
    } else {
      increment--;
    }
    
    println("INCREMENT: " + increment);
  }
  
  void updateUsername(String username) {
    this.username = username;
  }
  
  void loadCoinsImage() {
    coinsImg = loadImage("assets/coins.png");
  }
  
  void display() {
    if (!loggedIn) {
      allInv = new int[allInv.length];
      username = "---";
      title = "";
    }
    
  //Outline------------------------------------------
    strokeWeight(4);
    stroke(0);
    //Left half
    line(x, y, (x+hookLength), y);
    line(x, y, x, (y+h));
    line(x, (y+h), (x+hookLength), (y+h));
    //Right half
    line((x+w-hookLength), y, (x+w), y);
    line((x+w), y, (x+w), (y+h));
    line((x+w), (y+h), (x+w-hookLength), (y+h));
  //-------------------------------------------------
    
    //println(x + "  " + y + "  " + w + "  " + h);
    
    //Will be changed back in main loop
    textFont(font);
    textAlign(LEFT, TOP);
    
    //Coin increase/decrease animation
    if (goalCoins == coins) {
      increment = 0;
    } else {
      if ((increment > 0 && coins+increment > goalCoins) || (increment < 0 && coins-increment < goalCoins)) {
        increment = 0;
        coins = goalCoins;
      } else {
        coins += increment;
      }
    }
    
    //Coins
    imageMode(CORNER);
    image(coinsImg, x+(w*2/3)+5, y+h/2-25, 47, 50);
    textSize(30);
    fill(230, 182, 50); //Gold color
    if (loggedIn) {
      text(String.valueOf(coins), x+(w*2/3)+60, y+h/2-textAscent()+10);
    } else {
      text("---", x+(w*2/3)+60, y+h/2-textAscent()+10);
      coins = 0;
    }
    
    if (equipped[1] == 0) {
      updateTitle("");
    } else {
      updateTitle(titleDict[equipped[1]-50]);
    }

    //Title and username
    if (title != "") { //Has a title equipped
      textSize(20);
      fill(titleColor);
      text(title, x+(w*2/3/2-textWidth(title)/2), y+10);
      
      textSize(45);
      fill(0);
      text(username, x+(w*2/3/2-textWidth(username)/2), y+35);
    } else {
      textSize(45);
      fill(0);
      text(username, x+(w*2/3/2-textWidth(username)/2), y+25);
    }      
  
  }
  
  void fetchInventory(Client myClient) {
    //Pull request for server to send the users inventory
    myClient.write("inventory");
    
    inventory = clientRecv(myClient); //coins:skins
    String data[] = inventory.split(":");
    updateCoins(Integer.parseInt(data[0]));
    String temp = data[1];
    

    temp = temp.replace("s", ""); //character "s" to make it a string, not needed no more
    for (int i = 0; i < temp.length()/3; i++) { //Each skin id is 3 characters
       allInv[i] = Integer.parseInt(temp.substring(3*i, 3*i+3));
    }
    
    println("[COSMETICS] " + Arrays.toString(allInv));
  }
  
  private String clientRecv(Client myClient) {
    String dataIn;
    while (myClient.available() == 0) {}
  
    dataIn = myClient.readStringUntil(10); //Newline terminating character (10)
    dataIn = dataIn.replace("\n", ""); //Remove trailing character
    println("[SERVER] " + dataIn);
    
    return dataIn;
  }
  
  int[][] parsedCosmetics() {
    int[][] cosmetics = new int[3][];
    
    //Skins 0-49 (inclusive)
    int[] skins; int count = 0;
    for (int i = 0; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 49) {
        break;
      }
      count++;
    }
    skins = new int[count];
    for (int i = 0; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 49) {
        break;
      }
      skins[i] = allInv[i];
    }
    
    //Titles 50-99 (inclusive)
    int[] titles; count = 0;
    for (int i = skins.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 99) {
        break;
      }
      count++;
    }
    titles = new int[count];
    for (int i = skins.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 99) {
        break;
      }
      titles[i-skins.length] = allInv[i];
    }
    
    //Chats 100-149 (inclusive)
    int[] chats; count = 0;
    for (int i = skins.length+titles.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 149) {
        break;
      }
      count++;
    }
    chats = new int[count];
    for (int i = skins.length+titles.length; i < allInv.length; i++) {
      if (allInv[i] == 0) {
        break;
      }
      if (allInv[i] > 149) {
        break;
      }
      chats[i-(skins.length+titles.length)] = allInv[i];
    }
    
    //Culminate them into var to return
    cosmetics[0] = skins;
    cosmetics[1] = titles;
    cosmetics[2] = chats;
    println("[COSMETICS] [" + Arrays.toString(cosmetics[0]) + ", " + Arrays.toString(cosmetics[1]) + ", " + Arrays.toString(cosmetics[2]) + "]");
    return cosmetics;
  }
  
  void init() {
    //We just need to setup the font
    font = createFont("Calibri", 45, true);
    equipped[0] = 1; //Default skin
    //equipped[1] = 
  }

}
