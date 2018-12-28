import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * @author Tishon S. Gumbs
 */

/**
 * Notable Limitations of Project that may throw errors
 *
 * 1) All entries must be entered in the format [Question  Answer], for example "Q1 A" exception handling has not been implemented for wrong input
 *
 * 2) If user mistypes connection address and/or port number client will fail to connect
 *
 * 3) System automatically terminates if "JOIN" ignoring case is not entered
 */


public class Client {

    // Variables for client-server connection
    public static String ipaddr;
    public static int svr_port;
    public static String command;


    public static void main(String args[]) throws Exception {


        // Read user input for server connection
        Socket sk = null;
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter server ip address");
        ipaddr = sc.next();

        System.out.println("Enter server port");
        svr_port = sc.nextInt();

        System.out.println("Enter the command 'JOIN' to initiate connection");
        command = sc.next();

        // Wait for user to issue to issue join command
        if (command.equalsIgnoreCase("JOIN")) {

            sk = new Socket(ipaddr, svr_port);
        }
        else{
            System.exit(0);
        }


        //Establish basic communication
        BufferedReader sin = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        PrintStream sout = new PrintStream(sk.getOutputStream());
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));


        String s;

        //Prompt for student output
        System.out.print("Enter your answer \n");

        //Continually read input until client enters submit
        while (true) {
                s = stdin.readLine();
                sout.println(s);
                if (s.equalsIgnoreCase("submit")) {
                    System.out.println("Connection ended by client");
                    break;
                }

           // s = sin.readLine();

        }

        //Close all connections(readers and connection)
        sk.close();
        sin.close();
        sout.close();
        stdin.close();
    }

}
