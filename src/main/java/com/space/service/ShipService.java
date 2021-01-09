package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ShipService {

    public Ship getShip(Long id);

    public List<Ship> getShips(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating,
            Integer pageNumber,
            Integer pageSize,
            ShipOrder order
    );

    public List<Ship> getAllShips();

    public List<Ship> filterShips(
            List<Ship> shipsList,
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating
    );

    public List<Ship> sortShips(List<Ship> shipsList, ShipOrder order);

    public List<Ship> paginateShips(List<Ship> shipsList, Integer pageNumber, Integer pageSize);

    public Ship createShip(Ship ship);

    public boolean isShipValid(Ship ship);

    public Double calcRating(Double speed, Boolean isUsed, Date prodDate);

    public void deleteShip(Long id);

    public Ship updateShip(Ship currentShip, Ship updatedShip);

    boolean isShipEmpty(Ship ship);
}
