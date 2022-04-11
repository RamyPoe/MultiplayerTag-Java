package dependencies;

public class Room {
    //-1 represents a vacant spot (displayed by "--")
    public int accountNum1 = -1;
    public int accountNum2 = -1;


    //Methods
    public boolean isFull() {
        return accountNum1 != -1 && accountNum2 != -1;
    }

    public boolean isEmpty() {
        return accountNum1 == -1 && accountNum2 == -1;
    }

    public boolean hasOnePlayer() {
        return (accountNum1 == -1 && accountNum2 != -1) || (accountNum1 != -1 && accountNum2 == -1);
    }
    
    public void addPlayer(int accountNum) {
        if (accountNum1 == -1) {
            accountNum1 = accountNum;
        } else if (accountNum2 == -1) {
            accountNum2 = accountNum;
        }
    }

    public void removePlayer(int accountNum) {
        if (accountNum1 == accountNum) {
            accountNum1 = -1;
        } else if (accountNum2 == accountNum) {
            accountNum2 = -1;
        }
    }

    public boolean hasPlayer(int accountNum) {
        return accountNum1 == accountNum || accountNum2 == accountNum;       
    }

    public int getOnePlayer() {
        if (hasOnePlayer()) {
            return accountNum1 == -1 ? accountNum2 : accountNum1;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "[" + (accountNum1 == -1 ? "--" : accountNum1) + ", " + (accountNum2 == -1 ? "--" : accountNum2) + "]";
    }

}
