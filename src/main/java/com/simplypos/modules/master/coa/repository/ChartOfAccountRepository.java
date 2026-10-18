package com.simplypos.modules.master.coa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.master.coa.entity.ChartOfAccount;

public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Long> {
    Optional<ChartOfAccount> findByAccountCode(String accountCode);

    boolean existsByAccountCode(String accountCode);

    List<ChartOfAccount> findByAccountTypeOrderByAccountCode(String accountType);

    List<ChartOfAccount> findAllByOrderByAccountCode();
}