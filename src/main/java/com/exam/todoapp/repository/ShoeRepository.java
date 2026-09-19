package com.exam.todoapp.repository;

import com.exam.todoapp.model.Shoe;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository en mémoire pour les chaussures.
 * Utilise un ConcurrentHashMap thread-safe et un AtomicLong pour les IDs.
 * Pré-chargé avec 6 chaussures d'exemple au démarrage.
 */
@Repository
public class ShoeRepository {

    private final Map<Long, Shoe> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ShoeRepository() {
        // Données initiales réalistes
        save(Shoe.builder().nom("Air Max 90").marque("Nike").pointure(42).prix(new BigDecimal("129.99")).stock(45).couleur("Blanc/Rouge").build());
        save(Shoe.builder().nom("Samba OG").marque("Adidas").pointure(41).prix(new BigDecimal("99.95")).stock(30).couleur("Noir/Blanc").build());
        save(Shoe.builder().nom("Chuck Taylor All Star").marque("Converse").pointure(43).prix(new BigDecimal("74.99")).stock(60).couleur("Bleu Marine").build());
        save(Shoe.builder().nom("574").marque("New Balance").pointure(44).prix(new BigDecimal("109.00")).stock(20).couleur("Gris").build());
        save(Shoe.builder().nom("Old Skool").marque("Vans").pointure(40).prix(new BigDecimal("79.90")).stock(35).couleur("Noir/Blanc").build());
        save(Shoe.builder().nom("Classic Leather").marque("Reebok").pointure(42).prix(new BigDecimal("89.95")).stock(15).couleur("Blanc").build());
    }

    public List<Shoe> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Shoe> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Shoe save(Shoe shoe) {
        if (shoe.getId() == null) {
            shoe.setId(idCounter.getAndIncrement());
        }
        store.put(shoe.getId(), shoe);
        return shoe;
    }

    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
