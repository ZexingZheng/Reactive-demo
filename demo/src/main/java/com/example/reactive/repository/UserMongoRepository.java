package com.example.reactive.repository;

import com.example.reactive.entity.UserMongo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserMongoRepository extends ReactiveMongoRepository<UserMongo, String> {

    Flux<UserMongo> findByName(String name);
    
    Flux<UserMongo> findByNameContainingIgnoreCase(String name);
}
