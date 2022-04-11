package dependencies;

import java.io.File;  
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LogTable {
    //Hold user account details (user, pass, coins, etc.)
    public Entry[] authTable;
    
    //Excel sheet (log.xlsx)
    private static XSSFWorkbook wb;
    private static XSSFSheet sheet;

    //Constructor
    public LogTable(int numUsersAllocate) {
        //Allocate space for that many accounts
        authTable = new Entry[numUsersAllocate];
        setupLog();
        retrieveEntries();
    }

    //Methods
    private void setupLog() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        System.out.println("[LOG] Loading Excel sheet");
        try {
            File file = new File("log.xlsx");
            FileInputStream fis = new FileInputStream(file);

            //Creating Wokbook Instance from file data
            wb = new XSSFWorkbook(fis);   
            sheet = wb.getSheetAt(0);
            //Close fileStream
            fis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retrieveEntries() {
        String[] data = new String[5]; //num of attributes in Entry()

        //Read contents of file into local authTable which was previouslt allocated
        try {
            int max = numberEntries();
            for (int i = 0; i < max; i++) {
                //Fill array with account data
                for (int j = 0; j < 5; j++) {
                    data[j] = ReadCellData(i, j);
                }
                authTable[i] = new Entry(data[0], data[1], data[2], data[3], data[4]);
                System.out.println("[RETRIEVE] Added entry: " + data[0]+" "+ data[1]+" "+ data[2]+" "+ data[3]+" "+ data[4]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getUserIndex(String username) {
        for (int i = 0; i < authTable.length; i++) {
            if (authTable[i] != null) {
                if (authTable[i].username.equals(username)) {
                    return i;
                }
            } else {
                //Since the rest will also be null
                break;
            }
        }
        //Username wasn't found, return "error code"
        return -1;
    }

    public String fetchInventory(int clientIndex) {
        return String.valueOf(authTable[clientIndex].coins) + ":" + authTable[clientIndex].skins;
    }

    public void logOut(int clientIndex) {
        System.out.println("[LOGGING OUT] " + clientIndex);
        authTable[clientIndex].loggedIn = false;
    }

    public String authenticateLogin(String username, String password, String ip) {
        //To be safe
        username = username.trim();
        password = password.trim();

        //If the username is in the database
        for (int i = 0; i < authTable.length; i++) {
            if (authTable[i] != null) {
                if (authTable[i].username.equals(username)) {
                    if (authTable[i].password.equals(password)) {
                        if (authTable[i].loggedIn == false) {
                            //Set user as logged in
                            authTable[i].loggedIn = true;
                            return "Successfully Logged in!";
                        } else {
                            return "User is already logged in";
                        }
                    } else {
                        return "Username or password is incorrect";
                    }
                }
            } else {
                //Since the following acounts will also be null
                break;
            }
        }
        return "Username or password is incorrect";
    }

    public String authenticateSignup(String username, String password, String ip) {
        for (int i = 0; i < authTable.length; i++) {
            if (authTable[i] != null) {
                if (authTable[i].username.equals(username)) {
                    return "Account already exists, please login";
                }
            } else {
                //Since the following indicies will also be null
                break;
            }
        }

        //Check if computer is eligible to make account since the username doesn't exist
        for (int i = 0; i < authTable.length; i++) {
            if (authTable[i] != null) {
                if (authTable[i].ip.equals(ip)) {
                    return "Not eligible to make an account, contact an admin...";
                }
            } else {
                //Since the following acounts will also be null
                break;
            }
        }

        //Create account
        Row row = sheet.createRow(numberEntries());
        Cell cell = row.createCell(0); cell.setCellValue(ip);
        cell = row.createCell(1); cell.setCellValue(username);
        cell = row.createCell(2); cell.setCellValue(password);
        cell = row.createCell(3); cell.setCellValue("80"); //Start with no coins
        cell = row.createCell(4); cell.setCellValue("s001100101102"); //Start with default items

        addEntry(new Entry(ip, username, password, "80", "s001100101102")); //Add to local table

        try {
            FileOutputStream fout = new FileOutputStream("log.xlsx", false);
            wb.write(fout);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        return "Successfully created new Account! Please login...";
    }

    private void addEntry(Entry entry) {
        //Adds an entry to the local table in the next available slot, entries cannot be deleted
        try {
            for (int i = 0; i < authTable.length; i++) {
                if (authTable[i] == null) {
                    authTable[i] = entry;
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private int numberEntries() {
        //Checks the number of entries in the log file
        int count = 0;
        while (true) {
            // System.out.println("[DEBUG] looped!");
            try {
                ReadCellData(count, 0); //Means that it exists if it doesn't raise exception
                count++;
            }
            catch (Exception e) {
                System.out.println("[DEBUG] There are " + count + " accounts in the log");
                return count;
            }
        }
    }

    private String ReadCellData(int vRow, int vColumn) {   
        Row row=sheet.getRow(vRow); //returns the logical row  
        Cell cell=row.getCell(vColumn); //getting the cell representing the given column
        String val = "";
        if (String.valueOf(cell.getCellTypeEnum()).equals("STRING")) {
            val = cell.getStringCellValue();
        } else if (String.valueOf(cell.getCellTypeEnum()).equals("NUMERIC")) {
            val = String.valueOf(cell.getNumericCellValue()).split("\\.")[0]; //Remove decimal
        } else {
            System.out.println("[READ CELL] ERROR...  Unhandled cell type!");
        }
        return val;
    }

    public void WriteCellData(String data, int vRow, int vColumn) {
        Row row=sheet.getRow(vRow); //returns the logical row  
        Cell cell=row.getCell(vColumn); //getting the cell representing the given column 
        cell.setCellValue(data);

        //Write new info to the file
        try {
            FileOutputStream fout = new FileOutputStream("log.xlsx", false);
            wb.write(fout);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        //Debug print
        System.out.println("[FILE] Writing: " + data + " to: " + vRow + ", " + vColumn);
    }
}