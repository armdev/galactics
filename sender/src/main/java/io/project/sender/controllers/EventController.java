/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.project.sender.controllers;

import io.micrometer.core.annotation.Timed;
import io.project.sender.events.AccountEvent;
import io.project.sender.events.AccountEventDataGenerator;
import io.project.sender.transport.EventProducer;
import java.util.List;
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

    @GetMapping(path = "/send/10/messages")
    @CrossOrigin
    @Timed
    public ResponseEntity create() {
        List<AccountEvent> events = AccountEventDataGenerator.generateEvents();
        eventProducer.sendMessage(events);
        return ResponseEntity.status(HttpStatus.OK).body("Done");

    }
}
