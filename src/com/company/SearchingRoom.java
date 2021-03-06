package com.company;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class SearchingRoom {


    private Connection conn;
    private PreparedStatement statement = null;
    private ResultSet resultSet = null;

    public SearchingRoom(Connection mainConn) {
        conn = mainConn;
    }

    public ArrayList<String> questionsForSearchRoom(String purpose) {  //return en arraylist which contains all answers of the questions
        ArrayList<String> answers = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Searching for your best room ===");
        System.out.println("");
        System.out.println("How many are you? Input a number");
        try {
            int numberOfPeople = Integer.parseInt(scanner.nextLine());
            if (0 >= numberOfPeople || numberOfPeople > 6) {             //number of people can be chosen 1-5.
                System.out.println("We can book max 5 people at once. Please call again");
            } else {
                System.out.println("Which room do you want to stay?");
                System.out.println("1: Single room / 2: Double room / 3: Twin room / 4: Family room / 5: Executive suite room");
                int room_id = scanner.nextInt();
                if (0 >= room_id || room_id > 6){                       //room_id should be 1 to 5
                    System.out.println("Input a number between 1 to 5");
                }
                else {
                    if (numberOfPeople > room_id) {                     //max people in a room is same as the number of the room_id
                        System.out.println("The max people in this room is " + room_id + " people. Please change room type.");
                    } else {
                        answers.add(String.valueOf(numberOfPeople));
                        answers.add(String.valueOf(room_id));
                        System.out.println("When is your check in date? Input ex; 2020-01-30");
                        String checkIn = scanner.next();
                        LocalDate checkInDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(checkIn));
                        LocalDate startOfSeason = LocalDate.of(2020, 6, 1);
                        if (checkInDate.isBefore(startOfSeason)) {                //check-in date should be after 2020-06-01
                            System.out.println("The campaign season is between 2020-06-01 to 2020-07-30!");
                        } else {
                            answers.add(checkIn);
                            System.out.println("When is your check out date? Input ex; 2020-01-30");
                            String checkOut = scanner.next();                      //check-out date should be before 2020-07-30
                            LocalDate checkOutDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(checkOut));
                            LocalDate endOfSeason = LocalDate.of(2020, 7, 30);
                            if (checkOutDate.isAfter(endOfSeason)) {
                                System.out.println("The campaign season is between 2020-06-01 to 2020-07-30!");
                            } else {
                                answers.add(checkOut);
                                System.out.println(" ====== Popular filters  =======");
                                System.out.println("Input y if you think it's important to have");
                                System.out.println("Pool? y/n");
                                Scanner filter = new Scanner(System.in);
                                String poolAnswer = filter.nextLine();
                                int pool = 0;
                                if (poolAnswer.equals("y")) {
                                    pool = 1;
                                }
                                answers.add(String.valueOf(pool));
                                System.out.println("Evening entertainment? y/n");
                                String entertainment = filter.nextLine();
                                int evening_entertainment = 0;
                                if (entertainment.equals("y")) {
                                    evening_entertainment = 1;
                                }
                                answers.add(String.valueOf(evening_entertainment));

                                System.out.println("Kids club? y/n");
                                String kids = filter.nextLine();
                                int kids_club = 0;
                                if (kids.equals("y")) {
                                    kids_club = 1;
                                }
                                answers.add(String.valueOf(kids_club));
                                System.out.println("Restaurant? y/n");
                                String eat = filter.nextLine();
                                int restaurant = 0;
                                if (eat.equals("y")) {
                                    restaurant = 1;
                                }
                                answers.add(String.valueOf(restaurant));

                                double km_to_beach = 100.0;
                                double km_to_city = 100.0;
                                if (purpose.equals("Beach")) {
                                    km_to_beach = 2.0;                  //if purpose is "beach" then km_to_beach=2.0 from 100.0
                                } else if (purpose.equals("City")) {
                                    km_to_city = 2.0;                  ////if purpose is "city" then km_to_city=2.0 from 100.0
                                }
                                answers.add(String.valueOf(km_to_beach));
                                answers.add(String.valueOf(km_to_city));

                                System.out.println("Input a minimum number for guest-rating");
                                int guest_rating = filter.nextInt();
                                answers.add(String.valueOf(guest_rating));
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Please check and input correct information");
        }
        return answers;
    }

    public ArrayList<String> selectForRooms(ArrayList answers) { //get a list of hotels after filtering.

        if (answers.size() != 11) {                               //return en arraylist which contains (numberOfPeople,check_in,check_out,roomid)
            return null;
        } else {
            String numberOfPeople = (String) answers.get(0);
            String roomid = (String) answers.get(1);
            int room_id = Integer.parseInt(roomid);
            String check_in = (String) answers.get(2);
            String check_out = (String) answers.get(3);
            String po = (String) answers.get(4);
            int pool = Integer.parseInt(po);
            String enter = (String) answers.get(5);
            int evening_entertainment = Integer.parseInt(enter);
            String kids = (String) answers.get(6);
            int kids_club = Integer.parseInt(kids);
            String res = (String) answers.get(7);
            int restaurant = Integer.parseInt(res);
            String beach = (String) answers.get(8);
            double km_to_beach = Double.parseDouble(beach);
            String city = (String) answers.get(9);
            double km_to_city = Double.parseDouble(city);
            String rate = (String) answers.get(10);
            int guest_rating = Integer.parseInt(rate);

            try {
                statement = conn.prepareStatement("SELECT * FROM all_room_booked_and_unbooked \n" +
                        "WHERE room_id = ? and pool >= ?\n" +
                        "AND kids_club >= ? AND evening_entertainment >= ?\n" +
                        "and restaurant >= ? and  guest_rating >= ?\n" +
                        "and km_to_beach <= ? and km_to_city <= ?\n" +
                        "group by hotelroom_id\n" +
                        "HAVING check_in IS NULL OR check_out <= ? OR check_in >= ?\n" +
                        "order by guest_rating desc;");


                statement.setInt(1, room_id);
                statement.setInt(2, pool);
                statement.setInt(3, kids_club);
                statement.setInt(4, evening_entertainment);
                statement.setInt(5, restaurant);
                statement.setInt(6, guest_rating);
                statement.setDouble(7, km_to_beach);
                statement.setDouble(8, km_to_city);
                statement.setString(9, check_in);
                statement.setString(10, check_out);

                resultSet = statement.executeQuery();

            } catch (SQLException e) {
                e.printStackTrace();
            }


            String row = "";
            ArrayList<String> result = new ArrayList<>();

            try {
                if (!resultSet.next()){
                    System.out.println("There is no results after filtering. Try it again");
                    return null;

                } else {
                    System.out.println("This/these hotel(s) is available between " + check_in + " and " + check_out);
                    System.out.println("The result is sorted descending by guest-rating");
                    System.out.println("");
                    result.add(numberOfPeople);
                    result.add(check_in);
                    result.add(check_out);
                    result.add(roomid);

                    while (resultSet.next()) { ;
                        row = "Hotel id: " + resultSet.getInt("hotel_id")
                                + ", Hotel name: " + resultSet.getString("hotel_name")
                                + ", Room type: " + resultSet.getString("room_type")
                                + ", Room price/night: " + resultSet.getDouble("room_price_per_night")
                                + ", Distance to beach: " + resultSet.getDouble("km_to_beach")
                                + ", Distance to city: " + resultSet.getDouble("km_to_city")
                                + ", Guest rating: " + resultSet.getInt("guest_rating")
                                + ", Pool: " + resultSet.getInt("pool")
                                + ", Evening entertainment: " + resultSet.getInt("evening_entertainment")
                                + ", Kids club: " + resultSet.getInt("kids_club")
                                + ", Restaurant: " + resultSet.getInt("restaurant");
                        System.out.println(row);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }


    public int bookRoom(ArrayList result) {     //choose one hotelroom from the previous list and book. return book_id
        System.out.println("");
        String numberOfPeople = (String) result.get(0);
        int number_of_people = Integer.parseInt(numberOfPeople);
        String check_in = (String) result.get(1);
        String check_out = (String) result.get(2);
        String roomid = (String) result.get(3);
        int room_id = Integer.parseInt(roomid);
        System.out.println("Input the Hotel-id number for proceeding to book your hotel");
        System.out.println("==Important== Please input correct Hotel-id");
        Scanner sc = new Scanner(System.in);
        int hotel_id = sc.nextInt();
                                                                    // from here asking about options
        System.out.println("Family room and Executive suite room can get extra bed, 500kr/bed.");
        System.out.println("Do you want to have one? y/n");
        System.out.println("(Press 'n' if your room type is not Family room or Executive suite room)");
        Scanner booksc = new Scanner(System.in);
        String extrabed = booksc.nextLine();

        System.out.println("Will you have half pension or full pension?");
        System.out.println("h:half pension(500kr/person) / f:full pension(700kr/person) / n: don't need ");
        String pension = booksc.nextLine();
        int option_id = 6;
        if (extrabed.equals("y") && (pension.equals("n"))) {
            option_id = 1;
        } else if (extrabed.equals("y") && (pension.equals("h"))) {
            option_id = 4;
        } else if (extrabed.equals("y") && (pension.equals("f"))) {
            option_id = 5;
        } else if (extrabed.equals("n") && (pension.equals("n"))) {
            option_id = 6;
        } else if (extrabed.equals("n") && (pension.equals("h"))) {
            option_id = 2;
        } else if (extrabed.equals("n") && (pension.equals("f"))) {
            option_id = 3;
        } else {
            System.out.println("Please input information properly");
        }

        System.out.println("Input book-id");
        int book_id = booksc.nextInt();

        System.out.println("Input customer-id");
        int customer_id = booksc.nextInt();

        try{
            statement = conn.prepareStatement("INSERT INTO bookings(book_id,customer_id, hotel_id, room_id, option_id," +
                    "number_of_people, check_in, check_out) VALUES (?,?,?,?,?,?,?,?)");

            statement.setInt(1, book_id);
            statement.setInt(2, customer_id);
            statement.setInt(3, hotel_id);
            statement.setInt(4, room_id);
            statement.setInt(5, option_id);
            statement.setInt(6, number_of_people);
            statement.setString(7, check_in);
            statement.setString(8, check_out);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book_id;
    }


    public ResultSet searchByBookid(int book_id) {     //using book_id, select the booking information
        try {
            statement = conn.prepareStatement("SELECT * FROM bookings WHERE book_id = ?");
            statement.setInt(1, book_id);
            return statement.executeQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public ArrayList<String > bookingResult(ResultSet resultSet) {   //booking info
        try {
            ArrayList<String> result = new ArrayList<>();
            System.out.println("======== Booking information ========");
            while (resultSet.next()) {
                result.add(String.valueOf(resultSet.getInt("room_id")));
                result.add(String.valueOf(resultSet.getInt("option_id")));
                result.add(resultSet.getString("check_in"));
                result.add(resultSet.getString("check_out"));
                result.add(String.valueOf(resultSet.getInt("number_of_people")));
                result.add(String.valueOf(resultSet.getInt("hotel_id")));

                String row = "Book_id: " + resultSet.getInt("book_id")
                        + ", Customer_id: " + resultSet.getInt("customer_id")
                        + ", Hotel_id: " + resultSet.getInt("hotel_id")
                        + ", Room_id: " + resultSet.getInt("room_id")
                        + ", Option_id: " + resultSet.getInt("option_id")
                        + ", Number of People: " + resultSet.getInt("number_of_people")
                        + ", Check-in date: " + resultSet.getString("check_in")
                        + ", Check-out date: " + resultSet.getString("check_out") + ".";
                System.out.println(row);
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void bookingTotalPrice(ArrayList result) {       /// get total price for staying

        String roomid = (String) result.get(0);
        int room_id = Integer.parseInt(roomid);
        String op = (String) result.get(1);
        int option_id = Integer.parseInt(op);
        String checkin = (String) result.get(2);
        Date check_in = Date.valueOf(checkin);
        String checkout = (String) result.get(3);
        Date check_out = Date.valueOf(checkout);
        String people = (String) result.get(4);
        int number_of_people = Integer.parseInt(people);

        var diff = Math.abs(check_out.getTime() - check_in.getTime());

        var howManyDays = Math.ceil(diff/(1000 * 3600 * 24));
        if (howManyDays==0){                                   // even check-in date and check-out date is same, staying is counted as 1 night
            howManyDays=1;
        }

        double roomFee = 0;
        if (room_id == 1 || room_id == 2) {
            roomFee = 1200;                           //single and double room cost 1200 kr
        } else if (room_id == 3) {
            roomFee = 1300;                           //twin room costs 1300 kr
        } else if (room_id == 4) {
            roomFee = 1800;                           //family room costs 1800 kr
        } else if (room_id == 5) {
            roomFee = 2500;                           //executive suite room costs 2500 kr
        }
        double totalRoomFee = roomFee * howManyDays;  //room fee/total staying

        double optionFee = 0;
        double totalOptionFee = 0;

        if (option_id == 1) {                        //extra bed
            optionFee = 500;
            totalOptionFee = optionFee;
        } else if (option_id == 2) {                 //half pension
            optionFee = 500;
            totalOptionFee = optionFee * number_of_people;
        } else if (option_id == 3) {                 //full pension
            optionFee = 700;
            totalOptionFee = optionFee * number_of_people;
        } else if (option_id == 4) {                 //extra bed + half pension
            optionFee = 1000;
            totalOptionFee = optionFee + (number_of_people - 1) * 500;
        } else if (option_id == 5) {                 //extra bed + full pension
            optionFee = 1200;
            totalOptionFee = optionFee + (number_of_people - 1) * 700;
        } else{                                       //no bed , no pension
            totalOptionFee = 0;
        }

        double TotalPrice = totalRoomFee + totalOptionFee;
        System.out.println("");
        System.out.println("====================================");
        System.out.println(" Total price : " + TotalPrice + "Kr ");
        System.out.println("====================================");

    }

}
