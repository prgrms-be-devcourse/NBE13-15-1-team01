-- DB 스키마 초기화

-- 외래 키 검사 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 테이블 삭제
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menus;
DROP TABLE IF EXISTS categories;

-- 외래 키 검사 활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 카테고리 테이블 (이름 1~30자, 중복 금지, 대소문자 구분)
CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(30) COLLATE utf8mb4_bin NOT NULL UNIQUE COMMENT '카테고리명 (대소문자 구분)'
);

-- 2. 메뉴 테이블 (이름 1~100자, 가격 1~1,000,000, 설명 0~300자)
CREATE TABLE menus (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       category_id BIGINT NOT NULL,
                       name VARCHAR(100) COLLATE utf8mb4_bin NOT NULL UNIQUE COMMENT '메뉴명 (대소문자 구분 검색용)',
                       price INT NOT NULL COMMENT '가격 (1 ~ 1,000,000)',
                       description VARCHAR(300) NOT NULL DEFAULT '' COMMENT '설명 (비어있어도 빈 문자열)',
                       available BOOLEAN NOT NULL DEFAULT TRUE COMMENT '판매 가능 여부',
                       CONSTRAINT fk_menus_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- 3. 주문 테이블 (상태: PLACED, CANCELLED / 이메일 대소문자 구분)
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        email VARCHAR(100) COLLATE utf8mb4_bin NOT NULL COMMENT '고객 이메일 (대소문자 구분)',
                        address VARCHAR(200) COLLATE utf8mb4_bin NOT NULL COMMENT '배송 주소',
                        postcode VARCHAR(5) NOT NULL COMMENT '우편번호 5자리 (앞자리 0 유지)',
                        created_at DATETIME NOT NULL COMMENT '주문 접수 시각 (서울 기준)' DEFAULT CURRENT_TIMESTAMP,
                        shipping_date DATE NOT NULL COMMENT '배송 대상일 (14시 기준 계산)',
                        total_amount INT NOT NULL COMMENT '총 주문 금액',
                        status VARCHAR(20) NOT NULL DEFAULT 'PLACED' COMMENT 'PLACED 또는 CANCELLED',
                        cancelled_at DATETIME NULL COMMENT '취소 시각 (PLACED일 때는 null)',
                        memo VARCHAR(200) NOT NULL DEFAULT '' COMMENT '주문 메모 (초기값 빈 문자열)'
);

-- 4. 주문 항목 테이블 (주문 당시 메뉴명·단가·수량·항목금액 보존)
CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             menu_id BIGINT NOT NULL,
                             menu_name VARCHAR(100) NOT NULL COMMENT '접수 당시 메뉴명',
                             unit_price INT NOT NULL COMMENT '접수 당시 단가',
                             quantity INT NOT NULL COMMENT '수량 (1 ~ 20)',
                             line_amount INT NOT NULL COMMENT '단가 * 수량',
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_id) REFERENCES menus (id)
);