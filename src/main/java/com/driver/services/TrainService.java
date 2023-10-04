package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.exceptions.CustomException;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    TicketRepository ticketRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        StringBuilder stringBuilder = new StringBuilder();
        for(Station x :  trainEntryDto.getStationRoute()){
            stringBuilder.append(x.toString());
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        System.out.println("Route COnversion LOgic = " + stringBuilder);

        Train train = new Train();
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setRoute(stringBuilder.toString());

        Train savedTrain = trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        int seats = train.getNoOfSeats();
        String[] stations = train.getRoute().split(",");
        Station from = seatAvailabilityEntryDto.getFromStation();
        Station to = seatAvailabilityEntryDto.getToStation();
        boolean start = false;
        boolean end = true;
        int ct = 0;
        for(String x : stations){
            seats += offBoarders(train.getTrainId() , Station.valueOf(x));
            seats -= onBoarders(train.getTrainId() , Station.valueOf(x));
            System.out.println(seats + " " + x);
            System.out.println(seats);
            if(x.equals(from.toString()))start = true;
            if(x.equals(to.toString()))end = false;

            if(start && end){
                ct = Math.max(ct , seats);
            }
        }
       return ct;
    }

    public Integer availlableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        int seats = train.getNoOfSeats();
        String[] stations = train.getRoute().split(",");
        Station from = seatAvailabilityEntryDto.getFromStation();
        Station to = seatAvailabilityEntryDto.getToStation();
        boolean start = false;
        boolean end = true;
        int ct = Integer.MAX_VALUE;
        for(String x : stations){
            seats += offBoarders(train.getTrainId() , Station.valueOf(x));
            seats -= onBoarders(train.getTrainId() , Station.valueOf(x));
            System.out.println(seats + " " + x);
            System.out.println(seats);
            if(x.equals(from.toString()))start = true;
            if(x.equals(to.toString()))end = false;

            if(start && end){
                ct = Math.min(ct , seats);
            }
        }
        return ct;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        List<Ticket> tickets = ticketRepository.findAllByFromStation(station);
        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();
        if(!route.contains(station.toString()))throw new CustomException("");
        int ans = 0;
        for(Ticket x : tickets){
            System.out.println(x.getPassengersList().size() + " seee");
            if(x.getTrain().equals(train))ans += x.getPassengersList().size();
        }

        return ans;
    }

    public Integer onBoarders(Integer trainId,Station station) {
        List<Ticket> tickets = ticketRepository.findAllByFromStation(station);
        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();
        int ans = 0;
        for(Ticket x : tickets){
            if(x.getTrain().equals(train))ans += x.getPassengersList().size();
        }
        return ans;
    }


    public Integer offBoarders(Integer trainId, Station station) {
        List<Ticket> tickets = ticketRepository.findAllByToStation(station);
        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();
        int ans = 0;
        for(Ticket x : tickets){
            if(x.getTrain().equals(train))ans += x.getPassengersList().size();
        }
        return ans;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        List<Ticket> tickets = ticketRepository.findAllByTrain(trainRepository.findById(trainId).get());
        int ans = 0;
        for(Ticket x : tickets){
            for(Passenger passenger : x.getPassengersList()){
                ans = Math.max(passenger.getAge() , ans);
            }
        }
        return ans;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> trains = trainRepository.findAll();
        List<Integer> trainsId = new ArrayList<>();
        for(Train x : trains){
            String[] arr = x.getRoute().split(",");
            List<String> list = Arrays.asList(arr);
            if(!list.contains(station.toString()))continue;

            int ind = list.indexOf(station.toString());
            LocalTime time = x.getDepartureTime().plusHours(ind);
            if(time.isAfter(startTime)||time.isBefore(endTime)||time == endTime||time == startTime)trainsId.add(x.getTrainId());
        }

        return trainsId;
    }

}
