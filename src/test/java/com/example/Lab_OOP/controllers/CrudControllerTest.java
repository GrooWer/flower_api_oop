package com.example.Lab_OOP.controllers;

import com.example.Lab_OOP.models.Flower;
import com.example.Lab_OOP.repo.FlowerRepository;
import com.example.Lab_OOP.repo.UserRepository;
import com.example.Lab_OOP.security.SecurityConfig;
import com.example.Lab_OOP.security.jwt.JwtFilter;
import com.example.Lab_OOP.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CrudController.class)
@Import({SecurityConfig.class, CrudControllerTest.ThymeleafConfig.class}) // Добавляем конфиг Thymeleaf
@AutoConfigureMockMvc(addFilters = false)
@EnableMethodSecurity
public class CrudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlowerRepository flowerRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtUtil jwtUtil;

    // Конфигурация Thymeleaf для тестов
    @TestConfiguration
    static class ThymeleafConfig {
        @Bean
        public ITemplateResolver templateResolver() {
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            return resolver;
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCrudAccessWithUserRole() throws Exception {
        List<Flower> flowers = List.of(new Flower("Rose", 10, 5));
        Mockito.when(flowerRepository.findAll()).thenReturn(flowers);

        mockMvc.perform(get("/crud"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("flowers"))
                .andExpect(view().name("crud"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testEditFlowerFormSuccess() throws Exception {
        Flower flower = new Flower("Rose", 10, 5);
        flower.setId(1);

        Mockito.when(flowerRepository.existsById(1)).thenReturn(true);
        Mockito.when(flowerRepository.findById(1)).thenReturn(Optional.of(flower));

        mockMvc.perform(get("/crud/1/edit"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("flower"))
                .andExpect(view().name("crud-edit"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSubmitEditFlowerForm() throws Exception {
        Flower flower = new Flower("Rose", 10, 5);
        flower.setId(1);

        Mockito.when(flowerRepository.findById(1)).thenReturn(Optional.of(flower));
        Mockito.when(flowerRepository.save(any(Flower.class))).thenReturn(flower);

        mockMvc.perform(post("/crud/1/edit")
                        .with(csrf())
                        .param("name", "Tulip")
                        .param("price", "15")
                        .param("quantity", "20"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/crud"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteFlower() throws Exception {
        Flower flower = new Flower("Rose", 10, 5);
        flower.setId(1);

        Mockito.when(flowerRepository.findById(1)).thenReturn(Optional.of(flower));
        Mockito.doNothing().when(flowerRepository).delete(any(Flower.class));

        mockMvc.perform(post("/crud/1/remove")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/crud"));
    }
}