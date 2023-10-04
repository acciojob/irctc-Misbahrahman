package com.driver.repository;

import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Integer> {


    List<Ticket> findAllByFromStation(Station station);

    List<Ticket> findAllByTrain(Train train);

    List<Ticket> findAllByToStation(Station station);
}
