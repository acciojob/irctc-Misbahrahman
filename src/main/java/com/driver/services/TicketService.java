package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.exceptions.CustomException;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainService trainService;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //Seat Validations

        Station from = bookTicketEntryDto.getFromStation();
        Station to = bookTicketEntryDto.getToStation();
        int seatsNeeeded = bookTicketEntryDto.getNoOfSeats();
        int trainId = bookTicketEntryDto.getTrainId();
        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto(trainId , from , to);

        int seatsAvailable = trainService.availlableSeats(new SeatAvailabilityEntryDto(bookTicketEntryDto.getTrainId(),bookTicketEntryDto.getFromStation(),bookTicketEntryDto.getToStation()));
        if(seatsAvailable < seatsNeeeded)throw new Exception("Less tickets are available");

        //route validation
        Optional<Train> response = trainRepository.findById(trainId);
        if(!response.isPresent())throw new Exception();
        Train train = response.get();
        String route = train.getRoute();
        String[] arr = route.split(",");
        int fromInd = -1;
        int toInd = -1;

        for(int i = 0 ; i < arr.length ; i++){
            if(arr[i].equals(from.toString()))fromInd = i;
            else if(arr[i].equals(to.toString()))toInd = i;
        }
//        System.out.println(fromInd + " " +toInd);
        if(fromInd == -1 || toInd == -1 || toInd < fromInd)throw new Exception("Invalid stations");
        int totalFare = 300 * (toInd - fromInd - 1);


        Ticket ticket = new Ticket();
        ticket.setFromStation(from);
        ticket.setTrain(train);
        ticket.setTotalFare(totalFare);
        ticket.setToStation(to);


        List<Integer> ids = bookTicketEntryDto.getPassengerIds();
        for(int x : ids) {
            Optional<Passenger> res = passengerRepository.findById(x);
            if (!res.isPresent()) throw new Exception();
            Passenger passenger = res.get();
            ticket.getPassengersList().add(passenger);
            passenger.getBookedTickets().add(ticket);
        }


        Ticket savedTicket = ticketRepository.save(ticket);
        train.getBookedTickets().add(savedTicket);
        return savedTicket.getTicketId();

    }


}
