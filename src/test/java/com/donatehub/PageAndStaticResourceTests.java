package com.donatehub;

import com.donatehub.config.PageController;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PageAndStaticResourceTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PageController()).build();
    }

    @Test
    void testRootOpensDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("DonateHub")))
                .andExpect(content().string(Matchers.containsString("Community Dashboard")));
    }

    @Test
    void testIndexHtmlPage() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("DonateHub")));
    }

    @Test
    void testDonorsPage() throws Exception {
        mockMvc.perform(get("/donors.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Donors Management")));
    }

    @Test
    void testRecipientsPage() throws Exception {
        mockMvc.perform(get("/recipients.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Recipient Organizations")));
    }

    @Test
    void testDrivesPage() throws Exception {
        mockMvc.perform(get("/drives.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Donation Drives")));
    }

    @Test
    void testDonatedItemsPage() throws Exception {
        mockMvc.perform(get("/donated-items.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Donated Items & Distribution")));
    }

    @Test
    void testStaticAssetsExist() {
        assertTrue(new ClassPathResource("static/css/style.css").exists());
        assertTrue(new ClassPathResource("static/js/common.js").exists());
        assertTrue(new ClassPathResource("static/js/dashboard.js").exists());
        assertTrue(new ClassPathResource("static/js/donors.js").exists());
        assertTrue(new ClassPathResource("static/js/recipients.js").exists());
        assertTrue(new ClassPathResource("static/js/drives.js").exists());
        assertTrue(new ClassPathResource("static/js/donated-items.js").exists());
    }
}
