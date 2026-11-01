package com.example.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.example.api.dto.data.DataItem;

@RestController
@RequestMapping("/api")
public class DataController {

    @GetMapping("/data")
    public List<DataItem> getData() {
        return List.of(
                new DataItem(1L, HtmlUtils.htmlEscape("First demo post")),
                new DataItem(2L, HtmlUtils.htmlEscape("Second demo post"))
        );
    }
}
