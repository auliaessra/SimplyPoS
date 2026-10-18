package com.simplypos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.simplypos.modules.auth.repository",
        "com.simplypos.modules.master.coa.repository",
        "com.simplypos.modules.master.customer.repository",
        "com.simplypos.modules.master.vendor.repository",
        "com.simplypos.modules.master.item.repository",
        "com.simplypos.modules.master.unit.repository",
        "com.simplypos.modules.master.storeprice.repository",
        "com.simplypos.modules.purchase.repository",
        "com.simplypos.modules.sales.repository",
        "com.simplypos.modules.inventory.repository",
        "com.simplypos.modules.finance.repository",
        "com.simplypos.modules.setting.repository"
})
@EntityScan(basePackages = {
        "com.simplypos.entities",
        "com.simplypos.modules.master.coa.entity",
        "com.simplypos.modules.master.customer.entity",
        "com.simplypos.modules.master.vendor.entity",
        "com.simplypos.modules.master.item.entity",
        "com.simplypos.modules.master.unit.entity",
        "com.simplypos.modules.master.storeprice.entity",
        "com.simplypos.modules.purchase.entity",
        "com.simplypos.modules.sales.entity",
        "com.simplypos.modules.inventory.entity",
        "com.simplypos.modules.finance.entity",
        "com.simplypos.modules.setting.entity"
})
public class SimplyPosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplyPosApplication.class, args);
    }

}
