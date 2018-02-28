package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 *
 */
public class JDBC_students {

   
    private String dbUrl = "jdbc:mysql://localhost:3306/university";
    private String user = "root";
    private String password = "2591gHsS";
    private static Connection conn1;
    private ResultSet rs = null;
	Statement stmnt;


    /**
     * Open database
     */
    private void openDB() {
        try {
            // Load the driver (registers itself)
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception E) {
            System.err.println("Unable to load driver.");
            E.printStackTrace();
        }

        try {
            // Connect to the database
            conn1 = DriverManager.getConnection(dbUrl, user, password);
            System.out.println("*** Connected to the database ***");

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }


    /**
     * Part A. 
     */
    private void getStudentInfo(){

        try{
            //intialize variables variables
            int curStudentID = 0;
            int nextStudentID = 0;
            int creditHours = 0;
            double gpa = 0;
            double nGPA = 0;
            double courseGrade = 0.0;
            String classification = "";
            String grade = "";
            
        	//A ResultSet object maintains a cursor pointing to its current row of data. 
        	//Initially the cursor is positioned before the first row. The next method
        	//moves the cursor to the next row, and because it returns false when there 
        	//are no more rows in the ResultSet object, it can be used in a while loop to 
        	//iterate through the result set.
            stmnt = conn1.createStatement();


            //THE SQL statement
            rs = stmnt.executeQuery(
            		"SELECT t1.StudentID, t2.Classification, t2.CreditHours, t2.GPA,t1.Grade FROM" 
                    		+ " " +
                            "(SELECT  *" + " " +
                            "FROM Enrollment e) AS t1" + " " +
                            "\n" +
                            "LEFT JOIN" + " " +
                            "\n" +
                            "  (SELECT *" + " " +
                            "  FROM Student s) AS t2" + " " +
                            "  ON t1.StudentID = t2.StudentID" + " " +
                            "ORDER BY t1.StudentID;");
            
            //rs.next() will process each line
            while(rs.next()){
            	
                
                curStudentID = rs.getInt(1);

                //enters loop if encounters next student in the list next student is a new student
                if(curStudentID != nextStudentID && nextStudentID != 0){
                	
                    //can update creditHours based on classification now
                    if(creditHours > 89){
                    	classification = "Senior";
                    }else if(creditHours > 59 && creditHours < 90){
                    	classification = "Junior";
                    }else if(creditHours > 29 && creditHours < 60){
                    	classification = "Sophomore";
                    }else{
                    	classification = "Freshman";
                    }
                    //update
                    PreparedStatement ps = conn1.prepareStatement("UPDATE Student SET Classification = ?, GPA = ?, CreditHours = ? WHERE StudentID = ?");
                    ps.setString(1,classification);
                    ps.setDouble(2, nGPA);
                    ps.setInt(3,creditHours);
                    ps.setInt(4,nextStudentID);
                    ps.executeUpdate();
                    ps.close();
                    
                    System.out.println("New GPA: " + nGPA);
                    System.out.println("\n\nNew Student");
                    creditHours = 0;
                }

                //If one the same student still, get the data
                creditHours = rs.getInt(3);
                gpa = rs.getFloat(4);
                grade = rs.getString(5);
                classification = rs.getString(2);

                
        		if(grade == "A"){
        			courseGrade = 4.00;
        		}	
        		else if(grade == "A-"){
        			courseGrade = 3.67;
        		}
        		else if(grade == "B+"){
        			courseGrade = 3.33;
        		}
        		else if(grade == "B"){
        			courseGrade = 3.00;
        		}	
        		else if(grade == "B-"){
        			courseGrade = 2.67;
        		}
        		else if(grade == "C+"){
        			courseGrade = 2.33;
        		}
        		else if(grade == "C"){
        			courseGrade = 2.00;
        		}
        		else if(grade == "C-"){
        			courseGrade = 1.67;
        		}
        		else if(grade == "D+"){
        			courseGrade = 1.33;
        		}
        		else if(grade == "D"){
        			courseGrade = 1.00;
        		}
        		else if(grade == "F"){
        			courseGrade = 0;
        		}
        		System.out.println(rs.getInt(1));
                //calculate new GPA
                nGPA = (((gpa * creditHours - 3) + (3 * courseGrade)) / (creditHours));
                System.out.println("GPA " + gpa);
                System.out.println("Grade: " + grade);

                //peek next student
                if(rs.next()) {
                nextStudentID = rs.getInt(1);
                }

                //If the same one, we only need gpa from next class.
                if(nextStudentID == curStudentID){

                    nGPA = (((gpa * creditHours) + (3 * courseGrade)) / (creditHours + 3));
                    creditHours += 3;
                }
            }

            //stmnt.close();
            rs.close();

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }


    /**
     * Part B.
     */
    private void topFiveStudents(){
        try{

            //SQL
            rs = stmnt.executeQuery(
                    "SELECT t1.Name, t1.GPA, t2.Name FROM\n" +
                    "(SELECT p.Name, s.GPA, s.MentorID\n" +
                    "FROM Student s, Person p\n" +
                    "WHERE s.StudentID = p.ID\n" +
                    "      AND s.Classification = 'Senior'\n" +
                    "ORDER BY s.GPA DESC\n" +
                    "LIMIT 5) AS t1\n" +
                    "\n" +
                    "INNER JOIN\n" +
                    "(SELECT p.Name, i.InstructorID\n" +
                    " FROM Person p, Instructor i\n" +
                    " WHERE p.ID = i.InstructorID) AS t2\n" +
                    "ON t1.MentorID = t2.InstructorID;");

            PrintWriter writer = null;

            //print out
            try{
                writer = new PrintWriter("JDBC_StudentsOutput.txt","UTF-8");
                while(rs.next()){
                    writer.println("Student Name: " + rs.getString(1) + ", GPA: "
                            + rs.getDouble(2) + ". Mentor: " + rs.getString(3));

                }

            }catch (IOException e){

            }

            writer.close();
            rs.close();
       

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
    
    /**
     * Main.
     */
    public static void main(String[] args) throws Exception {
    	JDBC_students project3 = new JDBC_students();

        project3.openDB();
        //Part A
        project3.getStudentInfo();
        //Part B
        project3.topFiveStudents();
    }

}