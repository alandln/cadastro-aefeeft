package com.asce1dev.cadastroaefeeft.domain.repository;

import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>{
	
	Page<Cliente> findClienteByNomeStartingWithIgnoreCase(String nome, Pageable pageable);
	
	Page<Cliente> findClienteByCpfStartingWith(String cpf, Pageable pageable);

	Optional<Cliente> findByCpf(String cpf);
}
