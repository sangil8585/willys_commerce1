## ✈️ ERD

```mermaid
erDiagram

USER {
    VARCHAR id PK
    VARCHAR gender
    VARCHAR birth
    VARCHAR email
    BIGINT point
    TIMESTAMP created_at
    TIMESTAMP updated_at
    TIMESTAMP deleted_at
}

BRAND {
    BIGINT id PK
    VARCHAR name
    TIMESTAMP created_at
    TIMESTAMP updated_at
    TIMESTAMP deleted_at
}

PRODUCT {
    BIGINT id PK
    VARCHAR name
    BIGINT brand_id
    BIGINT price
    BIGINT stock
    TIMESTAMP created_at
    TIMESTAMP updated_at
    TIMESTAMP deleted_at
}

ORDER {
    BIGINT id PK
    BIGINT user_id FK
    TIMESTAMP created_at
    TIMESTAMP updated_at
    TIMESTAMP deleted_at
}

ORDER_ITEM {
    BIGINT id PK
    BIGINT order_id FK
    BIGINT product_id FK
    INT quantity
    BIGINT price
    TIMESTAMP created_at
    TIMESTAMP updated_at
    TIMESTAMP deleted_at
}

PRODUCT_LIKE {
    BIGINT id PK
    BIGINT user_id FK
    BIGINT product_id FK
    TIMESTAMP created_at
    TIMESTAMP updated_at
    TIMESTAMP deleted_at
}

USER ||--o{ ORDER : places
ORDER ||--|{ ORDER_ITEM : includes
PRODUCT ||--o{ ORDER_ITEM : ordered_in
BRAND ||--|{ PRODUCT : owns
USER ||--o{ PRODUCT_LIKE : likes
PRODUCT ||--o{ PRODUCT_LIKE : liked_by

```