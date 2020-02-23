package com.company;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Program {

    SqlConsole sqlConsole = new SqlConsole();
    SearchingRoom searchingRoom = new SearchingRoom(sqlConsole.getConn());
    Registration registration = new Registration(sqlConsole.getConn());
    CancellingRescheduling cancellingRescheduling = new CancellingRescheduling(sqlConsole.getConn());

    private Connection conn = null;
    private PreparedStatement statement;
    private ResultSet resultSet;


    public void start(){
        boolean on = true;
        while (on) {
            adminMenu();
            Scanner scanner = new Scanner(System.in);
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":                 //1 : Registering customer
                    String registeredSocialnr = registration.registerCustomer();
                    resultSet = registration.searchCustomerBySocialSecnr(registeredSocialnr);
                    registration.customerPrintResult(resultSet);
                    break;

                case "2":                 //2 : Searching & Booking room
                    String purpose = startProgram();
                    ArrayList answers = searchingRoom.questionsForSearchRoom(purpose);
                    ArrayList result = searchingRoom.selectForRooms(answers);
                    if (result==null){
                        break;
                    }
                    else{
                        int booking_id = searchingRoom.bookRoom(result);
                        resultSet = searchingRoom.searchByBookid(booking_id);
                    }

                    if(resultSet==null){
                        break;
                    }
                    else{
                        ArrayList forPrice = searchingRoom.bookingResult(resultSet);
                        searchingRoom.bookingTotalPrice(forPrice);
                        System.out.println("Thank you!");
                        System.out.println("Have a great trip!");
                    }
                    break;

                case "3":                       //3 : Cancellation of book
                    Scanner scan = new Scanner(System.in);
                    System.out.println("Input book-id that you want to cancel");
                    System.out.println("== Important == Please input correct book-id");
                    try {
                        int cancelBook = Integer.parseInt(scan.nextLine());                           //Input book_id number
                        resultSet = searchingRoom.searchByBookid(cancelBook);                         //SQL select... return the booking
                        ArrayList cancel = searchingRoom.bookingResult(resultSet);
                        if (cancel != null) {
                            System.out.println("");
                            System.out.println("Is this booking that you want to cancel? y/n");
                            String book = scan.nextLine();
                            if (book.equals("y")) {
                                cancellingRescheduling.deleteBook(cancelBook);
                                System.out.println("");
                                System.out.println("Your booking has been successfully cancelled");
                                System.out.println("");
                                resultSet = cancellingRescheduling.selectBookings();
                                System.out.println("========== Bookings table ==========");
                                System.out.println("");
                                searchingRoom.bookingResult(resultSet);
                            } else {
                                System.out.println("See you!");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Please input correct information");
                        e.printStackTrace();
                    }

                    break;

                case "4":                               //4 : Rescheduling book
                    Scanner reschedule = new Scanner(System.in);
                    System.out.println("Here we can change only your booking dates.");
                    System.out.println("If you wish other changing, please cancel your booking first,");
                    System.out.println("then, rebook your hotel.");
                    System.out.println("Do you want to reschedule your book? y/n");
                    String res = reschedule.nextLine();
                    if (res.equals("y")) {
                        System.out.println("Input book-id");
                        try {
                            int changeBook = Integer.parseInt(reschedule.nextLine());
                            resultSet = searchingRoom.searchByBookid(changeBook);
                            ArrayList book_info = searchingRoom.bookingResult(resultSet);
                            searchingRoom.bookingTotalPrice(book_info);
                            System.out.println("");
                            System.out.println("Is this booking that you want to reschedule? y/n");
                            String change = reschedule.nextLine();
                            if (change.equals("y")) {
                                System.out.println("When is your new check-in date? Input ex; 2020-01-30");
                                String newCheckIn = reschedule.next();
                                LocalDate newCheckInDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(newCheckIn));
                                LocalDate startOfSeason = LocalDate.of(2020, 6, 1);
                                if (newCheckInDate.isBefore(startOfSeason)) {
                                    System.out.println("Please call us later!");
                                } else {
                                    System.out.println("When is your check-out date? Input ex; 2020-01-30");
                                    String newCheckOut = reschedule.next();
                                    LocalDate newCheckOutDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(newCheckOut));
                                    LocalDate endOfSeason = LocalDate.of(2020, 7, 30);
                                    if (newCheckOutDate.isAfter(endOfSeason)) {
                                        System.out.println("Sorry! See you next year!");
                                    } else {
                                        cancellingRescheduling.rescheduleCheck(book_info, newCheckIn, newCheckOut);
                                        System.out.println("Is this ok? y/n");
                                        Scanner resche = new Scanner(System.in);
                                        String lastCheck = resche.nextLine();
                                        if (lastCheck.equals("y")) {
                                            cancellingRescheduling.reschedule(changeBook, newCheckIn, newCheckOut);
                                            resultSet = searchingRoom.searchByBookid(changeBook);
                                            ArrayList rebook = searchingRoom.bookingResult(resultSet);
                                            searchingRoom.bookingTotalPrice(rebook);
                                            System.out.println("Thank you!");
                                            System.out.println("Have a great trip!");
                                            break;
                                        } else if (lastCheck.equals("n")) {
                                            System.out.println("Call us again if any!");
                                            break;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                System.out.println("See you!");
                            }
                        } catch (Exception e) {
                            System.out.println("Input correct information");
                        }
                    } else {
                        System.out.println("See you!");
                    } break;



                case "9":                            //9: Quit
                    on = false;
                    System.exit(0);
                    break;

                default:
                    System.out.println("Select a number 1-4 or 9 to quit");
                    break;
            }
        }
    }

    private String startProgram(){
        boolean login = true;
        String purpose = "";
        while (login) {
            searchMenu();
            Scanner scanner = new Scanner(System.in);
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":                             //1: Beach holidays
                    String beach = "Beach";
                    purpose = beach;
                    break;

                case "2":
                    String urban = "Urban";           //2: Urban trip
                    purpose = urban;
                    break;

                default:
                    System.out.println("Enter 1 or 2");
                    break;
                }
            return purpose;
        }
        return null;
    }





    private void searchMenu() {
        System.out.println("");
        System.out.println("----------------------------------");
        System.out.println("What is the purpose of your trip ?");
        System.out.println("");
        System.out.println("  Beach holidays     : Enter '1'  ");
        System.out.println("   Urban trip        : Enter '2'  ");
        System.out.println("----------------------------------");
    }

    private void adminMenu() {
        System.out.println("");
        System.out.println("--------------------------------------");
        System.out.println("       Skåne travel system            ");
        System.out.println("--------------------------------------");
        System.out.println("Choose a number");
        System.out.println("1 : Registering customer");
        System.out.println("2 : Searching & Booking room");
        System.out.println("3 : Cancellation of book");
        System.out.println("4 : Rescheduling book");
        System.out.println("9 : Quit");
        System.out.println("--------------------------------------");
    }
}
