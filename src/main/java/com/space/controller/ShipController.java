package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {

    @Autowired
    private ShipServiceImpl shipService;

    @GetMapping("/ships")
    public List<Ship> getAllShips(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "order", required = false) ShipOrder order
    ) {
        return shipService.getShips(
                name,
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
                maxRating,
                pageNumber,
                pageSize,
                order
        );
    }

    @GetMapping("/ships/count")
    public int getShipsCount(@RequestParam(value = "name", required = false) String name,
                             @RequestParam(value = "planet", required = false) String planet,
                             @RequestParam(value = "shipType", required = false) ShipType shipType,
                             @RequestParam(value = "after", required = false) Long after,
                             @RequestParam(value = "before", required = false) Long before,
                             @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                             @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                             @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                             @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                             @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                             @RequestParam(value = "minRating", required = false) Double minRating,
                             @RequestParam(value = "maxRating", required = false) Double maxRating,
                             @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                             @RequestParam(value = "pageSize", required = false) Integer pageSize,
                             @RequestParam(value = "order", required = false) ShipOrder order) {
        List<Ship> shipsList = shipService.getAllShips();
        return shipService.filterShips(
                shipsList,
                name,
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
                maxRating
        ).size();
    }

    @PostMapping("/ships")
    public ResponseEntity<Ship> createShip(@RequestBody Ship newShip) {

        if (newShip.getUsed() == null) newShip.setUsed(false);

        if (!shipService.isShipValid(newShip))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Double rating = shipService.calcRating(newShip.getSpeed(), newShip.getUsed(), newShip.getProdDate());
        if (rating == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        newShip.setRating(rating);

        Ship createdShip = shipService.createShip(newShip);
        return new ResponseEntity<>(createdShip, HttpStatus.OK);
    }

    @GetMapping("/ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable(value = "id") String shipId) {
        Long id = convertStringIdToLong(shipId);

        if (id == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Ship returnedShip = shipService.getShip(id);

        if (returnedShip != null) {
            return new ResponseEntity<>(returnedShip, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/ships/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable(value = "id") String pathId) {
        ResponseEntity<Ship> responseEntity = getShip(pathId);

        if (responseEntity.getStatusCode().isError())
            return responseEntity;

        Ship deletedShip = responseEntity.getBody();

        if (deletedShip == null) {
            return responseEntity;
        } else {
            shipService.deleteShip(deletedShip.getId());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable(value = "id") String currentShipId, @RequestBody Ship updatedShip) {
        ResponseEntity<Ship> responseEntity = getShip(currentShipId);

        if (responseEntity.getStatusCode().isError() && responseEntity.getBody() == null)
            return responseEntity;

        Ship currentShip = responseEntity.getBody();

        if (shipService.isShipEmpty(updatedShip))
            return new ResponseEntity<>(currentShip, HttpStatus.OK);

        try {
            shipService.updateShip(currentShip, updatedShip);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(currentShip, HttpStatus.OK);
    }

    private Long convertStringIdToLong(String idStringValue) {
        Long id;

        try {
            id = Long.parseLong(idStringValue);
            if (id <= 0) id = null;
        } catch (NumberFormatException e) {
            return null;
        }

        return id;
    }

}
