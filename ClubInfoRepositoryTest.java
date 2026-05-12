package com.example.pagebuilder;

import com.example.pagebuilder.ClubInfo;
import com.example.pagebuilder.ClubRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ClubInfoRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ClubRepository clubRepository;
    @Test
    void testSaveAndFindClubInfo() {
        ClubInfo clubInfo = new ClubInfo();
        clubInfo.setName("Mi Club de Baloncesto");
        clubInfo.setLogoUrl("http://example.com/logo.png");
        clubInfo.setFacebook("https://facebook.com/miclub");
        clubInfo.setHeaderBackgroundColor("#FF0000");
        ClubInfo savedInfo = clubRepository.save(clubInfo);
        assertNotNull(savedInfo.getId());
        ClubInfo foundInfo = clubRepository.findById(savedInfo.getId()).orElse(null);
        assertNotNull(foundInfo);
        assertEquals("Mi Club de Baloncesto", foundInfo.getName());
        assertEquals("http://example.com/logo.png", foundInfo.getLogoUrl());
    }
}