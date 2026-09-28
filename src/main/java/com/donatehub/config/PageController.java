package com.donatehub.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageController {

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource index() {
        return new ClassPathResource("templates/index.html");
    }

    @GetMapping(value = "/donors.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource donors() {
        return new ClassPathResource("templates/donors.html");
    }

    @GetMapping(value = "/recipients.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource recipients() {
        return new ClassPathResource("templates/recipients.html");
    }

    @GetMapping(value = "/drives.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource drives() {
        return new ClassPathResource("templates/drives.html");
    }

    @GetMapping(value = "/donated-items.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource donatedItems() {
        return new ClassPathResource("templates/donated-items.html");
    }
}
