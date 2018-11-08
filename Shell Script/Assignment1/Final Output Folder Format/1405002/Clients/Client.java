package Clients;

import Utilities.ConnectionUtility;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        ConnectionUtility utility1 = new ConnectionUtility("localhost", 5000);
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter your student Id:");
        String sId = sc.next();     //getting the student id
        new Thread(new ClientFileSender(sId, utility1)).start();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
