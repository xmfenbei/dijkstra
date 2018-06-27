package com.xmfenbei.dijkstra;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DijkstraController {

    @RequestMapping(value = "/dijkstra", method = {RequestMethod.GET, RequestMethod.POST})
    public String dijkstra(@Validated DijkstraReqDTO reqDTO) {
        return "hello";
    }
}
