package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipServiceImpl implements ShipService {

    @Autowired
    private ShipRepository shipRepository;

    public ShipServiceImpl() {}

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
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
    ) {
        List<Ship> allShips = getAllShips();

        List<Ship> filteredShips = filterShips(allShips, name,
                planet,
                shipType,
                after,
                before,
                isUsed,
                minSpeed,
                maxSpeed,
                minCrewSize,
                maxCrewSize,
                minRating,
                maxRating);

        List<Ship> sortedShips = sortShips(filteredShips, order);

        List<Ship> pagedShips = paginateShips(sortedShips, pageNumber, pageSize);

        return pagedShips;
    }

    @Override
    public List<Ship> getAllShips() {
        return (List<Ship>) shipRepository.findAll();
    }

    @Override
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
    ) {
        List<Ship> filteredShips = new ArrayList<>(shipsList);

        for (Ship ship: shipsList) {
            // filter by name
            if (name != null)
                if (!ship.getName().contains(name))
                    filteredShips.remove(ship);
            // filter by planet
            if (planet != null)
                if (!ship.getPlanet().contains(planet))
                    filteredShips.remove(ship);
            // filter by shipType
            if (shipType != null)
                if (!ship.getShipType().equals(shipType))
                    filteredShips.remove(ship);
            // filter by production date
            if (after != null)
                if (!ship.getProdDate().after(new Date(after)))
                    filteredShips.remove(ship);
            if (before != null)
                if (!ship.getProdDate().before(new Date(before)))
                    filteredShips.remove(ship);
            // filter by is used
            if (isUsed != null)
                if (!ship.getUsed() == isUsed)
                    filteredShips.remove(ship);
            // filter by speed
            if (minSpeed != null)
                if (ship.getSpeed() < minSpeed)
                    filteredShips.remove(ship);
            if (maxSpeed != null)
                if (ship.getSpeed() > maxSpeed)
                    filteredShips.remove(ship);
            // filter by crew size
            if (minCrewSize != null)
                if (ship.getCrewSize() < minCrewSize)
                    filteredShips.remove(ship);
            if (maxCrewSize != null)
                if (ship.getCrewSize() > maxCrewSize)
                    filteredShips.remove(ship);
            // filter by rating
            if (minRating != null)
                if (ship.getRating() < minRating)
                    filteredShips.remove(ship);
            if (maxRating != null)
                if (ship.getRating() > maxRating)
                    filteredShips.remove(ship);
        }

        return filteredShips;
    }

    @Override
    public List<Ship> sortShips(List<Ship> shipsList, ShipOrder shipOrder) {

        ShipOrder order = shipOrder == null ? ShipOrder.ID : shipOrder;

        List<Ship> sortedShips = new ArrayList<>(shipsList);

        Collections.sort(sortedShips, (ship1, ship2) -> {
            switch (order) {
                case ID:
                    return ship1.getId().compareTo(ship2.getId());
                case DATE:
                    return ship1.getProdDate().compareTo(ship2.getProdDate());
                case SPEED:
                    return ship1.getSpeed().compareTo(ship2.getSpeed());
                case RATING:
                    return ship1.getRating().compareTo(ship2.getRating());
                default:
                    return 0;
            }
        });

        return sortedShips;
    }

    @Override
    public List<Ship> paginateShips(List<Ship> shipsList, Integer pageNumber, Integer pageSize) {
        Integer page = pageNumber == null ? 0 : pageNumber;
        Integer size = pageSize == null ? 3 : pageSize;

        if (page == 0 && shipsList.size() <= size)
            return shipsList;
        else {
            Integer from = page * size;
            Integer to = page * size + size;

            if (to > shipsList.size())
                to = shipsList.size();

            return shipsList.subList(from, to);
        }
    }

    @Override
    public Ship createShip(Ship ship) {
        shipRepository.save(ship);
        return ship;
    }

    @Override
    public Double calcRating(Double speed, Boolean isUsed, Date prodDate) {

        int currentYear = 3019;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prodDate);
        int prodYear = calendar.get(Calendar.YEAR);
        Double coefficient = 1.0;
        if (isUsed) coefficient = 0.5;

        return Math.round(((80 * speed * coefficient) / (currentYear -  prodYear + 1))*100)/100D;
    }

    @Override
    public void deleteShip(Long id) {
        Ship deletedShip = getShip(id);
        if (deletedShip != null)
            shipRepository.delete(deletedShip);
    }

    @Override
    public Ship updateShip(Ship currentShip, Ship updatedShip) throws IllegalArgumentException {
        boolean shouldChangeRating = false;

        final String name = updatedShip.getName();
        if (name != null) {
            if (isNameValid(name)) {
                currentShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        final String planet = updatedShip.getPlanet();
        if (planet != null) {
            if (isPlanetValid(planet)) {
                currentShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (updatedShip.getShipType() != null) {
            currentShip.setShipType(updatedShip.getShipType());
        }
        final Date prodDate = updatedShip.getProdDate();
        if (prodDate != null) {
            if (isProdDateValid(prodDate)) {
                currentShip.setProdDate(prodDate);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (updatedShip.getUsed() != null) {
            currentShip.setUsed(updatedShip.getUsed());
            shouldChangeRating = true;
        }
        final Double speed = updatedShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                currentShip.setSpeed(speed);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        final Integer crewSize = updatedShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewSizeValid(crewSize)) {
                currentShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (shouldChangeRating) {
            final double rating = calcRating(currentShip.getSpeed(), currentShip.getUsed(), currentShip.getProdDate());
            currentShip.setRating(rating);
        }
        shipRepository.save(currentShip);
        return currentShip;
    }

    @Override
    public boolean isShipValid(Ship ship) {
        if (isNameValid(ship.getName())
                && isPlanetValid(ship.getPlanet())
                && isCrewSizeValid(ship.getCrewSize())
                && isSpeedValid(ship.getSpeed())
                && isProdDateValid(ship.getProdDate())
                && isUsedValid(ship.getUsed())
        )
            return true;
        else
            return false;
    }

    @Override
    public boolean isShipEmpty(Ship ship) {
        if (ship.getId() == null
                && ship.getName() == null
                && ship.getPlanet() == null
                && ship.getUsed() == null
                && ship.getShipType() == null
                && ship.getRating() == null
                && ship.getProdDate() == null
                && ship.getSpeed() == null
                && ship.getCrewSize() == null
        ) return true;
        else
            return false;
    }

    private boolean isNameValid(String name) {
        if (name != null && name.length() > 0 && name.length() <= 50)
            return true;
        else
            return false;
    }

    private boolean isPlanetValid(String planet) {
        if (planet != null && planet.length() > 0 && planet.length() <= 50)
            return true;
        else return false;
    }

    private boolean isCrewSizeValid(Integer crewSize) {
        final int minCrewSize = 1;
        final int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;
    }

    private boolean isSpeedValid(Double speed) {
        final double minSpeed = 0.01;
        final double maxSpeed = 0.99;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    private boolean isUsedValid(Boolean getUsed) {
        if (getUsed != null)
            return true;
        else
            return false;
    }

    private boolean isProdDateValid(Date prodDate) {
        final Date startProd = getDateForYear(2800);
        final Date endProd = getDateForYear(3019);
        return prodDate != null && prodDate.after(startProd) && prodDate.before(endProd);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

}
