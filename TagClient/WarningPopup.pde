class WarningPopup {
  //Attributes
  boolean isActive;
  int w, h; //x and y not needed as it will be centered
  private int fade = 0; //For animation
  String text;
  
  //Constructor
  WarningPopup(int w, int h) {
    this.w = w;
    this.h = h;
  }
  
  //Methods
  void display() {
    if (isActive) {
      if (fade < 255) {
        fade += 17;
      }
    } else {
      if (fade > 0) {
        fade -= 17;
      }
    }
    
    //No point in drawing anything if everything is transparent
    if (fade == 0) {
      return;
    }
    //Make background darker/inaccessible
    noStroke();
    rectMode(CORNER); //Default
    fill(130, fade/2);
    rect(0, 0, width, height);
    
    //Draw the warning window
    strokeWeight(3);
    stroke(0); //Black
    rectMode(CENTER);
    fill(150, fade); //Gray
    rect(width/2, height/2, w, h);
    
    //Text for warning window
    textSize(20);
    fill(0, fade);
    textAlign(CENTER, CENTER);
    text(text, width/2, height/2);
    
    //Draw the close button
    textSize(30);
    textAlign(RIGHT, TOP);
    text("x", width/2+w/2-7, height/2-h/2+7);
    
    if (closePressed()) {
      isActive = false;
    }
  }
  
  void enable(String text) {
    this.text = text;
    isActive = true;
  }
  
  boolean closePressed() {    
    if (mousePressed == true) {
      //println(mouseX + "   |   " + mouseY);
      
      if (mouseX > width/2+w/2-27 && mouseX < width/2+w/2-7 && mouseY > height/2-h/2+7 && mouseY < height/2-h/2+32) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
}
