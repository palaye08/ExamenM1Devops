package com.exam.todoapp.controller;

import com.exam.todoapp.model.Shoe;
import com.exam.todoapp.repository.ShoeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoeController.class)
class ShoeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShoeRepository repository;

    @Test
    void getAll_shouldReturnListOfShoes() throws Exception {
        Shoe shoe = Shoe.builder()
                .id(1L).nom("Air Max 90").marque("Nike")
                .pointure(42).prix(new BigDecimal("129.99"))
                .stock(45).couleur("Blanc")
                .build();

        when(repository.findAll()).thenReturn(List.of(shoe));

        mockMvc.perform(get("/api/shoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Air Max 90"))
                .andExpect(jsonPath("$[0].marque").value("Nike"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        Shoe shoe = Shoe.builder()
                .id(2L).nom("Samba OG").marque("Adidas")
                .pointure(41).prix(new BigDecimal("99.95"))
                .stock(30).couleur("Noir")
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(shoe));

        mockMvc.perform(get("/api/shoes/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Samba OG"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/shoes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_withNewShoe() throws Exception {
        Shoe input = Shoe.builder()
                .nom("Chuck Taylor").marque("Converse")
                .pointure(43).prix(new BigDecimal("74.99"))
                .stock(60).couleur("Bleu")
                .build();

        Shoe saved = Shoe.builder()
                .id(7L).nom("Chuck Taylor").marque("Converse")
                .pointure(43).prix(new BigDecimal("74.99"))
                .stock(60).couleur("Bleu")
                .build();

        when(repository.save(any(Shoe.class))).thenReturn(saved);

        mockMvc.perform(post("/api/shoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nom").value("Chuck Taylor"));
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        when(repository.deleteById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/shoes/999"))
                .andExpect(status().isNotFound());
    }
}
