package com.example.NetflixData;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class UserData {
    private String totalMinutes;
    private String runtime;
    private String fullname;
    private String name;
    private ArrayList<String> genres;
    private String numberEpisodes;
    private String type;
}
