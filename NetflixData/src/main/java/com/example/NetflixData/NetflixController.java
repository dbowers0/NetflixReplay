package com.example.NetflixData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


@RestController
public class NetflixController {

    @Autowired
    NetflixService netflixService;

    @GetMapping("/file")
    private ArrayList<Object> test(@RequestBody MultipartFile file) throws FileNotFoundException, ExecutionException, InterruptedException, ParseException {
        return netflixService.test(file);
    }

    @GetMapping("/get")
    private NetflixDatabase netflix() throws ExecutionException, InterruptedException {
        return netflixService.netflix();
    }
}
