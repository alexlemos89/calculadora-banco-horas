package com.mycompany.calculadorabancodehoras.repository;

import com.mycompany.calculadorabancodehoras.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// O segredo está aqui: <Funcionario, String> 
// Funcionario é a classe e String é o tipo do registro
public interface RegistroRepository extends JpaRepository<Funcionario, String> {
}