package com.simplypos.config;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class StoreContext {

    private Long currentStoreId;

    public Long getCurrentStoreId() {
        return currentStoreId;
    }

    public void setCurrentStoreId(Long storeId) {
        this.currentStoreId = storeId;
    }

    public void clear() {
        this.currentStoreId = null;
    }

    public boolean isSet() {
        return currentStoreId != null;
    }
}