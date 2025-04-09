package io.project.receiver.controllers;

import io.micrometer.core.annotation.Timed;

import io.project.receiver.transport.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author lenovo
 */
@RestController
@Slf4j
@RequestMapping("/api/v2/events")
@RequiredArgsConstructor
public class EventController {

    private final EventProducer eventProducer;

    @GetMapping(path = "/reports")
    @CrossOrigin
    @Timed
    public ResponseEntity create() {
///
        return ResponseEntity.status(HttpStatus.OK).body("Done");

    }
}
