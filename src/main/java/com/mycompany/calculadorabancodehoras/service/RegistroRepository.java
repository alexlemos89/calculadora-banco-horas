package com.mycompany.calculadorabancodehoras.repository;

import com.mycompany.calculadorabancodehoras.model.Funcionario; // IMPORT CORRIGIDO PARA .model
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroRepository extends JpaRepository<Funcionario, String> {
}