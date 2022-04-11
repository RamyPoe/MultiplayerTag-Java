class PlayerPreview {
  //Attributes
  boolean showing; //Enabled or disabled
  
  PImage skin;
  int id; //Id of skin so we know what we're drawing
  
  PVector leftEye;
  PVector rightEye;
  
  int x, y, w, h;
  
  //Constructor
  PlayerPreview() {
    x = 920;
    y = 380;
    w = 300;
    h = 300;
    
    leftEye = new PVector(x+w/3, y+20);
    rightEye = new PVector(x+w*2/3, y+20);
  }
  
  //Methods
  void display() {
    if (skin == null) {return;}
    
    imageMode(CORNER);
    image(skin, x, y, w, h);
    
    //EYEBALLS-------------------------------------------------------------
    
    PVector mouseVector = new PVector(mouseX, mouseY);
    
    PVector leftPupil = leftEye.copy().add(mouseVector.copy().sub(leftEye).setMag(10));
    PVector rightPupil = rightEye.copy().add(mouseVector.copy().sub(rightEye).setMag(10));
    
    ellipseMode(CENTER);
    fill(255);
    stroke(50);
    strokeWeight(5);
    ellipse(leftEye.x, leftEye.y, 80, 80);
    ellipse(rightEye.x, rightEye.y, 80, 80);
  
    fill(0);
    noStroke();
    ellipse(leftPupil.x, leftPupil.y, 30, 30);
    ellipse(rightPupil.x, rightPupil.y, 30, 30);
    
  }
  
  void hide() {
    showing = false;
  }
  
  void show() {
    showing = true;
  }
  
  void setSkin(PImage skin, int id) {
    this.skin = skin;
    this.id = id;
  }
  
  void reset() {
    skin = null;
  }
  
  void init() {
    //Get the default skin
    skin = loadImage("assets/pskins/1r.png");
  }
  
}
