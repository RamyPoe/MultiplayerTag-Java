class QueueStatus {
  //Attributes
  
  int x; //X is set, Y will change for the animation
  float y;
  String text = "Searching for players"; //Base
  String displayText; //Will actually be shown
  
  float frame;
  int goalY = 25;
  
  
  //Contructor
  QueueStatus(int x) {
    this.x = x;
    y = -35; //temp, animation will be added to control this
  }

  //Methods
  void display() {
    if (y > -30) {
      displayText = text;
      for (int i = 0; i < (int) frame; i++) {
        displayText += ".";
      }
      
      fill(0);
      strokeWeight(3);
      line(x, y, x+60, y);
      line(x, y, x, y+30);
      
      fill(0);
      textSize(20);
      text(displayText, x+10, y+10);
      
      frame+=0.05;
      if (frame >= 4) {
        frame = 1;
      }
    }
  }
}
