class Player { 
  //Vars
  int velocity = 10;
  int size = 100;
  
  boolean it = false;
  int px, py;
  
  PImage skinR;
  PImage skinB;
  
  //Constructor
  Player (int x, int y, int skinId, boolean it) {  
    px = x; 
    py = y;
    this.it = it;
    
    //Load player skins
    skinR = loadImage("assets/pskins/"+skinId+"r.png");
    skinB = loadImage("assets/pskins/"+skinId+"b.png");
    
    if (skinR == null) {
      skinR = loadImage("assets/pskins/uk.png");
    }
    if (skinB == null) {
      skinB = loadImage("assets/pskins/uk.png");
    }
  } 
  
  //Methods
  void display() {
    //Player outline
    if (settings.playerOutline) {
      rectMode(CORNER);
      fill(100);
      noStroke();
      rect(px-1, py-1, size+2, size+2);
    }
    
    
    imageMode(CORNER);
    if (it) {
      image(skinR, px, py);
    } else {
      image(skinB, px, py);
    }
  }
  
  
}
