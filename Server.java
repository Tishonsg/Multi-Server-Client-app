import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tishon S. Gumbs
 */

/**
 * Notable Limitations of Project that may throw errors
 * <p>
 * 1) If server user enters "close" termination will only be triggered after the student attempts to enter an additional response after close has been issued
 * Otherwise a timer is implemented in the background for to close after a specified duration
 * <p>
 * 2) Entering invalid or unavailable port numbers will throw and error
 * <p>
 * 3) Please note that exception handling has not been implemented for any wrong user input
 */

public class Server {
    // Initialize variables

    int port;
    ServerSocket server = null;
    Socket client = null;
    ExecutorService pool = null;
    int clientcount = 0;

    //Default port
    public static int default_port;


    public static void main(String[] args) throws IOException {


        // Read input from command line
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter port number to enter server mode ");
        default_port = sc.nextInt();


        // Create Server object with user specified port
        Server serverobj = new Server(default_port);
        serverobj.startServer();

    }

    // Create instance of Server
    Server(int port) {
        this.port = port;
        pool = Executors.newFixedThreadPool(3);
    }

    public void startServer() throws IOException {

        server = new ServerSocket(default_port);
        System.out.println("Server Setup Successful\n");
        System.out.println("Any student can terminate their connection to the server by entering the text 'submit' \n");
        System.out.println("The server can be torn down by entering the text 'close' on the server side, but tear down will only occur after last student input");

        //Redirect output to log file
        try {
            PrintStream log = new PrintStream(new File("Server.log"));
            System.setOut(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Creating a new thread for each client that attempts to connect to the server
        while (true) {
            client = server.accept();
            clientcount++;
            ServerThread runnable = new ServerThread(client, clientcount, this);
            pool.execute(runnable);
        }

    }

    private static class ServerThread implements Runnable {

        Server server = null;
        Socket client = null;


        BufferedReader cin;
        PrintStream cout;

        // Read in input from
        // Scanner sc = new Scanner(System.in);
        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        int id;

        //Timer to read input from server
        int timeSpan = 2;
        //Timer to specify test duration
        int quiz = 300;

        // Designated string to store all communication between server and clieny
        String response;
        String serverInput;

        ServerThread(Socket client, int count, Server server) throws IOException {

            this.client = client;
            this.server = server;
            this.id = count;

            System.out.println("Student " + id + " established with client at " + System.currentTimeMillis());

            // Input from client
            cin = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // Output to client (optional)
            cout = new PrintStream(client.getOutputStream());

        }

        @Override
        public void run() {

            // Termination check
            boolean terminate = false;
            serverInput = "open";
            long startTime;
            long duration;
            duration = System.currentTimeMillis();

            //Create Dictionary to store student reponses

            HashMap<String, String> student_response = new HashMap<String, String>();
            String[] student_entry;

            try {
                while (true) {

                    startTime = System.currentTimeMillis();

                    //Attempt to read from input in specific time intervals

                    while ((System.currentTimeMillis() - startTime) < (timeSpan * 1000) && !sin.ready()) {

                    }
                    if (sin.ready()) {
                        serverInput = sin.readLine();
                    }


                    //Check for termination condition from server user
                    if (serverInput.equalsIgnoreCase("close")) {
                        // cout.println("You have successfully submitted all responses");
                        terminate = true;
                        //Print out student responses
                        System.out.println("Final Submission Student(" + id + ") :" + Arrays.asList(student_response));
                        //Redirect output and close connection
                        System.setOut(System.out);
                        System.out.println("Connection terminated by server");
                        break;
                    }

                    //Quiz timer to forcefully close connection
                    if ((System.currentTimeMillis() - duration) > (quiz * 1000)) {
                        terminate = true;
                        System.out.println("Final Submission Student (" + id + ") :" + Arrays.asList(student_response));
                        System.setOut(System.out);
                        System.out.println("Quiz time has expired");
                        break;


                    }

                    response = cin.readLine();

                    // Stop logging
                    if (response != null) {
                        if (response.equalsIgnoreCase("submit")) {
                            //Print student final responses
                            System.out.println("Final Submission Student (" + id + ") :" + Arrays.asList(student_response));
                            System.setOut(System.out);
                        } else {
                            //Creating entry for hashmap of Q and A
                            student_entry = response.split(" ");
                            String question = student_entry[0];
                            String answer = student_entry[1];
                            //Determine if student is changing response to a previous question or answering a new question
                            if (student_response.containsKey(question)) {
                                student_response.replace(question, answer);
                            } else {
                                student_response.put(question, answer);
                            }
                        }

                        //Print output to (log)
                        System.out.print("Student(" + id + ") :" + response + "\n");

                        //Print response from each respective student
                        if (!(response.equalsIgnoreCase("submit"))) {
                            cout.println(response);
                        }
                    }
                }

                /* Algorithm for safely tearing down server code */
                    cin.close();
                    client.close();
                    cout.close();

                    if (terminate == true) {
                        System.out.println("Server successfully torn down at " + System.currentTimeMillis());
                        System.exit(0);
                    }

                } catch(IOException e){
                    System.out.println("Error : " + e);
                }

            }

        }
    }
