package com.nicasia.cyberintel.repository;

import com.nicasia.cyberintel.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {}