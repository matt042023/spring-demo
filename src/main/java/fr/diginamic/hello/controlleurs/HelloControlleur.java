package fr.diginamic.hello.controlleurs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloControlleur {

    @GetMapping
    public String direHello() {
        return "hello";
    }
}
