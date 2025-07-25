## ✈️ 시퀀스 다이어그램

---

### 🎈 브랜드 정보 조회

```mermaid
sequenceDiagram
		actor User
		participant BC as BrandController (Interface)
		participant BF as BrandFacade (Application)
		participant BS as BrandService (Domain)
		participant BR as BrandRepository (Infrastructure)
		
		User->>+BC: GET /api/v1/brands/{brandId}
		BC->>+BF: getBrand(brandId)
		BF->>+BS: findById(brandId)
		BS->>+BR: findById(brandId)
		BR-->>BS: Optional<BrandEntity>
		BS-->>BF: Optional<BrandEntity>
	
		alt 브랜드 없음
		    BF-->>BC: throw BrandNotFoundException
		    BC-->>User: 404 Not Found + ApiResponse.fail()
		else 브랜드 존재
		    BF-->>BC: BrandInfo DTO
		    BC-->>User: 200 OK + ApiResponse.success()
		end

```

### 🎈 상품 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant PC as ProductController (Interface)
    participant PF as ProductFacade (Application)
    participant PS as ProductService (Domain)
    participant PR as ProductRepository (Infrastructure)

    User->>+PC: GET /api/v1/products?page=0&size=10
    PC->>+PF: getProducts(params)
    PF->>+PS: findProducts(params)
    PS->>+PR: findAll(params)
    PR-->>PS: List<ProductEntity>
    PS-->>PF: List<ProductEntity>

    alt 상품 없음
        PF-->>PC: empty list
        PC-->>User: 200 OK + []
    else 상품 존재
        PF-->>PC: ProductList DTO
        PC-->>User: 200 OK + Product List
    end

```

### 🎈 상품 정보 조회

```mermaid
sequenceDiagram
    actor User
    participant PC as ProductController (Interface)
    participant PF as ProductFacade (Application)
    participant PS as ProductService (Domain)
    participant PR as ProductRepository (Infrastructure)

    User->>+PC: GET /api/v1/products/{productId}
    PC->>+PF: getProductDetail(productId)
    PF->>+PS: findById(productId)
    PS->>+PR: findById(productId)
    PR-->>PS: Optional<ProductEntity>
    PS-->>PF: Optional<ProductEntity>

    alt 상품 없음
        PF-->>PC: throw ProductNotFoundException
        PC-->>User: 404 Not Found + ApiResponse.fail()
    else 상품 존재
        PF-->>PC: ProductDetail DTO
        PC-->>User: 200 OK + ApiResponse.success()
    end

```

---

### 🎈 상품 좋아요 등록

```mermaid
sequenceDiagram
    actor User
    participant UC as UserController (Interface)
    participant UF as UserFacade (Application)
    participant PS as ProductService (Domain)
    participant US as UserService (Domain)

    User->>+UC: POST /api/v1/products/{productId}/likes
    UC->>+UF: addLike(userId, productId)
    UF->>+PS: findById(productId)
    PS-->>UF: Optional<ProductEntity>

    alt 상품 없음
        UF-->>UC: throw ProductNotFoundException
        UC-->>User: 404 Not Found + ApiResponse.fail()
    else 상품 존재
        UF->>+US: addLike(userId, productId)
        US-->>UF: Updated Like Count
        UF-->>UC: Like DTO
        UC-->>User: 200 OK + ApiResponse.success()
    end

```

### 🎈 상품 좋아요 취소

```mermaid
sequenceDiagram
    actor User
    participant UC as UserController (Interface)
    participant UF as UserFacade (Application)
    participant PS as ProductService (Domain)
    participant US as UserService (Domain)

    User->>+UC: DELETE /api/v1/products/{productId}/likes
    UC->>+UF: removeLike(userId, productId)
    UF->>+PS: findById(productId)
    PS-->>UF: Optional<ProductEntity>

    alt 상품 없음
        UF-->>UC: throw ProductNotFoundException
        UC-->>User: 404 Not Found + ApiResponse.fail()
    else 상품 존재
        UF->>+US: removeLike(userId, productId)
        US-->>UF: Updated Like Count
        UF-->>UC: Like DTO
        UC-->>User: 200 OK + ApiResponse.success()
    end

```

### 🎈 내가 좋아요 한 상품 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant UC as UserController (Interface)
    participant UF as UserFacade (Application)
    participant US as UserService (Domain)

    User->>+UC: GET /api/v1/users/{userId}/likes
    UC->>+UF: getUserLikes(userId)
    UF->>+US: findLikesByUser(userId)

    US-->>UF: Optional<List<LikedProduct>>

    alt 유저 없음
        UF-->>UC: throw UserNotFoundException
        UC-->>User: 404 Not Found + ApiResponse.fail()
    else 유저 존재
        UF-->>UC: LikedProductList DTO
        UC-->>User: 200 OK + ApiResponse.success()
    end


```

---

### 🎈 주문 요청

```mermaid
sequenceDiagram
    actor User
    participant OC as OrderController (Interface)
    participant OF as OrderFacade (Application)
    participant OS as OrderService (Domain)
    participant PS as ProductService (Domain)
    participant US as UserService (Domain)

    User->>+OC: POST /api/v1/orders
    OC->>+OF: createOrder(userId, items)
    OF->>+PS: checkStock(items)
    PS-->>OF: Optional<StockCheckResult>

    alt 재고 부족
        OF-->>OC: throw OutOfStockException
        OC-->>User: 400 Bad Request + ApiResponse.fail()
    else 재고 확인 완료
        OF->>+US: checkUserPoints(userId, totalPrice)
        US-->>OF: Optional<PointCheckResult>

        alt 포인트 부족
            OF-->>OC: throw InsufficientPointException
            OC-->>User: 400 Bad Request + ApiResponse.fail()
        else 포인트 충분
            OF->>+OS: saveOrder(userId, items)
            OS-->>OF: Order Entity
            OF-->>OC: Order DTO
            OC-->>User: 201 Created + ApiResponse.success()
        end
    end

```

### 🎈 유저의 주문 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant OC as OrderController (Interface)
    participant OF as OrderFacade (Application)
    participant OS as OrderService (Domain)

    User->>+OC: GET /api/v1/users/{userId}/orders
    OC->>+OF: getUserOrders(userId)
    OF->>+OS: findOrdersByUser(userId)

    OS-->>OF: Optional<List<OrderEntity>>

    alt 유저 없음
        OF-->>OC: throw UserNotFoundException
        OC-->>User: 404 Not Found + ApiResponse.fail()
    else 유저 존재
        OF-->>OC: OrderList DTO
        OC-->>User: 200 OK + ApiResponse.success()
    end


```

### 🎈 단일 주문 상세 조회

```mermaid
sequenceDiagram
    actor User
    participant OC as OrderController (Interface)
    participant OF as OrderFacade (Application)
    participant OS as OrderService (Domain)

    User->>+OC: GET /api/v1/orders/{orderId}
    OC->>+OF: getOrderDetail(orderId)
    OF->>+OS: findById(orderId)
    OS-->>OF: Optional<OrderEntity>

    alt 주문 없음
        OF-->>OC: throw OrderNotFoundException
        OC-->>User: 404 Not Found + ApiResponse.fail()
    else 주문 존재
        OF-->>OC: Order DTO
        OC-->>User: 200 OK + ApiResponse.success()
    end

```